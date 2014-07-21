package es.ehu.si.ixa.pipe.nerc.features;

import java.util.List;

public class AdditionalContextFeatureGenerator extends FeatureGeneratorAdapter {

  private String[][] additionalContext;

//  public AdditionalContextFeatureGenerator() {
//  }

  public void createFeatures(List<String> features, String[] tokens, int index, String[] preds) {

    if (additionalContext != null && additionalContext.length != 0) {

      String[] context = additionalContext[index];

      for (String s : context) {
        features.add("ne=" + s);
      }
    }
  }

  public void setCurrentContext(String[][] context) {
    additionalContext = context;
  }
}
