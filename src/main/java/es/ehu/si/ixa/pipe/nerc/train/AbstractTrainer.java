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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

import org.apache.commons.io.FileUtils;

import es.ehu.si.ixa.pipe.nerc.eval.NameFinderEvaluator;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.formats.Conll02NameStream;
import es.ehu.si.ixa.pipe.nerc.formats.Conll03NameStream;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSampleDataStream;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSampleTypeFilter;
import es.ehu.si.ixa.pipe.nerc.formats.GermEval2014InnerNameStream;
import es.ehu.si.ixa.pipe.nerc.formats.GermEval2014OuterNameStream;

/**
 * Abstract class for common training functionalities. Every other trainer class
 * needs to extend this class.
 * @author ragerri
 * @version 2014-04-17
 */
public abstract class AbstractTrainer implements Trainer {

  public final static int DEFAULT_WINDOW_SIZE = 2;
  public final static int MIN_CHAR_NGRAM_LENGTH = 2;
  public final static int DEFAULT_CHAR_NGRAM_LENGTH = 6;
  /**
   * The language.
   */
  protected String lang;
  /**
   * String holding the training data.
   */
  protected String trainData;
  /**
   * String holding the testData.
   */
  protected String testData;
  /**
   * ObjectStream of the training data.
   */
  protected ObjectStream<CorpusSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  protected ObjectStream<CorpusSample> testSamples;
  /**
   * beamsize value needs to be established in any class extending this one.
   */
  protected int beamSize;
  /**
   * The corpus format: conll02, conll03, germEvalOuter2014, germEvalInner2014 and opennlp.
   */
  protected String corpusFormat;
  /**
   * features needs to be implemented by any class extending this one.
   */
  private AdaptiveFeatureGenerator features;

  /**
   * Constructs a trainer with training and test data, and with options for
   * language, beamsize for decoding, and corpus format (conll or opennlp).
   * @param aTrainData
   *          the training data
   * @param aTestData
   *          the test data
   * @param aLang
   *          the language
   * @param beamsize
   *          the beamsize
   * @param aCorpusFormat
   *          the corpus format
   * @param netypes
   *          the NE classes for which the training should be done
   * @throws IOException
   *           io exception
   */
  public AbstractTrainer(final String aTrainData,
      final String aTestData, final TrainingParameters params) throws IOException {
    this.lang = params.getSettings().get("Language");
    this.corpusFormat = params.getSettings().get("Corpus");
    this.trainData = aTrainData;
    this.testData = aTestData;
    trainSamples = getNameStream(trainData, lang, corpusFormat);
    testSamples = getNameStream(testData, lang, corpusFormat);
    this.beamSize = Integer.parseInt(params.getSettings().get("Beamsize"));
    String netypes = params.getSettings().get("Types");
    if (netypes.length() != 0) {
      String[] neTypes = netypes.split(",");
      trainSamples = new CorpusSampleTypeFilter(neTypes, trainSamples);
      testSamples = new CorpusSampleTypeFilter(neTypes, testSamples);
    }
  }

  /**
   * Constructs a trainer with only beamsize as option. This is used for tagging
   * time with the appropriate features.
   * @param beamsize
   *          the beamsize for decoding
   */
  public AbstractTrainer(final TrainingParameters params) {
    this.beamSize = Integer.parseInt(params.getSettings().get("Beamsize"));
  }

