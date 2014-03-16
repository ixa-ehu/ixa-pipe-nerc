package ixa.pipe.nerc.train;

import ixa.pipe.nerc.Dictionaries;
import ixa.pipe.nerc.DictionaryNameFinder;

import java.util.List;

import opennlp.tools.util.featuregen.FeatureGeneratorAdapter;


public class DictionaryFeatureGenerator extends FeatureGeneratorAdapter {

  private InSpanGenerator isg;
  
  public DictionaryFeatureGenerator(Dictionaries dict) {
    this("",dict);
  }
  public DictionaryFeatureGenerator(String prefix, Dictionaries dict) {
    setDictionary(prefix,dict);
  }
  
  public void setDictionary(Dictionaries dict) {
    setDictionary("",dict);
  }
  
  public void setDictionary(String name, Dictionaries dict) {
    isg = new InSpanGenerator(name, new DictionaryNameFinder(dict));
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    isg.createFeatures(features, tokens, index, previousOutcomes);
  }
  
}
