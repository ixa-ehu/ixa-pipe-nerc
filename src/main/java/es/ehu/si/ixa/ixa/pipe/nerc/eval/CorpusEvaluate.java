package es.ehu.si.ixa.ixa.pipe.nerc.eval;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.FMeasure;
import es.ehu.si.ixa.ixa.pipe.nerc.train.AbstractTrainer;

/**
 * Evaluation class mostly using {@link TokenNameFinderEvaluator}.
 *
 * @author ragerri
 * @version 2014-04-04
 */
public class CorpusEvaluate {

  /**
   * The reference corpus to evaluate against.
   */
  private ObjectStream<NameSample> referenceSamples;
  /**
   * The reference corpus to evaluate against.
   */
  private ObjectStream<NameSample> predictionSamples;
  /**
   * The FMeasure implementation.
   */
  private FMeasure fmeasure = new FMeasure();
  
  /**
   * Construct an evaluator to compare a prediction data file wrt to a testset.
   * The language, testset and corpusFormat of both the testset and prediction
   * are read from the props file.
   * @param predictionData the prediction data to be evaluated
   * @param props the properties file
   * @throws IOException exception
   */
  public CorpusEvaluate(final String predictionData, final Properties props) throws IOException {

    String lang = props.getProperty("language");
    String testSet = props.getProperty("testset");
    String corpusFormat = props.getProperty("corpusFormat");
    referenceSamples = AbstractTrainer.getNameStream(testSet, lang, corpusFormat);
    predictionSamples = AbstractTrainer.getNameStream(predictionData, lang, corpusFormat);
  }
  
  /**
   * Reads NameSamples to a list.
   * @param samples the name samples
   * @return the list
   * @throws IOException the exception
   */
  public List<NameSample> readSamplesToList(ObjectStream<NameSample> samples) throws IOException {
    NameSample sample;
    List<NameSample> nameSampleList = new ArrayList<NameSample>();
    while ((sample = samples.read()) != null) {
      nameSampleList.add(sample);
    }
    return nameSampleList;
  }

  /**
   * Evaluate and print precision, recall and F measure.
   * @throws IOException if test corpus not loaded
   */
  public final void evaluate() throws IOException {
    List<NameSample> refList = readSamplesToList(referenceSamples);
    List<NameSample> predList = readSamplesToList(predictionSamples);
    for (int i = 0; i < refList.size(); ++i) {
      fmeasure.updateScores(refList.get(i).getNames(), predList.get(i).getNames());
    }
    System.out.println(fmeasure.toString());
  }
  

}
