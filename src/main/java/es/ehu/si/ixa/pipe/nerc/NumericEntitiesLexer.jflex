package es.ehu.si.ixa.pipe.nerc;

/* --------------------------Usercode Section------------------------ */


import java.io.Reader;
import java.util.logging.Logger;
import java.util.regex.Pattern;

	
/* -----------------Options and Declarations Section----------------- */

%%

%class NumericEntitiesLexer
%unicode
%type Name
%caseless
%char

/* 
 * Member variables and functions
 */

%{

  private NameFactory nameFactory;
  private static final Logger LOGGER = Logger.getLogger(NumericEntitesLexer.class.getName());
  private boolean seenUntokenizableCharacter;
  private enum UntokenizableOptions { NONE_DELETE, FIRST_DELETE, ALL_DELETE, NONE_KEEP, FIRST_KEEP, ALL_KEEP }
  private UntokenizableOptions untokenizable = UntokenizableOptions.FIRST_DELETE;
  
  
  /////////////////
  //// OPTIONS ////
  /////////////////
  
  
  public NumericEntitiesLexer(Reader breader, NameFactory aNameFactory) {
    this(breader);
    this.nameFactory = aNameFactory;
  }

  
  ////////////////////////
  //// MAIN FUNCTIONS ////
  ////////////////////////
  
  
  private Name makeName(String nameString, String neType) {
    Name name = nameFactory.createName(nameString, neType, yychar, yylength());
    return name;
  }

%}

  ////////////////
  //// MACROS ////
  ///////////////


/*---- GENERIC NUMBERS ----*/

DIGIT = [:digit:]|[\u07C0-\u07C9]
NUM = {DIGIT}+|{DIGIT}*([.:,\u00AD\u066B\u066C]{DIGIT}+)+
NUMBER = [\-+]?{NUM}[.,]?
SUBSUPNUM = [\u207A\u207B\u208A\u208B]?([\u2070\u00B9\u00B2\u00B3\u2074-\u2079]+|[\u2080-\u2089]+)
FRACTION = ({DIGIT}{1,4}[- \u00A0])?{DIGIT}{1,4}(\\?\/|\u2044){DIGIT}{1,4}
FRACTION_TB3 = ({DIGIT}{1,4}-)?{DIGIT}{1,4}(\\?\/|\u2044){DIGIT}{1,4}
OTHER_FRACTION = [\u00BC\u00BD\u00BE\u2153-\u215E]
/* U+2200-U+2BFF has a lot of the various mathematical, etc. symbol ranges */
MISC_SYMBOL = [+%&~\^|\\¦\u00A7¨\u00A9\u00AC\u00AE¯\u00B0-\u00B3\u00B4-\u00BA\u00D7\u00F7\u0387\u05BE\u05C0\u05C3\u05C6\u05F3\u05F4\u0600-\u0603\u0606-\u060A\u060C\u0614\u061B\u061E\u066A\u066D\u0703-\u070D\u07F6\u07F7\u07F8\u0964\u0965\u0E4F\u1FBD\u2016\u2017\u2020-\u2023\u2030-\u2038\u203B\u203E-\u2042\u2044\u207A-\u207F\u208A-\u208E\u2100-\u214F\u2190-\u21FF\u2200-\u2BFF\u3012\u30FB\uFF01-\uFF0F\uFF1A-\uFF20\uFF3B-\uFF40\uFF5B-\uFF65\uFF65]
/* \uFF65 is Halfwidth katakana middle dot; \u30FB is Katakana middle dot */
/* Math and other symbols that stand alone: °²× ∀ */
// bullet chars: 2219, 00b7, 2022, 2024


/*---- TIMES ----*/

/*---- PERCENT ----*/

PERCENT = {NUMBER}\\s*(percent|por ciento|%)
PERCENT_NUMBER = %\\s*{NUMBER}

/*---- MONEY ----*/

