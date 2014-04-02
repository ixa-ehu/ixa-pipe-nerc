package es.ehu.si.ixa.pipe.nerc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class Dictionary {
  
  public final List<String> dictList = new ArrayList<String>();
  
  
  public Dictionary(InputStream dictFile) {
      try {
        loadDict(dictFile);
      } catch (IOException e) {
        e.getMessage();
      }
  }
  
  private void loadDict(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
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
