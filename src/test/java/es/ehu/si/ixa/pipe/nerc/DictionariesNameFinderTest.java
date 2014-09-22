package es.ehu.si.ixa.pipe.nerc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import opennlp.tools.util.Span;

import org.apache.commons.io.FileUtils;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

import es.ehu.si.ixa.pipe.nerc.dict.Dictionaries;

public class DictionariesNameFinderTest {

    private static DictionariesNameFinder finder = null;
    
    @BeforeClass
    public static void setUpClass() throws IOException {
        // copy to a temporary dir so that it can be loaded
        File dictsDir = Files.createTempDirectory("dicts").toFile();
        FileUtils.copyURLToFile(DictionariesNameFinderTest.class
                .getResource("/names.txt"),
                new File(dictsDir, "names.txt"));
        // now load it into a Dictionaries instance
        finder = new DictionariesNameFinder(
                new Dictionaries(dictsDir.getAbsolutePath()));
    }
    
    @Test
    public void oneOccurrence() throws IOException {
        List<Span> spans = finder.nercToSpansExact(new String[] {"Achilles"});
        assertEquals(1, spans.size());
    }

    @Test
    public void twoOccurrences() throws IOException {
        List<Span> spans = finder.nercToSpansExact(new String[] {
                "Achilles", "Apollo", "Zeus", "Achilles"});
        assertEquals(2, spans.size());
    }

}
