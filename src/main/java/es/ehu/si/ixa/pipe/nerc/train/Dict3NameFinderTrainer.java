
package es.ehu.si.ixa.pipe.nerc.train;

import es.ehu.si.ixa.pipe.nerc.Dictionary;

import java.io.IOException;
import java.io.InputStream;

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
 * Training NER based on Apache OpenNLP Machine Learning API 
 * This class implements Gazetteer features for Organization, Person and Location 
 * classes.   
 * 
 * @author ragerri 2014/03/19
 * 
 */

public class Dict3NameFinderTrainer extends AbstractNameFinderTrainer {

  Dictionary dictPer;
  Dictionary dictOrg;
  Dictionary dictLoc;
  Dictionary dictKnownPer;
  Dictionary dictKnownOrg;
  Dictionary dictKnownLoc;
  
  
  public Dict3NameFinderTrainer(String trainData, String testData, String lang, int beamsize,  String corpusFormat) throws IOException {
    super(trainData,testData,lang,beamsize,corpusFormat);
    InputStream dictFilePer = getClass().getResourceAsStream("/en/en-wiki-person.txt");
    dictPer = new Dictionary(dictFilePer);
    InputStream dictFileOrg = getClass().getResourceAsStream("/en/en-wiki-organization.txt");
    dictOrg = new Dictionary(dictFileOrg);
    InputStream dictFileLoc = getClass().getResourceAsStream("/en/en-wiki-location.txt");
    dictLoc = new Dictionary(dictFileLoc);
    InputStream dictFileKnownPer = getClass().getResourceAsStream("/en/en-known-person.txt");
    dictKnownPer = new Dictionary(dictFileKnownPer);
    InputStream dictFileKnownOrg = getClass().getResourceAsStream("/en/en-known-organization.txt");
    dictKnownOrg = new Dictionary(dictFileKnownOrg);
    InputStream dictFileKnownLoc = getClass().getResourceAsStream("/en/en-known-location.txt");
    dictKnownLoc = new Dictionary(dictFileKnownLoc);
    
    features = createFeatureGenerator();
  }
  
  public Dict3NameFinderTrainer(int beamsize) {
    super(beamsize);
    InputStream dictFilePer = getClass().getResourceAsStream("/en/en-wiki-person.txt");
    dictPer = new Dictionary(dictFilePer);
    InputStream dictFileOrg = getClass().getResourceAsStream("/en/en-wiki-organization.txt");
    dictOrg = new Dictionary(dictFileOrg);
    InputStream dictFileLoc = getClass().getResourceAsStream("/en/en-wiki-location.txt");
    dictLoc = new Dictionary(dictFileLoc);
    InputStream dictFileKnownPer = getClass().getResourceAsStream("/en/en-known-person.txt");
    dictKnownPer = new Dictionary(dictFileKnownPer);
    InputStream dictFileKnownOrg = getClass().getResourceAsStream("/en/en-known-organization.txt");
    dictKnownOrg = new Dictionary(dictFileKnownOrg);
    InputStream dictFileKnownLoc = getClass().getResourceAsStream("/en/en-known-location.txt");
    dictKnownLoc = new Dictionary(dictFileKnownLoc);
    
    features = createFeatureGenerator();
  }
  
  public AdaptiveFeatureGenerator createFeatureGenerator() {
        return new CachedFeatureGenerator(new AdaptiveFeatureGenerator[] {
            new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
            new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
            new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
            new BigramNameFeatureGenerator(),
            new Prefix34FeatureGenerator(),
            new SuffixFeatureGenerator(),
            new DictionaryFeatures("PERSON",dictPer), 
            new DictionaryFeatures("ORGANIZATION",dictOrg),
            new DictionaryFeatures("LOCATION",dictLoc),
            new DictionaryFeatures("KPERSON",dictKnownPer),
            new DictionaryFeatures("KORGANIZATION",dictKnownOrg),
            new DictionaryFeatures("KLOCATION",dictKnownLoc),
            new SentenceFeatureGenerator(true, false) });
  }
}
