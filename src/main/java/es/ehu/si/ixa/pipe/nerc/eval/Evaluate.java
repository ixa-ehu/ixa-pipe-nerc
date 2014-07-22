package es.ehu.si.ixa.pipe.nerc.eval;

import es.ehu.si.ixa.pipe.nerc.CLI;
import es.ehu.si.ixa.pipe.nerc.StatisticalNameFinder;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSampleTypeFilter;
import es.ehu.si.ixa.pipe.nerc.train.AbstractNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.DictNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.NameClassifier;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.NameModel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

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
  private static NameModel nercModel;
  /**
   * The name finder trainer to use for appropriate features.
   */
  private NameFinderTrainer nameFinderTrainer;
  /**
   * An instance of the probabilistic {@link NameFinderME}.
   */
  private NameClassifier nameFinder;
 
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
        nercModel = new NameModel(trainedModelInputStream);
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
      nameFinderTrainer = new DictNameFinderTrainer(dictionaries, beamsize);
    }
    else {
      StatisticalNameFinder statFinder = new StatisticalNameFinder(properties, beamsize);
      nameFinderTrainer = statFinder.getNameFinderTrainer(lang, features, beamsize);
    }
    nameFinder = new NameClassifier(nercModel, nameFinderTrainer.createFeatureGenerator(), beamsize);
  }

  /**
   * Evaluate and print precision, recall and F measure.
   * @throws IOException if test corpus not loaded
   */
  public final void evaluate() throws IOException {
    NameFinderEvaluator evaluator = new NameFinderEvaluator(nameFinder);
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
    NameFinderDetailedFMeasureListener detailedFListener = new NameFinderDetailedFMeasureListener();
    listeners.add(detailedFListener);
    NameFinderEvaluator evaluator = new NameFinderEvaluator(nameFinder,
        listeners.toArray(new NameFinderEvaluationMonitor[listeners.size()]));
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
    NameFinderEvaluator evaluator = new NameFinderEvaluator(nameFinder,
        listeners.toArray(new NameFinderEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }

}
