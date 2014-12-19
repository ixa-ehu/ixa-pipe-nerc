/*
 *  Copyright 2014 Rodrigo Agerri

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

package es.ehu.si.ixa.ixa.pipe.nerc.formats;

import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import opennlp.tools.namefind.NameSample;
import opennlp.tools.util.InputStreamFactory;
import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.ObjectStream;
import opennlp.tools.util.PlainTextByLineStream;
import opennlp.tools.util.Span;
import opennlp.tools.util.StringUtil;

/**
 * 4 fields tabulated format: word\tlemma\tpos\tner\n
 * 
 * @author ragerri
 * @version 2014-11-24
 * 
 */
public class TabulatedFormat implements ObjectStream<NameSample> {

  private final ObjectStream<String> lineStream;

  /**
   * Construct a Name Stream from a language and a {@code ObjectStream}.
   * 
   * @param lineStream
   */
  public TabulatedFormat(ObjectStream<String> lineStream) {
    this.lineStream = lineStream;
  }

  /**
   * Construct a Name Stream from a language and an input stream.
   * 
   * @param in
   *          an input stream to read data
   * @throws IOException
   *           the input stream exception
   */
  public TabulatedFormat(InputStreamFactory in) throws IOException {

    try {
      this.lineStream = new PlainTextByLineStream(in, "UTF-8");
      System.setOut(new PrintStream(System.out, true, "UTF-8"));
    } catch (UnsupportedEncodingException e) {
      // UTF-8 is available on all JVMs, will never happen
      throw new IllegalStateException(e);
    }
  }

  public NameSample read() throws IOException {

    List<String> tokens = new ArrayList<String>();
    List<String> neTypes = new ArrayList<String>();
    boolean isClearAdaptiveData = false;

    // Empty line indicates end of sentence
    String line;
    while ((line = lineStream.read()) != null && !StringUtil.isEmpty(line)) {
      isClearAdaptiveData = true;
      String fields[] = line.split("\t");
      if (fields.length == 2) {
        tokens.add(fields[0]);
        neTypes.add(fields[1]);
      } else {
        throw new IOException(
            "Expected four fields per line in training data, got "
                + fields.length + " for line '" + line + "'!");
      }
    }

    if (tokens.size() > 0) {
      // convert name tags into spans
      List<Span> names = new ArrayList<Span>();
      int beginIndex = -1;
      int endIndex = -1;
      for (int i = 0; i < neTypes.size(); i++) {
        String neTag = neTypes.get(i);
        if (neTag.startsWith("B-")) {
          if (beginIndex != -1) {
            names.add(extract(beginIndex, endIndex, neTypes.get(beginIndex)));
            beginIndex = -1;
            endIndex = -1;
          }
          beginIndex = i;
          endIndex = i + 1;
        } else if (neTag.startsWith("I-")) {
          endIndex++;
        } else if (neTag.equals("O")) {
          if (beginIndex != -1) {
            names.add(extract(beginIndex, endIndex, neTypes.get(beginIndex)));
            beginIndex = -1;
            endIndex = -1;
          }
        } else {
          throw new IOException("Invalid tag: " + neTag);
        }
      }
      // if one span remains, create it here
      if (beginIndex != -1)
        names.add(extract(beginIndex, endIndex, neTypes.get(beginIndex)));

      return new NameSample(tokens.toArray(new String[tokens.size()]),
          names.toArray(new Span[names.size()]), isClearAdaptiveData);
    } else if (line != null) {
      // Just filter out empty events, if two lines in a row are empty
      return read();
    } else {
      // source stream is not returning anymore lines
      return null;
    }
  }

  static final Span extract(int begin, int end, String beginTag)
      throws InvalidFormatException {

    String type = beginTag.substring(2);
    return new Span(begin, end, type);
  }

  public void reset() throws IOException, UnsupportedOperationException {
    lineStream.reset();
  }

  public void close() throws IOException {
    lineStream.close();
  }
}
