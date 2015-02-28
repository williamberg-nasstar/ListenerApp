package nu.mine.wberg.listenerapp;

import android.graphics.Color;
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

import nu.mine.wberg.listenerapp.environments.EnvironmentManager;
import nu.mine.wberg.listenerapp.environments.ListeningEnvironment;
import nu.mine.wberg.listenerapp.environments.Record;
import nu.mine.wberg.listenerapp.ui.GRadioGroup;


public class MainActivity extends ActionBarActivity {

    private static final int RECORD_TIME_MS = 1000;
    private static final String LOG_TAG = "Main";

    private final Handler threadHandler = new Handler();
    private EnvironmentManager environmentManager;
    private Record record;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        try {
            environmentManager = new EnvironmentManager(getResources());
        }
        catch (IOException | ClassNotFoundException e) {
            ((TextView)findViewById(R.id.errorLabel)).setText(R.string.error_message_unable_to_read_file + ": " + e.getMessage());
            Log.e(LOG_TAG, "Unable to load listening environments from file", e);
        }

        record = new Record(RECORD_TIME_MS);

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

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void recordForSelectedEnvironment(View v) {
        final String environment;
        try {
            environment = getSelectedEnvironment().trim();
        }
        catch (IllegalStateException e) {
            ((TextView)findViewById(R.id.errorLabel)).setText(e.getMessage());
            return;
        }

        ((TextView)findViewById(R.id.errorLabel)).setText("Recording...");

        new RecordHandler(environment).run();
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
        }
        else if (recordRadioButton2.isChecked()) {
            String environment = ((EditText)findViewById(R.id.recordText)).getText().toString();
            if (!"".equals(environment)) {
                return environment;
            }
            else {
                throw new IllegalStateException("New environment was indicated but no name was specified");
            }
        }

        throw new IllegalStateException("Select an environment to record");
    }

    private void loadEnvironments() {
        Spinner recordSpinner = (Spinner)findViewById(R.id.recordSpinner);
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item,
                environmentManager.getNamesToEnvironments().keySet().toArray(new String[] {}));
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        recordSpinner.setAdapter(dataAdapter);
    }

    private final class RecordHandler implements Runnable {

        private String environment;

        public RecordHandler(String environment) {
            this.environment = environment;
        }

        @Override
        public void run() {
            threadHandler.post(new StartRecording());
            record.run();
            while (record.isRecording()) {
                try {
                    Thread.sleep(1000);
                }
                catch (InterruptedException e) {
                    Log.e(LOG_TAG, "Interrupted while waiting for recording to finish", e);
                    threadHandler.post(new EndRecording());
                    return;
                }
            }

            Map<String, ListeningEnvironment> namesToEnvironments = environmentManager.getNamesToEnvironments();
            if (namesToEnvironments.containsKey(environment)) {
                ListeningEnvironment listeningEnvironment = namesToEnvironments.get(environment);
                listeningEnvironment.add(record.getRecording());
            }
            else {
                ListeningEnvironment listeningEnvironment = new ListeningEnvironment();
                listeningEnvironment.add(record.getRecording());
                namesToEnvironments.put(environment, listeningEnvironment);
                environmentManager.setNamesToEnvironments(namesToEnvironments);
            }

            try {
                environmentManager.saveListeningEnvironments();
            }
            catch (IOException e) {
                threadHandler.post(new ErrorSetter(e.getMessage()));
                threadHandler.post(new EndRecording());
                return;
            }

            threadHandler.post(new ErrorSetter("Recorded a new sample for '" + environment + "'"));
            threadHandler.post(new EndRecording());
        }
    }

    private final class ErrorSetter implements Runnable {

        private String error;

        private ErrorSetter(String error) {
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
