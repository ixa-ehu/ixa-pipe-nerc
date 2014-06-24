package es.ehu.si.ixa.pipe.nerc.train;

import es.ehu.si.ixa.pipe.nerc.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.Dictionary;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatures;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
 * Training NER based on Apache OpenNLP Machine Learning API. This class
 * implements Gazetteer features.
 *
 * @author ragerri 2014/06/24
 *
 */

public class DictNameFinderTrainer extends AbstractNameFinderTrainer {

  /**
   * The {@link Dictionaries} contained in the given directory.
   */
  private Dictionaries dictionaries;
  /**
   * A {@link Dictionary} object.
   */
  private Dictionary dictionary;
  /**
   * The prefix to be used in the {@link DictionaryFeatures}.
   */
  private String prefix;

  /**
   * Construct a DictionaryNameFinderTrainer.
   *
   * @param dictPath the path to the dictionaries to be used as features
   * @param trainData the training data
   * @param testData the test data for evaluation after training
   * @param lang the language
   * @param beamsize the beamsize for decoding it defaults to 3
   * @param corpusFormat the format is either opennlp or conll format
   * @param netypes filter by named entity classes to train specialized models
   * @throws IOException throws an exception
   */
  public DictNameFinderTrainer(final String dictPath, final String trainData,
      final String testData, final String lang, final int beamsize, final String corpusFormat,
      final String netypes) throws IOException {
    super(trainData, testData, lang, beamsize, corpusFormat, netypes);

    dictionaries = new Dictionaries(dictPath);
    features = createFeatureGenerator();

  }

  /**
   * Construct a DictionaryNameFinderTrainer for evaluation only.
   *
   * @param dictPath the path to the dictionaries
   * @param beamsize the beamsize for decoding; it defaults to 3
   */
  public DictNameFinderTrainer(final String dictPath, final int beamsize) {
    super(beamsize);

    dictionaries = new Dictionaries(dictPath);
    features = createFeatureGenerator();

  }

  /*
   * Creates a feature list to which the dictionary features are added.
   *  (non-Javadoc)
   * @see es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer#createFeatureGenerator()
   */
  public final AdaptiveFeatureGenerator createFeatureGenerator() {
    List<AdaptiveFeatureGenerator> featureList = createFeaturesList();
    addDictionariesToFeatureList(featureList);
    AdaptiveFeatureGenerator[] featuresArray = featureList
        .toArray(new AdaptiveFeatureGenerator[featureList.size()]);
    return new CachedFeatureGenerator(featuresArray);
  }

  /**
   * Create a list of {@link AdaptiveFeatureGenerator} features.
   *
   * @return the list of features
   */
  private List<AdaptiveFeatureGenerator> createFeaturesList() {

    List<AdaptiveFeatureGenerator> featuresList = new ArrayList<AdaptiveFeatureGenerator>(Arrays.asList(
        new WindowFeatureGenerator(new TokenFeatureGenerator(), 2, 2),
        new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), 2, 2),
        new OutcomePriorFeatureGenerator(), new PreviousMapFeatureGenerator(),
        new BigramNameFeatureGenerator(), new SentenceFeatureGenerator(true,
            false), new Prefix34FeatureGenerator(),
        new SuffixFeatureGenerator()));
    return featuresList;

  }

  /**
   * Adds the dictionary features to the feature list.
   *
   * @param featureList the feature list containing the dictionary features
   */
  private void addDictionariesToFeatureList(
      List<AdaptiveFeatureGenerator> featureList) {
    for (int i = 0; i < dictionaries.getDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getDictionaries().get(i);
      featureList.add(new DictionaryFeatures(prefix, dictionary));
    }
  }

}
