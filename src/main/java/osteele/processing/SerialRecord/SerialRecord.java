package osteele.processing.SerialRecord;

import processing.core.*;
import processing.serial.*;

/**
 * A container for an array of ints, that can send them or receive them from
 * the Serial port as comma-separated values terminated by a newline. (This is
 * the format that is used by the Arduino Serial Plotter.)
 */
public class SerialRecord {
  /** The number of values. */
  public int size;
  /** The array of values. */
  public int values[];
  /** The serial port. */
  public Serial serialPort;
  /** Set to true to print transmited and received lines to the console. */
  public boolean log = false;

  public SerialRecord(PApplet app, Serial port, int size) {
    this.app = app;
    this.serialPort = port;
    this.values = new int[size];
  }

  /** Send the values in the urrent record to the serial port. */
  public void send() {
    var record = Utils.stringInterpolate(values, ",");
    if (log) {
      PApplet.println("TX: " + record);
    }
    serialPort.write(record);
    serialPort.write('\n');
  }

  /**
   * If data is available on the serial port, synchronously read a line from
   * the serial port and store the values in the current record.
   */
  public void receiveIfAvailable() {
    if (serialPort.available() == 0) {
      return;
    }
    var line = serialPort.readStringUntil('\n');
    if (line != null) {
      pRxTime = this.app.millis();
      if (line.endsWith("\n")) {
        line = line.substring(0, line.length() - 1);
      }
      if (log) {
        PApplet.println("Rx: " + line);
      }
      pRxLine = line;
      processReceivedLine(line);
    }
  }

  /**
   * Display the last transmitted (TX) and received (RX) values on the canvas.
   *
   * @param x the x-coordinate of the upper-left corner of the display area
   * @param y the y-coordinate of the upper-left corner of the display area
   */
  public void draw(float x, float y) {
    this.app.text("TX: " + Utils.stringInterpolate(values, ","), x, y);
    if (pRxLine == null || !pRxLine.isEmpty()) {
      y += this.app.textAscent() + this.app.textDescent();
      var message = "Click to request an echo from the Arduino";
      if (pRxLine != null) {
        message = pRxLine;
        int age = this.app.millis() - pRxTime;
        if (age >= 1000) {
          message += String.format(" (%s ago)", humanTime(age));
        }
      }
      this.app.text("RX: " + message, x, y);
    }
  }

  /**
   * Display the last transmitted (TX) and received (RX) values at the lower
   * left corner of the canvas.
   */
  public void draw() {
    var y = this.app.height - 2 * (this.app.textAscent() + this.app.textDescent());
    draw(10, y);
  }

  /**
   * Request an echo from the Arduino, if it has been more than interval
   * milliseconds since the last echo that was requested through this means.
   *
   * @param interval the interval, in milliseconds, between requests for an echo
   */
  public void periodicEchoRequest(int interval) {
    if (pPeriodicEchoRequestTime + interval < this.app.millis()) {
      pPeriodicEchoRequestTime = this.app.millis();
      this.requestEcho();
    }
  }

  /**
   * Request an echo from the Arduino. It will send back the last values that it
   * has received.
   */
  public void requestEcho() {
    serialPort.write("!e\n");
  }

  private PApplet app;
  private String pRxLine;
  private int pRxTime;
  private int pPeriodicEchoRequestTime = 0;

  private String humanTime(int age) {
    if (age < 1000) {
      return String.format("%d ms", age);
    } else if (age < 60 * 1000) {
      return String.format("%d s", age / 1000);
    } else {
      int minutes = age / 60 / 1000;
      var s = String.format("%d minute", minutes);
      if (minutes > 1)
        s += "s";
      return s;
    }
  }

  private void processReceivedLine(String line) {
  }
}
