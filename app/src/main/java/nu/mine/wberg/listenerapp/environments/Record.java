package nu.mine.wberg.listenerapp.environments;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

public class Record {

    private static final String LOG_TAG = "Record";

    public short[] record(int recordTimeMs, int sampleRateHz) {
        Log.i(LOG_TAG, "Recording for " + recordTimeMs + "ms at " + sampleRateHz + "Hz");

        short[] recording = new short[recordTimeMs * sampleRateHz / 1000];
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        AudioRecord recorder = new AudioRecord(AudioSource.MIC, sampleRateHz, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, recording.length * 2);
        recorder.startRecording();
        recorder.read(recording, 0, recording.length);
        recorder.stop();
        recorder.release();

        return recording;
    }

}
