
package ixa.pipe.nerc.train;

import ixa.pipe.nerc.Gazetteer;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

import org.apache.commons.io.FileUtils;

/**
 * Training NER based on Apache OpenNLP Machine Learning API
 * 
 * @author ragerri 2013/11/20
 * 
 */

public class StatisticalNameFinderTrainer {

  String lang;  
  String trainData;
  String testData;
  ObjectStream<NameSample> trainSamples;
  ObjectStream<NameSample> testSamples;
  static Gazetteer dictPer;
  static Gazetteer dictOrg;
  static Gazetteer dictLoc;
  static Gazetteer dictKnownPer;
  static Gazetteer dictKnownOrg;
  static Gazetteer dictKnownLoc;
  
  public StatisticalNameFinderTrainer(String trainData, String testData, String lang) throws IOException {
    
    this.lang = lang;
    this.trainData = trainData;
    this.testData = testData;
    ObjectStream<String> trainStream = InputOutputUtils.readInputData(trainData);
    trainSamples = new Conll03NameStream(lang,trainStream);
    ObjectStream<String> testStream = InputOutputUtils.readInputData(testData);
    testSamples = new Conll03NameStream(lang,testStream);
    InputStream dictFilePer = getClass().getResourceAsStream("/en-wikipeople.lst");
    dictPer = new Gazetteer(dictFilePer);
    InputStream dictFileOrg = getClass().getResourceAsStream("/en-wikiorganization.lst");
    dictOrg = new Gazetteer(dictFileOrg);
    InputStream dictFileLoc = getClass().getResourceAsStream("/en-wikilocation.lst");
    dictLoc = new Gazetteer(dictFileLoc);
    InputStream dictFileKnownPer = getClass().getResourceAsStream("/en-known-people.txt");
    dictKnownPer = new Gazetteer(dictFileKnownPer);
    InputStream dictFileKnownOrg = getClass().getResourceAsStream("/en-known-organization.txt");
    dictKnownOrg = new Gazetteer(dictFileKnownOrg);
    InputStream dictFileKnownLoc = getClass().getResourceAsStream("/en-known-location.txt");
    dictKnownLoc = new Gazetteer(dictFileKnownLoc);
  }

  public static AdaptiveFeatureGenerator createDefaultFeatures1() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false) });
  }
  
  public static AdaptiveFeatureGenerator createDefaultFeatures() {
        return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
            new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
            new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
            new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
            new BigramNameFeatureGenerator(),
            new DictionaryFeatures("PERSON",dictPer),
            new DictionaryFeatures("ORGANIZATION",dictOrg),
            new DictionaryFeatures("LOCATION",dictLoc),
            new DictionaryFeatures("KPERSON",dictKnownPer),
            new DictionaryFeatures("KORGANIZATION",dictKnownOrg),
            new DictionaryFeatures("KLOCATION",dictKnownLoc),
            new SentenceFeatureGenerator(true, false) });
  }
 
  public TokenNameFinderModel train(TrainingParameters params)
      throws IOException {

    // FEATURES (if null, Apache OpenNLP loads some defaults)
    AdaptiveFeatureGenerator features = createDefaultFeatures();
    Map<String, Object> resources = null;

    // training model and evaluation
    TokenNameFinderModel trainedModel = NameFinderME.train(lang, null,
        trainSamples, params, features, resources);
    TokenNameFinderEvaluator nerEvaluator = this.evaluate(trainedModel,
        testSamples);
    System.out.println("Final Result: " + nerEvaluator.getFMeasure());
    return trainedModel;
  }

  public TokenNameFinderModel trainCrossEval(String trainData,
      String devData, TrainingParameters params, String[] evalRange)
      throws IOException {

    // get best parameters from cross evaluation
    List<Integer> bestParams = crossEval(trainData, devData, params, evalRange);
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

  private List<Integer> crossEval(String trainData, String devData,
      TrainingParameters params, String[] evalRange) throws IOException {

    // cross-evaluation
    System.out.println("Cross Evaluation:");
    // lists to store best parameters
    List<List<Integer>> allParams = new ArrayList<List<Integer>>();
    List<Integer> finalParams = new ArrayList<Integer>();
    
    // FEATURES (if null, Apache OpenNLP loads some defaults)
    AdaptiveFeatureGenerator features = createDefaultFeatures();
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
    
    for (int c = 0; c < cutoffList.size() + 1; c++) {
      int start = Integer.valueOf(evalRange[0]);
      int iterRange = Integer.valueOf(evalRange[1]);
      for (int i = start + 10; i < iterList.size() + 10; i += iterRange) {
        // reading data for training and test
        ObjectStream<String> trainStream = InputOutputUtils.readInputData(trainData);
        ObjectStream<String> devStream = InputOutputUtils.readInputData(devData);
        ObjectStream<NameSample> trainSamples = new Conll03NameStream(lang,trainStream);
        ObjectStream<NameSample> devSamples = new Conll03NameStream(lang,devStream);

        // dynamic creation of parameters
        params.put(TrainingParameters.ITERATIONS_PARAM, Integer.toString(i));
        params.put(TrainingParameters.CUTOFF_PARAM, Integer.toString(c));
        System.out.println("Trying with " + i + " iterations...");

        // training model
        TokenNameFinderModel trainedModel = NameFinderME.train(lang, null,
            trainSamples, params, features, resources);
        // evaluate model
        TokenNameFinderEvaluator nerEvaluator = this.evaluate(trainedModel,
            devSamples);
        double result = nerEvaluator.getFMeasure().getFMeasure();
        double precision = nerEvaluator.getFMeasure().getPrecisionScore();
        double recall = nerEvaluator.getFMeasure().getRecallScore();
        StringBuilder sb = new StringBuilder();
        sb.append("Iterations: ").append(i).append(" cutoff: ").append(c)
            .append(" ").append("PRF: ").append(precision).append(" ")
            .append(recall).append(" ").append(result).append("\n");
        FileUtils.write(new File("ner-results.txt"), sb.toString(), true);
        List<Integer> bestParams = new ArrayList<Integer>();
        bestParams.add(i);
        bestParams.add(c);
        results.put(bestParams, result);
        System.out.println();
        System.out.println("Iterations: " + i + " cutoff: " + c);
        System.out.println(nerEvaluator.getFMeasure());
      }
    }
    // print F1 results by iteration
    System.out.println();
    InputOutputUtils.printIterationResults(results);
    InputOutputUtils.getBestIterations(results, allParams);
    finalParams = allParams.get(0);
    System.out.println("Final Params " + finalParams.get(0) + " "
        + finalParams.get(1));
    return finalParams;
  }

  private TokenNameFinderEvaluator evaluate(TokenNameFinderModel trainedModel,
      ObjectStream<NameSample> testSamples) throws IOException {
    NameFinderME nerTagger = new NameFinderME(trainedModel,createDefaultFeatures(),NameFinderME.DEFAULT_BEAM_SIZE);
    TokenNameFinderEvaluator nerEvaluator = new TokenNameFinderEvaluator(
        nerTagger);
    nerEvaluator.evaluate(testSamples);
    return nerEvaluator;
  }

}
