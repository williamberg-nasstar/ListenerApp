package nu.mine.wberg.listenerapp.environments;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wberg
 */
public class ListeningEnvironment implements Serializable {

    private Set<short[]> recordings;
    private boolean saveDirty;

    public ListeningEnvironment() {
        recordings = new HashSet<>();
        saveDirty = false;
    }

    public void add(short[] recording) {
        recordings.add(recording);
        saveDirty = true;
    }

    public boolean isSaveDirty() {
        return saveDirty;
    }

    public void setSaveDirty(boolean saveDirty) {
        this.saveDirty = saveDirty;
    }

}
