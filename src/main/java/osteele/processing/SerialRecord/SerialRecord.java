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
    this.size = size;
    this.values = new int[size];
  }

  public int get() {
    return values[0];
  }

  /** Send the values in the urrent record to the serial port. */
  public void send() {
    var record = Utils.stringInterpolate(values, ",");
    pTxLine = record;
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
    while (serialPort.available() > 0) {
      var line = serialPort.readStringUntil('\n');
      if (line != null) {
        pRxTime = this.app.millis();
        while (!line.isEmpty()
            && (line.endsWith("\n") || Character.getNumericValue(line.charAt(line.length() - 1)) == -1)) {
          line = line.substring(0, line.length() - 1);
        }
        if (log) {
          PApplet.println("Rx: " + line);
        }
        pRxLine = line;
        if (!firstLine) {
          processReceivedLine(line);
        }
        firstLine = false;
      }
    }
  }

  public void read() {
    receiveIfAvailable();
  }

  /**
   * Display the last transmitted (TX) and received (RX) values on the canvas.
   *
   * @param x the x-coordinate of the upper-left corner of the display area
   * @param y the y-coordinate of the upper-left corner of the display area
   */
  public void draw(float x, float y) {
    receiveIfAvailable();
    if (pTxLine != null) {
      this.app.text("TX: " + pTxLine, x, y);
    }
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
    receiveIfAvailable();
  }

  /**
   * Request an echo from the Arduino. It will send back the last values that it
   * has received.
   */
  public void requestEcho() {
    serialPort.write("!e\n");
  }

  private PApplet app;
  private String pRxLine, pTxLine;
  private int pRxTime;
  private int pPeriodicEchoRequestTime = 0;
  private boolean firstLine = true;

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
    if (line.isBlank()) {
      return;
    }
    var values = line.split(",");
    if (values.length != size) {
      var message = String.format("Expected %d values, but received %d values", size, values.length);
      PGraphics.showWarning(message);
    }
    int n = Math.max(values.length, size);
    for (int i = 0; i < n; i++) {
      try {
        this.values[i] = Integer.parseInt(values[i]);
      } catch (NumberFormatException e) {
        PGraphics.showWarning("Received line has invalid value: " + values[i]);
        break;
      }
    }
  }
}
