package nu.mine.wberg.listenerapp.environments;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class Record extends Thread {

    private static final String LOG_TAG = "Record";
    private static final int SAMPLE_RATE_HZ = 44100;

    private final short[] recording;
    private volatile boolean currentlyRecording;

    public Record(int recordTimeMs) {
        recording = new short[recordTimeMs * SAMPLE_RATE_HZ / 1000];
        currentlyRecording = false;
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);
    }

    @Override
    public void run() {
        Log.i(LOG_TAG, "Running Record thread");

        AudioRecord recorder = new AudioRecord(AudioSource.MIC, SAMPLE_RATE_HZ, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recording.length * 2);
        currentlyRecording = true;
        recorder.startRecording();
        recorder.read(recording, 0, recording.length);
        recorder.stop();
        recorder.release();
        currentlyRecording = false;
    }

    public boolean isRecording() {
        return currentlyRecording;
    }

    public short[] getRecording() {
        return recording.clone();
    }

}
