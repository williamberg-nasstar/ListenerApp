package nu.mine.wberg.listenerapp.environments;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;

/**
 * @author wberg
 */
public class ListeningEnvironment implements Serializable {

    private boolean saveDirty;
    private Set<MfcFingerprint> mfcFingerprints;

    public ListeningEnvironment() {
        mfcFingerprints = new HashSet<>();
        saveDirty = false;
    }

    public void addMfcFingerprint(MfcFingerprint mfcFingerprint) {
        mfcFingerprints.add(mfcFingerprint);
        saveDirty = true;
    }

    public Set<MfcFingerprint> getMfcFingerprints() {
        return mfcFingerprints;
    }

    public boolean isSaveDirty() {
        return saveDirty;
    }

}
