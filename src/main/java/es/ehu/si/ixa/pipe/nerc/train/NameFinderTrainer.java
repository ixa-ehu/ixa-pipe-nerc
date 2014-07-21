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


import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

/**
 * This interface defines the feature creation, the training method and the
 * evaluation of the trained model.
 * @author ragerri
 * @version 2014-07-11
 */
public interface NameFinderTrainer {
  /**
   * Generates the adaptive features to train Named Entity taggers.
   * Every class extending {@code AbstractNameFinderTrainer} need to
   * provide an implementation of this method.
   * @return the adaptive features
   */
  AdaptiveFeatureGenerator createFeatureGenerator();

  /**
   * Generate {@link TokenNameFinderModel} models.
   * @param params
   *          the training parameters file
   * @return the model
   */
  NameModel train(TrainingParameters params);

  /**
   * Trains a model with cross evaluation. This only makes sense here
   * using {@code GISTrainer} optimization.
   * @param devData the development data
   * @param params the parameters
   * @param evalRange the range at which the evaluation is performed
   * @return the model trained with the best parameters
   */
  NameModel trainCrossEval(String devData,
      TrainingParameters params, String[] evalRange);

  /**
   * Evaluate the model on a test set.
   * @param trainedModel
   *          the trained model
   * @param testSamples
   *          the test set
   * @return the results in terms of precision and recall
   */
  NameFinderEvaluator evaluate(NameModel trainedModel,
      ObjectStream<CorpusSample> testSamples);

}

