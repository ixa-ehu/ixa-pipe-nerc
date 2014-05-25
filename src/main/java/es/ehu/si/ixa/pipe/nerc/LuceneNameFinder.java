package es.ehu.si.ixa.pipe.nerc;

import java.util.ArrayList;
import java.util.List;

import es.ehu.si.ixa.pipe.nerc.lucene.LuceneSearcher;

import opennlp.tools.namefind.NameFinderME;
import opennlp.tools.util.Span;

public class LuceneNameFinder implements NameFinder {
  
  private NameFactory nameFactory;
  private LuceneSearcher luceneSearcher;
  private String indexDir;
  private boolean debug = false;
  
  public LuceneNameFinder(String aIndexDir, NameFactory aNameFactory) {
    this.indexDir = aIndexDir;
    this.nameFactory = aNameFactory;
    luceneSearcher = new LuceneSearcher(indexDir, nameFactory);
  }
  
  public final List<Name> getNames(final String[] tokens) {

    List<Span> origSpans = nercToSpans(tokens);
    Span[] neSpans = NameFinderME.dropOverlappingSpans(origSpans
        .toArray(new Span[origSpans.size()]));
    List<Name> names = getNamesFromSpans(neSpans, tokens);
    return names;
  }
  
  public final List<Span> nercToSpans(final String[] tokens) {
    List<Span> neSpans = new ArrayList<Span>();
    String sentTokens = StringUtils.getSentenceFromTokens(tokens);
    List<Name> detectedNames = luceneSearcher.searchNames(sentTokens);
    for (Name detectedName : detectedNames) {
      List<Integer> neIds = StringUtils.exactTokenFinderIgnoreCase(detectedName.value(),
          tokens);
      if (!neIds.isEmpty()) {
        Span neSpan = new Span(neIds.get(0), neIds.get(1), detectedName.getType());
        if (debug) {
          System.err.println(neSpans.toString());
        }
        neSpans.add(neSpan);
      }
    }
    return neSpans;
  }  
  /**
   * Creates a list of {@link Name} objects from spans and tokens.
   *
   * @param neSpans
   *          the spans of the entities in the sentence
   * @param tokens
   *          the tokenized sentence
   * @return a list of {@link Name} objects
   */
  public final List<Name> getNamesFromSpans(final Span[] neSpans,
      final String[] tokens) {
    List<Name> names = new ArrayList<Name>();
    for (Span neSpan : neSpans) {
      String nameString = StringUtils.getStringFromSpan(neSpan, tokens);
      String neType = neSpan.getType();
      Name name = nameFactory.createName(nameString, neType, neSpan);
      names.add(name);
    }
    return names;
  }
  /**
   * Clear the adaptiveData for each document.
   */
  public void clearAdaptiveData() {
    // nothing to clear
  }


}
