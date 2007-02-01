/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved. 
                                                                                     
 Permission is hereby granted, free of charge, to any person obtaining a             
 copy of this software and associated documentation files (the                       
 "Software"), to deal in the Software without restriction, including               
 without limitation the rights to use, copy, modify, merge, publish,                 
 distribute, sublicense, and/or sell copies of the Software, and to                  
 permit persons to whom the Software is furnished to do so, subject to               
 the following conditions:                                                           
                                                                                     
 The above copyright notice and this permission notice shall be included             
 in all copies or substantial portions of the Software.                              
                                                                                     
 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS           
 OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF                          
 MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND                               
 NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE              
 LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION              
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION               
 WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.                     
                                                                                     
 Nothing in this notice shall be deemed to grant any rights to                       
 trademarks, copyrights, patents, trade secrets or any other intellectual            
 property of the licensor or any contributor except as expressly stated              
 herein. No patent license is granted separate from the Software, for                
 code that you delete from the Software, or for combinations of the                  
 Software with other software or hardware.                                           
*/

package org.openadaptor.auxil.convertor.delimited;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.convertor.AbstractConvertor;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;
import org.openadaptor.core.exception.NullRecordException;
import org.openadaptor.core.exception.RecordException;
import org.openadaptor.core.exception.RecordFormatException;

/**
 * Simple Converter for delimited Strings
 * 
 * @author Eddy Higgins
 */
public abstract class AbstractDelimitedStringConvertor extends AbstractConvertor {

  private final static Log log = LogFactory.getLog(AbstractDelimitedStringConvertor.class);

  public static final String DEFAULT_DELIMITER = ",";

  // Internal state:
  protected boolean nextRecordContainsFieldNames = false;

  // Bean properties exposed only in appropriate subclasses:
  protected boolean stripEnclosingQuotes = false;

  protected boolean addNeededEnclosingQuotes = false;

  // Bean properties:
  protected String[] fieldNames;

  protected String delimiter = AbstractDelimitedStringConvertor.DEFAULT_DELIMITER;

  protected char quoteChar = '\"'; // actually used to hold a char.

  protected boolean firstRecordContainsFieldNames = false;

  protected AbstractDelimitedStringConvertor() {
  }

  protected AbstractDelimitedStringConvertor(String id) {
    super(id);
  }
  
  // BEGIN Bean getters/setters
  // Todo: Mutability of _fieldNames array is not managed.

  /**
   * @return list of the field names that
   */
  public String[] getFieldNames() {
    return fieldNames;
  }

  /**
   * Set the list of field names to use when converting delimited strings into OrderedMaps
   * 
   * @param fieldNames
   *          array of field names
   */
  public void setFieldNames(String[] fieldNames) {
    this.fieldNames = fieldNames;
  }

  /**
   * Assign fieldNames from a List - allows trivial Spring Bean construction.
   * 
   * @param fieldNameList
   *          list of field names
   */
  public void setFieldNames(List fieldNameList) {
    setFieldNames((String[]) fieldNameList.toArray(new String[fieldNameList.size()]));
  }

  /**
   * @return the character that will be used as a delimiter between fields
   */
  public String getDelimiter() {
    return delimiter;
  }

  /**
   * Optional: set the field delimiter character (defaults to comma).
   * 
   * If you want tab delimited then your Spring XML config would look like: <verbatim> <property name="delimiter"
   * value="&#x09;"/> <!-- Tab separated --> </verbatim>
   * 
   * @param delimiter
   */
  public void setDelimiter(String delimiter) {
    this.delimiter = delimiter;
  }

  /**
   * @param quoteChar
   *          set the character that will be used when wrapping fields in quotes
   */
  public void setQuoteChar(char quoteChar) {
    this.quoteChar = quoteChar;
  }

  /**
   * @return the character that will be used when wrapping fields in quotes
   */
  public char getQuoteChar() {
    return quoteChar;
  }

  /**
   * 
   * @param firstRecordContainsFieldNames
   */
  public void setFirstRecordContainsFieldNames(boolean firstRecordContainsFieldNames) {
    this.firstRecordContainsFieldNames = firstRecordContainsFieldNames;
    nextRecordContainsFieldNames = firstRecordContainsFieldNames;
  }

