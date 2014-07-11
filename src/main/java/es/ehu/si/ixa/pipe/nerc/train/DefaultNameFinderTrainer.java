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

package es.ehu.si.ixa.pipe.nerc.train;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API.  
 * @author ragerri 2014/06/25
 * 
 */

public class DefaultNameFinderTrainer extends AbstractNameFinderTrainer {
  
  public DefaultNameFinderTrainer(String trainData, String testData, String lang, int beamsize, String corpusFormat, String netypes) throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);
    setFeatures(createFeatureGenerator());
  }
  
  public DefaultNameFinderTrainer(int beamsize) {
    super(beamsize);
    setFeatures(createFeatureGenerator());
  }
       
  public AdaptiveFeatureGenerator createFeatureGenerator() {
    List<AdaptiveFeatureGenerator> featureList = createFeatureList();
    AdaptiveFeatureGenerator[] featuresArray = featureList
        .toArray(new AdaptiveFeatureGenerator[featureList.size()]);
    return new CachedFeatureGenerator(featuresArray);
  }
  
  /**
   * Create a list of {@link AdaptiveFeatureGenerator} features.
   *
   * @return the list of features
   */
  public static List<AdaptiveFeatureGenerator> createFeatureList() {
    List<AdaptiveFeatureGenerator> featuresList = new ArrayList<AdaptiveFeatureGenerator>(Arrays.asList(
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(), new SentenceFeatureGenerator(true,
            false)));
    return featuresList;
  }
 
}
