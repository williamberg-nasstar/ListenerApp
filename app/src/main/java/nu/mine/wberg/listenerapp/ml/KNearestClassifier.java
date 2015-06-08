package nu.mine.wberg.listenerapp.ml;

import java.util.HashMap;
import java.util.Map;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.environments.ListeningEnvironment;

/**
 * @author wberg
 */
public class KNearestClassifier implements Classifier {

    private int k;

    public KNearestClassifier(int k) {
        this.k = k;
    }

    @Override
    public String classify(Map<String, ListeningEnvironment> population, MfcFingerprint candidate) {
        EnvironmentDistancePair[] nearestEnvironments = new EnvironmentDistancePair[k];

        for(int i = 0; i < nearestEnvironments.length; i++) {
            nearestEnvironments[i] = new EnvironmentDistancePair("", Double.MAX_VALUE);
        }

        for(String environmentName : population.keySet()) {
            ListeningEnvironment listeningEnvironment = population.get(environmentName);
            for (MfcFingerprint mfcFingerprint : listeningEnvironment.getMfcFingerprints()) {
                double pointDistance = euclideanDistance(mfcFingerprint.getMfc(), candidate.getMfc());

                for (int i = 0; i < nearestEnvironments.length; i++) {
                    if (pointDistance < nearestEnvironments[i].distance) {
                        nearestEnvironments[i] = new EnvironmentDistancePair(environmentName, pointDistance);
                        break;
                    }
                }
            }
        }

        HashMap<String, Integer> environmentCounts = new HashMap<>();
        for(EnvironmentDistancePair environmentDistancePair : nearestEnvironments) {
            if (environmentCounts.containsKey(environmentDistancePair.contextName)) {
                environmentCounts.put(environmentDistancePair.contextName, environmentCounts.get(environmentDistancePair.contextName) + 1);
            } else {
                environmentCounts.put(environmentDistancePair.contextName, 1);
            }
        }

        int highest = 0;
        String highestOccurringContext = "";
        for(String c : environmentCounts.keySet()) {
            if (environmentCounts.get(c) > highest) {
                highest = environmentCounts.get(c);
                highestOccurringContext = c;
            }
        }

        return highestOccurringContext;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double acc = 0.0;
        for(int i=0; i<a.length && i<b.length; i++)
            acc += (a[i]-b[i]) * (a[i]-b[i]);

        return Math.sqrt(acc);
    }

    private static class EnvironmentDistancePair {
        public String contextName;
        public double distance;

        public EnvironmentDistancePair(String contextName, double distance)
        {
            this.contextName = contextName;
            this.distance = distance;
        }
    }

}