  /*
   * (non-Javadoc)
   * @see
   * es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#train(opennlp.tools.util
   * .TrainingParameters)
   */
  public final NameModel train(final TrainingParameters params) {
    if (getFeatures() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractNameFinderTrainer must create and fill the AdaptiveFeatureGenerator features!");
    }
    Map<String, Object> resources = null;
    NameModel trainedModel = null;
    NameFinderEvaluator nerEvaluator = null;
    try {
      trainedModel = NameClassifier.train(lang, null, trainSamples, params,
          getFeatures(), resources);
      NameClassifier nerTagger = new NameClassifier(trainedModel, getFeatures(), beamSize);
      nerEvaluator = new NameFinderEvaluator(nerTagger);
      nerEvaluator.evaluate(testSamples);
    } catch (IOException e) {
      System.err.println("IO error while loading traing and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    System.out.println("Final Result: \n" + nerEvaluator.getFMeasure());
    return trainedModel;
  }

  /*
   * (non-Javadoc)
   * @see
   * es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#trainCrossEval(java.lang
   * .String, java.lang.String, opennlp.tools.util.TrainingParameters,
   * java.lang.String[])
   */
  public NameModel trainCrossEval(final String devData,
      final TrainingParameters params, final String[] evalRange) {

    // get best parameters from cross evaluation
    List<Integer> bestParams = null;
    try {
      bestParams = crossEval(devData, params, evalRange);
    } catch (IOException e) {
      System.err.println("IO error while loading training and test sets!");
      e.printStackTrace();
      System.exit(1);
    }
    TrainingParameters crossEvalParams = new TrainingParameters();
    crossEvalParams.put(TrainingParameters.ALGORITHM_PARAM, params.algorithm());
    crossEvalParams.put(TrainingParameters.ITERATIONS_PARAM,
        Integer.toString(bestParams.get(0)));
    crossEvalParams.put(TrainingParameters.CUTOFF_PARAM,
        Integer.toString(bestParams.get(1)));

    // use best parameters to train model
    NameModel trainedModel = train(crossEvalParams);
    return trainedModel;
  }

  /**
   * Cross evaluation method by iterations and cutoff. This only really makes
   * sense for GIS optimization {@code GISTrainer}.
   * @param devData
   *          the development data to do the optimization
   * @param params
   *          the parameters
   * @param evalRange
   *          the range at which perform the evaluation
   * @return the best parameters, iteration and cutoff
   * @throws IOException
   *           the io exception
   */
  private List<Integer> crossEval(final String devData,
      final TrainingParameters params, final String[] evalRange)
      throws IOException {

    System.err.println("Cross Evaluation:");
    // lists to store best parameters
    List<List<Integer>> allParams = new ArrayList<List<Integer>>();
    List<Integer> finalParams = new ArrayList<Integer>();
    Map<String, Object> resources = null;

    // F:<iterations,cutoff> Map
    Map<List<Integer>, Double> results = new LinkedHashMap<List<Integer>, Double>();
    // maximum iterations and cutoff
    Integer cutoffParam = Integer.valueOf(params.getSettings().get(
        TrainingParameters.CUTOFF_PARAM));
    List<Integer> cutoffList = new ArrayList<Integer>(Collections.nCopies(
        cutoffParam, 0));
    Integer iterParam = Integer.valueOf(params.getSettings().get(
        TrainingParameters.ITERATIONS_PARAM));
    List<Integer> iterList = new ArrayList<Integer>(Collections.nCopies(
        iterParam, 0));

    for (int cuttOff = 0; cuttOff < cutoffList.size() + 1; cuttOff++) {
      int start = Integer.valueOf(evalRange[0]);
      int iterRange = Integer.valueOf(evalRange[1]);
      for (int iteration = start + start; iteration < iterList.size() + start; iteration += iterRange) {
        // reading data for training and test
        ObjectStream<CorpusSample> aTrainSamples = getNameStream(trainData, lang,
            corpusFormat);
        ObjectStream<CorpusSample> devSamples = getNameStream(devData, lang,
            corpusFormat);

        // dynamic creation of parameters
        params.put(TrainingParameters.ITERATIONS_PARAM,
            Integer.toString(iteration));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cuttOff));
        System.err.println("Trying with " + iteration + " iterations...");

        // training model
        NameModel trainedModel = NameClassifier.train(lang, null,
            aTrainSamples, params, getFeatures(), resources);
        // evaluate model
        NameClassifier nerClassifier = new NameClassifier(trainedModel, getFeatures(), beamSize);
        NameFinderEvaluator nerEvaluator = new NameFinderEvaluator(nerClassifier);
        nerEvaluator.evaluate(devSamples);
        double result = nerEvaluator.getFMeasure().getFMeasure();
        double precision = nerEvaluator.getFMeasure().getPrecisionScore();
        double recall = nerEvaluator.getFMeasure().getRecallScore();
        StringBuilder sb = new StringBuilder();
        sb.append("Iterations: ").append(iteration).append(" cutoff: ")
            .append(cuttOff).append(" ").append("PRF: ").append(precision)
            .append(" ").append(recall).append(" ").append(result).append("\n");
        FileUtils.write(new File("ner-results.txt"), sb.toString(), true);
        List<Integer> bestParams = new ArrayList<Integer>();
        bestParams.add(iteration);
        bestParams.add(cuttOff);
        results.put(bestParams, result);
        System.out.println();
        System.out.println("Iterations: " + iteration + " cutoff: " + cuttOff);
        System.out.println(nerEvaluator.getFMeasure());
      }
    }
    // print F1 results by iteration
    System.err.println();
    InputOutputUtils.printIterationResults(results);
    InputOutputUtils.getBestIterations(results, allParams);
    finalParams = allParams.get(0);
    System.err.println("Final Params " + finalParams.get(0) + " "
        + finalParams.get(1));
    return finalParams;
  }

  /**
   * Getting the stream with the right corpus format.
   * @param inputData
   *          the input data
   * @param aLang
   *          the language
   * @param aCorpusFormat
   *          the corpus format
   * @return the stream from the several corpus formats
   * @throws IOException
   *           the io exception
   */
  public static ObjectStream<CorpusSample> getNameStream(final String inputData,
      final String aLang, final String aCorpusFormat) throws IOException {
    ObjectStream<CorpusSample> samples = null;
    if (aCorpusFormat.equalsIgnoreCase("conll03")) {
      ObjectStream<String> nameStream = InputOutputUtils.readInputData(inputData);
      samples = new Conll03NameStream(aLang, nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("conll02")) {
      ObjectStream<String> nameStream = InputOutputUtils
          .readInputData(inputData);
      samples = new Conll02NameStream(aLang, nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("germEvalOuter2014")) {
      ObjectStream<String> nameStream = InputOutputUtils
          .readInputData(inputData);
      samples = new GermEval2014OuterNameStream(nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("germEvalInner2014")) {
      ObjectStream<String> nameStream = InputOutputUtils
          .readInputData(inputData);
      samples = new GermEval2014InnerNameStream(nameStream);
    } else if (aCorpusFormat.equalsIgnoreCase("opennlp")) {
      ObjectStream<String> nameStream = InputOutputUtils.readInputData(inputData);
      samples = new CorpusSampleDataStream(nameStream);
    } else {
      System.err.println("Test set corpus format not valid!!");
      System.exit(1);
    }
    return samples;
  }

  /**
   * Get the features which are implemented in each of the trainers extending
   * this class.
   * @return the features
   */
  public final AdaptiveFeatureGenerator getFeatures() {
    return features;
  }

  /**
   * Set the features. This method is used in every trainer extending this
   * class.
   * @param aFeatures
   *          the implemented features
   */
  public final void setFeatures(final AdaptiveFeatureGenerator aFeatures) {
    this.features = aFeatures;
  }
  /**
   * Get the language.
   * @return the language
   */
  public final String getLanguage() {
    return lang;
  }

  /**
   * Set the language.
   * class.
   * @param aLang
   *          the language
   */
  public final void setLanguage(final String aLang) {
    this.lang = aLang;
  }


}
