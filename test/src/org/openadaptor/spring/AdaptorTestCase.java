/*
 * [[
 * Copyright (C) 2001 - 2006 The Software Conservancy as Trustee. All rights
 * reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a
 * copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be included
 * in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS
 * OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 *
 * Nothing in this notice shall be deemed to grant any rights to
 * trademarks, copyrights, patents, trade secrets or any other intellectual
 * property of the licensor or any contributor except as expressly stated
 * herein. No patent license is granted separate from the Software, for
 * code that you delete from the Software, or for combinations of the
 * Software with other software or hardware.
 * ]]
 */

package org.openadaptor.spring;

import org.openadaptor.spring.SpringApplication;
import org.openadaptor.util.ResourceUtil;

import junit.framework.TestCase;

public class AdaptorTestCase extends TestCase {

  public void testAdaptor1() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor1.xml"), null, "Adaptor");
  }
  
  public void testAdaptor1a() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor1a.xml"), null, "Adaptor");
  }
  
  public void testAdaptor2() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor2.xml"), null, "Adaptor");
  }
  
  public void testAdaptor2a() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor2a.xml"), null, "Adaptor");
  }
  
  public void testAdaptor3() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor3.xml"), null, "Adaptor");
  }
  
  public void testAdaptor3a() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor3a.xml"), null, "Adaptor");
  }
  
  public void testAdaptor4() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor4.xml"), null, "Adaptor");
  }

  public void testAdaptor4a() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor4a.xml"), null, "Adaptor");
  }

  public void testAdaptor5() {
    SpringApplication.runXml(ResourceUtil.getResourcePath(this, "adaptor5.xml"), null, "Adaptor");
  }
}