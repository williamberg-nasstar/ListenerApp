package nu.mine.wberg.listenerapp.analysis.mfcc.amfcc;

/**
 * @author wberg
 */
public class MfcFingerprint {

    private double[][] mfc;

    public MfcFingerprint(double[][] mfc) {
        this.mfc = mfc;
    }

    public double[][] getMfc() {
        return mfc;
    }

}
