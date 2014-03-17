package ixa.pipe.nerc;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class Gazetteer {
  
  public final List<String> gazetteerList = new ArrayList<String>();
  
  
  public Gazetteer(InputStream gazetteerFile) {
      try {
        loadGazetteer(gazetteerFile);
      } catch (IOException e) {
        e.getMessage();
      }
  }
  
  private void loadGazetteer(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        gazetteerList.add(line.trim());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }

}
