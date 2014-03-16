package ixa.pipe.nerc.train;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;
import opennlp.tools.util.featuregen.StringPattern;

import java.util.List;

/**
 * Generates features for different for the class of the token.
 */
public class POSFeatureGenerator extends FeatureGeneratorAdapter {

  private static final String TOKEN_CLASS_PREFIX = "wc";
  private static final String TOKEN_AND_CLASS_PREFIX = "w&c";

  private boolean generatePOSFeature;

  public POSFeatureGenerator() {
    this(false);
  }

  public POSFeatureGenerator(boolean generatePOSFeature) {
    this.generatePOSFeature = generatePOSFeature;
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] preds) {
    String wordClass = tokenFeature(tokens[index]);
    features.add(TOKEN_CLASS_PREFIX + "=" + wordClass);

    if (generatePOSFeature) {
      features.add(TOKEN_AND_CLASS_PREFIX + "=" + tokens[index].toLowerCase()
          + "," + wordClass);
    }
  }
  
  public static String tokenFeature(String token) {

    StringPattern pattern = StringPattern.recognize(token);
    
    String feat;
    if (pattern.isAllLowerCaseLetter()) {
      feat = "lc";
    }
    else if (pattern.digits() == 2) {
      feat = "2d";
    }
    else if (pattern.digits() == 4) {
      feat = "4d";
    }
    else if (pattern.containsDigit()) {
      if (pattern.containsLetters()) {
        feat = "an";
      }
      else if (pattern.containsHyphen()) {
        feat = "dd";
      }
      else if (pattern.containsSlash()) {
        feat = "ds";
      }
      else if (pattern.containsComma()) {
        feat = "dc";
      }
      else if (pattern.containsPeriod()) {
        feat = "dp";
      }
      else {
        feat = "num";
      }
    }
    else if (pattern.isAllCapitalLetter() && token.length() == 1) {
      feat = "sc";
    }
    else if (pattern.isAllCapitalLetter()) {
      feat = "ac";
    }
    else if (pattern.isInitialCapitalLetter()) {
      feat = "ic";
    }
    else {
      feat = "other";
    }

    return (feat);
  }
}
