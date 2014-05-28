package es.ehu.si.ixa.pipe.nerc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

/**
 * It defines a Dictionary class consisting of
 * a list of strings.
 *
 * @author ragerri
 *
 */
public class Dictionary {

  /**
   * The list to store the dictionary.
   */
  private final List<String> dictList = new ArrayList<String>();

  /**
   * Dictionary constructor.
   * @param fileInputStream the inputStream of the file
   */
  public Dictionary(final InputStream fileInputStream) {
      try {
        loadDict(fileInputStream);
      } catch (IOException e) {
        e.getMessage();
      }
  }

  /**
   * Get the dictionary.
   *
   * @return the dictionary as a list
   */
  public final List<String> getDict() {
    return dictList;
  }
  
  /**
   *
   * @param fileInputStream the inputstream of the file
   * @throws IOException input output exception
   */
  private void loadDict(final InputStream fileInputStream) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(fileInputStream, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        dictList.add(line.trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }

}
