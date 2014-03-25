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
import opennlp.tools.util.featuregen.SuffixFeatureGenerator;
import opennlp.tools.util.featuregen.TokenClassFeatureGenerator;
import opennlp.tools.util.featuregen.TokenFeatureGenerator;
import opennlp.tools.util.featuregen.WindowFeatureGenerator;

public class DictCoNLLNameFinderTrainer extends AbstractNameFinderTrainer {
  
  static Dictionary dictPerC;
  static Dictionary dictPerP;
  static Dictionary dictOrgC;
  static Dictionary dictOrgP;
  static Dictionary dictLocC;
  static Dictionary dictLocP;
  static Dictionary dictMiscC;
  static Dictionary dictMiscP;
  
  
  public DictCoNLLNameFinderTrainer(String trainData, String testData, String lang, int beamsize,  String corpusFormat) throws IOException {
    super(trainData,testData,lang,beamsize,corpusFormat);
    InputStream dictFileLoc = getClass().getResourceAsStream("/es/locationC.txt");
    dictLocC = new Dictionary(dictFileLoc);
    InputStream dictFileMisc = getClass().getResourceAsStream("/es/miscC.txt");
    dictMiscC = new Dictionary(dictFileMisc);
    InputStream dictFileOrg = getClass().getResourceAsStream("/es/organizationC.txt");
    dictOrgC = new Dictionary(dictFileOrg);
    InputStream dictFilePer = getClass().getResourceAsStream("/es/personC.txt");
    dictPerP = new Dictionary(dictFilePer);
    InputStream dictFileKnownLoc = getClass().getResourceAsStream("/es/locationP.txt");
    dictLocP = new Dictionary(dictFileKnownLoc);
    InputStream dictFileKnownMisc = getClass().getResourceAsStream("/es/miscP.txt");
    dictMiscP = new Dictionary(dictFileKnownMisc);
    InputStream dictFileOrgP = getClass().getResourceAsStream("/es/organizationP.txt");
    dictOrgP = new Dictionary(dictFileOrgP);
    InputStream dictFilePersonP = getClass().getResourceAsStream("/es/personP.txt");
    dictPerP = new Dictionary(dictFilePersonP);
    
    features = createFeatureGenerator();
  }
  
  public DictCoNLLNameFinderTrainer(int beamsize) {
    super(beamsize);
    InputStream dictFileLoc = getClass().getResourceAsStream("/es/locationC.txt");
    dictLocC = new Dictionary(dictFileLoc);
    InputStream dictFileMisc = getClass().getResourceAsStream("/es/miscC.txt");
    dictMiscC = new Dictionary(dictFileMisc);
    InputStream dictFileOrg = getClass().getResourceAsStream("/es/organizationC.txt");
    dictOrgC = new Dictionary(dictFileOrg);
    InputStream dictFilePer = getClass().getResourceAsStream("/es/personC.txt");
    dictPerP = new Dictionary(dictFilePer);
    InputStream dictFileKnownLoc = getClass().getResourceAsStream("/es/locationP.txt");
    dictLocP = new Dictionary(dictFileKnownLoc);
    InputStream dictFileKnownMisc = getClass().getResourceAsStream("/es/miscP.txt");
    dictMiscP = new Dictionary(dictFileKnownMisc);
    InputStream dictFileOrgP = getClass().getResourceAsStream("/es/organizationP.txt");
    dictOrgP = new Dictionary(dictFileOrgP);
    InputStream dictFilePersonP = getClass().getResourceAsStream("/es/personP.txt");
    dictPerP = new Dictionary(dictFilePersonP);
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
            new DictionaryFeatures("LOCC",dictLocC),
            new DictionaryFeatures("MISCC",dictMiscC),
            new DictionaryFeatures("ORGC",dictOrgC),
            new DictionaryFeatures("PERC",dictPerC),
            new DictionaryFeatures("LOCP",dictLocP),
            new DictionaryFeatures("MISCP",dictMiscP),
            new DictionaryFeatures("ORGP",dictOrgP),
            new DictionaryFeatures("PERP",dictPerP),
            new SentenceFeatureGenerator(true, false) });
  }

	
}
