package org.openadaptor.auxil.connector.jdbc.writer.deprecated;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import junit.framework.TestCase;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jdbc.JDBCConnection;
import org.openadaptor.auxil.connector.jdbc.reader.xml.ResultSetToXMLConverter;
import org.openadaptor.auxil.connector.jdbc.writer.deprecated.JDBCWriteConnector;
import org.openadaptor.auxil.connector.jdbc.writer.deprecated.orderedmap.SQLStatementConverter;
import org.openadaptor.auxil.connector.jdbc.writer.deprecated.orderedmap.TableStatementConverter;
import org.openadaptor.auxil.orderedmap.OrderedHashMap;

public class JDBCWriteConnectorTestCase extends TestCase {

  private static final Log log = LogFactory.getLog(JDBCWriteConnectorTestCase.class);
  
  private static final String DB_DRIVER="org.hsqldb.jdbcDriver";
  private static final String DB_URL="jdbc:hsqldb:mem:test";
  private static final String DB_USER="sa";
  private static final String DB_PASSWORD="";

  static String SCHEMA = "CREATE MEMORY TABLE TRADE(TRADEID INTEGER NOT NULL,BUYSELL CHAR(1) NOT NULL,SECID INTEGER NOT NULL,PARTYID INTEGER NOT NULL,QTY INTEGER NOT NULL,PRICE FLOAT NOT NULL)";
  static String[] ROWS = {
    "INSERT INTO TRADE VALUES(1,'B',1,1,1000000,3.25)",
    "INSERT INTO TRADE VALUES(2,'B',1,1,500000,3.21)",
    "INSERT INTO TRADE VALUES(3,'S',2,1,250000,1.01)",
    "INSERT INTO TRADE VALUES(4,'B',2,1,1000000,0.99)",
    "INSERT INTO TRADE VALUES(5,'S',1,1,1000000,3.26)",
  };

  static OrderedHashMap[] maps = { 
    new OrderedHashMap(), 
    new OrderedHashMap(), 
    new OrderedHashMap(), 
    new OrderedHashMap(), 
    new OrderedHashMap(), 
  };
  
  static {
    maps[0].put("TRADEID", new Integer(1));
    maps[1].put("TRADEID", new Integer(2));
    maps[2].put("TRADEID", new Integer(3));
    maps[3].put("TRADEID", new Integer(4));
    maps[4].put("TRADEID", new Integer(5));
    
    maps[0].put("BUYSELL", "B");
    maps[1].put("BUYSELL", "B");
    maps[2].put("BUYSELL", "S");
    maps[3].put("BUYSELL", "B");
    maps[4].put("BUYSELL", "S");
    
    maps[0].put("SECID", new Integer(1));
    maps[1].put("SECID", new Integer(1));
    maps[2].put("SECID", new Integer(2));
    maps[3].put("SECID", new Integer(2));
    maps[4].put("SECID", new Integer(1));
    
    maps[0].put("PARTYID", new Integer(1));
    maps[1].put("PARTYID", new Integer(1));
    maps[2].put("PARTYID", new Integer(1));
    maps[3].put("PARTYID", new Integer(1));
    maps[4].put("PARTYID", new Integer(1));
    
    maps[0].put("QTY", new Integer(1000000));
    maps[1].put("QTY", new Integer(500000));
    maps[2].put("QTY", new Integer(250000));
    maps[3].put("QTY", new Integer(1000000));
    maps[4].put("QTY", new Integer(1000000));
    
    maps[0].put("PRICE", new Double(3.25));
    maps[1].put("PRICE", new Double(3.21));
    maps[2].put("PRICE", new Double(1.01));
    maps[3].put("PRICE", new Double(0.99));
    maps[4].put("PRICE", new Double(3.26));
  }
  
  protected JDBCConnection jdbcConnection;

  private Object[] expectedEndState;

