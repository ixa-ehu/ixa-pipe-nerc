package ixa.pipe.nerc.eval;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import ixa.pipe.nerc.StatisticalNameFinder;
import ixa.pipe.nerc.train.Conll03NameStream;
import ixa.pipe.nerc.train.InputOutputUtils;
import ixa.pipe.nerc.train.NameFinderTrainer;
import opennlp.tools.cmdline.namefind.NameEvaluationErrorListener;
import opennlp.tools.cmdline.namefind.TokenNameFinderDetailedFMeasureListener;
import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderEvaluationMonitor;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.eval.EvaluationMonitor;

public class Evaluate {
  
  private ObjectStream<NameSample> testSamples;
  private InputStream trainedModel;
  private TokenNameFinderModel nercModel;
  private NameFinderTrainer nameFinderTrainer;
  private NameFinderME nameFinder;
  
  
  public Evaluate(String testData, String model, String lang, int beamsize) throws IOException {
    
    ObjectStream<String> testStream = InputOutputUtils.readInputData(testData);
    testSamples = new Conll03NameStream(lang,testStream);
    StatisticalNameFinder statFinder = new StatisticalNameFinder(lang,model);
    trainedModel = statFinder.getModel(lang, model);
    
    try {
      nercModel = new TokenNameFinderModel(trainedModel);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }
    nameFinderTrainer = statFinder.getNameFinderTrainer(model,beamsize);
    nameFinder = new NameFinderME(nercModel,nameFinderTrainer.createFeatureGenerator(),beamsize);
  }
  
  public void evaluate() throws IOException {
    TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder);
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }
  
  public void detailEvaluate() throws IOException {
    List<EvaluationMonitor<NameSample>> listeners = new LinkedList<EvaluationMonitor<NameSample>>();
    TokenNameFinderDetailedFMeasureListener detailedFListener = new TokenNameFinderDetailedFMeasureListener();
    listeners.add(detailedFListener);
    TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder,listeners.toArray(new TokenNameFinderEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(detailedFListener.toString());
    
  }
  public void evalError() throws IOException {
    List<EvaluationMonitor<NameSample>> listeners = new LinkedList<EvaluationMonitor<NameSample>>();
    listeners.add(new NameEvaluationErrorListener());
    TokenNameFinderEvaluator evaluator = new TokenNameFinderEvaluator(nameFinder,listeners.toArray(new TokenNameFinderEvaluationMonitor[listeners.size()]));
    evaluator.evaluate(testSamples);
    System.out.println(evaluator.getFMeasure());
  }
  
  

}
