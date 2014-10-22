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

import opennlp.tools.formats.Conll02NameSampleStream;
import opennlp.tools.formats.Conll03NameSampleStream;
import opennlp.tools.formats.EvalitaNameSampleStream;
import opennlp.tools.namefind.BilouCodec;
import opennlp.tools.namefind.BioCodec;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.NameSampleDataStream;
import opennlp.tools.namefind.NameSampleTypeFilter;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderFactory;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;

import com.google.common.base.Charsets;
import com.google.common.io.Files;

import es.ehu.si.ixa.pipe.nerc.formats.GermEval2014InnerNameStream;
import es.ehu.si.ixa.pipe.nerc.formats.GermEval2014OuterNameStream;

/**
 * Abstract class for common training functionalities. Every other trainer class
 * needs to extend this class.
 * @author ragerri
 * @version 2014-04-17
 */
public abstract class AbstractTrainer implements Trainer {
  
  /**
   * The language.
   */
  private String lang;
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
  protected ObjectStream<NameSample> trainSamples;
  /**
   * ObjectStream of the test data.
   */
  protected ObjectStream<NameSample> testSamples;
  /**
   * beamsize value needs to be established in any class extending this one.
   */
  protected int beamSize;
  /**
   * The sequence encoding of the named entity spans, e.g., BIO or BILOU.
   */
  private String sequenceCodec;
  /**
   * The corpus format: conll02, conll03, germEvalOuter2014, germEvalInner2014 and opennlp.
   */
  protected String corpusFormat;
  /**
   * The named entity types.
   */
  private static int types;
  /**
   * features needs to be implemented by any class extending this one.
   */
  private TokenNameFinderFactory nameClassifierFactory;

  /**
   * Construct a trainer with training and test data, and with options for
   * language, beamsize for decoding, sequence codec and corpus format (conll or opennlp).
   * @param aTrainData
   *          the training data
   * @param aTestData
   *          the test data
   * @throws IOException
   *           io exception
   */
  public AbstractTrainer(final String aTrainData,
      final String aTestData, final TrainingParameters params) throws IOException {
    
    this.lang = Flags.getLanguage(params);
    this.corpusFormat = Flags.getCorpusFormat(params);
    this.trainData = aTrainData;
    this.testData = aTestData;
    trainSamples = getNameStream(trainData, lang, corpusFormat);
    testSamples = getNameStream(testData, lang, corpusFormat);
    this.beamSize = Flags.getBeamsize(params);
    this.sequenceCodec = Flags.getSequenceCodec(params);
    if (params.getSettings().get("Types") != null) {
      String netypes = params.getSettings().get("Types");
      String[] neTypes = netypes.split(",");
      trainSamples = new NameSampleTypeFilter(neTypes, trainSamples);
      testSamples = new NameSampleTypeFilter(neTypes, testSamples);
      types = neTypes.length;
    }
  }

