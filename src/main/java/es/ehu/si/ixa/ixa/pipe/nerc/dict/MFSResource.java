/*
 *  Copyright 2015 Rodrigo Agerri

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

package es.ehu.si.ixa.ixa.pipe.nerc.dict;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import opennlp.tools.util.InvalidFormatException;
import opennlp.tools.util.model.ArtifactSerializer;
import opennlp.tools.util.model.SerializableArtifact;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.ListMultimap;

/**
 * @author ragerri
 * @version 2015-03-11
 * 
 */
public class MFSResource implements SerializableArtifact {

  private static final Pattern spacePattern = Pattern.compile("\t");
  
  public static class MFSResourceSerializer implements ArtifactSerializer<MFSResource> {

    public MFSResource create(InputStream in) throws IOException,
        InvalidFormatException {
      return new MFSResource(in);
    }

    public void serialize(MFSResource artifact, OutputStream out)
        throws IOException {
      artifact.serialize(out);
    }
  }
  
  /**
   * The dictionary for finding the MFS.
   */
  private ListMultimap<String, String> multiMap = ArrayListMultimap.create();
  
  /**
   * Build the MFS Dictionary.
   * @param in the input stream
   * @throws IOException the io exception
   */
  public MFSResource(InputStream in) throws IOException {
    BufferedReader breader = new BufferedReader(new InputStreamReader(
        in));
    String line;
    try {
      while ((line = breader.readLine()) != null) {
        String[] elems = spacePattern.split(line);
        multiMap.put(elems[0], elems[1]);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }
  
  /**
   * Look-up lemma#pos in dictionary.
   * @param word the word
   * @param postag the postag
   * @return the frequency#supersense list of values
   */
  public List<String> getMFS(String lemmaPOSClass) {
    List<String> mfsList = multiMap.get(lemmaPOSClass);
    return mfsList;
   }
  
  public void serialize(OutputStream out) throws IOException {
    Writer writer = new BufferedWriter(new OutputStreamWriter(out));

    for (Map.Entry<String, String> entry : multiMap.entries()) {
      writer.write(entry.getKey() + "\t" + entry.getValue() +"\n");
    }
    writer.flush();
  }

  public Class<?> getArtifactSerializerClass() {
    return MFSResourceSerializer.class;
  }

}



