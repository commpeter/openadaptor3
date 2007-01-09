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

package org.oa3.auxil.connector.soap;

import javax.servlet.ServletException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.xfire.service.Service;
import org.codehaus.xfire.service.binding.ObjectServiceFactory;
import org.codehaus.xfire.service.invoker.BeanInvoker;
import org.codehaus.xfire.transport.http.XFireServlet;
import org.oa3.auxil.connector.http.JettyReadConnector;

/**
 * ReadConnector that exposes a webservice which allows external clients to send it data
 * @author perryj
 *
 */
public class ReadConnectorWebService extends JettyReadConnector implements IStringDataProcessor {

  private static final Log log = LogFactory.getLog(ReadConnectorWebService.class);

  private String serviceName = "openadaptorws";

  public ReadConnectorWebService() {
    super();
  }

  public ReadConnectorWebService(String id) {
    super(id);
  }
  
  public void setServiceName(final String name) {
    serviceName = name;
  }
  
  public void connect() {
    if (getServlet() == null) {
      setServlet(new StringDataProcessorServlet());
    }
    super.connect();
  }

  public void process(String s) {
    enqueue(s);
  }
  
  public String getEndpoint() {
    StringBuffer buffer = new StringBuffer();
    buffer.append("http://");
    buffer.append(getJettyHost());
    buffer.append(":");
    buffer.append(getJettyPort());
    buffer.append(getContext().equals("/") ? "" : getContext());
    buffer.append(getPath().replaceAll("\\*", ""));
    buffer.append(serviceName);
    buffer.append("?wsdl");
    return buffer.toString();
  }

  class StringDataProcessorServlet extends XFireServlet {

    private static final long serialVersionUID = 1L;

    public void init() throws ServletException
    {
      super.init();
      ObjectServiceFactory factory = new ObjectServiceFactory(getXFire().getTransportManager(), null);
      Service service = factory.create(IStringDataProcessor.class, serviceName, null, null);
      log.info(getId() + " created webservice " + getEndpoint());
      service.setInvoker(new BeanInvoker(ReadConnectorWebService.this));
      getController().getServiceRegistry().register(service);
    }
  }

}