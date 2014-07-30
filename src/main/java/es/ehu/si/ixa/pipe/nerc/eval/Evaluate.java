package es.ehu.si.ixa.pipe.nerc.eval;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.eval.EvaluationMonitor;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSample;
import es.ehu.si.ixa.pipe.nerc.formats.CorpusSampleTypeFilter;
import es.ehu.si.ixa.pipe.nerc.train.AbstractTrainer;
import es.ehu.si.ixa.pipe.nerc.train.FixedTrainer;
import es.ehu.si.ixa.pipe.nerc.train.NameClassifier;
import es.ehu.si.ixa.pipe.nerc.train.Trainer;
import es.ehu.si.ixa.pipe.nerc.train.NameModel;

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
  private Trainer nameFinderTrainer;
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
  public Evaluate(final Properties properties, final TrainingParameters params) throws IOException {
    
    String testSet = properties.getProperty("testSet");
    String model = properties.getProperty("model");
    String lang = params.getSettings().get("Language");
    String corpusFormat = params.getSettings().get("Corpus");
    String neTypes = params.getSettings().get("Types");
    Integer beamsize = Integer.parseInt(params.getSettings().get("Beamsize"));
    
    testSamples = AbstractTrainer.getNameStream(testSet, lang, corpusFormat);
    if (neTypes.length() != 0) {
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
    nameFinderTrainer = new FixedTrainer(params);
    nameFinder = new NameClassifier(nercModel, nameFinderTrainer.createFeatureGenerator(params), beamsize);
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
