package nu.mine.wberg.listenerapp.ml;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.environments.ListeningEnvironment;

public class KNearestClassifier implements Classifier {

    private int k;

    public KNearestClassifier(int k) {
        this.k = k;
    }

    @Override
    public String classify(Map<String, ListeningEnvironment> population, MfcFingerprint candidate) {
        List<EnvironmentDistancePair> environments = new ArrayList<>();

        for(String environmentName : population.keySet()) {
            ListeningEnvironment listeningEnvironment = population.get(environmentName);
            for (MfcFingerprint mfcFingerprint : listeningEnvironment.getMfcFingerprints()) {
                double pointDistance = euclideanDistance(mfcFingerprint.getMfc(), candidate.getMfc());
                environments.add(new EnvironmentDistancePair(environmentName, pointDistance));
                Log.d("classifier", environmentName + " was at distance " + pointDistance);
            }
        }

        Collections.sort(environments);
        List<EnvironmentDistancePair> kNearest = environments.subList(0, k);

        HashMap<String, Integer> environmentCounts = new HashMap<>();
        for (EnvironmentDistancePair environmentDistancePair : kNearest) {
            if (environmentCounts.containsKey(environmentDistancePair.contextName)) {
                environmentCounts.put(environmentDistancePair.contextName, environmentCounts.get(environmentDistancePair.contextName) + 1);
            } else {
                environmentCounts.put(environmentDistancePair.contextName, 1);
            }
        }

        int highest = 0;
        String highestOccurringContext = "";
        for(String c : environmentCounts.keySet()) {
            Log.d("classifier", "context " + c + " occurred " + environmentCounts.get(c) + " times in " + k + " nearest");
            if (environmentCounts.get(c) > highest) {
                highest = environmentCounts.get(c);
                highestOccurringContext = c;
            }
        }

        Log.d("classifier", "selected context " + highestOccurringContext);
        return highestOccurringContext;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double acc = 0.0;
        for(int i = 0; i < a.length && i < b.length; i++) {
            acc += (a[i] - b[i]) * (a[i] - b[i]);
        }

        return Math.sqrt(acc);
    }

    private static class EnvironmentDistancePair implements Comparable {
        public String contextName;
        public double distance;

        public EnvironmentDistancePair(String contextName, double distance) {
            this.contextName = contextName;
            this.distance = distance;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(distance, ((EnvironmentDistancePair)o).distance);
        }

    }

}
