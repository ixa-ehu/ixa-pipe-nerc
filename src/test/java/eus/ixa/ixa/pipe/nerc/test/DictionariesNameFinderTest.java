package eus.ixa.ixa.pipe.nerc.test;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.BeforeClass;
import org.junit.Test;

import eus.ixa.ixa.pipe.ml.nerc.DictionariesNERTagger;
import eus.ixa.ixa.pipe.ml.resources.Dictionaries;
import eus.ixa.ixa.pipe.ml.utils.Span;

public class DictionariesNameFinderTest {

  private static DictionariesNERTagger finder = null;

  @BeforeClass
  public static void setUpClass() throws IOException {
    // copy to a temporary dir so that it can be loaded
    File dictsDir = Files.createTempDirectory("dicts").toFile();
    Files.copy(
        DictionariesNameFinderTest.class.getResourceAsStream("/names.txt"),
        new File(dictsDir, "names.txt").toPath());
    // now load it into a Dictionaries instance
    finder = new DictionariesNERTagger(
        new Dictionaries(dictsDir.getAbsolutePath()));
  }

  @Test
  public void oneOccurrence() throws IOException {
    Span[] spans = finder.nercToSpansExact(new String[] { "Achilles" });
    assertEquals(1, spans.length);
  }

  @Test
  public void twoOccurrences() throws IOException {
    Span[] spans = finder.nercToSpansExact(
        new String[] { "Achilles", "Apollo", "Zeus", "Achilles" });
    assertEquals(2, spans.length);
  }

}
