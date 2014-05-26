package es.ehu.si.ixa.pipe.nerc.train;

import java.io.IOException;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API.  
 * @author ragerri 2014/03/19
 * 
 */

public class OpenNLPDefaultTrainer extends AbstractNameFinderTrainer {
 
  
  public OpenNLPDefaultTrainer(String trainData, String testData, String lang, int beamsize, String corpusFormat, String netypes) throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);
    features = createFeatureGenerator();
  }
  
  public OpenNLPDefaultTrainer(int beamsize) {
    super(beamsize);
    features = createFeatureGenerator();
  }
       
  public AdaptiveFeatureGenerator createFeatureGenerator() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false)
        });
  }
 
}
