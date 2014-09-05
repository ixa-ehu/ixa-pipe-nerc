/*
 *  Copyright 2014 Rodrigo Agerri

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

package es.ehu.si.ixa.pipe.nerc.dict;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.commons.io.FileUtils;


/**
 * 
 * Class to load a Clark cluster document: word\\s+word_class\\s+prob
 * https://github.com/ninjin/clark_pos_induction
 * 
 * The file containing the clustering lexicon has to be passed as the 
 * argument of the --distSimPath parameter.
 * 
 * @author ragerri
 * @version 2014/07/29
 * 
 */
public class ClarkCluster {

  /**
   * The list of Dictionary in which to load the lexicon.
   */
  private static Dictionary dictionary;
  /**
   * The lowercase dictionary as HashMap<String, String>.
   */
  private static Dictionary dictionaryIgnoreCase;

  /**
   * Construct the
   * 
   * @param inputDir
   *          the input directory
   */
  public ClarkCluster(final String inputDir) {
    if (dictionary == null && dictionaryIgnoreCase == null) {
      try {
        loadDictionary(inputDir);
      } catch (IOException e) {
        e.getMessage();
      }
    }
  }
  
  /**
   * Get the list of dictionaries as HashMaps.
   * 
   * @return a list of the dictionaries as HashMaps
   */
  public final Dictionary getDictionary() {
    return dictionary;
  }

  /**
   * Get the lower case dictionaries.
   * 
   * @return a list of the dictionaries as HashMaps
   */
  public final Dictionary getIgnoreCaseDictionary() {
    return dictionaryIgnoreCase;
  }


  /**
   * Load the lexicon.
   * 
   * @param inputFile
   *          the input file containing the clustering lexicon
   * @throws IOException
   *           throws an exception if directory does not exist
   */
  private void loadDictionary(final String inputFile) throws IOException {
    File inputPath = new File(inputFile);
    System.err.println("\tLoading clustering lexicon...: " + inputPath.getCanonicalPath());
    List<String> fileLines = FileUtils.readLines(inputPath, "UTF-8");
    dictionary = new Dictionary();
    dictionaryIgnoreCase = new Dictionary();
    for (String line : fileLines) {
      String[] lineArray = line.split(" ");
      if (lineArray.length == 2) {
        dictionary.populate(lineArray[0],lineArray[1]);
        dictionaryIgnoreCase.populate(lineArray[0].toLowerCase(), lineArray[1]);
      }
      else {
        System.err.println("Clustering lexicon not well-formed after line:");
        System.err.println(line);
        System.exit(1);
      }
      }
  }

}