  /**
   * @return true if the first record is to be processed as a header record
   */
  public boolean getFirstRecordContainsFieldNames() {
    return firstRecordContainsFieldNames;
  }

  // END Bean getters/setters

  // BEGIN implementation IRecordProcessor interface

  /**
   * if the firstRecordContainsFieldNames flag is set then we also set the nextRecordContainsFieldNames flag
   */
  public void reset() {
    if (firstRecordContainsFieldNames)
      nextRecordContainsFieldNames = true;
  }

  // END implementation of IRecordProcessor interface

  /**
   * Takes the supplied delimited string and chops it via the <code>delimiter</code> character. Each field is then
   * added as an attribute to a map. The attribute name are generated either from the <code>fieldNames</code> list or
   * if this is not defined then automatically generated.
   * 
   * @param delimitedString
   *          the delimited string to process
   * 
   * @return a map containing an attribute for each field in the supplied string
   * 
   * @throws NullRecordException
   *           if the string passed is null
   * @throws RecordException
   *           if the incoming record does not match up with the structure expected
   */
  protected IOrderedMap convertDelimitedStringToOrderedMap(String delimitedString) {
    IOrderedMap map;
    if (delimitedString == null) {
      throw new NullRecordException("Null values not permitted");
    }

    String[] values = extractValues(delimitedString);
    int received = values.length;
    // If the delimited String has field names specified, then use them.
    // If insufficient fields are provied to match the names - throw an exception.
    // If too many fields are supplied, keep them, but give them auto-generated names.
    if (fieldNames != null) {
      int count = fieldNames.length;
      AbstractDelimitedStringConvertor.validateStructure(count, received);
      map = new OrderedHashMap();
      for (int i = 0; i < count; i++) {// Add the named ones
        map.put(fieldNames[i], values[i]);
      }
      for (int j = count; j < received; j++) { // Now add the nameless poor souls.
        map.add(values[j]);
      }
    } else { // don't care about names. Ain't got none.
      map = new OrderedHashMap(Arrays.asList(values));
    }
    return map;
  }

  /**
   * Takes the supplied delimited string and chops it via the <code>delimiter</code> character. Will strip quotes if
   * the <code>stripEnclosingQuotes</code> flag is set.
   * 
   * @param delimitedString
   *          the delimited string to process
   * 
   * @return an array of strings corresponding to the fields in the string supplied
   */
  protected String[] extractValues(String delimitedString) {
    String[] values = delimitedString.split(delimiter);
    if (stripEnclosingQuotes)
      stripEnclosingQuotes(values);

    return values;
  }

  /**
   * Convert an ordered map into a delimited String. <p/>
   * 
   * If the <code>fieldNames</code> have been provided then use them to get the attribute values from the map. In this
   * case the order that the fields are output is based on the order of the <code>fieldNames</code>. <p/>
   * 
   * Otherwise, we output all the attributes from the map in the order that they are encountered by the map.get() call.
   * <p/>
   * 
   * Fields will be quoted if necessary (ie. if addNeededEnclosingQuotes is set).
   * 
   * @param map
   *          the record source
   * 
   * @return DelimitedString representing the ordered map
   * 
   * @throws NullRecordException
   *           if the map is null
   * @throws RecordFormatException
   *           if the field being quoted is not a CharSequence
   */
  protected String convertOrderedMapToDelimitedString(IOrderedMap map) throws RecordException {
    if (map == null)
      throw new NullRecordException("Null values not permitted");

    StringBuffer sb = new StringBuffer();
    int count = map.size();
    if (fieldNames != null) { // Then just output those fields.
      for (int i = 0; i < fieldNames.length; i++) {
        String fieldName = fieldNames[i];
        if (map.containsKey(fieldName))
          sb.append(addEnclosingQuotes(map.get(fieldName)));
        else
          log.warn("map does not contain expected key \'" + fieldName + "\'. value will be empty");

        if (i < fieldNames.length - 1)
          sb.append(delimiter);
      }
    } else { // Output every field found.
      for (int i = 0; i < count; i++) {
        sb.append(addEnclosingQuotes(map.get(i)));
        if (i < count - 1)
          sb.append(delimiter);
      }
    }
    return sb.toString();
  }

