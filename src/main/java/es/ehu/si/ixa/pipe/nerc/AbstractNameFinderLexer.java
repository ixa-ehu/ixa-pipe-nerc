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
  
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

  /**
   * An abstract tokenizer which implements Tokenizer interface. It also provides 
   * a <code>getToken()</code> function which does need to be implemented/override by any 
   * Tokenizer extending the @link AbstractTokenizer. By implementing the <code>getToken()</code>
   * method implementations specify actual behaviour of the functions provided 
   * by this Tokenizer, which makes it easier to actually implement new Tokenizers;
   * 
   * ixa-pipe-tok provides an example in the @link JFlexLexerTokenizer where an 
   * implementation of <code>getToken()</code> makes the <code>next()</code> 
   * function behave like the <code>yylex()</code> function provided by
   *  @link JFlexLexer. It also specifies the behaviour of the other functions of 
   * this Tokenizer. 
   * 
   * Null tokens not allowed, because null is used in the protected nextToken field to state that no more
   * tokens are available.
   *
   * @author ragerri
   * @version 2013-18-12
   */

  public abstract class AbstractNameFinderLexer<T> implements NameFinderLexer<T> {

    protected T nextToken; // = null;

    /**
     *
     * @return the next token or null if no token exists.
     */
    protected abstract T getToken();

    /**
     * Returns the next token from this Tokenizer by calling getToken()
     *
     * @return the next token
     * @throws java.util.NoSuchElementException
     *          if not tokens are available
     */
    public T next() {
      if (nextToken == null) {
        nextToken = getToken();
      }
      T result = nextToken;
      nextToken = null;
      if (result == null) {
        throw new NoSuchElementException();
      }
      return result;
    }

    /**
     * Uses getToken() to 
     * returns <code>true</code> if this Tokenizer has more tokens
     * 
     */
    public boolean hasNext() {
      if (nextToken == null) {
        nextToken = getToken();
      }
      return nextToken != null;
    }

    /**
     * This is an optional operation, by default not supported.
     */
    public void remove() {
      throw new UnsupportedOperationException();
    }

    /**
     * This is an optional operation, by default supported.
     *
     * @return The next token according to getToken()
     * @throws java.util.NoSuchElementException
     *          if the token stream has no more tokens.
     */
    public T lookAhead() {
      if (nextToken == null) {
        nextToken = getToken();
      }
      if (nextToken == null) {
        throw new NoSuchElementException();
      }
      return nextToken;
    }

    /**
     * Returns text as a List of tokens.
     *
     * @return A list of all tokens remaining in the underlying Reader
     */
    public List<T> tokenize() {
      final long start = System.nanoTime();
      List<T> result = new ArrayList<T>();
      while (hasNext()) {
        result.add(next());
      }
      final long duration = System.nanoTime() - start;
      final double toksPerSecond = (double) result.size() / ((double) duration / 1000000000.0);
      System.err.printf("ixa-pipe-tok tokenized %d tokens at %.2f tokens per second.%n", result.size(), toksPerSecond);
      return result;
    }


}
