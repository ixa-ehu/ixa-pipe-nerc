/*
 *Copyright 2013 Rodrigo Agerri

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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.namefind.TokenNameFinderModel;
import opennlp.tools.util.Span;

/**
 * Simple Named Entity Recognition module based on Apache OpenNLP.
 * 
 * English model trained by IXA NLP Group.
 * 
 * @author ragerri 2012/10/30
 * 
 */

public class NERC {

  private TokenNameFinderModel nercModel;
  private NameFinderME nercDetector;
  private NameFactory nameFactory;

  /**
   * It constructs an object NERC from the NERC class. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model.
   */
  public NERC(InputStream trainedModel) {

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

    nercDetector = new NameFinderME(nercModel);
  }
  
  /**
   * It constructs an object NERC from the NERC class. First it loads a model,
   * then it initializes the nercModel and finally it creates a nercDetector
   * using such model.
   */
  public NERC(InputStream trainedModel, NameFactory nameFactory) {
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

    nercDetector = new NameFinderME(nercModel);
  }


  /**
   * This method receives as an input an array of Apache OpenNLP tokenized text
   * and calls the NameFinderME.find(tokens) to recognize and classify Named
   * Entities.
   * 
   * From Apache OpenNLP documentation: "After every document clearAdaptiveData
   * must be called to clear the adaptive data in the feature generators. Not
   * calling clearAdaptiveData can lead to a sharp drop in the detection rate
   * after a few documents."
   * 
   * @param tokens
   *          an array of tokenized text
   * @return an array of OpenNLP Spans of annotated text
   */
  public Span[] nercToSpans(String[] tokens) {
    Span[] annotatedText = nercDetector.find(tokens);
    nercDetector.clearAdaptiveData();
    return annotatedText;

  }
  
  /**
   * @param tokens
   *          an array of tokenized text
   * @return an array of OpenNLP Spans of annotated text
   */
  public List<Name> getNames(String[] tokens) {
    List<Name> names = new ArrayList<Name>();
    Span[] origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans);
    for (Span neSpan : neSpans) {
      String sentence = getStringFromSpan(neSpan,tokens);
      String nameString = neSpan.getCoveredText(sentence).toString();
      String neType = neSpan.getType();
      Name name = nameFactory.createName(nameString, neType, neSpan);
      names.add(name);
    }
    return names;

  }
  
  /**
   * 
   * It takes a NE span indexes and the tokens in a sentence and produces the
   * string to which the NE span corresponds to. This function is used to get
   * the NE textual representation from a Span.
   * 
   * @param Span
   *          reducedSpan
   * @param String
   *          [] tokens
   * @return named entity string
   */
  public String getStringFromSpan(Span reducedSpan, String[] tokens) {
    StringBuilder sb = new StringBuilder();
    for (int si = reducedSpan.getStart(); si < reducedSpan.getEnd(); si++) {
      sb.append(tokens[si]).append(" ");
    }
    return sb.toString().trim();
  }
  
  public static void concatenateSpans(List<Span> listSpans, Span[] probSpans) {
    for (Span span : probSpans) {
      listSpans.add(span);
    }
  }


}
