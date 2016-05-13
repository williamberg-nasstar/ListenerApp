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
import nu.mine.wberg.listenerapp.speakers.SpeakerManager;
import nu.mine.wberg.listenerapp.speakers.Record;
import nu.mine.wberg.listenerapp.ml.Classifier;

/**
 * This class contains both the listening task, and the methods for scheduling
 * it. These need to be kept together. However, this BroadcastReceiver must be
 * specified in this class, and not as an inner class: AndroidManifest.xml does
 * not support inner classes as receivers. Instead, to schedule the task, we use
 * static methods.
 *
 * This class would be more testable if its dependencies were injected, to
 * replace the static methods.
 */
public class ListenModeTask extends BroadcastReceiver {

    private static final int RECORD_TIME_MS = 1000;
    private static final String LOG_TAG = "intent_service";

    private static SpeakerManager currentSpeakerManager;
    private static MFCC currentMfcc;
    private static Classifier currentClassifier;

    /**
     * Schedules
     */
    public static void startListenMode(Context context, int intervalMs,
                                       SpeakerManager speakerManager, MFCC mfcc, Classifier classifier) {
        currentSpeakerManager = speakerManager;
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

    /**
     * Called when the alarm manager triggers this task
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i("listening", "listening for speaker");

        short[] recording = (new Record()).record(RECORD_TIME_MS, MainActivity.sampleRateHz);
        MfcFingerprint mfcFingerprint = new MfcFingerprint(currentMfcc.process(recording, MainActivity.ATTENUATION_FACTOR));
        String speaker = currentClassifier.classify(currentSpeakerManager.getNamesToSpeakers(), mfcFingerprint);

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
