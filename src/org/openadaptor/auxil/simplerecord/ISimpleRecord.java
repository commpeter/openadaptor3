/*
 Copyright (C) 2001 - 2010 The Software Conservancy as Trustee. All rights reserved.

 Permission is hereby granted, free of charge, to any person obtaining a copy of
 this software and associated documentation files (the "Software"), to deal in the
 Software without restriction, including without limitation the rights to use, copy,
 modify, merge, publish, distribute, sublicense, and/or sell copies of the Software,
 and to permit persons to whom the Software is furnished to do so, subject to the
 following conditions:

 The above copyright notice and this permission notice shall be included in all 
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE
 SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.

 Nothing in this notice shall be deemed to grant any rights to trademarks, copyrights,
 patents, trade secrets or any other intellectual property of the licensor or any
 contributor except as expressly stated herein. No patent license is granted separate
 from the Software, for code that you delete from the Software, or for combinations
 of the Software with other software or hardware.
*/

package org.openadaptor.auxil.simplerecord;

import org.openadaptor.core.exception.RecordException;

/**
 * Common accessor interface for records.
 * <p>
 * This interface is intended to provide an implementation neutral mechanism to interact with record Objects. For
 * example, expression parsers (/evaluators) use this to read and set attributes from incoming records.
 * 
 * @author Eddy Higgins
 * @author Kevin Scully
 */
public interface ISimpleRecord extends Cloneable {
  /**
   * Fetch an attribute using the supplied key value.
   * <p>
   * The key value might, for example, specify:
   * <UL>
   * <LI>An attribute name </LI>
   * <LI>An XPath expression to locate the value within an XML document
   * </UL>
   * 
   * @param key
   *          Object which will be used to locate the required attribute
   * @return the value corresponding to the supplied key. <tt>Null</tt> if not found or if returned value is
   *         <tt>null</tt>
   * @throws RecordException
   *           if the operation cannot be performed.
   */
  public Object get(Object key) throws RecordException;

  /**
   * Store an attribute value using the supplied key value.
   * <p>
   * If the attribute already exists, then it's value should be overwritten with the supplied value.
   * <p>
   * If the attribute does not exist, then it should be added (if possible) to the record.
   * <p>
   * The key value might, for example, specify:
   * <UL>
   * <LI>An attribute name </LI>
   * <LI>An XPath-like expression to locate the value within an XML document
   * </UL>
   * 
   * @param key
   *          Object which will be used to locate the required attribute
   * @param value
   *          which is the object which is to be associated with the key.
   * @return the previous value corresponding to the supplied key, or <tt>null</tt> if no value was previously bound
   *         to the key.
   * @throws RecordException
   *           if the operation cannot be performed.
   */
  public Object put(Object key, Object value) throws RecordException;

  /**
   * Create a clone of this object.
   * 
   * @return clone of this object.
   */
  public Object clone();

  /**
   * Return the underlying record object which this accessor is fronting.
   * <p>
   * for <code>OrderedHashMap</code> implementations, it will return <tt>this</tt>.
   * 
   * @return underlying record object which this accessor is fronting.
   */
  public Object getRecord();

  /**
   * Clear the underlying Record.
   * <p>
   * Actual meaning depends on the underlying fronted implementation.
   */
  public void clear();

  /**
   * Remove (and return) the attribute from this <code>ISimpleRecord</code> which corresponds to the supplied key.
   * <p>
   * If the record does not contains any corresponding attribute, then <tt>null</tt> is returned. If the underlying
   * implementation is a <code>Map</code>, then it should behave exactly as Map.remove().
   * 
   * @param key
   *          The key associated with the attribute to be removed
   * @return The attribute associated with the key, or <code>null</code> if there was no value associated or that
   *         value was itself null.
   * @throws RecordException
   *           if the operation cannot be performed.
   */
  public Object remove(Object key) throws RecordException;

  /**
   * Returns <tt>true</tt> if this record contains a mapping for the specified key.
   * <p>
   * More formally, returns <tt>true</tt> if and only if this map contains at a mapping for a key k such that
   * (key==null ? k==null : key.equals(k)). (There can be at most one such mapping.)
   * 
   * @param key
   *          The key to locate within the Record
   * @return <tt>true</tt> if this map contains a mapping for the specified
   */
  public boolean containsKey(Object key);
}
