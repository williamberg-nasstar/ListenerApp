package nu.mine.wberg.listenerapp.ml;

import java.util.Map;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.speakers.SpeakerData;

public interface Classifier {

    String classify(Map<String, SpeakerData> population, MfcFingerprint candidate);

}
