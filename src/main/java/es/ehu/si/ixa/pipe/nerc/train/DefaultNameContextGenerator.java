package es.ehu.si.ixa.pipe.nerc.train;

import java.util.ArrayList;
import java.util.List;

import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.FeatureGeneratorUtil;
import es.ehu.si.ixa.pipe.nerc.features.NameContextGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;


/**
 * Class for determining contextual features for a tag/chunk style
 * named-entity recognizer.
 */
public class DefaultNameContextGenerator implements NameContextGenerator {

  private AdaptiveFeatureGenerator featureGenerators[];

  @Deprecated
  private static AdaptiveFeatureGenerator windowFeatures = new CachedFeatureGenerator(
      new AdaptiveFeatureGenerator[]{
      new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
      new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
      new OutcomePriorFeatureGenerator(),
      new PreviousMapFeatureGenerator(),
      new BigramFeatureGenerator()
      });

  /**
   * Creates a name context generator with the specified cache size.
   */
  public DefaultNameContextGenerator(AdaptiveFeatureGenerator... featureGenerators) {

    if (featureGenerators != null) {
      this.featureGenerators = featureGenerators;
    }
    else {
      // use defaults
      this.featureGenerators = new AdaptiveFeatureGenerator[]{
          windowFeatures,
          new PreviousMapFeatureGenerator()};
    }
  }

  public void addFeatureGenerator(AdaptiveFeatureGenerator generator) {
      AdaptiveFeatureGenerator generators[] = featureGenerators;
      featureGenerators = new AdaptiveFeatureGenerator[featureGenerators.length + 1];
      System.arraycopy(generators, 0, featureGenerators, 0, generators.length);
      featureGenerators[featureGenerators.length - 1] = generator;
  }

  public void updateAdaptiveData(String[] tokens, String[] outcomes) {

    if (tokens != null && outcomes != null && tokens.length != outcomes.length) {
        throw new IllegalArgumentException(
            "The tokens and outcome arrays MUST have the same size!");
      }

    for (AdaptiveFeatureGenerator featureGenerator : featureGenerators) {
      featureGenerator.updateAdaptiveData(tokens, outcomes);
    }
  }

  public void clearAdaptiveData() {
    for (AdaptiveFeatureGenerator featureGenerator : featureGenerators) {
      featureGenerator.clearAdaptiveData();
    }
  }

  /**
   * Return the context for finding names at the specified index.
   * @param index The index of the token in the specified toks array for which the context should be constructed.
   * @param tokens The tokens of the sentence.  The <code>toString</code> methods of these objects should return the token text.
   * @param preds The previous decisions made in the tagging of this sequence.  Only indices less than i will be examined.
   * @param additionalContext Addition features which may be based on a context outside of the sentence.
   * 
   * @return the context for finding names at the specified index.
   */
  public String[] getContext(int index, String[] tokens, String[] preds, Object[] additionalContext) {
    List<String> features = new ArrayList<String>();

    for (AdaptiveFeatureGenerator featureGenerator : featureGenerators) {
      featureGenerator.createFeatures(features, tokens, index, preds);
    }

    //previous outcome features
    String po = NameClassifier.OTHER;
    String ppo = NameClassifier.OTHER;

    if (index > 1){
      ppo = preds[index-2];
    }

    if (index > 0) {
      po = preds[index-1];
    }
    features.add("po=" + po);
    features.add("pow=" + po + "," + tokens[index]);
    features.add("powf=" + po + "," + FeatureGeneratorUtil.tokenFeature(tokens[index]));
    features.add("ppo=" + ppo);

    return features.toArray(new String[features.size()]);
  }
}
