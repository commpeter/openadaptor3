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
package org.openadaptor.auxil.processor.jndi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.NamingEnumeration;

import org.jmock.Mock;
import org.jmock.cglib.MockObjectTestCase;
import org.openadaptor.auxil.connector.jndi.JNDIReadConnector;
import org.openadaptor.auxil.connector.jndi.JNDISearch;
import org.openadaptor.auxil.orderedmap.IOrderedMap;
import org.openadaptor.core.exception.OAException;
import org.openadaptor.core.exception.RecordException;

import  org.openadaptor.auxil.orderedmap.OrderedHashMap;

/**
 * Unit tests for {@link JNDIEnhancementProcessor}.
 * 
 * @author Kris Lachor
 */
public class JNDIEnhancementProcessorTestCase extends MockObjectTestCase {

  JNDIEnhancementProcessor processor = new JNDIEnhancementProcessor();
  
  JNDIReadConnector mockReadConnector = new MockJNDIReadConnector();
  
  String recordKeyUsedAsSearchBase = null;
  
  String configDefinedSearchFilter = null;
  
  String recordKeySetByExistence = "testRecordKeySetByExistence";
  
  Mock mockJNDISearch;
  
  Mock mockNamingEnumeration;
  
  Map incomingMap = new HashMap();
  {
    incomingMap.put("incomingMapKey1", "incomingMapValue1");
    incomingMap.put("incomingMapKey2", "incomingMapValue2");
  }
  
  Map outgoingMap = new HashMap();
  {
    outgoingMap.put("outgoingMapKey1", "outgoingMapValue1");
    outgoingMap.put("outgoingMapKey2", "outgoingMapValue2");
  }
  
  
  /**
   * @see junit.framework.TestCase#setUp()
   */
  protected void setUp() throws Exception {
    super.setUp();
    processor.setReader(mockReadConnector);
    mockNamingEnumeration = new Mock(NamingEnumeration.class);
  }

  
  private void setValidateExpectations(){
    mockJNDISearch.expects(once()).method("getSearchBases");
    mockJNDISearch.expects(atLeastOnce()).method("getFilter");
    mockJNDISearch.expects(once()).method("getAttributes");
    mockJNDISearch.expects(once()).method("setAttributes");
  }

  private void validate(){
    setValidateExpectations();
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.isEmpty());
  }
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * 
   * @TODO business logic outside of property checks implemented in #validate
   * 
   */
  public void testValidate1() {
    processor.setRecordKeyUsedAsSearchBase("foo1");
    processor.setRecordKeySetByExistence(recordKeySetByExistence);
    processor.setIncomingMap(incomingMap);
    validate();
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * Missing (incomingMap and recordKeyUsedAsSearchBase), and missing (outgoingMap and recordKeySetByExistence)
   * 
   * @TODO business logic outside of property checks implemented in #validate
   * 
   */
  public void testValidate2() {
    setValidateExpectations();
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 3);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * Missing (outgoingMap and recordKeySetByExistence), incomingMap and fiter
   * 
   */
  public void testValidate3() {
    setValidateExpectations();
    processor.setRecordKeyUsedAsSearchBase("foo1");
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 2);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#validate
   * (java.util.List)}.
   * Missing (incomingMap and filter), and missing (outgoingMap and recordKeySetByExistence)
   */
  public void testValidate4() {
    setValidateExpectations();
    processor.setRecordKeyUsedAsSearchBase("foo1");
    processor.setRecordKeySetByExistence(recordKeySetByExistence);
    List exceptions = new ArrayList();
    processor.validate(exceptions);  
    assertTrue(exceptions.size() == 1);
  }

  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#processOrderedMap
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * No search filter set (all of: recordKeyUsedAsSearchBase, configDefinedSearchFilter, incomingMap are empty).
   */
  public void testProcessOrderedMap1() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    try{
      processor.processOrderedMap(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }

  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#processOrderedMap
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * Sets recordKeyUsedAsSearchBase property.
   * recordKeyUsedAsSearchBase not in the input map.
   */
  public void testProcessOrderedMap2() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    processor.setRecordKeyUsedAsSearchBase("test");
    try{
      processor.processOrderedMap(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#processOrderedMap
   * (org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * getTreatMultiValuedAttributesAsArray = false
   * Search returns no results.
   */
  public void testProcessOrderedMap3() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    processor.setIncomingMap(incomingMap);
    processor.setRecordKeyUsedAsSearchBase("foo1");
    processor.setRecordKeySetByExistence(recordKeySetByExistence);
    mockJNDISearch.expects(once()).method("setSearchBases").with(eq(new String[]{"bar1"}));
    mockJNDISearch.expects(once()).method("setFilter");
    mockJNDISearch.expects(once()).method("getTreatMultiValuedAttributesAsArray").will(returnValue(false));
    mockJNDISearch.expects(once()).method("getJoinArraysWithSeparator");
    mockJNDISearch.expects(once()).method("execute").will(returnValue(mockNamingEnumeration.proxy()));
    mockNamingEnumeration.expects(once()).method("hasMore").will(returnValue(false));
    validate();
    processor.processOrderedMap(map);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#
   * tailorSearchToThisRecord(org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * 'normal' flow
   */
  public void testTailorSearchToThisRecord1() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    
    processor.setIncomingMap(incomingMap);
    processor.setRecordKeyUsedAsSearchBase("foo1");
    processor.setRecordKeySetByExistence(recordKeySetByExistence);

    mockJNDISearch.expects(once()).method("setSearchBases").with(eq(new String[]{"bar1"}));
    mockJNDISearch.expects(once()).method("setFilter");
   
    validate();
    processor.tailorSearchToThisRecord(map);
  }
  
  
  /**
   * Test method for {@link org.openadaptor.auxil.processor.jndi.JNDIEnhancementProcessor#
   * tailorSearchToThisRecord(org.openadaptor.auxil.orderedmap.IOrderedMap)}.
   * 
   * recordKeyUsedAsSearchBase not occuring in input iorderedmap
   */
  public void testTailorSearchToThisRecord2() {
    IOrderedMap map = new OrderedHashMap();
    map.put("foo1", "bar1");
    
    processor.setIncomingMap(incomingMap);
    processor.setRecordKeyUsedAsSearchBase("foo2");
    processor.setRecordKeySetByExistence(recordKeySetByExistence);
   
    validate();
    try{
      processor.tailorSearchToThisRecord(map);
    }catch(RecordException re){
      return;
    }
    assertTrue(false);
  }
  

  /**
   * Inner mock. 
   */
  class MockJNDIReadConnector extends JNDIReadConnector{
    
    public MockJNDIReadConnector() {

      mockJNDISearch = mock(JNDISearch.class, "mockJNDISearch");
      setSearch((JNDISearch)mockJNDISearch.proxy());
    }

    public void connect() {}
    
    public Object[] next(long timeoutMs) throws OAException {
       return null;
    }

    public JNDISearch getSearch() {
      return this.search;
    }        
  }
}