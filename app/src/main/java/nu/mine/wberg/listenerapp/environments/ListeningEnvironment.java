package nu.mine.wberg.listenerapp.environments;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * @author wberg
 */
public class ListeningEnvironment implements Serializable {

    private Set<short[]> recordings;

    public ListeningEnvironment() {
        recordings = new HashSet<>();
    }

    public void add(short[] recording) {
        recordings.add(recording);
    }

}
