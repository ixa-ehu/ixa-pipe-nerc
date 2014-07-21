package es.ehu.si.ixa.pipe.nerc.eval;

import es.ehu.si.ixa.pipe.nerc.CLI;
import es.ehu.si.ixa.pipe.nerc.StatisticalNameFinder;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.train.AbstractNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.CorpusSample;
import es.ehu.si.ixa.pipe.nerc.train.CorpusSampleTypeFilter;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.cmdline.namefind.NameEvaluationErrorListener;
import opennlp.tools.cmdline.namefind.TokenNameFinderDetailedFMeasureListener;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderEvaluationMonitor;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.EvaluationMonitor;

/**
 * Evaluation class mostly using {@link TokenNameFinderEvaluator}.
 *
 * @author ragerri
 * @version 2013-04-04
 */
public class Evaluate {

  /**
   * The reference corpus to evaluate against.
   */
  private ObjectStream<CorpusSample> testSamples;
  /**
   * Static instance of {@link TokenNameFinderModel}.
   */
  private static TokenNameFinderModel nercModel;
  /**
   * The name finder trainer to use for appropriate features.
   */
  private NameFinderTrainer nameFinderTrainer;
  /**
   * An instance of the probabilistic {@link NameFinderME}.
   */
  private NameFinderME nameFinder;
 
  /**
   * Construct an evaluator.
   *
   * @param testData the reference data to evaluate against
   * @param model the model to be evaluated
   * @param features the features
   * @param lang the language
   * @param beamsize the beam size for decoding
   * @param corpusFormat the format of the testData corpus
   * @throws IOException if input data not available
   */
  public Evaluate(final Properties properties, final Dictionaries dictionaries) throws IOException {
    
    String testSet = properties.getProperty("testSet");
    String lang = properties.getProperty("lang");
    String corpusFormat = properties.getProperty("corpusFormat");
    String neTypes = properties.getProperty("neTypes");
    String model = properties.getProperty("model");
    String features = properties.getProperty("features");
    Integer beamsize = Integer.parseInt(properties.getProperty("beamsize"));
    
    testSamples = AbstractNameFinderTrainer.getNameStream(testSet, lang, corpusFormat);
    if (!neTypes.equals(CLI.DEFAULT_NE_TYPES)) {
      String[] neTypesArray = neTypes.split(",");
      testSamples = new CorpusSampleTypeFilter(neTypesArray, testSamples);
    }
    InputStream trainedModelInputStream = null;
    try {
      if (nercModel == null) {
        trainedModelInputStream = new FileInputStream(model);
        nercModel = new TokenNameFinderModel(trainedModelInputStream);
      }
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
          System.err.println("Could not load model!");
        }
      }
    }
   
    if (features.equalsIgnoreCase("dict")) {
      nameFinderTrainer = chooseDictTrainer(lang, dictionaries, beamsize);
    }
    else {
      StatisticalNameFinder statFinder = new StatisticalNameFinder(properties, beamsize);
      nameFinderTrainer = statFinder.getNameFinderTrainer(lang, features, beamsize);
    }
    nameFinder = new NameFinderME(nercModel, nameFinderTrainer.createFeatureGenerator(), beamsize);
  }

  /**
   * Evaluate and print precision, recall and F measure.
   * @throws IOException if test corpus not loaded
   */
  public final void evaluate() throws IOException {
    TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder);
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }
  /**
   * Evaluate and print the precision, recall and F measure per
   * named entity class.
   *
   * @throws IOException if test corpus not loaded
   */
  public final void detailEvaluate() throws IOException {
    List<EvaluationMonitor<CorpusSample>> listeners = new LinkedList<EvaluationMonitor<CorpusSample>>();
    TokenNameFinderDetailedFMeasureListener detailedFListener = new TokenNameFinderDetailedFMeasureListener();
    listeners.add(detailedFListener);
    TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder,
        listeners.toArray(new TokenNameFinderEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(detailedFListener.toString());
  }
  /**
   * Evaluate and print every error.
   * @throws IOException if test corpus not loaded
   */
  public final void evalError() throws IOException {
    List<EvaluationMonitor<CorpusSample>> listeners = new LinkedList<EvaluationMonitor<CorpusSample>>();
    listeners.add(new NameEvaluationErrorListener());
    TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder,
        listeners.toArray(new TokenNameFinderEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }
  
  /**
   * Choose the NameFinder training according to feature type and language.
   * @return the name finder trainer
   * @throws IOException throws
   */
  public static NameFinderTrainer chooseDictTrainer(final String lang, Dictionaries dictionaries, int beamsize) throws IOException {
    NameFinderTrainer nercTrainer = null;
        if (lang.equalsIgnoreCase("de")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.de.DictNameFinderTrainer(dictionaries, beamsize);
        }
        else if (lang.equalsIgnoreCase("en")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.en.DictNameFinderTrainer(dictionaries, beamsize);
        }
        else if (lang.equalsIgnoreCase("es")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.es.DictNameFinderTrainer(dictionaries, beamsize);
        }
        else if (lang.equalsIgnoreCase("it")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.it.DictNameFinderTrainer(dictionaries, beamsize);
        }
        else if (lang.equalsIgnoreCase("nl")) {
          nercTrainer = new es.ehu.si.ixa.pipe.nerc.train.lang.nl.DictNameFinderTrainer(dictionaries, beamsize);
        }
        else {
        System.err
            .println("You need to provide the directory containing the dictionaries!\n");
        System.exit(1);
      }
    return nercTrainer;
  }


}
