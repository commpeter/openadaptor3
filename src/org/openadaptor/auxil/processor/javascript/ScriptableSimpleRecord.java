/*
 Copyright (C) 2001 - 2007 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.auxil.processor.javascript;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.mozilla.javascript.*;
import org.openadaptor.auxil.simplerecord.ISimpleRecord;
import org.openadaptor.core.exception.RecordFormatException;
/**
 * This class provides a (Scriptable) wrapper around ISimpleRecord.
 * It permits reference to an ISimpleRecord from with javascript.
 * <br>
 * As part of the Rhino javascript mapping mechansim, methods which
 * are prefixec with jsFunction_ are exposed as methods within the
 * javascript environment. For example jsFunction_get(Object key) will
 * be available with the javascript as get(Object key).
 * <br>
 * In addition, it will clone() the wrapped simplerecord if the
 * operation being attempted is to modify the record. Thus
 * the original ISimpleRecord should never itself be modified.
 * <br>
 * See individual method signatures for more detail.
 * <br>
 * See the Rhino documentation for further details on the mappping.
 * @author higginse
 *
 */
public class ScriptableSimpleRecord extends ScriptableObject  {
  public static final long serialVersionUID = 0x00;
  private static final Log log = LogFactory.getLog(ScriptableSimpleRecord.class);
  public static final String CLASSNAME=ScriptableSimpleRecord.class.getName();
  private ISimpleRecord simpleRecord;
  private boolean modified=false;
  
  /**
   * zero-argument constructor used by Rhino runtime to create instances.
   * <br>
   * This is not indended for use by anything other than the Rhino environment.
   */
  public ScriptableSimpleRecord() {}

  // Method jsConstructor defines the JavaScript constructor
  /**
   * This corresponds roughly to the constructor for the 
   * ScriptableSimpleRecord. 
   * <br>
   * See the Rhino documentation for further detail on usage.
   */
  public void jsConstructor(Object simpleRecord) { 
    if (simpleRecord instanceof ISimpleRecord){
      this.simpleRecord=(ISimpleRecord)simpleRecord;
    }
    else {
      log.warn("Supplied record is not an ISimpleRecord instance, but "+simpleRecord.getClass().getName());
      throw new RecordFormatException("Only ISimpleRecord instances may be used within scripts");
    }
  }

  /**
   * Return the className of this class.
   * <br>
   * Required by Rhino.
   */
  public String getClassName() { 
    return CLASSNAME; 
    }

 /**
  * Mapping of ISimpleRecord.get(Object key).
  * @param key Map key for which a value is being retrieved.
  * @return Object the value for the key as defined by ISimpleRecord.get(Object key)
  */
  public Object jsFunction_get(Object key) {
    log.debug("get("+key+") invoked");
    Object value=simpleRecord.get(key);
    return value;
  }

  /**
   * Mapping of ISimpleRecord.put(Object key, Object value).
   * <br>
   * This differs from a trivial mapping in that it guarantees that
   * the original simple record has already cloned() first.
   * <br>
   * Note: Always forces flags as modified, even if new value = old value.
   */
  public void jsFunction_put(Object key,Object value) {
    log.debug("put("+key+","+value+") invoked");
    modify();
    simpleRecord.put(key, value);
  }
 
  /**
   * Convenience wrapper around jsFunction_put(Object key, Object value).
   * <br>
   * 
   */
  public void jsFunction_set(Object key,Object value) {
    jsFunction_put(key, value);
  }

  /**
   * Mapping of ISimpleRecord.remove(Object key).
   * <br>
   * This differs from a trivial mapping in that it guarantees that
   * the original simple record has already cloned() first.
   * <br>
   * Note: Always forces flags as modified, even if new value = old value.
   */
    public Object jsFunction_remove(Object key) {
    log.debug("remove("+key+") invoked");
    modify();
    return simpleRecord.remove(key);
  }

    /**
     * Mapping of ISimpleRecord.toString().
     */
  public String jsFunction_toString() {
    return simpleRecord.toString();
  }

  /**
   * Mapping of ISimpleRecord.containsKey(Object key).
   */
  public boolean js_Function_containsKey(Object key) {
    log.debug("containsKey("+key+") invoked");
    return simpleRecord.containsKey(key);
  }
  
  /**
   * Returns the underlying simpleRecord associated with 
   * this mapping. 
   * <br>
   * Note that it may be a (modified) clone of the original
   * record that this instance was created with.
   * @return
   */
  public ISimpleRecord getSimpleRecord() {
    return simpleRecord;
  }
  
  /**
   * This will clone the simpleRecord, if it hasn't already
   * been cloned.
   *
   */
  private void modify() {
    if (!modified) {
      log.debug("Modifying a simpleRecord - cloning original");
      simpleRecord=(ISimpleRecord)simpleRecord.clone();
      modified=true;
    }
  }
}
