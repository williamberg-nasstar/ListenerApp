package nu.mine.wberg.listenerapp.activity;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import java.io.IOException;
import java.util.Map;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.analysis.mfcc.bmfcc.MFCC;
import nu.mine.wberg.listenerapp.environments.EnvironmentManager;
import nu.mine.wberg.listenerapp.environments.ListeningEnvironment;
import nu.mine.wberg.listenerapp.environments.Record;

public class RecordNewSpeakerIntentService extends IntentService {

    private static String currentSpeaker;
    private static EnvironmentManager currentEnvironmentManager;
    private static MFCC currentMfcc;
    private static ResultReceiver currentResultReceiver;

    private static final int RECORD_TIME_MS = 1000;

    public RecordNewSpeakerIntentService() {
        super("recordnewspeaker");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("recording", "recording new speaker");

        short[] record = (new Record()).record(RECORD_TIME_MS, MainActivity.sampleRateHz);
        MfcFingerprint mfcFingerprint = new MfcFingerprint(currentMfcc.process(record, MainActivity.ATTENUATION_FACTOR));

        Map<String, ListeningEnvironment> namesToEnvironments = currentEnvironmentManager.getNamesToEnvironments();

        if (namesToEnvironments.containsKey(currentSpeaker)) {
            ListeningEnvironment listeningEnvironment = namesToEnvironments.get(currentSpeaker);
            listeningEnvironment.addMfcFingerprint(mfcFingerprint);
        } else {
            ListeningEnvironment listeningEnvironment = new ListeningEnvironment();
            listeningEnvironment.addMfcFingerprint(mfcFingerprint);
            namesToEnvironments.put(currentSpeaker, listeningEnvironment);
            currentEnvironmentManager.setNamesToEnvironments(namesToEnvironments);
        }

        Bundle resultData = new Bundle();
        resultData.putString("speaker", currentSpeaker);

        try {
            currentEnvironmentManager.saveListeningEnvironments();
        } catch (IOException e) {
            currentResultReceiver.send(1, resultData);
        }

        currentResultReceiver.send(0, resultData);
    }

    public static void recordNewSpeaker(Context context,
                                 ResultReceiver resultReceiver,
                                 String speaker,
                                 EnvironmentManager environmentManager,
                                 MFCC mfcc) {
        currentResultReceiver = resultReceiver;
        currentSpeaker = speaker;
        currentEnvironmentManager = environmentManager;
        currentMfcc = mfcc;

        Intent intent = new Intent(context, RecordNewSpeakerIntentService.class);
        context.startService(intent);
    }

}
