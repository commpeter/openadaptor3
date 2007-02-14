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

package org.openadaptor.auxil.connector.jms;

import java.util.List;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.IllegalStateException;
import javax.jms.InvalidClientIDException;
import javax.jms.JMSException;
import javax.jms.Session;
import javax.jms.XAConnection;
import javax.jms.XAConnectionFactory;
import javax.naming.Context;
import javax.naming.NamingException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.auxil.connector.jndi.JNDIConnection;
import org.openadaptor.core.Component;
import org.openadaptor.core.exception.ConnectionException;
import org.openadaptor.core.exception.ValidationException;

/**
 * Utility class providing JMS support to the JMSReadConnector and JMSWriteConnector classes. This class
 * is written to use JMS 1.1 compliant code.
 * <p/>
 * Primary purpose is to manage a single JMS Connection. Also responsible for JNDI lookups for ConnectionFactories
 * and Destinations. Delegates This to an instance of JNDOConnection
 * <p/>
 * Main Properties
 * <ul>
 * <li><b>connectionFactoryName</b>     Name used to look up connectionfactory in JNDI
 * <li><b>connectionFactory</b>         Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
 * <li><b>username</b>                  Username to authenticate the JMS Connection (optional)
 * <li><b>password</b>                  Password to authenticate the JMS Connection (optional)
 * <li><b>clientID</b>                  Default is not set. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
 * <li><b>jndiConnection</b>            JndiConnection used for lookups
 * </ul>
 * <p/>
 */
public class JMSConnection extends Component {

  private static final Log log = LogFactory.getLog(JMSConnection.class);

  /**
   * Name used to look up connectionfactory in JNDI.
   */
  private String connectionFactoryName = null;

  /**
   * Password to authenticate the JMS Connection (optional).
   */
  private String password = null;

  /**
   * Username to authenticate the JMS Connection (optional).
   */
  private String username = null;

  /**
   * JndiConnection used for lookups.
   */
  private JNDIConnection jndiConnection;

  /**
   * ID for client. Default is not set as can be set by ConnectionFactory. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
   */
  private String clientID = null;

  /**
   * Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
   */
  private ConnectionFactory connectionFactory = null;

  private Context ctx;

  private Connection connection = null;

  // Connection Support

  /**
   * Close the connection. Ignores any current clients.
   */
  public void disconnect() {
    if (connection != null) {
      close();
    }
  }

  /**
   * Close the connection connector is the last client.
   */
  public void disconnectFor(Component connector) {
    // Todo Should keep track of Connector Clients and only close when all clients closed.
    disconnect();
  }

  /**
   * True if there is an existing connection.
   *
   * @return boolean
   */
  public boolean isConnected() {
    return (connection != null);
  }

  // End Connection


  // Validate

  public void validate(List exceptions) {
    if ((getConnectionFactory() == null) && (connectionFactoryName == null)) {
      exceptions.add( new ValidationException("One of properties connectionFactory or connectionFactoryName must be set.", this));
    }
    if(jndiConnection == null) {
      exceptions.add(new ValidationException("Property jndiConnection is mandatory", this));
    }
  }

  // End Validate

  // Session Stuff

  /**
   * Create and return a JMS Session for a JMSReadConnector.
   *
   * @param connector The client JMSReadConnector
   * @return Session A JMS Session
   */
  public Session createSessionFor(JMSReadConnector connector) {
    if (!isConnected()) { // Connection created lazily.
      createConnection();
      startConnection();
    }
    return createSessionFor(connector.isTransacted(), connector.getAcknowledgeMode());
  }

  /**
   * Create and return a JMS Session for a JMSWriteConnector.
   *
   * @return Session A JMS Session
   */
  public Session createSessionFor(JMSWriteConnector connector) {
    if (!isConnected()) {
      createConnection();
      startConnection(); // Todo Do we really need to start the connection here?
    }
    return createSessionFor(connector.isTransacted(), connector.getAcknowledgeMode());
  }

  protected Session createSessionFor(boolean isTransacted, int acknowledgeMode) {
    Session newSession;
    try {
      if (!isTransacted && (connection instanceof XAConnection)) {
        newSession = ((XAConnection) connection).createXASession();
      }
      else {
        newSession = (connection.createSession(isTransacted, acknowledgeMode));
      }
    } catch (JMSException jmse) {
      throw new ConnectionException("Unable to create session from connection", jmse, this);
    }
    return newSession;
  }

  // End Session Stuff

  protected void setConnection(Connection connection) {
    this.connection = connection;
  }

  protected Connection getConnection() {
    return connection;
  }

