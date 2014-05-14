/*
 * Copyright 2014 Rodrigo Agerri

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package es.ehu.si.ixa.pipe.nerc;

import java.io.BufferedReader;
import java.io.IOException;


/**
 *  IxaPipeTokenizer is based on the {@link IxaPipeLexer} class. 
 *  This Tokenizer overrides {@link AbstractNameFinderLexer} getToken() method 
 *  by using the {@link IxaPipeLexer} yylex() method.  
 *  
 * This Tokenizer tokenizes running text but also provides normalization functions 
 * to comply with annotation in corpora such as Penn Treebank for English and 
 * Ancora Corpus for Spanish. Most of the normalization rules have been adapted from 
 * the @link PTBLexer class of Stanford CoreNLP version 3.2.0, but with many changes for 
 * other requirements, such as those of the Ancora corpus. 
 *  
 * Specifically, apart from English Penn Treebank-compliant tokenization, 
 * this Tokenizer provides:
 *  
 * <ol>
 *  <li> multilingual treatment of apostrophes for Catalan, French and Italian styles 
 *       (l' aquila, c' est, etc.) possibly applying to other languages with the same 
 *       rules for splitting apostrophes. 
 *  <li> multilingual support for non-breaking prefixes, adding language-specific 
 *       acronyms and person titles, etc., for Dutch, German, French, Italian and Spanish. 
 *  <li> normalization for Ancora corpus in Spanish.
 *  <li> paragraph tokenization to provide paragraph information.
 *  </ol> 
 *      
 * By default, the tokenizer does PTB3 normalization style except brackets and forward 
 * slashes (value "default" of ixa-pipe-tok -normalization parameter as described below). 
 * To change these options, the ixa-pipe-tok CLI currently provides four options, accessible via the 
 * "-normalization" parameter. 
 * 
 * <ol>
 *   <li>sptb3: Strict Penn Treebank normalization. Performs all normalizations listed below 
 *       except tokenizeNLs. 
 *   <li>ptb3: Activates all SPTB3 normalizations except: 
 *     <ol>
 *       <li>Acronym followed by a boundaryToken, the boundaryToken in this option
 *           is duplicated: "S.A." -> "S.A. .", whereas sptb3 does "S.A ." (only exception
 *           in sptb3 is "U.S." for which the last dot is duplicated. 
 *       <li> This option returns fractions such as "2 3/4" as a Token object, 
 *          but sptb3 separate them into two Token objects. 
 *     </ol>
 *   <li> default: ptb3 minus (all types of) brackets and escapeForwardSlash normalizations.
 *   <li> ancora: Ancora corpus based normalization. Like default, except that every 
 *        quote is normalized into ascii quotes. 
 * </ol> 
 * 
 * The normalization performed by the four options above are (in the order in which
 * they appear in the @link JFlexLexer specification):
 * <ol> 
 * <li>tokenizeParagraphs: creates Paragraph Tokens when more than newlines are found.
 *       Paragraphs are denoted by "*<P>*"
 *   <li>tokenizeNLs: create Token objects with newline characters
 *   <li>escapeForwardSlash: escape / and * -> \/ \*
 *   <li>normalizeBrackets: Normalize ( and ) into -LRB- and -RRB- respectively
 *   <li>normalizeOtherBrackets: Normalize {} and[] into -LCB-, -LRB- and -RCB-, -RRB-
 *   <li>latexQuotes: Normalize to ``, `, ', '' for every quote (discouraged by Unicode).
 *   <li>unicodeQuotes: Normalize quotes to the range U+2018-U+201D,
 *       following Unicode recommendation.
 *   <li>asciiQuotes: Normalize quote characters to ascii ' and ". The quotes preference 
 *       default order is latex -> Unicode -> ascii
 *   <li>sptb3Normalize: normalize fractions and Acronyms as described by the sptb3 option above. 
 *   <li>ptb3Dashes: Normalize various dash characters into "--",
 *   <li>normalizeAmpersand: Normalize the XML &amp;amp; into an ampersand
 *   <li>normalizeSpace: Turn every spaces in tokens (phone numbers, fractions
 *     get turned into non-breaking spaces (U+00A0).
 *   <li>normalizeFractions: Normalize fraction characters to forms like "1/2"   
 *   <li>normalizeCurrency: Currency mappings into $, #, or "cents", reflecting
 *     the fact that nothing else appears in the PTB3 WSJ (not Euro).
 *   <li>ptb3Ldots: Normalize ellipses into ...
 *   <li>unicodeLdots: Normalize dot and optional space sequences into the Unicode 
 *       ellipsis character (U+2026). Dots order of application is ptb3Ldots -> UnicodeLdots.
 * </ol>
 * 
 * For more CLI options, please check {@link CLI} javadoc and README file. 
 * @author ragerri
 * @version 2013-11-27
 * 
 */
 
public class NumericEntitiesFinder<T> extends AbstractNameFinderLexer<T> {

  
  private NumericEntitiesLexer jlexer;
  
  /**
   * Construct a new Tokenizer which uses the @link JFlexLexer specification
   * 
   * 
   * @param breader Reader
   * @param tokenFactory The TokenFactory that will be invoked to convert
   *        each string extracted by the @link JFlexLexer  into a @Token object
   * @param normalize the values of the -normalize parameter
   * @option options Paragraph options
   * 
   */
  public NumericEntitiesFinder(BufferedReader breader, NameFactory nameFactory) {
    jlexer = new NumericEntitiesLexer(breader, nameFactory);
  }

  /**
   * It obtains the next token. This functions performs the actual tokenization 
   * by calling the @link JFlexLexer yylex() function
   *
   * @return the next token or null if none exists.
   */
  @Override
  @SuppressWarnings("unchecked")
  public T getToken() {
    try {
      return (T) jlexer.yylex();
    } catch (IOException e) {
      e.printStackTrace();
    }
    return nextToken;
  }
  
}
