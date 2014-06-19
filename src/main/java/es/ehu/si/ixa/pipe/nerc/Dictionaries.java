package es.ehu.si.ixa.pipe.nerc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.io.FileUtils;


public class Dictionaries {

  public static List<String> dictNames = new ArrayList<String>();
  public static List<HashMap<String, String>> dictionaries = null;
  public static List<HashMap<String, String>> dictionariesIgnoreCase = null;
  
  public Dictionaries(final String inputDir) {
    try {
      loadDictionaries(inputDir);
    } catch (IOException e) {
      e.getMessage();
    }
  }
  
  /**
   * Get the dictionary.
   *
   * @return the dictionary as a list
   */
  public final List<HashMap<String,String>> getDictionaries() {
    return dictionaries;
  }
  
  /**
   * Get the dictionary.
   *
   * @return the dictionary as a list
   */
  public final List<HashMap<String,String>> getIgnoreCaseDictionaries() {
    return dictionariesIgnoreCase;
  }

 
  private void loadDictionaries(final String inputDir) throws IOException {
    File inputPath = new File(inputDir);
    if (inputPath.isDirectory()) {
      Collection<File> files = FileUtils.listFiles(inputPath, null, true);
      List<File> fileList = new ArrayList<File>(files);
      dictionaries = new ArrayList<HashMap<String, String>>(files.size());
      dictionariesIgnoreCase = new ArrayList<HashMap<String, String>>(
          files.size());
      for (int i = 0; i < fileList.size(); ++i) {
        System.err.println("\tloading gazzetteer:...." + fileList.get(i).getCanonicalPath());
        dictionaries.add(new HashMap<String,String>());
        dictionariesIgnoreCase.add(new HashMap<String,String>());
        List<String> fileLines = FileUtils.readLines(fileList.get(i), "UTF-8");
        for (String line : fileLines) {
          String[] lineArray = line.split(";");
          dictionaries.get(i).put(lineArray[0], lineArray[1]);
          if((!line.equalsIgnoreCase("in"))&&(!line.equalsIgnoreCase("on"))&&(!line.equalsIgnoreCase("us"))&&(!line.equalsIgnoreCase("or"))&&(!line.equalsIgnoreCase("am"))) {
            dictionariesIgnoreCase.get(i).put(lineArray[0].toLowerCase(),lineArray[1]);
          }
        }
      }
      System.err.println("found " + dictionaries.size() + " gazetteers");
    }
    
  }
  

}