  /*
   * (non-Javadoc)
   * @see
   * es.ehu.si.ixa.pipe.nerc.train.Trainer#train(opennlp.tools.util
   * .TrainingParameters)
   */
  public final TokenNameFinderModel train(final TrainingParameters params) {
    if (getNameClassifierFactory() == null) {
      throw new IllegalStateException(
          "Classes derived from AbstractNameFinderTrainer must create and fill the AdaptiveFeatureGenerator features!");
    }
    TokenNameFinderModel trainedModel = null;
    TokenNameFinderEvaluator nerEvaluator = null;
    try {
      trainedModel = NameFinderME.train(lang, null, trainSamples, params,
          nameClassifierFactory);
      NameFinderME nerTagger = new NameFinderME(trainedModel);
      nerEvaluator = new TokenNameFinderEvaluator(nerTagger);
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
  public TokenNameFinderModel trainCrossEval(final String devData,
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
    TokenNameFinderModel trainedModel = train(crossEvalParams);
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
        ObjectStream<NameSample> aTrainSamples = getNameStream(trainData, lang,
            corpusFormat);
        ObjectStream<NameSample> devSamples = getNameStream(devData, lang,
            corpusFormat);

        // dynamic creation of parameters
        params.put(TrainingParameters.ITERATIONS_PARAM,
            Integer.toString(iteration));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(cuttOff));
        System.err.println("Trying with " + iteration + " iterations...");

        // training model
        TokenNameFinderModel trainedModel = NameFinderME.train(lang, null,
            aTrainSamples, params, getNameClassifierFactory());
        // evaluate model
        NameFinderME nerClassifier = new NameFinderME(trainedModel);
        TokenNameFinderEvaluator nerEvaluator = new TokenNameFinderEvaluator(nerClassifier);
        nerEvaluator.evaluate(devSamples);
        double result = nerEvaluator.getFMeasure().getFMeasure();
        double precision = nerEvaluator.getFMeasure().getPrecisionScore();
        double recall = nerEvaluator.getFMeasure().getRecallScore();
        StringBuilder sb = new StringBuilder();
        sb.append("Iterations: ").append(iteration).append(" cutoff: ")
            .append(cuttOff).append(" ").append("PRF: ").append(precision)
            .append(" ").append(recall).append(" ").append(result).append("\n");
        Files.append( sb.toString(), new File("ner-results.txt"), Charsets.UTF_8);
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
  public static ObjectStream<NameSample> getNameStream(final String inputData,
      final String aLang, final String aCorpusFormat) throws IOException {
    ObjectStream<NameSample> samples = null;
    if (aCorpusFormat.equalsIgnoreCase("conll03")) {
      ObjectStream<String> nameStream = InputOutputUtils.readInputData(inputData);
      if (aLang.equalsIgnoreCase("en")) {
        samples = new Conll03NameSampleStream(Conll03NameSampleStream.LANGUAGE.EN, nameStream, types);
      }
      else if (aLang.equalsIgnoreCase("de")) {
        samples = new Conll03NameSampleStream(Conll03NameSampleStream.LANGUAGE.DE, nameStream, types);
      } 
    } else if (aCorpusFormat.equalsIgnoreCase("conll02")) {
      ObjectStream<String> nameStream = InputOutputUtils
          .readInputData(inputData);
      if (aLang.equalsIgnoreCase("es")) {
        samples = new Conll02NameSampleStream(Conll02NameSampleStream.LANGUAGE.ES, nameStream, types);
      }
      else if (aLang.equalsIgnoreCase("nl")) {
        samples = new Conll02NameSampleStream(Conll02NameSampleStream.LANGUAGE.NL, nameStream, types);
      }
    } else if (aCorpusFormat.equalsIgnoreCase("evalita")) {
      ObjectStream<String> nameStream = InputOutputUtils.readInputData(inputData);
      samples = new EvalitaNameSampleStream(EvalitaNameSampleStream.LANGUAGE.IT, nameStream, types);
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
      samples = new NameSampleDataStream(nameStream);
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
  public final TokenNameFinderFactory getNameClassifierFactory() {
    return nameClassifierFactory;
  }
  
  public final void setNameClassifierFactory(TokenNameFinderFactory aFactory) {
    this.nameClassifierFactory = aFactory;
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
   * @param aLang
   *          the language
   */
  public final void setLanguage(final String aLang) {
    this.lang = aLang;
  }
  
  /**
   * Get the Sequence codec.
   * @return the sequence codec
   */
  public final String getSequenceCodec() {
    String seqCodec = null;
    if ("BIO".equals(sequenceCodec)) {
      seqCodec = BioCodec.class.getName();
    }
    else if ("BILOU".equals(sequenceCodec)) {
      seqCodec = BilouCodec.class.getName();
    }
    return seqCodec;
  }
  
  /**
   * Set the sequence codec.
   * @param aSeqCodec the sequence codec to be set
   */
  public final void setSequenceCodec(final String aSeqCodec) {
    this.sequenceCodec = aSeqCodec;
  }

}
