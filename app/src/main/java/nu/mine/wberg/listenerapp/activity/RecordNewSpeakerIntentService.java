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
import nu.mine.wberg.listenerapp.speakers.SpeakerManager;
import nu.mine.wberg.listenerapp.speakers.SpeakerData;
import nu.mine.wberg.listenerapp.speakers.Record;

/**
 * Provides a static method to run the record new speaker background service.
 */
public class RecordNewSpeakerIntentService extends IntentService {

    private static String currentSpeaker;
    private static SpeakerManager currentSpeakerManager;
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

        Map<String, SpeakerData> namesToSpeakers = currentSpeakerManager.getNamesToSpeakers();

        if (namesToSpeakers.containsKey(currentSpeaker)) {
            SpeakerData speaker = namesToSpeakers.get(currentSpeaker);
            speaker.addMfcFingerprint(mfcFingerprint);
        } else {
            SpeakerData speaker = new SpeakerData();
            speaker.addMfcFingerprint(mfcFingerprint);
            namesToSpeakers.put(currentSpeaker, speaker);
            currentSpeakerManager.setNamesToSpeakers(namesToSpeakers);
        }

        Bundle resultData = new Bundle();
        resultData.putString("speaker", currentSpeaker);

        try {
            currentSpeakerManager.saveListeningSpeakers();
        } catch (IOException e) {
            currentResultReceiver.send(1, resultData);
        }

        currentResultReceiver.send(0, resultData);
    }

    public static void recordNewSpeaker(Context context,
                                 ResultReceiver resultReceiver,
                                 String speaker,
                                 SpeakerManager speakerManager,
                                 MFCC mfcc) {
        currentResultReceiver = resultReceiver;
        currentSpeaker = speaker;
        currentSpeakerManager = speakerManager;
        currentMfcc = mfcc;

        Intent intent = new Intent(context, RecordNewSpeakerIntentService.class);
        context.startService(intent);
    }

}
