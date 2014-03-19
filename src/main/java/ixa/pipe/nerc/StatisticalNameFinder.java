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

package ixa.pipe.nerc;

import ixa.pipe.nerc.train.AbstractNameFinderTrainer;
import ixa.pipe.nerc.train.BaselineNameFinderTrainer;
import ixa.pipe.nerc.train.Dict3NameFinderTrainer;
import ixa.pipe.nerc.train.DictLbjNameFinderTrainer;
import ixa.pipe.nerc.train.NameFinderTrainer;

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

   private TokenNameFinderModel nercModel;
   private NameFinderME nameFinder;
   private NameFactory nameFactory;
   private NameFinderTrainer nameFinderTrainer;

  /**
   * Construct a StatisticalNameFinder. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model.
   */
  public StatisticalNameFinder(InputStream trainedModel,String model) {

    try {
      nercModel = new TokenNameFinderModel(trainedModel);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }
    nameFinderTrainer = getNameFinderTrainer(model);
    nameFinder = new NameFinderME(nercModel,nameFinderTrainer.createFeatureGenerator(),AbstractNameFinderTrainer.GREEDY_BEAM_SIZE);
  }
  
  /**
   * Construct a StatisticalNameFinder. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model. This constructor also uses a NameFactory to create {@link Name}
   * objects
   */
  public StatisticalNameFinder(InputStream trainedModel, NameFactory nameFactory, String model) {
    this.nameFactory = nameFactory;
    try {
      nercModel = new TokenNameFinderModel(trainedModel);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      if (trainedModel != null) {
        try {
          trainedModel.close();
        } catch (IOException e) {
        }
      }
    }
    nameFinderTrainer = getNameFinderTrainer(model);
    nameFinder = new NameFinderME(nercModel,nameFinderTrainer.createFeatureGenerator(),AbstractNameFinderTrainer.GREEDY_BEAM_SIZE);
  }

  public NameFinderTrainer getNameFinderTrainer(String model) {
    if (model.equalsIgnoreCase("baseline")) {
      nameFinderTrainer = new BaselineNameFinderTrainer();
    }
    else if (model.equalsIgnoreCase("dict3")) {
      nameFinderTrainer = new Dict3NameFinderTrainer();
    }
    else if (model.equalsIgnoreCase("dictlbj")) {
      nameFinderTrainer = new DictLbjNameFinderTrainer();
      }
    return nameFinderTrainer;
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
  
}
