package ixa.pipe.nerc.train;

import java.io.IOException;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.SuffixFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API.  
 * @author ragerri 2014/03/19
 * 
 */

public class BaselineNameFinderTrainer extends AbstractNameFinderTrainer {
 
  
  public BaselineNameFinderTrainer(String trainData, String testData, String lang, int beamsize, String corpusFormat) throws IOException {
    super(trainData,testData,lang,beamsize,corpusFormat);
    features = createFeatureGenerator();
  }
  
  public BaselineNameFinderTrainer(int beamsize) {
    super(beamsize);
    features = createFeatureGenerator();
  }
       
  public AdaptiveFeatureGenerator createFeatureGenerator() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false),
        new Prefix34FeatureGenerator(),
        new SuffixFeatureGenerator()
        });
  }

}
