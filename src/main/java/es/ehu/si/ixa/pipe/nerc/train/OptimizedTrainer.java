package es.ehu.si.ixa.pipe.nerc.train;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import opennlp.tools.util.TrainingParameters;

import com.google.common.collect.Sets;

import es.ehu.si.ixa.pipe.nerc.IntPair;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;
import es.ehu.si.ixa.pipe.nerc.dict.Dictionary;
import es.ehu.si.ixa.pipe.nerc.features.AdaptiveFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.BigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CachedFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.CharacterNgramFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.DictionaryFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.OutcomePriorFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.Prefix34FeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.PreviousMapFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SentenceFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.SuffixFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TokenFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.TrigramClassFeatureGenerator;
import es.ehu.si.ixa.pipe.nerc.features.WindowFeatureGenerator;

public class OptimizedTrainer {
  
  /**
   * The {@link Dictionaries} contained in the given directory.
   */
  private static Dictionaries dictionaries;
  /**
   * A {@link Dictionary} object.
   */
  private static Dictionary dictionary;
  /**
   * The prefix to be used in the {@link DictionaryFeatureGenerator}.
   */
  private static String prefix;

  
  public OptimizedTrainer(TrainingParameters params, Dictionaries aDictionaries) {
    if (dictionaries == null) {
      dictionaries = aDictionaries;
    }
  }
  
  /**
   * Generates a CachedFeatureGenerator (e.g. featureset) from a combination of every
   * possible features according to the variable parameters (e.g. window size, and so on).
   * The result is a list of featuresets ready to be fed to the trainer which, upon evaluation,
   * will decide which featureset is the best one for a given testset.
   * @param params the training parameters
   * @return the list of featuresets to be used in training
   */
  public List<AdaptiveFeatureGenerator> getAllPossibleFeatures(TrainingParameters params) {
    
    List<Set<AdaptiveFeatureGenerator>> featureLists = createFeatureLists(params);
    Set<List<AdaptiveFeatureGenerator>> allFeatures = Sets.cartesianProduct(featureLists);
    List<AdaptiveFeatureGenerator> allCachedFeatures = new ArrayList<AdaptiveFeatureGenerator>();
    for (List<AdaptiveFeatureGenerator> featGen : allFeatures) {
      AdaptiveFeatureGenerator[] featGenArray = featGen.toArray(new AdaptiveFeatureGenerator[featGen.size()]);
      allCachedFeatures.add(new CachedFeatureGenerator(featGenArray));
    }
    return allCachedFeatures;
  }
  
  
  /**
   * Create a list of list {@link AdaptiveFeatureGenerator} features.
   *
   * @return the lists of feature sets
   */
  public List<Set<AdaptiveFeatureGenerator>> createFeatureLists(TrainingParameters params) {
    
    List<Set<AdaptiveFeatureGenerator>> featureLists = new ArrayList<Set<AdaptiveFeatureGenerator>>();
    int leftWindow = getWindowRange(params).get(0);
    int rightWindow = getWindowRange(params).get(1);
    int minLength = getNgramRange(params).get(0);
    int maxLength = getNgramRange(params).get(1);
    
    addWindowTokenFeatureList(leftWindow, rightWindow, featureLists);
    addWindowTokenClassFeatureList(leftWindow, rightWindow, featureLists);
    addOutcomePriorFeatureList(featureLists);
    addPreviousMapFeatureList(featureLists);
    addSentenceFeatureList(featureLists);
    addPrefixFeatures(featureLists);
    addSuffixFeatures(featureLists);
    addBigramClassFeatureList(featureLists);
    addTrigramClassFeatureList(featureLists);
    addCharNgramFeatures(minLength, maxLength, featureLists);
    addDictionaryFeatures(featureLists);
    
    return featureLists;
  }
  
  public static void addWindowTokenFeatureList(int leftWindow, int rightWindow, List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureSet = new HashSet<AdaptiveFeatureGenerator>();
    List<IntPair> windowParams = getListFromRange(leftWindow, rightWindow);
    for (IntPair elem: windowParams) {
      featureSet.add(new WindowFeatureGenerator(new TokenFeatureGenerator(), elem.getSource(), elem.getTarget()));
    }
    featureLists.add(featureSet);
  }
  
