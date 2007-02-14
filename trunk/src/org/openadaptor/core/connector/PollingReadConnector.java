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

package org.openadaptor.core.connector;

import java.text.ParseException;
import java.util.Date;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openadaptor.core.Component;
import org.openadaptor.core.IReadConnector;
import org.openadaptor.core.exception.ComponentException;
import org.openadaptor.core.exception.ValidationException;
import org.openadaptor.core.transaction.ITransactional;
import org.quartz.CronExpression;
import org.quartz.CronTrigger;

/**
 * This is a IReadConnector that wraps another IReadConnector, it can be
 * configured to poll at fixed intervals or using a cron format. When this
 * connector polls it calls the following on the IReadConnector it is
 * wrapping...
 * <ul>
 * <li>{@link IReadConnector#connect()}
 * <li>{@link IReadConnector#next(long)}, until {@link IReadConnector#isDry()}
 * returns true
 * <li>{@link IReadConnector#disconnect()}
 * </ul>
 * 
 * Allows adaptors to poll files, directories, databases rss feeds etc... <p/>
 * 
 * There are three modes of operation:
 * 
 * <ol>
 * <li>cronExpression - uses a crontab like expression to schedule the adaptor
 * polling frequency</li>
 * 
 * <li>pollInterval - sets the adaptor to poll at regular intervals</li>
 * 
 * <li>neither - Out of the box, without setting any polling intervals or using
 * the crontab, the adaptor will perform a single poll and then exit</li>
 * </ol>
 * 
 * See <a href="http://www.opensymphony.com/quartz">quartz</a> documentation
 * for cron expression format
 * 
 * @author Fred Perry
 */
public class PollingReadConnector extends Component implements IReadConnector, ITransactional {

  private static final Log log = LogFactory.getLog(PollingReadConnector.class);

  private Date reconnectTime;

  private Date startTime;

  private int count;

  private int limit = 1;

  private long intervalMs = -1;

  private IReadConnector delegate;

  private boolean forceInitialPoll;

  private CronTrigger cron;

  public PollingReadConnector() {
  }

  public PollingReadConnector(String id) {
    super(id);
  }

  /**
   * Mandatory
   * 
   * @param delegate
   *          the IReadConnector that will do the actual data reading. This is
   *          the connector that gets called once per poll.
   */
  public void setDelegate(final IReadConnector delegate) {
    this.delegate = delegate;
  }

  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X milliseconds
   */
  public void setPollIntervalMs(final long interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalMs] will take precedence");

