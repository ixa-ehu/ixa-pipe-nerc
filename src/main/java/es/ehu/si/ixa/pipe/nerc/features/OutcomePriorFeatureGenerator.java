package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;


/**
 * The definition feature maps the underlying distribution of outcomes.
 */
public class OutcomePriorFeatureGenerator extends FeatureGeneratorAdapter {

  public static final String OUTCOME_PRIOR_FEATURE = "def";

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    features.add(OUTCOME_PRIOR_FEATURE);
  }
}

