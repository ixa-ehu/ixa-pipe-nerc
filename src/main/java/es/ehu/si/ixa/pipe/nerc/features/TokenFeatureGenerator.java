package es.ehu.si.ixa.pipe.nerc.features;

import java.util.ArrayList;
import java.util.List;

import es.ehu.si.ixa.pipe.nerc.dict.BrownCluster;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

/**
 * Generates a feature which contains the token itself.
 */
public class TokenFeatureGenerator extends FeatureGeneratorAdapter {

  private static final String WORD_PREFIX = "w";
  private boolean lowercase;
  private boolean brownFeatures;
  private BrownCluster brownCluster;
  public static String unknowndistSimClass = "JAR";
  public static final int[] pathLengths = {4,6,10,20}; 

  public TokenFeatureGenerator(boolean lowercase, boolean brownFeatures, BrownCluster brownCluster) {
    this.lowercase = lowercase;
    this.brownFeatures = brownFeatures;
    if (brownFeatures) {
      this.brownCluster = brownCluster;
    }
  }
  
  public TokenFeatureGenerator(boolean lowercase) {
    this(lowercase, false, null);
  }

  public TokenFeatureGenerator() {
    this(true, false, null);
  }

  public void loadBrownClusterLexicon(Dictionary brownLexicon) {
    brownLexicon = brownCluster.getDictionary();
  }
  
 public String[] getWordClasses(String token, Dictionary brownLexicon) {
    
    String distSim = brownLexicon.getDict().get(token);
    List<String> pathLengthsList = new ArrayList<String>();
    pathLengthsList.add(distSim.substring(0, Math.min(distSim.length(), pathLengths[0])));
    for (int i = 1; i < pathLengths.length; ++i) {
      if (pathLengths[i - 1] < pathLengths.length) {
        pathLengthsList.add(distSim.substring(0, Math.min(distSim.length(), pathLengths[i])));
      }
    }
    String[] paths = new String[pathLengthsList.size()];
    for (int i = 0; i < pathLengthsList.size(); ++i) {
      paths[i] = pathLengthsList.get(i);
    }
    return paths;
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {
  
    if (brownFeatures) {
      Dictionary brownLexicon = new Dictionary();
      loadBrownClusterLexicon(brownLexicon);
      String[] wordClasses = getWordClasses(tokens[index], brownLexicon);
      for (String wordClass : wordClasses) {
        features.add(WORD_PREFIX + "=" + tokens[index] + "," + wordClass);
      }
      if (lowercase) {
        for (String wordClass : wordClasses) {
          features.add(WORD_PREFIX + "=" + tokens[index].toLowerCase() + "," + wordClass);
        }
      }
    }
    if (lowercase) {
      features.add(WORD_PREFIX + "=" + tokens[index].toLowerCase());
    }
    else {
      features.add(WORD_PREFIX + "=" + tokens[index]);
    }
  }
}
