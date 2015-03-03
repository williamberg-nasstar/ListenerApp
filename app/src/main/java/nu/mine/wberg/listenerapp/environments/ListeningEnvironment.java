package nu.mine.wberg.listenerapp.environments;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import nu.mine.wberg.listenerapp.analysis.mfcc.amfcc.MfcFingerprint;

/**
 * @author wberg
 */
public class ListeningEnvironment implements Serializable {

    private boolean saveDirty;
    private MfcFingerprint mfcFingerprint;

    public ListeningEnvironment() {
        saveDirty = false;
    }

    public void setMfcFingerprint(MfcFingerprint mfcFingerprint) {
        this.mfcFingerprint = mfcFingerprint;
        saveDirty = true;
    }

    public boolean isSaveDirty() {
        return saveDirty;
    }

    public void setSaveDirty(boolean saveDirty) {
        this.saveDirty = saveDirty;
    }

}
