package nu.mine.wberg.listenerapp.ml;

import java.util.Map;

import nu.mine.wberg.listenerapp.analysis.mfcc.MfcFingerprint;
import nu.mine.wberg.listenerapp.environments.ListeningEnvironment;

/**
 * @author wberg
 */
public interface Classifier {

    String classify(Map<String, ListeningEnvironment> population, MfcFingerprint candidate);

}
