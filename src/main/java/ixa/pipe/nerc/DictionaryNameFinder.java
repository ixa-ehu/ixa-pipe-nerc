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
import java.util.ArrayList;
import java.util.List;


import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

 /**
 * Named Entity Recognition module based on {@link Dictionary} objects
 * This class provides the following functionalities:
 * 
 * <ol>
 * <li> string matching against of a string (typically tokens) against a Dictionary containing
 *      names. This function is also used to implement Dictionary based features in the training
 *      package.
 * <li> tag: Provided a Dictionary it tags only the names it matches against it
 * <li> post: This function checks for names in the Dictionary that have not been detected
 *      by a {@link StatisticalNameFinder}; it also corrects the Name type for those detected 
 *      by a {@link StatisticalNameFinder} but also present in a dictionary
 * <ol>
 *  
 * @author ragerri 2014/03/14
 * 
 */

 public class DictionaryNameFinder implements NameFinder {

   private static final String DEFAULT_TYPE = "MISC";
   private final String type;
   
   private NameFactory nameFactory;
   private Dictionary dict;
   private final static boolean DEBUG = false;

 
  public DictionaryNameFinder(Dictionary dict, String type) {
    this.dict = dict;
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null!");
    }
    this.type = type;
  }
  
  public DictionaryNameFinder(Dictionary dict) {
    this(dict, DEFAULT_TYPE);
  }
  
  public DictionaryNameFinder(Dictionary dict, String type, NameFactory nameFactory) {
    this.dict = dict;
    this.nameFactory = nameFactory;
    if (type == null) {
      throw new IllegalArgumentException("type cannot be null!");
    }
    
    this.type = type;
  }
  
  public DictionaryNameFinder(Dictionary dict, NameFactory nameFactory) {
    this(dict,DEFAULT_TYPE,nameFactory);
  }
  
  /**
   * {@link Dictionary} based Named Entity Detection and Classification
   * 
   * @param tokens
   * @return a list of detected {@link Name} objects 
   */
  public List<Name> getNames(String[] tokens) {
    
    List<Span> origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans.toArray(new Span[origSpans.size()]));
    List<Name> names = getNamesFromSpans(neSpans,tokens);
    return names;
  }
  
  /**
   * Detects Named Entities in a {@link Dictionary} by NE type
   * ignoring case
   * 
   * @param tokens 
   * @return spans of the Named Entities all 
   */
   public List<Span> nercToSpans(String[] tokens) {
     List<Span> neSpans = new ArrayList<Span>();
     for (String neDict : dict.dictList) {
       List<Integer> neIds = StringUtils.exactTokenFinderIgnoreCase(neDict,tokens);
       if (!neIds.isEmpty()) {
         Span neSpan = new Span(neIds.get(0), neIds.get(1), type);
         if (DEBUG) {
           System.err.println(neSpans.toString());
         }
         neSpans.add(neSpan); 
       }
     }
     return neSpans;
   }
   
   /**
    * Detects Named Entities in a {@link Dictionary} by NE type
    * This method is case sensitive 
    * 
    * @param tokens 
    * @return spans of the Named Entities all 
    */
    public List<Span> nercToSpansExact(String[] tokens) {
      List<Span> neSpans = new ArrayList<Span>();
      for (String neDict : dict.dictList) {
        List<Integer> neIds = StringUtils.exactTokenFinder(neDict,tokens);
        if (!neIds.isEmpty()) {
          Span neSpan = new Span(neIds.get(0), neIds.get(1), type);
          if (DEBUG) {
            System.err.println(neSpans.toString());
          }
          neSpans.add(neSpan); 
        }
      }
      return neSpans;
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
   * Concatenates two span lists adding the spans of the second parameter
   * to the list in first parameter
   * 
   * @param allSpans
   * @param neSpans
   */
  public void concatenateSpans(List<Span> allSpans, List<Span> neSpans) {
    for (Span span : neSpans) {
      allSpans.add(span);
    }
  }
  
  /**
   * Concatenates two span lists adding the spans to a new list
   * if the span in the second parameter does not exist in the first
   * 
   * @param preList
   * @param postList
   * @return a list containing the {@link Span}s of the two lists if they are not duplicated
   */
  public List<Span> concatenateNoOverlappingSpans(List<Span> preList, List<Span> postList) {
    List<Span> allSpans = new ArrayList<Span>();
    if (postList.isEmpty()) {
      allSpans.addAll(preList);
      if (DEBUG) {
        System.err.println("No dict spans in this sentence, adding probabilistic spans only!");
      }
    }
      else {
        for (Span span1 : preList) {
          for (Span span2 : postList) {
            if (!span1.contains(span2)) {
              allSpans.add(span2);
              if (DEBUG) {
                System.err.println("adding dict span! " + span2.toString());
              }
            }
            else if(span2.contains(span1)) {
              allSpans.add(span2);
            }
            else if(span1.contains(span2)) {
              allSpans.add(span2);
            }
            else {
              allSpans.add(span1);
              if (DEBUG) {
                System.err.println("adding prob span! " + span1.toString());
              }
            }
          }
        }
      } 
    return allSpans;
  }
  
  public void clearAdaptiveData() {
    // nothing to clear
  }

}
