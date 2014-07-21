package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

import opennlp.tools.ngram.NGramModel;
import opennlp.tools.util.StringList;

/**
 * The {@link CharacterNgramFeatureGenerator} uses character ngrams to
 * generate features about each token.
 * The minimum and maximum length can be specified.
 */
public class CharacterNgramFeatureGenerator extends FeatureGeneratorAdapter {

  private final int minLength;
  private final int maxLength;

  public CharacterNgramFeatureGenerator(int minLength, int maxLength) {
    this.minLength = minLength;
    this.maxLength = maxLength;
  }

  /**
   * Initializes the current instance with min 2 length and max 5 length of ngrams.
   */
  public CharacterNgramFeatureGenerator() {
    this(2, 5);
  }

  public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {

    NGramModel model = new NGramModel();
    model.add(tokens[index], minLength, maxLength);

    for (StringList tokenList : model) {

      if (tokenList.size() > 0) {
        features.add("ng=" + tokenList.getToken(0).toLowerCase());
      }
    }
  }
}

