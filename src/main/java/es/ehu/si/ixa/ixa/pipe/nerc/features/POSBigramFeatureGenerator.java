/*
 * Copyright 2015 Rodrigo Agerri

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

package es.ehu.si.ixa.ixa.pipe.nerc.features;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.featuregen.ArtifactToSerializerMapper;
import opennlp.tools.util.featuregen.CustomFeatureGenerator;
import opennlp.tools.util.featuregen.FeatureGeneratorResourceProvider;
import opennlp.tools.util.model.ArtifactSerializer;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.POSModelResource;

/**
 * Generate bigrams of pos tags as features.
 * @author ragerri
 * @version 2015-10-03
 */
public class POSBigramFeatureGenerator extends CustomFeatureGenerator implements ArtifactToSerializerMapper {
  
  private POSModelResource posModelResource;
  private String[] currentSentence;
  private String[] currentTags;
  
  public POSBigramFeatureGenerator() {
  }
  
  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {
    //cache pos tagger results for each sentence
    if (currentSentence != tokens) {
      currentSentence = tokens;
      currentTags = posModelResource.posTag(tokens);
    }
    String posTag = currentTags[index];
    features.add("posTag=" + posTag);
    if (index > 0) {
      String pposTag = currentTags[index - 1];
      features.add("pposTag,posTag=" + pposTag + "," + posTag);
    }
    if (index + 1 < tokens.length) {
      String nposTag = currentTags[index + 1];
      features.add("posTag,nposTag=" + posTag + "," + nposTag);
    }
  }
  

  @Override
  public void updateAdaptiveData(String[] tokens, String[] outcomes) {
    
  }

  @Override
  public void clearAdaptiveData() {
    
  }

  @Override
  public void init(Map<String, String> properties,
      FeatureGeneratorResourceProvider resourceProvider)
      throws InvalidFormatException {
    Object dictResource = resourceProvider.getResource(properties.get("model"));
    if (!(dictResource instanceof POSModelResource)) {
      throw new InvalidFormatException("Not a POSModelResource for key: " + properties.get("model"));
    }
    this.posModelResource = (POSModelResource) dictResource;
  }
  
  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("posmodelserializer", new POSModelResource.POSModelResourceSerializer());
    return Collections.unmodifiableMap(mapping);
  }
}

