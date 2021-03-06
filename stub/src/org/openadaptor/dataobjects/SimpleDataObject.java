/*
 #* [[
 #* Copyright (C) 2000-2003 The Software Conservancy as Trustee. All rights
 #* reserved.
 #*
 #* Permission is hereby granted, free of charge, to any person obtaining a
 #* copy of this software and associated documentation files (the
 #* "Software"), to deal in the Software without restriction, including
 #* without limitation the rights to use, copy, modify, merge, publish,
 #* distribute, sublicense, and/or sell copies of the Software, and to
 #* permit persons to whom the Software is furnished to do so, subject to
 #* the following conditions:
 #*
 #* The above copyright notice and this permission notice shall be included
 #* in all copies or substantial portions of the Software.
 #*
 #* THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 #* OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 #* MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 #* NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 #* LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 #* OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 #* WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 #*
 #* Nothing in this notice shall be deemed to grant any rights to
 #* trademarks, copyrights, patents, trade secrets or any other intellectual
 #* property of the licensor or any contributor except as expressly stated
 #* herein. No patent license is granted separate from the Software, for
 #* code that you delete from the Software, or for combinations of the
 #* Software with other software or hardware.
 #* ]]
 */

package org.openadaptor.dataobjects;

import org.openadaptor.StubException;

/**
 * Stub for legacy openadaptor code so that legacy components can
 * be compiled and distributed
 */

public class SimpleDataObject implements DataObject {

  public SimpleDataObject(String newTypename){
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public SimpleDataObject(DOType type){
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

// Haven't needed SDOType so far.
//  public SimpleDataObject(SDOType type){
//    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
//  }

  public DataObject cloneEmpty() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public boolean equals(DataObject other) {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public Object getAttributeValue(String name) throws InvalidParameterException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public DOType getType() {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public boolean isNullValue(String name) throws InvalidParameterException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

  public void setAttributeValue(String name, Object value) throws InvalidParameterException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }
  
  public void addAttributeValue(String attrName, Object value) throws InvalidParameterException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }
  public void addAttributeValue(String attrName, Object value, DOType valueType) throws InvalidParameterException {
    throw new StubException(StubException.WARN_LEGACY_OA_JAR);
  }

}
