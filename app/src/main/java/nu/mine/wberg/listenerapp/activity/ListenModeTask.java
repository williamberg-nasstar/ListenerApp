package nu.mine.wberg.listenerapp.activity;

import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import nu.mine.wberg.listenerapp.R;
import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.analysis.mfcc.bmfcc.MFCC;
import nu.mine.wberg.listenerapp.environments.EnvironmentManager;
import nu.mine.wberg.listenerapp.environments.Record;
import nu.mine.wberg.listenerapp.ml.Classifier;


public class ListenModeTask extends BroadcastReceiver {

    private static final int RECORD_TIME_MS = 1000;
    private static final String LOG_TAG = "intent_service";

    private static EnvironmentManager currentEnvironmentManager;
    private static MFCC currentMfcc;
    private static Classifier currentClassifier;

    public static void startListenMode(Context context, int intervalMs,
                                       EnvironmentManager environmentManager, MFCC mfcc, Classifier classifier) {
        currentEnvironmentManager = environmentManager;
        currentMfcc = mfcc;
        currentClassifier = classifier;

        AlarmManager am = (AlarmManager)context.getSystemService(Context.ALARM_SERVICE);
        Intent i = new Intent(context, ListenModeTask.class);
        PendingIntent pi = PendingIntent.getBroadcast(context.getApplicationContext(), 0, i, 0);
        am.setInexactRepeating(AlarmManager.RTC_WAKEUP, 0, intervalMs, pi);
    }

    public static void stopListenMode(Context context) {
        Intent intent = new Intent(context, ListenModeTask.class);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarmManager.cancel(sender);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("listening", "listening for speaker");

        short[] recording = (new Record()).record(RECORD_TIME_MS, MainActivity.sampleRateHz);
        MfcFingerprint mfcFingerprint = new MfcFingerprint(currentMfcc.process(recording, MainActivity.ATTENUATION_FACTOR));
        String speaker = currentClassifier.classify(currentEnvironmentManager.getNamesToEnvironments(), mfcFingerprint);

        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.app_icon)
                        .setContentTitle(speaker)
                        .setContentText(speaker + " is nearby");

        Intent notification = new Intent(context, MainActivity.class);
        TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
        stackBuilder.addParentStack(MainActivity.class);
        stackBuilder.addNextIntent(notification);
        PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(
                0,
                PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(resultPendingIntent);
        NotificationManager mNotificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(0, mBuilder.build());
    }
}
