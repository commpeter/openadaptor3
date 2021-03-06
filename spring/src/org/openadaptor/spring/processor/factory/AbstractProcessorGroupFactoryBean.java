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

package org.openadaptor.spring.processor.factory;


import org.openadaptor.auxil.expression.ExpressionException;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ProcessingException;
import org.openadaptor.core.processor.ProcessorGroup;
import org.springframework.beans.factory.FactoryBean;

/**
 * Abstract superclass for openadaptor FactoryBeans that instantiate ProcessorGroups of IRecordProcessors. This class
 * is written to respond true to isSingleton. It caches the created Object (always a ProcessorGroup).
 * <p/>
 * WARNING: This is a Prototype and is subject to rapid change.
 */
public abstract class AbstractProcessorGroupFactoryBean extends Component implements FactoryBean {

  /**
   * Processor class that is primary element of group generated by this bean.
   */
  protected static final String CLASSNAME = ProcessorGroup.class.getName();

  /** Cache the generated ProcessorGroup so that the same one is always returned. */
  protected ProcessorGroup cachedObject = null;

  // FactoryBean implementation

  /**
   * Get the generated object creeatin it if necessary.
   *
   * @return  created ProcessorGroup.
   * @throws Exception
   */
  public Object getObject() throws Exception {
    if (cachedObject == null) {
      cachedObject = createObject();
    }
    return cachedObject;
  }

  /**
   * True if this FactoryBean represents a created singleton.
   * @return  True if creating a singleton.
   */
  public boolean isSingleton() {
    return true;
  }

  /**
   * Type of created Object.
   * @return Type of created Object.
   */
  public Class getObjectType() {
    return getObjectType(CLASSNAME);
  }

  // End FactoryBean implementation

  // Factory support methods

  /**
   * generate class from typename.
   * @param typeName String representing type.
   * @return Class of created object.
   */
  protected Class getObjectType(String typeName) {
    try {
      return Class.forName(typeName);
    } catch (ClassNotFoundException e) {
      throw new ProcessingException("Cannot find Class [" + typeName + "] in classpath.", this);
    }
  }

  /**
   * Instantiate the ProcessorGroup. This is the Object constructed by this factory. This Object
   * will be cached by the implementation of getObject()
   *
   * @return ProcessorGroup The instantiated object.
   */
  abstract protected ProcessorGroup createObject() throws IllegalAccessException, InstantiationException,
      ExpressionException;

  // End Factory support methods

}
