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
package org.oa3.auxil.expression.function;

/*
 * File: $Header: /cvs/oa3/src/org/oa3/expression/function/NowFn.java,v 1.1 2006/11/02 14:19:58 higginse Exp $ Rev:
 * $Revision: 1.1 $ Created Oct 31 2006 by Eddy Higgins
 */
import java.util.Date;

/**
 * Generate a <code>java.util.Date</code> object representing the current date/time.
 * 
 * @author Eddy Higgins
 */
public class NowFn extends AbstractFunction {
  public static final String NAME = "now";

  // private static final Log log = LogFactory.getLog(NowFn.class);

  public NowFn() {
    super(NAME, 0);
  }

  /**
   * Generate a <code>java.util.Date</code> object representing the current date/time.
   * 
   * @param args
   *          Object[] which should be empty.
   * @return Date corresponding to the current date/time.
   */
  protected Object operate(Object[] args) {
    return new Date();
  }
}
