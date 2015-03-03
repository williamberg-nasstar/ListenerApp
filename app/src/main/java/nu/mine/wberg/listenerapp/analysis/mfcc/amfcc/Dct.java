package nu.mine.wberg.listenerapp.analysis.mfcc.amfcc;

public class Dct {

    int cepstraCount;
    int filterCount;

    public Dct(int cepstraCount, int filterCount) {
        this.cepstraCount = cepstraCount;
        this.filterCount = filterCount;
    }

    public double[] performDCT(double y[]) {
        double cepc[] = new double[cepstraCount];
        for (int n = 1; n <= cepstraCount; n++) {
            for (int i = 1; i <= filterCount; i++) {
                cepc[n - 1] += y[i - 1] * Math.cos(Math.PI * (n - 1) / filterCount * (i - 0.5));
            }
        }
        return cepc;
    }
}