    this.intervalMs = interval;
  }

  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X seconds
   */
  public void setPollIntervalSecs(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalSecs] will take precedence");

    this.intervalMs = interval * 1000;
  }

  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X minutes
   */
  public void setPollIntervalMins(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalMins] will take precedence");

    this.intervalMs = interval * 60 * 1000;
  }

  /**
   * Optional
   * 
   * @param interval
   *          set the adaptor to poll ever X hpurs
   */
  public void setPollIntervalHours(final int interval) {
    if (intervalMs > -1)
      log.warn("Multiple poll interval property settings detected. " + "[pollIntervalHours] will take precedence");

    this.intervalMs = interval * 60 * 60 * 1000;
  }

  /**
   * Optional. Defaults to 1
   * 
   * @param limit
   *          the number of polls to perform before exiting the adaptor. A limit
   *          of less than 1 indicates an infinte loop and the adaptor will
   *          never exit!
   */
  public void setPollLimit(final int limit) {
    this.limit = limit;

    if (limit == 0)
      log.warn("A PollLimit of zero will result in an infinite polling loop");
  }

  /**
   * Takes a single line crontab entry and schedules the polling accordingly. A
   * crontab entry must be a text string consisting six fields. The fields must
   * be separated by blank characters. The first five fields must be integer
   * patterns that specify the following:
   * 
   * <pre>
   *       Minute (0-59)
   *       Hour (0-23)
   *       Day of the month (1-31)
   *       Month of the year (1-12)
   *       Day of the week (0-6 with 0=Sunday)
   * </pre>
   * 
   * Each of these patterns can be either an asterisk (meaning all valid
   * values), an element or a list of elements separated by commas. An element
   * must be either a number or two numbers separated by a hyphen (meaning an
   * inclusive range). The specification of days can be made by two fields (day
   * of the month and day of the week). If month, day of month and day of week
   * are all asterisks, every day will be matched. If either the month or day of
   * month is specified as an element or list, but the day of week is an
   * asterisk, the month and day of month fields will specify the days that
   * match. If both month and day of month are specified as asterisk, but day of
   * week is an element or list, then only the specified days of the week will
   * match. Finally, if either the month or day of month is specified as an
   * element or list, and the day of week is also specified as an element or
   * list, then any day matching either the month and day of month or the day of
   * week, will be matched. <p/>
   * 
   * The sixth field in a crontab entry is traditionally a string that will be
   * executed by the shell at the specified times. In our case it must be a "?"
   * character. <p/>
   * 
   * Overrides <em>pollInterva</em> properties.
   * 
   * @param s
   *          a single line containing the crontab entry
   * 
   * @throws RuntimeException
   *           if the cron entry is invalid
   */
  public void setCronExpression(String s) {
    cron = new CronTrigger();
    try {
      cron.setCronExpression(new CronExpression(s));
    } catch (ParseException e) {
      throw new RuntimeException("cron parse exception", e);
    }
    limit = 0;
  }

  /**
   * @param force
   *          if true and you are using cron then an initial poll will be
   *          preformed as soon as the adaptor starts regardless of the cron
   *          settings. After that the polling frequency will be governed by
   *          cron.
   */
  public void setForceInitialPoll(final boolean force) {
    this.forceInitialPoll = force;
  }

  public final void connect() {
    initTimes();
    delegate.connect();
    count++;
    log.debug(getId() + " poll count = " + count);
    calculateReconnectTime();
  }

  public final void disconnect() {
    delegate.disconnect();
  }

  public final Object[] next(long timeoutMs) throws ComponentException {

    Date now = new Date();

    if (delegate.isDry() && now.after(reconnectTime)) {
      disconnect();
      connect();
    }

    if (!delegate.isDry() && now.after(startTime)) {
      return delegate.next(timeoutMs);
    } else {
      sleepNoThrow(timeoutMs);
      return null;
    }
  }

  private void initTimes() {
    if (startTime == null) {
      startTime = new Date();
      if (cron != null && !forceInitialPoll) {
        startTime = cron.getFireTimeAfter(startTime);
      }
    }
    if (reconnectTime == null) {
      reconnectTime = new Date();
    }
  }

  private void sleepNoThrow(long timeoutMs) {
    try {
      Thread.sleep(timeoutMs);
    } catch (InterruptedException e) {
      // ignore errors
    }
  }

  private void calculateReconnectTime() {
    if (cron != null) {
      reconnectTime = cron.getFireTimeAfter(reconnectTime);
    } else {
      reconnectTime = new Date(reconnectTime.getTime() + intervalMs);
    }
    log.info(getId() + " next poll time = " + reconnectTime.toString());
  }

  public final boolean isDry() {
    return delegate.isDry() && limit > 0 && count >= limit;
  }

  public final Object getReaderContext() {
    return delegate.getReaderContext();
  }

  public final Object getResource() {
    if (delegate instanceof ITransactional) {
      return ((ITransactional) delegate).getResource();
    } else {
      return null;
    }
  }

  /**
   * Checks that the mandatory properties have been set
   * 
   * @param exceptions
   *          list of exceptions that any validation errors will be appended to
   */
  public void validate(List exceptions) {
    if (delegate == null) {
      exceptions.add(new ValidationException("[delegate] property not set. " 
          + "Please supply an instance of " + IReadConnector.class.getName() + " for it", this));
    }

    if (cron != null) {
      if (intervalMs > -1) {
        log.warn("[cronExpression] takes precedence over [pollIntervalX] which will be ignored");
      }

      if (limit > 0) {
        log.warn("[cronExpression] takes precedence over [pollLimit] which will be ignored");
      }
    }

    if (cron == null && forceInitialPoll) {
      log.warn("Property [forceInitialPoll] is only applicable when using the [cronExpression]. "
          + "It will be ignored");
    }
  }
}
