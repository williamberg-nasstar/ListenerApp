package nu.mine.wberg.listenerapp.speakers;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;

public class Speaker implements Serializable {

    private boolean saveDirty;
    private Set<MfcFingerprint> mfcFingerprints;

    public Speaker() {
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
