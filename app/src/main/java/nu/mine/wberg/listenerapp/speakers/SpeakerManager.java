package nu.mine.wberg.listenerapp.speakers;

import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import nu.mine.wberg.listenerapp.R;

public class SpeakerManager implements Serializable {

    private static final String TEMPORARY_SAVE_FILE_SUFFIX = ".new";

    private Resources resources;
    private Map<String, Speaker> namesToSpeakers;

    /**
     * When created, loads from R.app_dir on external storage
     */
    public SpeakerManager(Resources resources) throws IOException, ClassNotFoundException {
        this.resources = resources;
        namesToSpeakers = loadListeningSpeakers();
    }

    public Map<String, Speaker> getNamesToSpeakers() {
        return namesToSpeakers;
    }

    public HashMap<String, Speaker> getSerializableNamesToSpeakers() {
        return new HashMap<>(getNamesToSpeakers());
    }

    public void setNamesToSpeakers(Map<String, Speaker> namesToSpeakers) {
        this.namesToSpeakers = namesToSpeakers;
    }

    private Map<String, Speaker> loadListeningSpeakers() throws IOException, ClassNotFoundException {
        Map<String, Speaker> result = new HashMap<>();

        File esDir = Environment.getExternalStorageDirectory();
        File laDir = new File(esDir, resources.getString(R.string.app_dir));
        if (!laDir.isDirectory()) {
            laDir.mkdir();
            return result;
        }

        for (File file : laDir.listFiles()) {
            String filename = file.getName();
            if (!filename.endsWith(resources.getString(R.string.speaker_file_suffix))) {
                continue;
            }

            String speakerName = filename.substring(0, filename.length() - 4);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream input = new ObjectInputStream(fis);

            try {
                result.put(speakerName, (Speaker)input.readObject());
            }
            finally {
                 input.close();
            }
        }

        return result;
    }

    /**
     * Saves to R.app_dir in external storage
     */
    public void saveListeningSpeakers() throws IOException {
        File esDir = Environment.getExternalStorageDirectory();
        File laDir = new File(esDir, resources.getString(R.string.app_dir));
        if (!laDir.isDirectory()) {
            laDir.mkdir();
        }

        for (String name : namesToSpeakers.keySet()) {
            Speaker speaker = namesToSpeakers.get(name);
            if (!speaker.isSaveDirty()) {
                continue;
            }

            String filename = name.concat(resources.getString(R.string.speaker_file_suffix));
            String temporaryFilename = filename.concat(TEMPORARY_SAVE_FILE_SUFFIX);
            File file = new File(laDir, filename);
            File fileTemporary = new File(laDir, temporaryFilename);

            FileOutputStream fos = new FileOutputStream(fileTemporary);
            ObjectOutputStream output = new ObjectOutputStream(fos);
            try {
                output.writeObject(namesToSpeakers.get(name));
            }
            finally {
                output.close();
            }

            file.delete();
            fileTemporary.renameTo(file);
        }
    }

}
