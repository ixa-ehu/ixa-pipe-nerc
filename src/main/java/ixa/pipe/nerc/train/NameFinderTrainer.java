package ixa.pipe.nerc.train;

import opennlp.tools.namefind.NameSample;
import opennlp.tools.namefind.TokenNameFinderEvaluator;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.TrainingParameters;
import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;

public interface NameFinderTrainer {
  
  public AdaptiveFeatureGenerator createFeatureGenerator();
  
  public TokenNameFinderModel train(TrainingParameters params);
  
  public TokenNameFinderModel trainCrossEval(String trainData,
      String devData, TrainingParameters params, String[] evalRange);

  public TokenNameFinderEvaluator evaluate(TokenNameFinderModel trainedModel,
      ObjectStream<NameSample> testSamples); 


}
