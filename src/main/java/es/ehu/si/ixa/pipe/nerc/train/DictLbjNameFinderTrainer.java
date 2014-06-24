
package es.ehu.si.ixa.pipe.nerc.train;


import es.ehu.si.ixa.pipe.nerc.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.Dictionary;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatures;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;

import java.io.IOException;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.SuffixFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API 
 * This class implements Gazetteer features as explained in 
 * Ratinov, Lev and Dan Roth. Design Challenges and Misconceptions in 
 * Named Entity Recognition. In CoNLL 2009  
 * 
 * @author ragerri 2014/03/19
 * 
 */

public class DictLbjNameFinderTrainer extends AbstractNameFinderTrainer {
  
  private Dictionaries dictionaries;
  private Dictionary dictionary;
  private String prefix;
  
  public DictLbjNameFinderTrainer(String dictPath, String trainData, String testData, String lang, int beamsize, String corpusFormat, String netypes) throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);
    
    dictionaries = new Dictionaries(dictPath);
    for (int i = 0; i < dictionaries.getDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getDictionaries().get(i);
      features = createFeatureGenerator();
    }
    
  }
  
  public DictLbjNameFinderTrainer(String dictPath, int beamsize) {
    super(beamsize);
    
    dictionaries = new Dictionaries(dictPath);
    for (int i = 0; i < dictionaries.getDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getDictionaries().get(i);
      features = createFeatureGenerator();
    }
    
  }
  
  public AdaptiveFeatureGenerator createFeatureGenerator() {
    return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(),
        new SentenceFeatureGenerator(true, false),
        new Prefix34FeatureGenerator(),
        new SuffixFeatureGenerator(),
        new DictionaryFeatures(prefix, dictionary)
        });
    }
   
}