  public static void addWindowTokenClassFeatureList(int leftWindow, int rightWindow, List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>();
    List<IntPair> windowParams = getListFromRange(leftWindow, rightWindow);
    for (IntPair elem: windowParams) {
      featureList.add(new WindowFeatureGenerator(new TokenClassFeatureGenerator(true), elem.getSource(), elem.getTarget()));
    }
    featureLists.add(featureList);
  }
  
  public static void addOutcomePriorFeatureList(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>(Arrays.asList(
        new OutcomePriorFeatureGenerator()));
    featureLists.add(featureList);
  }
  
  public static void addPreviousMapFeatureList(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>(Arrays.asList(
        new PreviousMapFeatureGenerator()));
    featureLists.add(featureList);
  }
  
  public static void addSentenceFeatureList(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>(Arrays.asList(
        new SentenceFeatureGenerator(true,
            false)));
    featureLists.add(featureList);
  }

  public static void addPrefixFeatures(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>();
    featureList.add(new Prefix34FeatureGenerator());
    featureLists.add(featureList);
  }
  public static void addSuffixFeatures(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>();
    featureList.add(new SuffixFeatureGenerator());
    featureLists.add(featureList);
  }
  
  public static void addBigramClassFeatureList(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>(Arrays.asList(
        new BigramClassFeatureGenerator()));
    featureLists.add(featureList);
  }
  
  public static void addTrigramClassFeatureList(List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>(Arrays.asList(
        new TrigramClassFeatureGenerator()));
    featureLists.add(featureList);
  }
  
  public static void addCharNgramFeatures(int minLength, int maxLength, List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>();
    List<IntPair> rangeList = getListFromRange(minLength, maxLength);
    for (IntPair pair : rangeList) {
      featureList.add(new CharacterNgramFeatureGenerator(pair.getSource(), pair.getTarget()));
    }
    featureLists.add(featureList);
  }
  
  /**
   * Adds the dictionary features to the feature list.
   *
   * @param featureList the feature list containing the dictionary features
   */
  private static void addDictionaryFeatures(final List<Set<AdaptiveFeatureGenerator>> featureLists) {
    Set<AdaptiveFeatureGenerator> featureList = new HashSet<AdaptiveFeatureGenerator>();
    for (int i = 0; i < dictionaries.getIgnoreCaseDictionaries().size(); i++) {
      prefix = dictionaries.getDictNames().get(i);
      dictionary = dictionaries.getIgnoreCaseDictionaries().get(i);
      featureList.add(new DictionaryFeatureGenerator(prefix, dictionary));
    }
    featureLists.add(featureList);
  }
  
  public static List<IntPair> getListFromRange(int minLength, int maxLenght) {
    List<Integer> rangeList1 = new ArrayList<Integer>();
    List<Integer> rangeList2 = new ArrayList<Integer>();
    Set<IntPair> rangeSet = new HashSet<IntPair>();
    for (int i = minLength; i < maxLenght + 1; ++i) {
      rangeList1.add(i);
      rangeList2.add(i);
    }
    for (Integer elem : rangeList1) {
      for (Integer item : rangeList2) {
        IntPair pair = new IntPair(elem, item);
        rangeSet.add(pair);
      }
    }
    List<IntPair> rangeList = new ArrayList<IntPair>(rangeSet);
    Collections.sort(rangeList);
    return rangeList;
  }
  
  public static List<Integer> getWindowRange(TrainingParameters params) {
    List<Integer> windowRange = new ArrayList<Integer>();
    String windowParam = params.getSettings().get("Window");
    String[] windowArray = windowParam.split("[ :-]");
    if (windowArray.length == 2) {
      windowRange.add(Integer.parseInt(windowArray[0]));
      windowRange.add(Integer.parseInt(windowArray[1]));
      
    }
    return windowRange;
  }
  
  public static List<Integer> getNgramRange(TrainingParameters params) {
    List<Integer> ngramRange = new ArrayList<Integer>();
    String charngramParam = params.getSettings().get("CharNgram");
    String[] charngramArray = charngramParam.split("[ :-]");
    if (charngramArray.length == 2) {
      ngramRange.add(Integer.parseInt(charngramArray[0]));
      ngramRange.add(Integer.parseInt(charngramArray[1]));
      
    }
    return ngramRange;
  }
  

}