DOLLAR = ([A-Z]*\$|#)
/* These are cent and pound sign, euro and euro, and Yen, Lira */
OTHER_CURRENCIES= [\u00A2\u00A3\u00A4\u00A5\u0080\u20A0\u20AC\u060B\u0E3F\u20A4\uFFE0\uFFE1\uFFE5\uFFE6]

////////////////
//// DATES ////
///////////////

/*---- NUMERIC DATES ----*/

NUMERIC_DATE = {DIGIT}{1,2}[\-\/]{DIGIT}{1,2}[\-\/]{DIGIT}{2,4}

/*---- WORD DATES ----*/

MONTH_DE = 
MONTH_EN = January|February|March|April|June|July|August|September|October|November|December
MONTH_ES = Enero|Febrero|Marzo|Abril|Mayo|Junio|Julio|Agosto|Septiembre|Octubre|Noviembre|Diciembre
MONTH_FR = 
MONTH_IT =
MONTH_NL =
MONTH = {MONTH_DE}|{MONTH_EN}|{MONTH_ES}|{MONTH_FR}|{MONTH_IT}|{MONTH_NL}

DAY_DE = 
DAY_EN = Monday|Tuesday|Wednesday|Thursday|Friday|Saturday|Sunday
DAY_ES = Lunes|Martes|Miércoles|Jueves|Viernes|Sábado|Domingo
DAY_FR = 
DAY_IT = 
DAY_NL =

WORD_DATE = {MONTH}|{DAY}

/*---- ABBREV_DATES ----*/

ABBREV_MONTH_DE = Jän|März|Mai|Okt|Dez
ABBREV_MONTH_EN = Jan|Feb|Mar|Apr|Jun|Jul|Aug|Sep|Sept|Oct|Nov|Dec
ABBREV_MONTH_ES = Ene|Febr|May|Abr|Ag|Dic
ABBREV_MONTH_FR = janv|févr|mars|avril|juin|juil|août|déc
ABBREV_MONTH_IT = genn|febbr|magg|giugno|luglio|sett|ott
ABBREV_MONTH_NL = maart|mei|juni|juli|okt
ABBREV_MONTH = {ABBREV_MONTH_DE}|{ABBREV_MONTH_EN}|{ABBREV_MONTH_ES}|{ABBREV_MONTH_FR}|{ABBREV_MONTH_IT}|{ABBREV_MONTH_NL}


ABBREV_DAY_DE = So|Mo|Di|Mi|Do|Fr|Sa
ABBREV_DAY_EN = Mon|Tue|Tues|Wed|Thu|Thurs|Fri|Sat|Sun
ABBREV_DAY_ES = Lun|Mar|Miér|Jue|Vier|Sáb|Dom
ABBREV_DAY_FR = lun|mer|jeu|ven|sam|dim
ABBREV_DAY_IT = mar|gio|ven|sab
ABBREV_DAY_NL = ma|woe|vrij|za|zo|wo|vr 

ABBREV_DAY = {ABBREV_DAY_DE}|{ABBREV_DAY_EN}|{ABBREV_DAY_ES}|{ABBREV_DAY_FR}|{ABBREV_DAY_IT}|{ABBREV_DAY_NL}
ABBREV_DATE = ({ABBREV_MONTH}|{ABBREV_DAY}

DATE = {NUMERIC_DATE}|{WORD_DATE}|{ABBREV_DATE}

/* ------------------------Lexical Rules Section---------------------- */

%%


/*---- DATES ----*/

{DATE}                      { String txt = yytext();
                                return makeToken(txt, "DATE");
                            }

/*---- MONEY ----*/

{DOLLAR}                    { return makeToken(); }
{OTHER_CURRENCIES}          {   if (normalizeCurrency) {
			        				String normString = normalizeCurrency(yytext());
			        				return makeToken(normString);
                                } 
                                else {
                                return makeToken();
                                }
                            }
                          
                            
/* -------- NON BREAKING PREFIXES ------------*/

/* Any acronym can be treated as sentence final iff followed by this list 
* of words (pronouns, determiners, and prepositions, etc.). "U.S." is the single 
* big source of errors.  Character classes make this rule case sensitive! (This is needed!!) 
*/
{ACRONYMS}/({SPACENLS})({ACRO_NEXT_WORD}){SPACENL} {
                          // try to work around an apparent jflex bug where it
                          // gets a space at the token end by getting
                          // wrong the length of the trailing context.
                          while (yylength() > 0) {
                            char last = yycharat(yylength()-1);
                            if (last == ' ' || last == '\t' || (last >= '\n' && last <= '\r' || last == '\u0085')) {
                              yypushback(1);
                            } else {
                              break;
                            }
                          }
                          String s;
                          if (sptb3Normalize && ! "U.S.".equals(yytext())) {
                            yypushback(1); // return a period for next time
                            s = yytext();
                          } else {
                            s = yytext();
                            yypushback(1); // return a period for next time
                          }
                          return makeToken(s);
                        }

/* Special case to get ca., fig. or Prop. before numbers */
{ABBREV_NUMBER}/{SPACENL}?[:digit:]   {
                          // try to work around an apparent jflex bug where it
                          // gets a space at the token end by getting
                          // wrong the length of the trailing context.
                          while (yylength() > 0) {
                            char last = yycharat(yylength()-1);
                            if (last == ' ' || last == '\t' || (last >= '\n' && last <= '\r' || last == '\u0085')) {
                              yypushback(1);
                            } else {
                              break;
                            }
                          }
			  				return makeToken();
						}
						
/* Special case to get pty. ltd. or pty limited. 
 * Also added "Co." since someone complained, but usually a comma after it. 
 */
(pt[eyEY]|co)\./{SPACE}(ltd|lim)  { return makeToken(); }

{ABBREV_DATES}/{SENTEND}   {    String s;
                          	if (sptb3Normalize && ! "U.S.".equals(yytext())) {
                                yypushback(1); // return a period for next time
                                s = yytext();
                                } 
                                else {
                                    s = yytext();
                                    yypushback(1); // return a period for next time
                                }
                          	return makeToken(s); 
                            }


{ABBREV_DATES}/[^][^]        { return makeToken(); }

/* this one should only match at the end of file
 * since the previous one matches even newlines
*/
{ABBREV_DATES}               { String s;
                              if (sptb3Normalize && ! "U.S.".equals(yytext())) {
                              yypushback(1); // return a period for next time
                              s = yytext();
                              } else {
                                s = yytext();
                                yypushback(1); // return a period for next time
                              }
                              return makeToken(s);
                             }

{SPECIAL_ABBREV_PREFIX}     { return makeToken(); }
{ABBREV_UPPER}/{SPACE}      { return makeToken(); }

{ACRONYM}/{SPACENL}         { return makeToken(); }
{APOS_DIGIT_DIGIT}/{SPACENL} { return makeToken(); }

{WORD}\./{INTRA_SENT_PUNCT} {   String origTxt = yytext();
                                String normString = normalizeSoftHyphen(origTxt);
				return makeToken(normString); 
                            }

{PHONE}                 	{ String txt = yytext();
                          		if (normalizeSpace) {
                            	txt = txt.replace(' ', '\u00A0'); // change space to non-breaking space
                          	}
                          		if (normalizeBrackets) {
                            		txt = LEFT_PAREN_PATTERN.matcher(txt).replaceAll(openRB);
                            		txt = RIGHT_PAREN_PATTERN.matcher(txt).replaceAll(closeRB);
                          		}
                          	return makeToken(txt);
                        	}

/*---- QUOTES ----*/ 
{DOUBLE_QUOTE}/[A-Za-z0-9$]  { return normalizeQuotes(yytext(), true); }
{DOUBLE_QUOTE}               { return normalizeQuotes(yytext(), false); }

{LESS_THAN}              	{ return makeToken("<"); }
{GREATER_THAN}           	{ return makeToken(">"); }

{SMILEY}/[^A-Za-z] 			{ 	String txt = yytext();
                  				if (normalizeBrackets) {
                    				txt = LEFT_PAREN_PATTERN.matcher(txt).replaceAll(openRB);
                    				txt = RIGHT_PAREN_PATTERN.matcher(txt).replaceAll(closeRB);
                  				}
                  				return makeToken(txt);
                			}
                			
{ASIANSMILEY}        		{ 	String txt = yytext();
                  				if (normalizeBrackets) {
                    			txt = LEFT_PAREN_PATTERN.matcher(txt).replaceAll(openRB);
                    			txt = RIGHT_PAREN_PATTERN.matcher(txt).replaceAll(closeRB);
                  				}
                  				return makeToken(txt);
                			}
                			
/*---- BRACKETS ----*/ 

\{                          {   if (normalizeOtherBrackets) {
                    	            return makeToken(openCB); 
                            }
                  	        else {
                    		    return makeToken();
                  			}
                	    	}
\}              		
			    			{   if (normalizeOtherBrackets) {
                    		        return makeToken(closeCB); 
                                    }
                  	    	    else {
                    		        return makeToken();
                  		    	}
                	    	}
                
\[                          {   if (normalizeOtherBrackets) {
                                    return makeToken(openSB); 
                                }
                                else {
                                    return makeToken();
                                }
                            }       
\]                          {   if (normalizeOtherBrackets) {
                                    return makeToken(closeSB); 
                                }
                                else {
                                    return makeToken();
                                }                    
                            }
\(                          {   if (normalizeBrackets) {
                                    return makeToken(openRB); 
                                }
                                else {
                                    return makeToken();
                                }
                            }
\)                          {   if (normalizeBrackets) {
                                    return makeToken(closeRB); 
                                }
                                else {
                                    return makeToken();
                                }
                            }

{HYPHENS}       			{ 	if (yylength() >= 3 && yylength() <= 4 && ptb3Dashes) {
                    			return makeToken(ptbDash);
                  				} else {
                    			return makeToken();
                  				}
                			}
                			
{LDOTS}         			{ return normalizeMultiDots(yytext()); }

{OTHER_PUNCT}      			{ return makeToken(); }
{ASTERISK}         			{ 	if (escapeForwardSlash) {
                    			return makeToken(escape(yytext(), '*')); }
                  				else {
                    			return makeToken();
                  				}
                			}

/*---- START and END Sentence ----*/
                			
{INTRA_SENT_PUNCT}       	{ return makeToken(); }
[?!]+          	 			{ return makeToken(); }

[.¡¿\u037E\u0589\u061F\u06D4\u0700-\u0702\u07FA\u3002]  { return makeToken(); }
=               			{ return makeToken(); }
\/              			{ 	if (escapeForwardSlash) {
                    			return makeToken(escape(yytext(), '/')); }
                  				else {
                    			return makeToken();
                  				}
                			}

/*---- OTHER NONBREAKING WORDS with ACRONYMS and HYPHENS ----*/                			
{WORD_AMP}\./{INTRA_SENT_PUNCT}	{ return makeToken(normalizeSoftHyphen(yytext())); }
{WORD_AMP}        			{ return makeToken(normalizeSoftHyphen(yytext())); }
{OTHER_HYPHEN_WORDS}\./{INTRA_SENT_PUNCT}	{ return makeToken(); }
{OTHER_HYPHEN_WORDS}        { return makeToken(); }
{WORD_HYPHEN_ACRONYM}\./{INTRA_SENT_PUNCT}	{ return normalizeAmpNext(); }
{WORD_HYPHEN_ACRONYM}       { return normalizeAmpNext(); }

/*---- QUOTES ----*/
/* invert quote - often but not always right */
'/[A-Za-z][^ \t\n\r\u00A0] 	{ return normalizeQuotes(yytext(), true); }
                                         
{APOS_AUX}        			{   return normalizeQuotes(yytext(), false); }
{QUOTES}        			{   return normalizeQuotes(yytext(), false); }
{FAKEDUCKFEET}  			{   return makeToken(); }
{MISC_SYMBOL}    			{   return makeToken(); }

{PARAGRAPH}                             {   if (tokenizeParagraphs) { 
                                                return makeToken(PARAGRAPH_TOKEN);
                                            }
                                        } 

{NEWLINE}      				{   if (tokenizeNLs) {
                      			        return makeToken(NEWLINE_TOKEN); 
                			    }
                			}							

/*---- skip non printable characters ----*/

[\\x00-\\x19]|{SPACES}		{ }

/*---- warn about other non tokenized characters ----*/

.       { String str = yytext();
          int first = str.charAt(0);
          String msg = String.format("Untokenizable: %s (U+%s, decimal: %s)", yytext(), Integer.toHexString(first).toUpperCase(), Integer.toString(first));
          switch (untokenizable) {
            case NONE_DELETE:
              break;
            case FIRST_DELETE:
              if ( ! this.seenUntokenizableCharacter) {
                LOGGER.warning(msg);
                this.seenUntokenizableCharacter = true;
              }
              break;
            case ALL_DELETE:
              LOGGER.warning(msg);
              this.seenUntokenizableCharacter = true;
              break;
            case NONE_KEEP:
              return makeToken();
            case FIRST_KEEP:
              if ( ! this.seenUntokenizableCharacter) {
                LOGGER.warning(msg);
                this.seenUntokenizableCharacter = true;
              }
              return makeToken();
            case ALL_KEEP:
              LOGGER.warning(msg);
              this.seenUntokenizableCharacter = true;
              return makeToken();
          }
        }
<<EOF>> 					{ return null; }

/*skip everything else*/
/*.|\n 			{ } */


