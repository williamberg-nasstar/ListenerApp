package nu.mine.wberg.listenerapp.environments;

import java.util.Set;

/**
 * @author wberg
 */
public class ListeningEnvironment {

    private Set<short[]> recordings;

    public void add(short[] recording) {
        recordings.add(recording);
    }

}
