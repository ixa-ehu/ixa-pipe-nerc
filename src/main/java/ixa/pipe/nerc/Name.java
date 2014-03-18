/*
 *Copyright 2014 Rodrigo Agerri

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
package ixa.pipe.nerc;

import opennlp.tools.util.Span;


/**
 * A <code>Name</code> object contains a single String, a {@link Span}, a startOffset and 
 * the length of the String. These attributes are set or returned
 * in response to requests.
 * 
 * @author ragerri
 * @version 2013-03-12
 * 
 */

public class Name {

  private String str;
  private Span neSpan;
  private String type;

  /**
   * Start position of the <code>Name</code> in the original input string
   */
  private int startOffset = -1;

  /**
   * Length of the Name in the original input string
   */
  private int nameLength = -1;
  
  /**
   * Create a new <code>Name</code> with a null content (i.e., str).
   */
  public Name() {
  }

  /**
   * Create a new <code>Name</code> with the given string
   * 
   * @param str The new label's content
   * @param type
   */
  public Name(String str, String type) {
    this.str = str;
    this.type = type.toUpperCase();
  }
  
  /**
   * Create a new <code>Name</code> with the given string and Span
   * 
   * @param str The new label's content
   * @param type
   * @param neSpan the span of the Name
   */
  public Name(String str, String type, Span neSpan) { 
    this.str = str;
    this.type = type.toUpperCase();
    this.neSpan = neSpan;
  }

  /**
   * Creates a new <code>Name</code> with the given content.
   * 
   * @param str
   *          The new label's content
   * @param type
   * @param startOffset
   *          Start offset in original text
   * @param nameLength
   *          End offset in original text
   */
  public Name(String str, String type, int startOffset, int nameLength) {
    this.str = str;
    this.type = type.toUpperCase();
    setStartOffset(startOffset);
    setNameLength(nameLength);
  }
  
  /**
   * Creates a new <code>Name</code> with the given content.
   * 
   * @param str
   *          The new label's content
   * @param type 
   * @param neSpan the name span
   * @param startOffset
   *          Start offset in original text
   * @param nameLength
   *          End offset in original text
   */
  public Name(String str, String type, Span neSpan, int startOffset, int nameLength) {
    this.str = str;
    this.type = type.toUpperCase();
    this.neSpan = neSpan;
    setStartOffset(startOffset);
    setNameLength(nameLength);
  }

  /**
   * Return the word value of the label (or null if none).
   * 
   * @return String the word value for the label
   */
  public String value() {
    return str;
  }
  
  /**
   * Return the type of the Name
   * 
   * @return
   */
  public String getType() { 
    return type;
  }
  
  /**
   * Return the Span (or null if none).
   * 
   * @return the Span
   */
  public Span getSpan() {
    return neSpan;
  }
  

  /**
   * Set the value for the label.
   * 
   * @param value
   *          The value for the label
   */
  public void setValue(final String value) {
    str = value;
  }
  
  /**
   * Set type of the Name
   * @param neType
   */
  public void setType(final String neType) { 
    type = neType.toUpperCase();
  }
  
  /**
   * Set the Span for the Name
   * 
   * @param neSpan 
   */
  public void setSpan(final Span span) {
    neSpan = span;
  }

  /**
   * Set the label from a String.
   * 
   * @param str
   *          The str for the label
   */
  public void setFromString(final String str) {
    this.str = str;
  }

  @Override
  public String toString() {
    return str;
  }

  public int startOffset() {
    return startOffset;
  }

  public int nameLength() {
    return nameLength;
  }

  public void setStartOffset(int beginPosition) {
    this.startOffset = beginPosition;
  }

  public void setNameLength(int nameLength) {
    this.nameLength = nameLength;
  }
 
}
