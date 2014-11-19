package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.ArrayList;
import java.util.List;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.AggregatedFeatureGenerator;

/**
 * Generates previous and next features for a given {@link AdaptiveFeatureGenerator}.
 * The window size can be specified.
 *
 * Features:
 * Current token is always included unchanged
 * Previous tokens are prefixed with p distance
 * Next tokens are prefix with n distance
 */
public class WindowFeatureGenerator implements AdaptiveFeatureGenerator {

  public static final String PREV_PREFIX = "p";
  public static final String NEXT_PREFIX = "n";

  private final AdaptiveFeatureGenerator generator;

  private final int prevWindowSize;
  private final int nextWindowSize;

  /**
   * Initializes the current instance with the given parameters.
   *
   * @param generator Feature generator to apply to the window.
   * @param prevWindowSize Size of the window to the left of the current token.
   * @param nextWindowSize Size of the window to the right of the current token.
   */
  public WindowFeatureGenerator(AdaptiveFeatureGenerator generator, int prevWindowSize,  int nextWindowSize) {
    if (prevWindowSize < 1 || nextWindowSize < 1) {
      throw new IllegalArgumentException("window parameter must be at least 1. " +
          "minLength=" + prevWindowSize + ", maxLength= " + nextWindowSize);
    }
    this.generator = generator;
    this.prevWindowSize = prevWindowSize;
    this.nextWindowSize = nextWindowSize;
  }
  
  /**
   * Initializes the current instance with the given parameters.
   * 
   * @param prevWindowSize
   * @param nextWindowSize
   * @param generators
   */
  public WindowFeatureGenerator(int prevWindowSize, int nextWindowSize, AdaptiveFeatureGenerator... generators) {
    this(new AggregatedFeatureGenerator(generators), prevWindowSize, nextWindowSize);
  }
  
  /**
   * Initializes the current instance. The previous and next window size is 5.
   *
   * @param generator feature generator
   */
  public WindowFeatureGenerator(AdaptiveFeatureGenerator generator) {
    this(generator, 6, 6);
  }
  
  /**
   * Initializes the current instance with the given parameters.
   * 
   * @param generators array of feature generators
   */
  public WindowFeatureGenerator(AdaptiveFeatureGenerator... generators) {
    this(new AggregatedFeatureGenerator(generators), 5, 5);
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
    // current features
    generator.createFeatures(features, tokens, index, preds);

    // previous features
    for (int i = 1; i < prevWindowSize + 1; i++) {
      if (index - i >= 0) {

        List<String> prevFeatures = new ArrayList<String>();

        generator.createFeatures(prevFeatures, tokens, index - i, preds);

        for (String prevFeature : prevFeatures) {
          features.add(PREV_PREFIX + i + prevFeature);
        }
      }
    }

    // next features
    for (int i = 1; i < nextWindowSize + 1; i++) {
      if (i + index < tokens.length) {

        List<String> nextFeatures = new ArrayList<String>();

        generator.createFeatures(nextFeatures, tokens, index + i, preds);

        for (String nextFeature : nextFeatures) {
          features.add(NEXT_PREFIX + i + nextFeature);
        }
      }
    }
  }

  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    generator.updateAdaptiveData(tokens, outcomes);
  }

  public void clearAdaptiveData() {
      generator.clearAdaptiveData();
  }

  @Override
  public String toString() {
    return super.toString()+": Prev window size: " + prevWindowSize +", Next window size: " + nextWindowSize;
  }
}