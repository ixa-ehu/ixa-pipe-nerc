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
package es.ehu.si.ixa.pipe.nerc.dict;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.SerializableArtifact;

import com.google.common.io.CharStreams;

/**
 * Dictionary class which creates a HashMap<String, String> from 
 * a tab separated file name\tclass\t.
 * 
 * @author ragerri
 * @version 2014-10-13
 * 
 */
public class Dictionary implements SerializableArtifact {

  public static class DictionarySerializer implements ArtifactSerializer<Dictionary> {

    public Dictionary create(InputStream in) throws IOException,
        InvalidFormatException {
      return new Dictionary(in);
    }

    public void serialize(Dictionary artifact, OutputStream out)
        throws IOException {
      artifact.serialize(out);
    }
  }
  
  private Map<String, String> dictionary = new HashMap<String, String>();

  public Dictionary(InputStream in) throws IOException {

    BufferedReader breader = new BufferedReader(new InputStreamReader(in, Charset.forName("UTF-8")));
    List<String> fileLines = CharStreams.readLines(breader);
    for (String line : fileLines) {
      String[] lineArray = line.split("\t");
      if (lineArray.length == 2) {
        dictionary.put(lineArray[0].toLowerCase(), lineArray[1]);
      }
    }
  }

  /**
   * Look up a string in the dictionary.
   * @param string the string to be searched
   * @return the string found
   */
  public String lookup(String string) {
    return dictionary.get(string);
  }
  
  /**
   * Get the <key,value> size of the dictionary.
   * @return maximum token count in the dictionary
   */
  public int getMaxTokenCount() {
    return dictionary.size();
  }
  
  /**
   * Get the Map<String, String> dictionary.
   * @return the dictionary map
   */
  public final Map<String, String> getDict() {
    return dictionary;
  }

  public void serialize(OutputStream out) throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(out));

    for (Map.Entry<String, String> entry : dictionary.entrySet()) {
      writer.write(entry.getKey() + " " + entry.getValue() + "\n");
    }
    writer.flush();
  }

  public Class<?> getArtifactSerializerClass() {
    return DictionarySerializer.class;
  }

}
