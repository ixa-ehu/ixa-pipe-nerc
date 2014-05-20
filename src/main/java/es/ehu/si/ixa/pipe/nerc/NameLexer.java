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

import java.util.Iterator;
import java.util.List;

/**
 * Tokenizers break up text into individual Objects. The decisions to specify
 * this interface are pragmatically based on the main Tokenizer implementation
 * provided by ixa-pipe-tok, namely, the @link IxaPipeTokenizer. That
 * implementation uses JFlex to create a scanner which recognizes certain
 * patterns in running text and creates @link Token objects. The default API of {@link
 * IxaPipeLexer} provides a <code>yylex()</code> method that behaves roughly like a
 * <code>next()</code> Iterator function.
 * 
 * Thus, Tokenizer implementations will probably implement and/or override the
 * <code>next()</code> function. For example @link IxaPipeTokenizer provides
 * an implementation of
 * <code>next()<code> that uses internally the <code>yylex()</code> function of @link
 * IxaPipeLexer to obtain the Token objects. Other implementations, e.g., a 
 * @link WhiteSpaceTokenizer, are also expected to implement the <code>next()</code> function.
 * 
 * The same reason (JFlex API) dictates that implementations of this interface
 * are expected to have a constructor takes a Reader as argument.
 * 
 * A Tokenizer extends the Iterator interface, but it also provides a lookahead
 * operation <code>lookAhead()</code>.
 * 
 * @author ragerri
 * @version 2013-18-12
 */
public interface NameLexer<T> extends Iterator<T> {

  /**
   * Returns the next token from this Tokenizer.
   * 
   * @return the next token
   * @throws java.util.NoSuchElementException
   *           if the are not any tokens.
   */
  public T next();

  /**
   * Returns <code>true</code> if and only if this Tokenizer has more elements.
   */
  public boolean hasNext();

  /**
   * Removes from last element returned by the iterator. This method can be
   * called only once per call to next.
   */
  public void remove();

  /**
   * Returns the next token, without removing it, from the Tokenizer, so that
   * the same token will be again returned on the next call to next() or
   * lookAhead(). This is useful for conditional decisions on sentence
   * boundaries, for example.
   * 
   * @return the next token
   * @throws java.util.NoSuchElementException
   *           if the token stream has no more tokens.
   */
  public T lookAhead();

  /**
   * Returns all tokens of this Tokenizer as a List
   * 
   * @return A list of all the tokens
   */
  public List<T> tokenize();

}
