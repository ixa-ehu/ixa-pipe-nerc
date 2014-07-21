package es.ehu.si.ixa.pipe.nerc.features;

import opennlp.tools.util.BeamSearchContextGenerator;

/**
 * Interface for generating the context for an name finder by specifying a set of geature generators.
 *
 */
public interface NameContextGenerator extends BeamSearchContextGenerator<String> {

  /**
   * Adds a feature generator to this set of feature generators.
   * @param generator The feature generator to add.
   */
  public void addFeatureGenerator(AdaptiveFeatureGenerator generator);

  /**
   * Informs all the feature generators for a name finder that the specified tokens have been classified with the coorisponds set of specified outcomes.
   * @param tokens The tokens of the sentence or other text unit which has been processed.
   * @param outcomes The outcomes associated with the specified tokens.
   */
  public void updateAdaptiveData(String[] tokens, String[] outcomes);

  /**
   * Informs all the feature generators for a name finder that the context of the adaptive data (typically a document) is no longer valid.
   */
  public void clearAdaptiveData();

}
