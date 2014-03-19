
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
  //lbj
  static Gazetteer cardinalNumber;
  static Gazetteer currencyFinal;
  static Gazetteer knownCorporations;
  static Gazetteer knownCountry;
  static Gazetteer knownJobs;
  static Gazetteer knownName;
  static Gazetteer knownNamesBig;
  static Gazetteer knownNationalities;
  static Gazetteer knownPlace;
  static Gazetteer knownState;
  static Gazetteer knownTitle;
  static Gazetteer measurements;
  static Gazetteer ordinalNumber;
  static Gazetteer temporalWords;
  static Gazetteer wikiArtWork;
  static Gazetteer wikiArtWorkRedirects;
  static Gazetteer wikiCompetitionsBattlesEvents;
  static Gazetteer wikiCompetitionsBattlesEventsRedirects;
  static Gazetteer wikiFilms;
  static Gazetteer wikiFilmsRedirects;
  static Gazetteer wikiLocations;
  static Gazetteer wikiLocationRedirects;
  static Gazetteer wikiManMadeObjectNames;
  static Gazetteer wikiManMadeObjectNamesRedirects;
  static Gazetteer wikiOrganizations;
  static Gazetteer wikiOrganizationsRedirects;
  static Gazetteer wikiPeople;
  static Gazetteer wikiPeopleRedirects;
  static Gazetteer wikiSongs;
  static Gazetteer wikiSongsRedirects;
  
  
  public StatisticalNameFinderTrainer(String trainData, String testData, String lang) throws IOException {
    
    this.lang = lang;
    this.trainData = trainData;
    this.testData = testData;
    ObjectStream<String> trainStream = InputOutputUtils.readInputData(trainData);
    trainSamples = new Conll03NameStream(lang,trainStream);
    ObjectStream<String> testStream = InputOutputUtils.readInputData(testData);
    testSamples = new Conll03NameStream(lang,testStream);
    /*InputStream dictFilePer = getClass().getResourceAsStream("/en-wikipeople.lst");
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
    dictKnownLoc = new Gazetteer(dictFileKnownLoc);*/
    //lbj dictionaries
    InputStream cardinalNumberFile = getClass().getResourceAsStream("/lbj/cardinalNumber.txt");
    cardinalNumber = new Gazetteer(cardinalNumberFile);
    InputStream currencyFinalFile = getClass().getResourceAsStream("/lbj/currencyFinal.txt");
    currencyFinal = new Gazetteer(currencyFinalFile);
    InputStream knownCorporationsFile = getClass().getResourceAsStream("/lbj/known_corporations.lst");
    knownCorporations = new Gazetteer(knownCorporationsFile);
    InputStream knownCountryFile = getClass().getResourceAsStream("/lbj/known_country.lst");
    knownCountry = new Gazetteer(knownCountryFile);
    InputStream knownJobsFile = getClass().getResourceAsStream("/lbj/known_jobs.lst");
    knownJobs = new Gazetteer(knownJobsFile);
    InputStream knownNameFile = getClass().getResourceAsStream("/lbj/known_name.lst");
    knownName = new Gazetteer(knownNameFile);
    InputStream knownNameBigFile = getClass().getResourceAsStream("/lbj/known_names.big.lst");
    knownNamesBig = new Gazetteer(knownNameBigFile);
    InputStream knownNationalitiesFile = getClass().getResourceAsStream("/lbj/known_nationalities.lst");
    knownNationalities = new Gazetteer(knownNationalitiesFile);
    InputStream knownPlaceFile = getClass().getResourceAsStream("/lbj/known_place.lst");
    knownPlace = new Gazetteer(knownPlaceFile);
    InputStream knownStateFile = getClass().getResourceAsStream("/lbj/known_state.lst");
    knownState = new Gazetteer(knownStateFile);
    InputStream knownTitleFile = getClass().getResourceAsStream("/lbj/known_title.lst");
    knownTitle = new Gazetteer(knownTitleFile);
    InputStream measurementsFile = getClass().getResourceAsStream("/lbj/measurments.txt");
    measurements = new Gazetteer(measurementsFile);
    InputStream ordinalNumberFile = getClass().getResourceAsStream("/lbj/ordinalNumber.txt");
    ordinalNumber = new Gazetteer(ordinalNumberFile);
    InputStream temporalWordsFile = getClass().getResourceAsStream("/lbj/temporal_words.txt");
    temporalWords = new Gazetteer(temporalWordsFile);
    InputStream wikiArtWorkFile = getClass().getResourceAsStream("/lbj/WikiArtWork.lst");
    wikiArtWork = new Gazetteer(wikiArtWorkFile);
    InputStream wikiArtWorkRedirectsFile = getClass().getResourceAsStream("/lbj/WikiArtWorkRedirects.lst");
    wikiArtWorkRedirects = new Gazetteer(wikiArtWorkRedirectsFile);
    InputStream wikiCompetitionsBattlesEventsFile = getClass().getResourceAsStream("/lbj/WikiCompetitionsBattlesEvents.lst");
    wikiCompetitionsBattlesEvents = new Gazetteer(wikiCompetitionsBattlesEventsFile);
    InputStream wikiCompetitionsBattlesEventsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiCompetitionsBattlesEventsRedirects.lst");
    wikiCompetitionsBattlesEventsRedirects = new Gazetteer(wikiCompetitionsBattlesEventsRedirectsFile);
    InputStream wikiFilmsFile = getClass().getResourceAsStream("/lbj/WikiFilms.lst");
    wikiFilms = new Gazetteer(wikiFilmsFile);
    InputStream wikiFilmsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiFilmsRedirects.lst");
    wikiFilmsRedirects = new Gazetteer(wikiFilmsRedirectsFile);
    InputStream wikiLocationsFile = getClass().getResourceAsStream("/lbj/WikiLocations.lst");
    wikiLocations = new Gazetteer(wikiLocationsFile);
    InputStream wikiLocationsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiLocationsRedirects.lst");
    wikiLocationRedirects = new Gazetteer(wikiLocationsRedirectsFile);
    InputStream wikiManMadeObjectNamesFile = getClass().getResourceAsStream("/lbj/WikiManMadeObjectNames.lst");
    wikiManMadeObjectNames = new Gazetteer(wikiManMadeObjectNamesFile);
    InputStream wikiManMadeObjectNamesRedirectsFile = getClass().getResourceAsStream("/lbj/WikiManMadeObjectNamesRedirects.lst");
    wikiManMadeObjectNamesRedirects = new Gazetteer(wikiManMadeObjectNamesRedirectsFile);
    InputStream wikiOrganizationsFile = getClass().getResourceAsStream("/lbj/WikiOrganizations.lst");
    wikiOrganizations = new Gazetteer(wikiOrganizationsFile);
    InputStream wikiOrganizationsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiOrganizationsRedirects.lst");
    wikiOrganizationsRedirects = new Gazetteer(wikiOrganizationsRedirectsFile);
    InputStream wikiPeopleFile = getClass().getResourceAsStream("/lbj/WikiPeople.lst");
    wikiPeople = new Gazetteer(wikiPeopleFile);
    InputStream wikiPeopleRedirectsFile = getClass().getResourceAsStream("/lbj/WikiPeopleRedirects.lst");
    wikiPeopleRedirects = new Gazetteer(wikiPeopleRedirectsFile);
    InputStream wikiSongsFile = getClass().getResourceAsStream("/lbj/WikiSongs.lst");
    wikiSongs = new Gazetteer(wikiSongsFile);
    InputStream wikiSongsRedirectsFile = getClass().getResourceAsStream("/lbj/WikiSongsRedirects.lst");
    wikiSongsRedirects = new Gazetteer(wikiSongsRedirectsFile);
    
  }
  
