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


import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.IDataProcessor;
import org.openadaptor.util.JavascriptEngine.JavascriptResult;

public  class JavascriptProcessor implements IDataProcessor {

  private static final Log log = LogFactory.getLog(JavascriptProcessor.class);

  private JavascriptBinding javascript=new JavascriptBinding();
 
  // BEGIN Bean getters/setters
  public void setJavascriptBinding(JavascriptBinding javascript) {
    this.javascript=javascript;
  }
  public JavascriptBinding getJavascriptBinding() {
    return javascript;
  }

  public String getScript() {
    return javascript.getScript();
  }
  public void setScript(String script) {
    if (javascript==null) {
      javascript=new JavascriptBinding();
    }
    javascript.setScript(script);
  }

  // END Bean getters/setters

  public JavascriptProcessor() {
    super();
  }

  public Object[] process(Object input) {
    JavascriptResult jsResult=javascript.execute(input);
    return generateOutput(jsResult);
  }
  
  /**
   * Generate an output record array.
   * Default behaviour is just to wrap the outputRecord in an Object[].
   */
  protected Object[] generateOutput(JavascriptResult jsResult) {
    Object result=jsResult.executionResult;
    log.info("Result type:"+(result==null?"<null>":result.getClass().getName()));
    //log.info("Result was: "+context.toString(result));
    log.info("Result: "+result);
    return new Object[] {jsResult.outputRecord};
  }
  
  public void validate(List exceptions) {
  }

  public void reset(Object context) {
  }
 
}
