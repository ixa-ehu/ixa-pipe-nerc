/*
 *Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package es.ehu.si.ixa.pipe.nerc;

import es.ehu.si.ixa.pipe.nerc.train.BaselineNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.Dict3NameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.DictLbjNameFinderTrainer;
import es.ehu.si.ixa.pipe.nerc.train.NameFinderTrainer;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

 /**
 * Named Entity Recognition module based on Apache OpenNLP Machine Learning API
 *  
 * @author ragerri 2014/03/14
 * 
 */

 public class StatisticalNameFinder implements NameFinder {

   private InputStream trainedModelInputStream;
   private static TokenNameFinderModel nercModel;
   private NameFinderME nameFinder;
   private NameFactory nameFactory;
   private NameFinderTrainer nameFinderTrainer;
   public static final int DEFAULT_BEAM_SIZE = 3;
   
  public StatisticalNameFinder(String lang, String model, String features, int beamsize) {
  
    try {
      if (model.equalsIgnoreCase("baseline")) {
        trainedModelInputStream = getModel(lang,model);
        System.err.println("No model chosen, reverting to baseline model!");
      }
      else if (trainedModelInputStream == null) {
        trainedModelInputStream = new FileInputStream(model);
      }
      nercModel = new TokenNameFinderModel(trainedModelInputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
        }
      }
    }
    nameFinderTrainer = getNameFinderTrainer(features,beamsize);
    nameFinder = new NameFinderME(nercModel,nameFinderTrainer.createFeatureGenerator(),beamsize);
  }
  
  public StatisticalNameFinder(String lang,String model,String features) {
	  this(lang,model,features,DEFAULT_BEAM_SIZE);
  }
  
  public StatisticalNameFinder(String lang, NameFactory nameFactory, String model, String features, int beamsize) {
   
    this.nameFactory = nameFactory;
    
    try {
      if (model.equalsIgnoreCase("baseline")) {
        trainedModelInputStream = getModel(lang,model);
        System.err.println("No model chosen, reverting to baseline model!");
      }
      else if (trainedModelInputStream == null) {
        trainedModelInputStream = new FileInputStream(model);
      }
      nercModel = new TokenNameFinderModel(trainedModelInputStream);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModelInputStream != null) {
        try {
          trainedModelInputStream.close();
        } catch (IOException e) {
        }
      }
    }
    nameFinderTrainer = getNameFinderTrainer(features,beamsize);
    nameFinder = new NameFinderME(nercModel,nameFinderTrainer.createFeatureGenerator(),beamsize);
  }
  
  public StatisticalNameFinder(String lang, NameFactory nameFactory, String model, String features) {
	  this(lang,nameFactory,model,features,DEFAULT_BEAM_SIZE);
  }
  
  /**
   * Probabilistic Named Entity Classifier
   * 
   * Takes an array of tokens, calls nercToSpans function 
   * for probabilistic NERC and returns a List of {@link Name} objects
   * containing the nameString, the type and the {@link Span}
   * 
   * @param tokens an array of tokenized text
   * @return a List of names
   */
  public List<Name> getNames(String[] tokens) {
    List<Span> origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans.toArray(new Span[origSpans.size()]));
    List<Name> names = getNamesFromSpans(neSpans,tokens);
    return names;
  }
  
  /**
   * This method receives as input an array of tokenized text
   * and calls the NameFinderME.find(tokens) to recognize and classify Named
   * Entities. It outputs the spans of the detected and classified Named Entities. 
   * 
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   * 
   * @param tokens
   *          an array of tokenized text
   * @return an list of {@link Span}s of Named Entities
   */
  public List<Span> nercToSpans(String[] tokens) {
    Span[] annotatedText = nameFinder.find(tokens);
    clearAdaptiveData();
    List<Span> probSpans = new ArrayList<Span>(Arrays.asList(annotatedText));
    return probSpans;
  }
  
  /**
   * Creates a list of {@link Name} objects from spans and tokens
   * 
   * @param neSpans
   * @param tokens
   * @return a list of {@link Name} objects
   */
  public List<Name> getNamesFromSpans(Span[] neSpans, String[] tokens) {
    List<Name> names = new ArrayList<Name>();
    for (Span neSpan : neSpans) { 
      String nameString = StringUtils.getStringFromSpan(neSpan,tokens);
      String neType = neSpan.getType();
      Name name = nameFactory.createName(nameString, neType, neSpan);
      names.add(name);
    }
    return names;
  }
 
  /**
   * Forgets all adaptive data which was collected during previous
   * calls to one of the find methods.
   *
   * This method is typical called at the end of a document.
   */
  public void clearAdaptiveData() {
    nameFinder.clearAdaptiveData();
  }
  
  public NameFinderTrainer getNameFinderTrainer(String features, int beamsize) {
    if (features.equalsIgnoreCase("baseline")) {
      nameFinderTrainer = new BaselineNameFinderTrainer(beamsize);
    }
    else if (features.equalsIgnoreCase("dict3")) {
      nameFinderTrainer = new Dict3NameFinderTrainer(beamsize);
    }
    else if (features.equalsIgnoreCase("dictlbj")) {
      nameFinderTrainer = new DictLbjNameFinderTrainer(beamsize);
      }
    return nameFinderTrainer;
  }
  
  private InputStream getModel(String lang, String model) {

    if (lang.equalsIgnoreCase("en")) {
      trainedModelInputStream = getClass().getResourceAsStream("/en/en-nerc-perceptron-baseline-c0-b3.bin");
    }
    if (lang.equalsIgnoreCase("es")) {
      trainedModelInputStream = getClass().getResourceAsStream("/es/es-nerc-maxent-baseline-500-c4-b3.bin");
    }
    return trainedModelInputStream;
  }
  
}