public StatisticalNameFinderTrainer() throws IOException {
    
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
    //lbj dictionaries
    
  }

  public static AdaptiveFeatureGenerator createDefaultFeatures1() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false) });
  }
  
  public AdaptiveFeatureGenerator createDefaultFeatures2() {
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
  
  public static AdaptiveFeatureGenerator createDefaultFeatures() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false),
        new DictionaryFeatures(cardinalNumber),
        new DictionaryFeatures(currencyFinal),
        new DictionaryFeatures(knownCorporations),
        new DictionaryFeatures(knownCountry),
        new DictionaryFeatures(knownJobs),
        new DictionaryFeatures(knownName),
        new DictionaryFeatures(knownNamesBig),
        new DictionaryFeatures(knownNationalities),
        new DictionaryFeatures(knownPlace),
        new DictionaryFeatures(knownState),
        new DictionaryFeatures(knownTitle),
        new DictionaryFeatures(measurements),
        new DictionaryFeatures(ordinalNumber),
        new DictionaryFeatures(temporalWords),
        new DictionaryFeatures(wikiArtWork),
        new DictionaryFeatures(wikiArtWorkRedirects),
        new DictionaryFeatures(wikiCompetitionsBattlesEvents),
        new DictionaryFeatures(wikiCompetitionsBattlesEventsRedirects),
        new DictionaryFeatures(wikiFilms),
        new DictionaryFeatures(wikiFilmsRedirects),
        new DictionaryFeatures(wikiLocations),
        new DictionaryFeatures(wikiLocationRedirects),
        new DictionaryFeatures(wikiManMadeObjectNames),
        new DictionaryFeatures(wikiManMadeObjectNamesRedirects),
        new DictionaryFeatures(wikiOrganizations),
        new DictionaryFeatures(wikiOrganizationsRedirects),
        new DictionaryFeatures(wikiPeople),
        new DictionaryFeatures(wikiPeopleRedirects),
        new DictionaryFeatures(wikiSongs),
        new DictionaryFeatures(wikiSongsRedirects)
        });
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
