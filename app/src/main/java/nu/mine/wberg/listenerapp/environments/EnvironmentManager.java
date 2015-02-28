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
        File environments = new File(laDir, resources.getString(R.string.app_environments_file));
        if (!environments.isFile()) {
            return result;
        }

        FileInputStream fis = new FileInputStream(environments);
        ObjectInputStream input = new ObjectInputStream(fis);

        try {
            return (Map<String, ListeningEnvironment>)input.readObject();
        }
        finally {
             input.close();
        }
    }

    public void saveListeningEnvironments() throws IOException {
        File esDir = Environment.getExternalStorageDirectory();
        File laDir = new File(esDir, resources.getString(R.string.app_dir));
        if (!laDir.isDirectory()) {
            laDir.mkdir();
        }

        String environmentsFilename = resources.getString(R.string.app_environments_file);
        String environmentsTemporaryFilename = environmentsFilename.concat(TEMPORARY_SAVE_FILE_SUFFIX);
        File environments = new File(laDir, environmentsFilename);
        File environmentsTemporary = new File(laDir, environmentsTemporaryFilename);

        FileOutputStream fos = new FileOutputStream(environmentsTemporary);
        ObjectOutputStream output = new ObjectOutputStream(fos);
        try {
            output.writeObject(namesToEnvironments);
        }
        finally {
            output.close();
        }

        environments.delete();
        environmentsTemporary.renameTo(environments);
    }

}
