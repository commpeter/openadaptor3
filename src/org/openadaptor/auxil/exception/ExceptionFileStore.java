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

package org.openadaptor.auxil.exception;

import java.io.File;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;

public class ExceptionFileStore implements ExceptionStore {

  private static final Log log = LogFactory.getLog(ExceptionFileStore.class);
  
  private static final String LAST_ID_XML = "lastId.xml";
  
  
  File dir;
  File idfile;
  long lastId;
  
  public ExceptionFileStore(String dirname) {
    this.dir = new File(dirname);
    if (dir.exists() && ! dir.isDirectory()) {
      throw new RuntimeException(dirname + " is not a directory");
    }
    if (!dir.exists() && !dir.mkdir()) {
      throw new RuntimeException(dirname + " cannont be created");
    }
    idfile = new File(dir, LAST_ID_XML);
    if (idfile.exists()) {
      readLastId();
    } else {
      lastId = 0;
    }
  }
  
  private synchronized void readLastId() {
    SAXReader reader = new SAXReader();
    try {
      Document doc = reader.read(idfile);
      Node node = doc.selectSingleNode("id");
      lastId = Long.parseLong(node.getText());
    } catch (DocumentException e) {
      throw new RuntimeException("failed to read lastId", e);
    }
  }

  private synchronized String getNextId() {
    String id = String.valueOf(++lastId);
    try {
      XMLWriter writer = new XMLWriter(new FileWriter(idfile));
      Document doc = DocumentHelper.createDocument();
      doc.addElement("id").setText(id);
      writer.write(doc);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("failed to write lastId", e);
    }
    return id;
  }

  public String[] getAllIds() {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".xml") && !name.equals(idfile.getName());
      }
    });
    List ids = new ArrayList();
    for (int i = 0; i < files.length; i++) {
      ids.add(files[i].split("-")[0]);
    }
    Collections.sort(ids);
    Collections.reverse(ids);
    return (String[]) ids.toArray(new String[ids.size()]);
  }

  public String getExceptionForId(final String id) {
    return getExceptionDocument(id).asXML();
  }

  private Document getExceptionDocument(final String id) {
    try {
      SAXReader reader = new SAXReader();
      return reader.read(new File(dir, getFilename(id)));
    } catch (DocumentException e) {
      throw new RuntimeException("failed to exception " + e.getMessage(), e);
    }
  }

  private String getFilename(final String id) {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.startsWith(id) && name.endsWith(".xml");
      }
    });
    if (files.length == 0) {
      throw new RuntimeException("id " + id + " not found");
    }
    return files[0];
  }

  public String store(String exception) {
    String id = getNextId();
    try {
      Document doc = DocumentHelper.parseText(exception);
      String from  = getText(doc, MessageExceptionXmlConverter.FROM_PATH, "retry");
      String time  = doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
          + "/" + MessageExceptionXmlConverter.TIME).getText();
      String filename = id + "-" + from + "-" + time + ".xml";
      save(doc, filename);
    } catch (Exception e) {
      log.error("failed to store exception " + exception);
      throw new RuntimeException("failed to store exception", e);
    }
    return id;
  }

  private void save(Document doc, String filename) {
    try {
      XMLWriter writer = new XMLWriter(new FileWriter(new File(dir, filename)));
      writer.write(doc);
      writer.close();
    } catch (IOException e) {
      throw new RuntimeException("failed to save file, " + e.getMessage(), e);
    }
  }

  public void incrementRetry(String id) {
    String filename = getFilename(id);
    Document doc = getExceptionDocument(id);
    Node node = doc.selectSingleNode(MessageExceptionXmlConverter.RETRIES_PATH);
    int retries = Integer.parseInt(node.getText());
    node.setText(String.valueOf(retries+1));
    save(doc, filename);
  }

  public void delete(final String id) {
    File file = new File(dir, getFilename(id));
    file.delete();
  }

  public void deleteAll() {
    String[] files = dir.list(new FilenameFilter() {
      public boolean accept(File dir, String name) {
        return name.endsWith(".xml") && !name.equals(idfile.getName());
      }
    });
    for (int i = 0; i < files.length; i++) {
      File file = new File(dir, files[i]);
      file.delete();
    }
  }

  public ExceptionSummary getExceptionSummary(String id) {
    Document doc = getExceptionDocument(id);
    ExceptionSummary summary = new ExceptionSummary();
    summary.setId(id);
    summary.setMessage(getText(doc, MessageExceptionXmlConverter.MESSAGE_PATH, ""));
    summary.setFrom(getText(doc, MessageExceptionXmlConverter.FROM_PATH, ""));
    summary.setDate(new Date(getLong(doc, MessageExceptionXmlConverter.TIME_PATH, 0)));
    summary.setRetryAddress(getText(doc, MessageExceptionXmlConverter.RETRY_ADDRESS_PATH, ""));
    summary.setComponentId(getText(doc, MessageExceptionXmlConverter.COMPONENT_PATH, ""));
    summary.setRetries(getInt(doc, MessageExceptionXmlConverter.RETRIES_PATH, 0));
    return summary;
  }

  private String getText(Document doc, String path, String defaultValue) {
    try {
      Node n = doc.selectSingleNode(path);
      return n.getText();
    } catch (RuntimeException e) {
      return defaultValue;
    }
  }
  
  private long getLong(Document doc, String path, long defaultValue) {
    try {
      Node n = doc.selectSingleNode(path);
      return Long.parseLong(n.getText());
    } catch (RuntimeException e) {
      return defaultValue;
    }
  }
  
  private int getInt(Document doc, String path, int defaultValue) {
    try {
      Node n = doc.selectSingleNode(path);
      return Integer.parseInt(n.getText());
    } catch (RuntimeException e) {
      return defaultValue;
    }
  }
  
  public String getDataForId(String id) {
    Document doc = getExceptionDocument(id);
    return doc.selectSingleNode("//" + MessageExceptionXmlConverter.MESSAGE_EXCEPTION 
        + "/" + MessageExceptionXmlConverter.DATA).getText();
  }

}