  /**
   * Convert an ordered map into a delimited String header (containing the names of the attributes rather than their
   * values). <p/>
   * 
   * If <code>fieldNames</code> has been set then we simply use them. Otherwise we take each attribute in the supplied
   * map and add its name to the list <p/>
   * 
   * Fields will be quoted if necessary (ie. if addNeededEnclosingQuotes is set).
   * 
   * @param map
   * 
   * @return DelimitedString representing the ordered map header
   * 
   * @throws NullRecordException
   *           if the map is null
   * @throws RecordFormatException
   *           if the field being quoted is not a CharSequence
   */
  protected String convertOrderedMapToDelimitedStringHeader(IOrderedMap map) throws RecordException {
    if (map == null)
      throw new NullRecordException("Null values not permitted");

    StringBuffer sb = new StringBuffer();
    if (fieldNames != null) { // Then just output those fields.
      for (int i = 0; i < fieldNames.length; i++) {
        if (i > 0)
          sb.append(delimiter);
        sb.append(addEnclosingQuotes(fieldNames[i]));
      }
    } else { // Output every field found.
      List keys = map.keys();
      for (int i = 0; i < keys.size(); i++) {
        if (i > 0)
          sb.append(delimiter);
        sb.append(addEnclosingQuotes(keys.get(i)));
      }
    }
    return sb.toString();
  }

  /**
   * Checks that the incoming record structure is as expected. If the number of fields received is greater than the
   * number expected then we write a warning to the logs saying that the extra fields will have auto-generated field
   * names.
   * 
   * @param expected
   *          number of fields expected
   * @param received
   *          number of fields received
   * 
   * @throws RecordFormatException
   *           if the number of expected fields is greater than the number received.
   */
  private static void validateStructure(int expected, int received) throws RecordException {
    if (expected > received) {
      throw new RecordFormatException("Expected " + expected + " fields, but received only " + received);
    }

    else if (received > expected)
      log.warn(received + " fields, received, but " + expected + " expected. Remainder will have automatic names");
  }

  /**
   * Loops through the array of strings and strips any enclosing quote chars from each element.
   * 
   * @param strings
   */
  private void stripEnclosingQuotes(String[] strings) {
    if (strings == null)
      return;

    for (int i = 0; i < strings.length; i++)
      strings[i] = stripEnclosingQuotes(strings[i]);
  }

  /**
   * If the supplied string starts AND ends with a <code>quoteChar</code> then they will be removed.
   * 
   * @param possiblyQuotedString
   * 
   * @return the "dequoted" string
   */
  private String stripEnclosingQuotes(String possiblyQuotedString) {
    String s = possiblyQuotedString;
    if (s == null)
      return s;

    int lastIndex = s.length() - 1;
    if ((lastIndex > 0) && (s.charAt(0) == quoteChar) && (s.charAt(lastIndex) == quoteChar))
      s = s.substring(1, lastIndex);

    return s;
  }

  /**
   * Wraps the field in <code>quoteChar</code>.
   * 
   * @param field
   * 
   * @return the quoted string
   * 
   * @throws RecordFormatException
   *           if the field is not a CharSequence
   */
  private Object addEnclosingQuotes(Object field) throws RecordFormatException {
    Object result = field;
    if (field != null && addNeededEnclosingQuotes) {
      if (field instanceof CharSequence) {
        String s = (field instanceof String) ? (String) field : field.toString();

        if (s.indexOf(delimiter) >= 0) {
          // It contains the delimiter and this bean has been told to insert quotes.
          if (s.indexOf(quoteChar) >= 0) {
            // Houston, we have a problem -- already contains embedded quote char
            log.warn("Field value already contains an embedded quote character, so not been quoted: " + s);
          } else {
            // Quote the field value
            StringBuffer sb = new StringBuffer(2 + s.length());
            sb.append(quoteChar).append(s).append(quoteChar);
            result = sb.toString();
          }
        }
      } else {
        throw new RecordFormatException("Field is not a CharSequence. Field class is " + field.getClass().getName()
            + " and field value is: " + field);
      }
    }
    return result;
  }
}