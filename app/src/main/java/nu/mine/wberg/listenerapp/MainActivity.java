package nu.mine.wberg.listenerapp;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.analysis.mfcc.bmfcc.MFCC;
import nu.mine.wberg.listenerapp.environments.EnvironmentManager;
import nu.mine.wberg.listenerapp.environments.ListeningEnvironment;
import nu.mine.wberg.listenerapp.environments.Record;
import nu.mine.wberg.listenerapp.ml.KNearestClassifier;
import nu.mine.wberg.listenerapp.ui.GRadioGroup;


public class MainActivity extends ActionBarActivity {

    private static final double ATTENUATION_FACTOR = Double.MAX_VALUE;

    private static final int windowWidth = 256;
    private static final int sampleRateHz = 44100;
    private static final int mfccCoefficientCount = 5;
    private static final double lowerFilterFreq = 20;
    private static final double upperFilterFreq = 22050;
    private static final int filterCount = 40;

    private static final int RECORD_TIME_MS = 1000;
    private static final String LOG_TAG = "Main";

    private final Handler threadHandler = new Handler();
    private EnvironmentManager environmentManager;
    private MFCC mfcc;
    private KNearestClassifier kNearestClassifier;

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

        mfcc = new MFCC(sampleRateHz, windowWidth, mfccCoefficientCount, true, lowerFilterFreq, upperFilterFreq, filterCount);
        kNearestClassifier = new KNearestClassifier(1);

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

    public void recordForSelectedEnvironment(View v) {
        final String environment;
        try {
            environment = getSelectedEnvironment().trim();
        } catch (IllegalStateException e) {
            ((TextView)findViewById(R.id.errorLabel)).setText(e.getMessage());
            return;
        }

        threadHandler.post(new Alert("Recording for environment '" + environment + "'"));

        try {
            (new RecordEnvironmentHandler().execute(environment)).get();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted while recording for environment", e);
            threadHandler.post(new Alert("Interrupted while recording for environment"));
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, "Record task aborted", e);
            threadHandler.post(new Alert("Record task aborted"));
        }
    }

    public void testAudio(View v) {
        threadHandler.post(new Alert("Recording"));

        String environment = "";
        try {
            environment = (new TestEnvironmentHandler().execute()).get();
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Interrupted while recording", e);
            threadHandler.post(new Alert("Interrupted while recording"));
        } catch (ExecutionException e) {
            Log.e(LOG_TAG, "Record task aborted", e);
            threadHandler.post(new Alert("Record task aborted"));
        }

        threadHandler.post(new Alert("Tested environment: " + environment));
    }

    private String getSelectedEnvironment() throws IllegalStateException {
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

    private class RecordEnvironmentHandler extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... params) {
            if (params == null || params.length < 1 || params[0] == null) {
                throw new IllegalArgumentException();
            }
            String environment = params[0];

            short[] record = record();
            int sum = 0;
            for (short s : record) {
                sum += s;
            }
            Log.i("sum", "" + sum);
            MfcFingerprint mfcFingerprint = analyseRecording(record);

            Map<String, ListeningEnvironment> namesToEnvironments = environmentManager.getNamesToEnvironments();

            if (namesToEnvironments.containsKey(environment)) {
                ListeningEnvironment listeningEnvironment = namesToEnvironments.get(environment);
                listeningEnvironment.addMfcFingerprint(mfcFingerprint);
            } else {
                ListeningEnvironment listeningEnvironment = new ListeningEnvironment();
                listeningEnvironment.addMfcFingerprint(mfcFingerprint);
                namesToEnvironments.put(environment, listeningEnvironment);
                environmentManager.setNamesToEnvironments(namesToEnvironments);
            }

            try {
                environmentManager.saveListeningEnvironments();
            } catch (IOException e) {
                threadHandler.post(new Alert(e.getMessage()));
                threadHandler.post(new EndRecording());
            }

            return null;
        }

    }

    private class TestEnvironmentHandler extends AsyncTask<Void, Void, String> {

        @Override
        protected String doInBackground(Void... params) {
            return kNearestClassifier.classify(environmentManager.getNamesToEnvironments(), analyseRecording(record()));
        }

    }

    private void loadEnvironments() {
        Spinner recordSpinner = (Spinner)findViewById(R.id.recordSpinner);
        Set<String> strings = environmentManager.getNamesToEnvironments().keySet();
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                strings.toArray(new String[strings.size()]));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordSpinner.setAdapter(dataAdapter);
    }

    private short[] record() {
        threadHandler.post(new StartRecording());
        short[] recording = (new Record()).record(RECORD_TIME_MS, sampleRateHz);
        threadHandler.post(new EndRecording());
        return recording;
    }

    private MfcFingerprint analyseRecording(short[] recording) {
        return new MfcFingerprint(mfcc.process(recording, ATTENUATION_FACTOR));
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

    private final class StartRecording implements Runnable {

        @Override
        public void run() {
            TextView errorLabel = (TextView)findViewById(R.id.errorLabel);
            errorLabel.setText("");
            errorLabel.setBackgroundColor(Color.RED);
        }

    }

    private final class EndRecording implements Runnable {

        @Override
        public void run() {
            TextView errorLabel = (TextView)findViewById(R.id.errorLabel);
            errorLabel.setBackgroundColor(Color.TRANSPARENT);
        }

    }

}
