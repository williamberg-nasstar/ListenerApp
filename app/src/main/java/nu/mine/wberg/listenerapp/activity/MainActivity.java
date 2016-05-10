package nu.mine.wberg.listenerapp.activity;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.io.IOException;
import java.util.Set;

import nu.mine.wberg.listenerapp.R;
import nu.mine.wberg.listenerapp.analysis.mfcc.bmfcc.MFCC;
import nu.mine.wberg.listenerapp.environments.EnvironmentManager;
import nu.mine.wberg.listenerapp.ml.Classifier;
import nu.mine.wberg.listenerapp.ml.KNearestClassifier;


public class MainActivity extends ActionBarActivity {

    public static final double ATTENUATION_FACTOR = Double.MAX_VALUE;
    public static final int WINDOW_WIDTH = 256;
    public static final int sampleRateHz = 44100;
    public static final int mfccCoefficientCount = 5;
    public static final double lowerFilterFreq = 20;
    public static final double upperFilterFreq = 22050;
    public static final int filterCount = 40;

    private static final int knnK = 5;

    private static final String LOG_TAG = "main_activity";
    private static final int LISTEN_MODE_INTERVAL_MS = 10000;

    private final Handler threadHandler = new Handler();
    private EnvironmentManager environmentManager;

    private boolean listenMode = false;
    private boolean recordMode = false;
    private MFCC mfcc;
    private Classifier classifier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            environmentManager = new EnvironmentManager(getResources());
        }
        catch (IOException | ClassNotFoundException e) {
            threadHandler.post(new Alert(R.string.error_message_unable_to_read_file + ": " + e.getMessage()));
            Log.e(LOG_TAG, "Unable to load listening environments from file", e);
        }

        mfcc = new MFCC(sampleRateHz, WINDOW_WIDTH, mfccCoefficientCount, true, lowerFilterFreq, upperFilterFreq, filterCount);
        classifier = new KNearestClassifier(knnK);

        RadioButton recordRadioButton1 = (RadioButton)findViewById(R.id.recordRadioButton1);
        RadioButton recordRadioButton2 = (RadioButton)findViewById(R.id.recordRadioButton2);
        new GRadioGroup(recordRadioButton1, recordRadioButton2);

        loadEnvironments();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }

    private void loadEnvironments() {
        Spinner recordSpinner = (Spinner) findViewById(R.id.recordSpinner);
        Set<String> strings = environmentManager.getNamesToEnvironments().keySet();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                strings.toArray(new String[strings.size()]));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordSpinner.setAdapter(dataAdapter);
    }

    public void recordSelectedSpeakerButton(View v) {
        if (recordMode) {
            return;
        }

        final String speaker;
        try {
            speaker = getSelectedSpeaker().trim();
        } catch (IllegalStateException e) {
            threadHandler.post(new Alert("Record failed: " + e.getMessage()));
            return;
        }

        recordMode = true;
        threadHandler.post(new Alert("Recording for speaker '" + speaker + "'"));

        RecordNewSpeakerIntentService.recordNewSpeaker(this, new RecordNewSpeakerResultReceiver(new Handler()), speaker, environmentManager, mfcc);
    }

    public void toggleListenMode(View v) throws IOException, ClassNotFoundException {
        if (recordMode) {
            threadHandler.post(new Alert("Unable to listen while in record mode"));
            return;
        }

        if (!listenMode) {
            listenMode = true;
            threadHandler.post(new Alert("Listen mode enabled"));
            ListenModeTask.startListenMode(this, LISTEN_MODE_INTERVAL_MS, environmentManager, mfcc, classifier);
        }
        else {
            listenMode = false;
            threadHandler.post(new Alert("Listen mode disabled"));
            ListenModeTask.stopListenMode(this);
        }
    }

    private String getSelectedSpeaker() throws IllegalStateException {
        RadioButton recordRadioButton1 = (RadioButton)findViewById(R.id.recordRadioButton1);
        RadioButton recordRadioButton2 = (RadioButton)findViewById(R.id.recordRadioButton2);

        if (recordRadioButton1.isChecked()) {
            String environment = (String)((Spinner)findViewById(R.id.recordSpinner)).getSelectedItem();
            if (null != environment && !"".equals(environment)) {
                return environment;
            }
            throw new IllegalStateException("Existing environment was indicated but no existing environment was selected");
        } else if (recordRadioButton2.isChecked()) {
            String environment = ((EditText)findViewById(R.id.recordText)).getText().toString();
            if (!"".equals(environment)) {
                return environment;
            } else {
                throw new IllegalStateException("New environment was indicated but no name was specified");
            }
        }

        throw new IllegalStateException("Select an environment to record");
    }

    private final class Alert implements Runnable {

        private String error;

        private Alert(String error) {
            this.error = error;
        }

        @Override
        public void run() {
            ((TextView)findViewById(R.id.errorLabel)).setText(error);
        }

    }

    @SuppressLint("ParcelCreator")
    public class RecordNewSpeakerResultReceiver extends ResultReceiver {

        public RecordNewSpeakerResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            recordMode = false;
            String speaker = resultData.getString("speaker");
            if (resultCode == 0) {
                threadHandler.post(new Alert("Recorded new fingerprint for " + speaker));
            } else {
                threadHandler.post(new Alert("Failed to record new fingerprint for " + speaker));
            }
            recordMode = false;
        }

    }

}
