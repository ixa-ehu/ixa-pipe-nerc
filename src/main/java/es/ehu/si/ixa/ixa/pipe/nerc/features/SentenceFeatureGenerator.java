package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.List;
import java.util.Map;

import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;

public class SentenceFeatureGenerator extends CustomFeatureGenerator {

  private Map<String, String> attributes;

  public SentenceFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {

    if (attributes.get("begin").equalsIgnoreCase("true") && index == 0) {
      features.add("S=begin");
    }

    if (attributes.get("end").equalsIgnoreCase("true") && tokens.length == index + 1) {
      features.add("S=end");
    }
  }

  public void init(Map<String, String> attributes, FeatureGeneratorResourceProvider resourceProvider) {
    this.attributes = attributes;
    setBeginSentenceAttribute(attributes);
    setEndSentenceAttribute(attributes);
  }

  private void setBeginSentenceAttribute(Map<String, String> attributes) {
    attributes.put("begin", "true");
  }

  private void setEndSentenceAttribute(Map<String, String> attributes) {
    attributes.put("end", "false");
  }

  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {

  }

  @Override
  public void clearAdaptiveData() {

  }

}
