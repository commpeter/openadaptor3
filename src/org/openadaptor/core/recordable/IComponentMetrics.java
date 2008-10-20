/*
 Copyright (C) 2001 - 2008 The Software Conservancy as Trustee. All rights reserved.

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

package org.openadaptor.core.recordable;

import org.openadaptor.core.lifecycle.ILifecycleListener;

/**
 * Represents a component that maintains runtime metrics about messages it
 * processed.
 * 
 * DRAFT. NOT READY FOR USE.
 * 
 * TODO change name to IComponentMetrics. IComponentMetrics will be a new interface,
 * with one method returning IComponentMetrics. All Nodes will be RecordableComponents.
 * 
 * TODO this interface should not need to extends from ILifcecleListener, it's got nothing 
 * to do with it. The implementation can implement ILifecycleListener though. 
 * 
 * @author Kris Lachor
 */
public interface IComponentMetrics extends ISimpleComponentMetrics, ILifecycleListener{
  
//	long [] getInputMsgCounts();
	
    String getInputMsgs();
    
//	String getProcessTimeMax();
	
//	String getProcessTimeMin();
	
	String getProcessTime();
    
//    String getIntervalTimeMax();
    
//    String getIntervalTimeMin();
    
    String getIntervalTime();

//	String [] getInputMsgTypes();
    
//    String [] getOutputMsgTypes();
    
    long getDiscardedMsgCount();
    
//    long getOutputMsgCount();
    
    long getExceptionMsgCount();
    
    String getUptime();
    
    boolean enabled();
    
    void enable();
    
    void disable();
}
