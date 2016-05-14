package nu.mine.wberg.listenerapp.ml;

import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.speakers.SpeakerData;

public class KNearestClassifier implements Classifier {

    private int k;

    public KNearestClassifier(int k) {
        this.k = k;
    }

    @Override
    public String classify(Map<String, SpeakerData> population, MfcFingerprint candidate) {
        List<SpeakerDistancePair> speakers = new ArrayList<>();

        for(String speakerName : population.keySet()) {
            SpeakerData speaker = population.get(speakerName);
            for (MfcFingerprint mfcFingerprint : speaker.getMfcFingerprints()) {
                double pointDistance = euclideanDistance(mfcFingerprint.getMfc(), candidate.getMfc());
                speakers.add(new SpeakerDistancePair(speakerName, pointDistance));
                Log.d("classifier", speakerName + " was at distance " + pointDistance);
            }
        }

        Collections.sort(speakers);
        List<SpeakerDistancePair> kNearest = speakers.subList(0, k);

        HashMap<String, Integer> speakerCounts = new HashMap<>();
        for (SpeakerDistancePair speakerDistancePair : kNearest) {
            if (speakerCounts.containsKey(speakerDistancePair.speakerName)) {
                speakerCounts.put(speakerDistancePair.speakerName, speakerCounts.get(speakerDistancePair.speakerName) + 1);
            } else {
                speakerCounts.put(speakerDistancePair.speakerName, 1);
            }
        }

        int highest = 0;
        String highestOccurringContext = "";
        for(String c : speakerCounts.keySet()) {
            Log.d("classifier", "speaker " + c + " occurred " + speakerCounts.get(c) + " times in " + k + " nearest");
            if (speakerCounts.get(c) > highest) {
                highest = speakerCounts.get(c);
                highestOccurringContext = c;
            }
        }

        Log.d("classifier", "selected speaker " + highestOccurringContext);
        return highestOccurringContext;
    }

    private double euclideanDistance(double[] a, double[] b) {
        double acc = 0.0;
        for(int i = 0; i < a.length && i < b.length; i++) {
            acc += (a[i] - b[i]) * (a[i] - b[i]);
        }

        return Math.sqrt(acc);
    }

    private static class SpeakerDistancePair implements Comparable {
        public String speakerName;
        public double distance;

        public SpeakerDistancePair(String speakerName, double distance) {
            this.speakerName = speakerName;
            this.distance = distance;
        }

        @Override
        public int compareTo(Object o) {
            return Double.compare(distance, ((SpeakerDistancePair)o).distance);
        }

    }

}
