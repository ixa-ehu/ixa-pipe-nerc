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

import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SentenceFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API. These are the
 * default Apache OpenNLP 1.5.3 features. This featureset is kept here for
 * upstream compatibility. See {@code DefaultNameContextGenerator}.
 * @author ragerri
 * @version 2014-07-11
 */
public class DefaultNameFinderTrainer extends AbstractNameFinderTrainer {
  /**
   * Construct default opennlp name finder trainer.
   * @param trainData the training data
   * @param testData the test data
   * @param lang the language
   * @param beamsize the beamsize
   * @param corpusFormat the corpus format
   * @param netypes the NE classes
   * @throws IOException throw if input data problems
   */
  public DefaultNameFinderTrainer(final String trainData, final String testData, final String lang, final int beamsize, final String corpusFormat, final String netypes) throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);
    setFeatures(createFeatureGenerator());
  }
  /**
   * Construct trainer with beamsize features only.
   * @param beamsize the beamsize
   */
  public DefaultNameFinderTrainer(final int beamsize) {
    super(beamsize);
    setFeatures(createFeatureGenerator());
  }
  /* (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#createFeatureGenerator()
   */
  public final AdaptiveFeatureGenerator createFeatureGenerator() {
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
        new BigramFeatureGenerator(), new SentenceFeatureGenerator(true,
            false)));
    return featuresList;
  }
}