  protected void setUp() throws Exception {
    super.setUp();
    
    // create connection, this automatically starts up hsql in memory db
    // when the connection is made
    // the shutdown property ensures that the server is shutdown when
    // the connection is closed
    
    jdbcConnection = new JDBCConnection();
    jdbcConnection.setDriver(DB_DRIVER);
    jdbcConnection.setUrl(DB_URL);
    jdbcConnection.setUsername(DB_USER);
    jdbcConnection.setPassword(DB_PASSWORD);
    Properties props = new Properties();
    props.setProperty("shutdown", "true");
    jdbcConnection.setProperties(props);
    jdbcConnection.connect();
    
    //
    // run in schema
    //
    
    PreparedStatement s = jdbcConnection.getConnection().prepareStatement(SCHEMA);
    s.executeUpdate();
    s.close();
    
    //
    // run in expected end state, then select out and convert to xml
    //
    
    for (int i = 0; i < ROWS.length; i++) {
      s = jdbcConnection.getConnection().prepareStatement(ROWS[i]);
      s.executeUpdate();
      s.close();
    }
    expectedEndState = getExpectedEndState();

    //
    // delete all rows
    //
    
    s = jdbcConnection.getConnection().prepareStatement("DELETE FROM TRADE");
    s.executeUpdate();
    s.close();
}

  protected void tearDown() throws Exception {
    super.tearDown();
    jdbcConnection.disconnect();
  }
  
  public void testJDBCWriter() throws Exception {
    JDBCWriteConnector writer = new JDBCWriteConnector("jdbcWriter");
    writer.setJdbcConnection(jdbcConnection);
    writer.connect();
    writer.deliver(new Object[] {ROWS[0]});
    writer.deliver(new Object[] {ROWS[1], ROWS[2], ROWS[3], ROWS[4]});
    checkState();
  }

  public void testJDBCWriterWithOMSQLConverter() throws Exception {
    JDBCWriteConnector writer = new JDBCWriteConnector("jdbcWriter");
    writer.setJdbcConnection(jdbcConnection);
    SQLStatementConverter converter = new SQLStatementConverter();
    converter.setSql("INSERT INTO TRADE VALUES ($TRADEID$, '$BUYSELL$', $SECID$, $PARTYID$, $QTY$, $PRICE$)");
    writer.setStatementConverter(converter);
    writer.connect();
    
    writer.deliver(new Object[] {maps[0]});
    writer.deliver(new Object[] {maps[1], maps[2], maps[3], maps[4]});
    checkState();
  }

  public void testJDBCWriterWithOMTableConverter() throws Exception {
    JDBCWriteConnector writer = new JDBCWriteConnector("jdbcWriter");
    writer.setJdbcConnection(jdbcConnection);
    TableStatementConverter converter = new TableStatementConverter();
    converter.setTableName("TRADE");
    writer.setStatementConverter(converter);
    writer.connect();
    
    writer.deliver(new Object[] {maps[0]});
    writer.deliver(new Object[] {maps[1], maps[2], maps[3], maps[4]});
    checkState();
  }

  public void testJDBCWriterWithOMTableConverterAndMapping() throws Exception {
    Map mapping = new HashMap();
    mapping.put("TRADEID", "tradeid");
    mapping.put("BUYSELL", "buysell");
    mapping.put("SECID", "secid");
    mapping.put("PARTYID", "partyid");
    mapping.put("QTY", "qty");
    mapping.put("PRICE", "price");
    JDBCWriteConnector writer = new JDBCWriteConnector("jdbcWriter");
    writer.setJdbcConnection(jdbcConnection);
    TableStatementConverter converter = new TableStatementConverter();
    converter.setTableName("TRADE");
    converter.setMapping(mapping);
    writer.setStatementConverter(converter);
    writer.connect();
    
    writer.deliver(new Object[] {maps[0]});
    writer.deliver(new Object[] {maps[1], maps[2], maps[3], maps[4]});
    checkState();
  }

  private Object[] getExpectedEndState() throws SQLException {
    PreparedStatement s;
    s = jdbcConnection.getConnection().prepareStatement("SELECT * FROM TRADE ORDER BY TRADEID");
    ResultSet rs = s.executeQuery();
    ResultSetToXMLConverter converter = new ResultSetToXMLConverter();
    return converter.convertAll(rs);
  }

  private void checkState() throws SQLException {
    Object[] state = getExpectedEndState();
    boolean match = state.length == expectedEndState.length;
    for (int i = 0; match && i < state.length; i++) {
      match &= state[i].equals(expectedEndState[i]);
    }
    if (!match) {
      if (log.isDebugEnabled()) {
        log.debug("end state : ");
        for (int i = 0; i < state.length; i++) {
          log.debug(state[i]);
        }
        log.debug("expected end state : ");
        for (int i = 0; i < expectedEndState.length; i++) {
          log.debug(expectedEndState[i]);
        }
      }
      fail("end state does not equal expected end state");
    }
  }

}
