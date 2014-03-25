package ixa.pipe.nerc;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import opennlp.tools.util.Span;


public class RegexNameFinder implements NameFinder {

		
	  private final Pattern[] mPatterns;
	  private final String sType;

	  public RegexNameFinder(Pattern[] patterns, String type) {
	    if (patterns == null || patterns.length == 0) {
	      throw new IllegalArgumentException("patterns must not be null or empty!");
	    }

	    mPatterns = patterns;
	    sType = type;
	  }

	  public RegexNameFinder(Pattern patterns[]) {
	    if (patterns == null || patterns.length == 0) {
	      throw new IllegalArgumentException("patterns must not be null or empty!");
	    }

	    mPatterns = patterns;
	    sType = null;
	  }
	  
	  public Span[] find(String tokens[]) {
	    Map<Integer, Integer> sentencePosTokenMap = new HashMap<Integer, Integer>();

	    StringBuffer sentenceString = new StringBuffer(tokens.length *  10);

	    for (int i = 0; i < tokens.length; i++) {

	      int startIndex = sentenceString.length();
	      sentencePosTokenMap.put(startIndex, i);

	      sentenceString.append(tokens[i]);

	      int endIndex = sentenceString.length();
	      sentencePosTokenMap.put(endIndex, i + 1);

	      if (i < tokens.length - 1) {
	        sentenceString.append(' ');
	      }
	    }

	    Collection<Span> annotations = new LinkedList<Span>();

	    for (Pattern mPattern : mPatterns) {
	      Matcher matcher = mPattern.matcher(sentenceString);

	      while (matcher.find()) {
	        Integer tokenStartIndex =
	            sentencePosTokenMap.get(matcher.start());
	        Integer tokenEndIndex =
	            sentencePosTokenMap.get(matcher.end());

	        if (tokenStartIndex != null && tokenEndIndex != null) {
	          Span annotation = new Span(tokenStartIndex, tokenEndIndex, sType);
	          annotations.add(annotation);
	        }
	      }
	    }

	    return annotations.toArray(
	        new Span[annotations.size()]);
	  }
	  
	  public void clearAdaptiveData() {
	    // nothing to clear
	  }

	public List<Name> getNames(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Span> nercToSpans(String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Name> getNamesFromSpans(Span[] neSpans, String[] tokens) {
		// TODO Auto-generated method stub
		return null;
	}
	}



