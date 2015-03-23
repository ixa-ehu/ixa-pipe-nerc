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
import es.ehu.si.ixa.ixa.pipe.nerc.dict.LemmaResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.MFSResource;
import es.ehu.si.ixa.ixa.pipe.nerc.dict.POSModelResource;

/**
 * Generate pos tag, pos tag class, lemma and most frequent sense as feature of
 * the current token. This feature generator can also be placed in a sliding
 * window.
 * 
 * @author ragerri
 * @version 2015-03-13
 */
public class MFSFeatureGenerator extends CustomFeatureGenerator implements
    ArtifactToSerializerMapper {

  private POSModelResource posModelResource;
  private LemmaResource lemmaDictResource;
  private MFSResource mfsDictResource;
  private String[] currentSentence;
  private String[] currentTags;
  private List<String> currentLemmas;
  private List<String> currentMFSList;
  private String startSymbol = null;
  private String endSymbol = null;

  public MFSFeatureGenerator() {
  }

  public void createFeatures(List<String> features, String[] tokens, int index,
      String[] previousOutcomes) {

    // cache results for each sentence
    if (currentSentence != tokens) {
      currentSentence = tokens;
      currentTags = posModelResource.posTag(tokens);
      currentLemmas = lemmaDictResource.lookUpLemmaArray(tokens, currentTags);
      currentMFSList = mfsDictResource.getLabeledMFS(currentLemmas, currentTags);
    }
    
    String curStem = currentLemmas.get(index);
    String curTok = tokens[index];
    String curPOS = currentTags[index];
    String curShape = normalize(tokens[index]);
    String firstSense = currentMFSList.get(index);
    String prevLabel = startSymbol;
    
    String prevShape = startSymbol;
    String prevPOS = startSymbol;
    String prevStem = startSymbol;
    String nextShape = startSymbol;
    String nextPOS = endSymbol;
    String nextStem = endSymbol;
    
    String prev2Shape = startSymbol;
    String prev2POS = startSymbol;
    String prev2Stem = startSymbol;
    String next2Shape = startSymbol;
    String next2POS = endSymbol;
    String next2Stem = endSymbol;
    
    if (index - 2 >= 0) {
      prev2Shape = normalize(tokens[index - 2]);
      prev2Stem = currentLemmas.get(index - 2);
      prev2POS = currentTags[index - 2];
    }
    if (index - 1 >= 0 ) {
      prevShape = normalize(tokens[index - 1]);
      prevStem = currentLemmas.get(index - 1);
      prevPOS = currentTags[index - 1];
      //TODO checkout prevLabel value
      prevLabel = previousOutcomes[index - 1];
      prevLabel = bilouToBio(prevLabel);
    }
    if (index + 1 < tokens.length) {
      nextShape = normalize(tokens[index + 1]);
      nextStem = currentLemmas.get(index + 1);
      nextPOS = currentTags[index + 1];
    }
    if (index + 2 < tokens.length) {
      next2Shape = normalize(tokens[index + 2]);
      next2Stem = currentLemmas.get(index + 2);
      next2POS = currentTags[index + 2];
    }
    
    features.add("bias");
    
    if (firstSense == null) {
      firstSense = "O";
    }
    features.add("firstSense=" + firstSense);
    features.add("firstSense,curTok=" + firstSense + "," + curStem);
    
    if (prevLabel != startSymbol) {
      features.add("prevLabel=" + prevLabel);
    }
    
    if(curPOS.equals("NN") || curPOS.equals("NNS")){
        features.add("curPOS_common");
    }
    if(curPOS.equals("NNP") || curPOS.equals("NNPS")){
        features.add("curPOS_proper");
    }
    features.add("curTok=" + curStem);
    features.add("curPOS=" + curPOS);
    features.add("curPOS_0" + curPOS.charAt(0));
    
    if (prevPOS != startSymbol) {
      features.add("prevTok=" + prevStem);
      features.add("prevPOS=" + prevPOS);
      features.add("prevPOS_0=" + prevPOS.charAt(0));
    }
    
    if (nextPOS != endSymbol) {
      features.add("nextTok=" + nextStem);
      features.add("nextPOS=" + nextPOS);
      features.add("nextPOS_0" + nextPOS.charAt(0));
    }
    
    if (prev2POS != startSymbol) {
      features.add("prev2Tok=" + prev2Stem);
      features.add("prev2POS=" + prev2POS);
      features.add("prev2POS_0=" + prev2POS.charAt(0));
    }
    if (next2POS != endSymbol) {
      features.add("next2Tok=" + next2Stem);
      features.add("next2POS=" + next2POS);
      features.add("next2POS_0=" + next2POS.charAt(0));
    }
    
    features.add("curShape=" + curShape);
    if (prevPOS != startSymbol) {
      features.add("prevShape=" + prevShape);
    }
    if (nextPOS != endSymbol) {
      features.add("nextShape=" + nextShape);
    }
    if (prev2POS != startSymbol) {
      features.add("prev2Shape=" + prev2Shape);
    }
    if (next2POS != endSymbol) {
      features.add("next2Shape=" + next2Shape);
    }
    
    //word shapes with no window
    String firstCharCurTok = curTok.substring(0, 1);
    if (firstCharCurTok.toLowerCase().equals(firstCharCurTok)) {
      features.add("curTokLowercase");
    } else if (index == 0) {
      features.add("curTokUpperCaseFirstChar");
    } else {
      features.add("curTokUpperCaseOther");
    }
    
  }
  
  /**
   * Normalize upper case, lower case, digits and duplicate characters.
   * 
   * @param token
   *          the token to be normalized
   * @return the normalized tokens
   */
  private String normalize(String token) {
    String normalizedToken = "";

    char currentCharacter;
    int prevCharType = -1;
    char charType = '~';
    boolean addedStar = false;
    for (int i = 0; i < token.length(); i++) {

      currentCharacter = token.charAt(i);
      if (currentCharacter >= 'A' && currentCharacter <= 'Z') {
        charType = 'X';
      } else if (currentCharacter >= 'a' && currentCharacter <= 'z') {
        charType = 'x';
      } else if (currentCharacter >= '0' && currentCharacter <= '9') {
        charType = 'd';
      } else {
        charType = currentCharacter;
      }

      if (charType == prevCharType) {
        if (!addedStar) {
          normalizedToken += "*";
          addedStar = true;
        }
      } else {
        addedStar = false;
        normalizedToken += charType;
      }
      prevCharType = charType;
    }
    return normalizedToken;
  }

  
  public static String bilouToBio(String label) {
    if (label.endsWith("-unit")) {
      label = "B-" + label.split("-unit")[0];
    } else if (label.endsWith("-start")) {
      label = "B-" + label.split("-start")[0];
    } else if (label.endsWith("-cont")) {
      label = "I-" + label.split("-cont")[0];
    } else if (label.endsWith("-last")) {
      label = "I-" + label.split("-last")[0];
    } else if (label.equalsIgnoreCase("other")) {
      label = "O";
    }
    return label;
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
    Object posResource = resourceProvider.getResource(properties.get("model"));
    if (!(posResource instanceof POSModelResource)) {
      throw new InvalidFormatException("Not a POSModelResource for key: "
          + properties.get("model"));
    }
    this.posModelResource = (POSModelResource) posResource;
    Object lemmaResource = resourceProvider.getResource(properties.get("dict"));
    if (!(lemmaResource instanceof LemmaResource)) {
      throw new InvalidFormatException("Not a LemmaResource for key: "
          + properties.get("dict"));
    }
    this.lemmaDictResource = (LemmaResource) lemmaResource;
    Object mfsResource = resourceProvider.getResource(properties.get("mfs"));
    if (!(mfsResource instanceof MFSResource)) {
      throw new InvalidFormatException("Not a MFSResource for key: "
          + properties.get("mfs"));
    }
    this.mfsDictResource = (MFSResource) mfsResource;
  }

  @Override
  public Map<String, ArtifactSerializer<?>> getArtifactSerializerMapping() {
    Map<String, ArtifactSerializer<?>> mapping = new HashMap<>();
    mapping.put("posmodelserializer",
        new POSModelResource.POSModelResourceSerializer());
    mapping.put("lemmadictserializer",
        new LemmaResource.LemmaResourceSerializer());
    mapping.put("mfsdictserializer", new MFSResource.MFSResourceSerializer());
    return Collections.unmodifiableMap(mapping);
  }
}
