/*
 *  Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */
package es.ehu.si.ixa.pipe.nerc.features;

import es.ehu.si.ixa.pipe.nerc.DictionaryNameFinder;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;

import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;


public class DictionaryFeatureGenerator extends CustomFeatureGenerator {

  private InSpanGenerator isg;
  private Map<String, String> attributes;
  
  public DictionaryFeatureGenerator() {
  }
  
  public void setDictionary(String name, Dictionary dict) {
    isg = new InSpanGenerator(name, new DictionaryNameFinder(dict));
  }
  public void createFeatures(List<String> features, String[] tokens, int index, String[] previousOutcomes) {
    isg.createFeatures(features, tokens, index, previousOutcomes);
  }
  
  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void clearAdaptiveData() {
    // TODO Auto-generated method stub
    
  }
  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    this.attributes = properties;
    attributes.put("prefix", XMLFeatureDescriptor.prefix);
    attributes.put("dict", XMLFeatureDescriptor.dictionary.getClass().getName());
    setDictionary(attributes.get("prefix"), XMLFeatureDescriptor.dictionary);
    
  }
  
  
  
}
