package nu.mine.wberg.listenerapp.analysis.mfcc;

import java.io.Serializable;

/**
 * @author wberg
 */
public class MfcFingerprint implements Serializable {

    /**
     * Mean values (over windows) for each MFC index.
     */
    private double[] mfc;

    public MfcFingerprint(double[] mfcFingerprint) {
        this.mfc = mfcFingerprint;
    }

    public MfcFingerprint(double[][] mfcCoefficients) {
        int windows = mfcCoefficients.length;
        int doors = mfcCoefficients[0].length;

        mfc = new double[doors];
        for (int i = 0; i < doors; i++) {
            double sum = 0;
            for (int j = 0; j < windows; j++) {
                sum += mfcCoefficients[j][i];
            }
            mfc[i] = sum / doors;
        }
    }

    public double[] getMfc() {
        return mfc;
    }

}
