package es.ehu.si.ixa.pipe.nerc.dict;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.google.common.base.Charsets;
import com.google.common.io.Files;


/**
 * 
 * Class to load a Brown cluster document: word\tword_class\tprob
 * http://metaoptimize.com/projects/wordreprs/
 * 
 * The file containing the clustering lexicon has to be passed as the 
 * argument of the --distSimBrownPath parameter.
 * 
 * @author ragerri
 * @version 2014/07/29
 * 
 */
public class BrownCluster {

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
  public BrownCluster(final String inputDir) {
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
    List<String> fileLines = Files.readLines(inputPath, Charsets.UTF_8);
    dictionary = new Dictionary();
    dictionaryIgnoreCase = new Dictionary();
    for (String line : fileLines) {
      String[] lineArray = line.split("\\t");
      if (lineArray.length == 3) {
        int freq = Integer.parseInt(lineArray[2]);
        if (freq > 5) {
          dictionary.populate(lineArray[1],lineArray[0]);
          dictionaryIgnoreCase.populate(lineArray[1].toLowerCase(), lineArray[0]);
        }
      }
      else {
        System.err.println("Brown Clustering lexicon not well-formed after line:");
        System.err.println(line);
        System.exit(1);
      }
      }
  }

}

