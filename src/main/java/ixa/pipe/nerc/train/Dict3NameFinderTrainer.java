
package ixa.pipe.nerc.train;

import ixa.pipe.nerc.Dictionary;

import java.io.IOException;
import java.io.InputStream;

import opennlp.tools.util.featuregen.AdaptiveFeatureGenerator;
import opennlp.tools.util.featuregen.BigramNameFeatureGenerator;
import opennlp.tools.util.featuregen.CachedFeatureGenerator;
import opennlp.tools.util.featuregen.OutcomePriorFeatureGenerator;
import opennlp.tools.util.featuregen.PreviousMapFeatureGenerator;
import opennlp.tools.util.featuregen.SentenceFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

/**
 * Training NER based on Apache OpenNLP Machine Learning API 
 * This class implements Gazetteer features for Organization, Person and Location 
 * classes.   
 * 
 * @author ragerri 2014/03/19
 * 
 */

public class Dict3NameFinderTrainer extends AbstractNameFinderTrainer {

  static Dictionary dictPer;
  static Dictionary dictOrg;
  static Dictionary dictLoc;
  static Dictionary dictKnownPer;
  static Dictionary dictKnownOrg;
  static Dictionary dictKnownLoc;
  
  
  public Dict3NameFinderTrainer(String trainData, String testData, String lang) throws IOException {
    super(trainData,testData,lang);
    features = createFeatureGenerator();
    
    InputStream dictFilePer = getClass().getResourceAsStream("/en/wikiperson.txt");
    dictPer = new Dictionary(dictFilePer);
    InputStream dictFileOrg = getClass().getResourceAsStream("/en/wikiorganization.txt");
    dictOrg = new Dictionary(dictFileOrg);
    InputStream dictFileLoc = getClass().getResourceAsStream("/en/wikilocation.txt");
    dictLoc = new Dictionary(dictFileLoc);
    InputStream dictFileKnownPer = getClass().getResourceAsStream("/en/known-person.txt");
    dictKnownPer = new Dictionary(dictFileKnownPer);
    InputStream dictFileKnownOrg = getClass().getResourceAsStream("/en/known-organization.txt");
    dictKnownOrg = new Dictionary(dictFileKnownOrg);
    InputStream dictFileKnownLoc = getClass().getResourceAsStream("/en/known-location.txt");
    dictKnownLoc = new Dictionary(dictFileKnownLoc);
  }
  
  public Dict3NameFinderTrainer() {
    super();
    features = createFeatureGenerator();
  }
  
  public AdaptiveFeatureGenerator createFeatureGenerator() {
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
}
