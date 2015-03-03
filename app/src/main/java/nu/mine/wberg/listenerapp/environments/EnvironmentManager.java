package nu.mine.wberg.listenerapp.environments;

import android.content.res.Resources;
import android.os.Environment;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

import nu.mine.wberg.listenerapp.R;

/**
 * @author wberg
 */
public class EnvironmentManager {

    private static final String TEMPORARY_SAVE_FILE_SUFFIX = ".new";

    private Resources resources;
    private Map<String, ListeningEnvironment> namesToEnvironments;

    public EnvironmentManager(Resources resources) throws IOException, ClassNotFoundException {
        this.resources = resources;
        namesToEnvironments = loadListeningEnvironments();
    }

    public Map<String, ListeningEnvironment> getNamesToEnvironments() {
        return namesToEnvironments;
    }

    public void setNamesToEnvironments(Map<String, ListeningEnvironment> namesToEnvironments) {
        this.namesToEnvironments = namesToEnvironments;
    }

    private Map<String, ListeningEnvironment> loadListeningEnvironments() throws IOException, ClassNotFoundException {
        Map<String, ListeningEnvironment> result = new HashMap<>();

        File esDir = Environment.getExternalStorageDirectory();
        File laDir = new File(esDir, resources.getString(R.string.app_dir));
        if (!laDir.isDirectory()) {
            laDir.mkdir();
            return result;
        }

        for (File file : laDir.listFiles()) {
            String filename = file.getName();
            if (!filename.endsWith(resources.getString(R.string.environment_file_suffix))) {
                continue;
            }

            String environmentName = filename.substring(0, filename.length() - 4);
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream input = new ObjectInputStream(fis);

            try {
                result.put(environmentName, (ListeningEnvironment)input.readObject());
            }
            finally {
                 input.close();
            }
        }

        return result;
    }

    public void saveListeningEnvironments() throws IOException {
        File esDir = Environment.getExternalStorageDirectory();
        File laDir = new File(esDir, resources.getString(R.string.app_dir));
        if (!laDir.isDirectory()) {
            laDir.mkdir();
        }

        for (String name : namesToEnvironments.keySet()) {
            ListeningEnvironment listeningEnvironment = namesToEnvironments.get(name);
            if (!listeningEnvironment.isSaveDirty()) {
                continue;
            }

            String filename = name.concat(resources.getString(R.string.environment_file_suffix));
            String temporaryFilename = filename.concat(TEMPORARY_SAVE_FILE_SUFFIX);
            File file = new File(laDir, filename);
            File fileTemporary = new File(laDir, temporaryFilename);

            FileOutputStream fos = new FileOutputStream(fileTemporary);
            ObjectOutputStream output = new ObjectOutputStream(fos);
            try {
                output.writeObject(namesToEnvironments.get(name));
            }
            finally {
                output.close();
            }

            file.delete();
            fileTemporary.renameTo(file);
        }
    }

}
