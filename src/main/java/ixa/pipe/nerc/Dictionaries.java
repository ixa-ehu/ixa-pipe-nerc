package ixa.pipe.nerc;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.io.IOUtils;
import org.apache.commons.io.LineIterator;

public class Dictionaries {

  InputStream peopleFile;
  public final Set<String> person = new HashSet<String>();
  public final Set<String> location = new HashSet<String>();
  public final Set<String> organization = new HashSet<String>();

  private void loadPeopleList(InputStream file) throws IOException {
    LineIterator lineIterator = IOUtils.lineIterator(file, "UTF-8");
    try {
      while (lineIterator.hasNext()) {
        String line = lineIterator.nextLine();
        person.add(line.toLowerCase());
      }
    } finally {
      LineIterator.closeQuietly(lineIterator);
    }
  }

  public InputStream getDictionaries(String lang) {

    if (lang.equals("en")) {
      peopleFile = getClass().getResourceAsStream("/en-wikipeople.lst");
    }
    return peopleFile;
  }

  public Dictionaries(String lang) {
    if (lang.equalsIgnoreCase("en")) {
      peopleFile = getDictionaries(lang);
      try {
        loadPeopleList(peopleFile);
      } catch (IOException e) {
        e.getMessage();
      }
    }
  }
}
