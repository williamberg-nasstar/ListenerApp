package nu.mine.wberg.listenerapp;

import android.os.Bundle;
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

    private static final int RECORD_TIME_MS = 10000;

    private static final String LOG_TAG = "Main";
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
            environment = getSelectedEnvironment();
        }
        catch (IllegalStateException e) {
            ((TextView)findViewById(R.id.errorLabel)).setText(e.getMessage());
            return;
        }

        record.run();
        new Thread() {
            @Override
            public void run() {
                while (record.isRecording()) {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        Log.e(LOG_TAG, "Interrupted while waiting for recording to finish", e);
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
                    }
                }
            }
        }.run();
    }

    private String getSelectedEnvironment() throws IllegalStateException {
        RadioButton recordRadioButton1 = (RadioButton)findViewById(R.id.recordRadioButton1);
        RadioButton recordRadioButton2 = (RadioButton)findViewById(R.id.recordRadioButton2);

        if (recordRadioButton1.isSelected()) {
            String environment = ((EditText)findViewById(R.id.recordText)).getText().toString();
            if (!"".equals(environment)) {
                return environment;
            }
            else {
                throw new IllegalStateException("New environment was indicated but no name was specified");
            }
        }
        else if (recordRadioButton2.isSelected()) {
            String environment = (String)((Spinner)findViewById(R.id.recordSpinner)).getSelectedItem();
            if (null != environment && !"".equals(environment)) {
                return environment;
            }
            throw new IllegalStateException("Existing environment was indicated but no existing environment was selected");
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
}