  protected void createConnection() {
    if (log.isDebugEnabled())
      log.debug("connecting...");
    try {
      if (connectionFactory == null) {
        Object factoryObject = getCtx().lookup(connectionFactoryName);
        if (factoryObject instanceof ConnectionFactory) {
          connectionFactory = (ConnectionFactory) factoryObject;
        } else { //We don't have a valid connectionFactory.
          if (connectionFactory == null) {
            String reason = "Unable to get a connectionFactory. This may be because the required JMS implementation jars are not available on the classpath";
            log.error(reason);
            throw new ConnectionException(reason, this);
          } else {
            String reason = "Factory object is not an instance of ConnectionFactory. This should not happen";
            log.error(reason);
            throw new ConnectionException(reason, this);
          }
        }
      }

      setConnection(createConnection(connectionFactory));

      if (clientID != null) {
        // A clientID has been configured so attempt to set it.
        try {
          connection.setClientID(clientID);
        } catch (InvalidClientIDException ice) {
          throw new ConnectionException(
              "Error setting clientID. ClientID either duplicate(most likely) or invalid. Check for other connected clients",
              ice, this);
        } catch (IllegalStateException ise) {
          throw new ConnectionException(
              "Error setting clientID. ClientID most likely administratively set. Check ConnectionFactory settings",
              ise, this);
        }
      }
    } catch (JMSException e) {
      log.error("JMSException during connect." + e);
      throw new ConnectionException(" JMSException during connect.", e, this);
    } catch (NamingException e) {
      log.error("NamingException during connect." + e);
      throw new ConnectionException("NamingException during connect.", e, this);
    }
  }

  protected Connection createConnection(ConnectionFactory factory) throws JMSException {
    Connection newConnection;
    boolean useUsername = true;
    if (username == null || username.compareTo("") == 0) {
      useUsername = false;
    }
    if (factory instanceof XAConnectionFactory) {
      if (useUsername) {
        newConnection = ((XAConnectionFactory) factory).createXAConnection(username, password);
      } else {
        newConnection = ((XAConnectionFactory) factory).createXAConnection();
      }
    } else {
      if (useUsername) {
        newConnection = factory.createConnection(username, password);
      } else {
        newConnection = factory.createConnection();
      }
    }
    return newConnection;
  }

  protected void installAsExceptionListener(ExceptionListener listener) {
    try {
      connection.setExceptionListener(listener);
    } catch (JMSException e) {
      throw new ConnectionException("Unable to install [" + listener + "] as ExceptionListener. ", e, this);
    }
  }

  protected void startConnection() {
    try {
      connection.start();
    } catch (JMSException e) {
      log.error("JMSException during connection start." + e);
      throw new ConnectionException(" JMSException during connection start.", e, this);
    }
  }

  /** Close everything */
  protected void close() {
    try {
      if (connection != null) {
        connection.close();
      }
      setConnection(null);
    } catch (JMSException e) {
      throw new ConnectionException("Unable close jms connection for  [" + getId() + "]", e, this);
    }
  }

  // JNDI Support

  public Destination lookupDestination( String destinationName ) {
    try {
      return (Destination) getCtx().lookup(destinationName);
    } catch (NamingException e) {
      throw new ConnectionException("Unable to resolve Destination for [" + destinationName + "]", e, this);
    }
  }

  protected Context getCtx() throws NamingException {
    if (ctx == null) {
      ctx = jndiConnection.connect();
    }
    return ctx;
  }

  protected void setCtx(Context ctx) {
    this.ctx = ctx;
  }

  // End JNDI support

  // Bean Properties

  /**
   * Name used to look up connectionfactory in JNDI.
   */
  public void setConnectionFactoryName(String connectionFactoryName) {
    this.connectionFactoryName = connectionFactoryName;
  }

  protected String getConnectionFactoryName() { return connectionFactoryName; }

  /**
   * Real JMS ConnectionFactory. When this is set <code>connectionFactoryName</code> is ignored.
   */
  public void setConnectionFactory(ConnectionFactory connectionFactory) {
    this.connectionFactory = connectionFactory;
  }

  protected ConnectionFactory getConnectionFactory() { return connectionFactory; }

  /**
   * Password to authenticate the JMS Connection (optional).
   */
  public void setPassword(String password) {
    this.password = password;
  }

  protected String getPassword() { return password; }

  /**
   * Username to authenticate the JMS Connection (optional).
   */
  public void setUsername(String username) {
    this.username = username;
  }

  protected String getUsername() { return username; }

  /**
   * Set the JNDIConnection used to look up jndi managed resources.
   */
  public void setJndiConnection(JNDIConnection jndiConnection) {
    this.jndiConnection = jndiConnection;
  }

  /**
   * Default is not set. Use with caution. ConnectionFactory must be configured to allow the clientID to be set.
   */
  public void setClientID(String clientID) {
    this.clientID = clientID;
  }

  protected String getClientID() {
    return clientID;
  }
  // End Bean Properties


}
