package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

/**
 * An interface for generating features for name entity identification and for
 * updating document level contexts.
 * <p>
 * Most implementors do not need the adaptive functionality of this interface,
 * they should extend the {@link FeatureGeneratorAdapter} class instead.
 * <p>
 * <b>Note:</b><br>
 * Feature generation is not thread safe and a instance of a feature generator
 * must only be called from one thread. The resources used by a feature
 * generator are typically shared between man instances of features generators
 * which are called from many threads and have to be thread safe.
 * 
 * @see FeatureGeneratorAdapter
 */
public interface AdaptiveFeatureGenerator {

  /**
   * Adds the appropriate features for the token at the specified index with the
   * specified array of previous outcomes to the specified list of features.
   * 
   * @param features
   *          The list of features to be added to.
   * @param tokens
   *          The tokens of the sentence or other text unit being processed.
   * @param index
   *          The index of the token which is currently being processed.
   * @param previousOutcomes
   *          The outcomes for the tokens prior to the specified index.
   */
  void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes);

  /**
   * Informs the feature generator that the specified tokens have been
   * classified with the corresponding set of specified outcomes.
   * 
   * @param tokens
   *          The tokens of the sentence or other text unit which has been
   *          processed.
   * @param outcomes
   *          The outcomes associated with the specified tokens.
   */
  void updateAdaptiveData(String[] tokens, String[] outcomes);

  /**
   * Informs the feature generator that the context of the adaptive data
   * (typically a document) is no longer valid.
   */
  void clearAdaptiveData();

}
