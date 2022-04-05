package osteele.processing.SerialRecord;

import java.util.*;
import processing.core.*;
import processing.serial.*;

/**
 * A container for an array of ints, that can send them or receive them from
 * the Serial port as comma-separated values terminated by a newline. (This is
 * the format that is used by the Arduino Serial Plotter.)
 */
public class SerialRecord {
  /** The serial port. */
  public Serial serialPort;
  /** The number of values. */
  public int size;
  /** The array of values. */
  public int values[];
  /** The array of field names. */
  public String fieldNames[];

  public SerialRecord(PApplet app, Serial port, int size) {
    this.app = app;
    this.portConnection = PortConnection.get(app, port);
    this.serialPort = port;
    this.size = size;
    this.values = new int[size];
    this.fieldNames = new String[size];
  }

  public int get() {
    return values[0];
  }

  /** Set to true to print transmited and received lines to the console. */
  public void log(boolean flag) {
    mLog = flag;
  }

  /** Call to print transmited and received lines to the console. */
  public void log() {
    log(true);
  }

  /** Send the values in the urrent record to the serial port. */
  public void send() {
    var record = Utils.stringInterpolate(values, ",");
    pTxLine = record;
    if (mLog) {
      PApplet.println("TX: " + record);
    }
    serialPort.write(record);
    serialPort.write('\n');
  }

  /**
   * If data is available on the serial port, synchronously read a line from
   * the serial port and store the values in the current record.
   */
  public boolean receiveIfAvailable() {
    var line = portConnection.read(mLog);
    if (line != null) {
      processReceivedLine(line);
      return true;
    }
    return false;
  }

  /**
   * If data is available on the serial port, synchronously read a line from the
   * serial port and store the values in the current record. A synonym for
   * readIfAvailable().
   */
  public boolean read() {
    return receiveIfAvailable();
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values on the
   * canvas.
   *
   * @param x the x-coordinate of the upper-left corner of the display area
   * @param y the y-coordinate of the upper-left corner of the display area
   */
  public void draw(float x, float y) {
    var pRxLine = portConnection.read(mLog);
    if (pRxLine == null) {
      pRxLine = portConnection.pRxLine;
    }
    if (pTxLine != null) {
      this.app.text("TX: " + pTxLine, x, y);
    }
    if (pRxLine == null || !pRxLine.isEmpty()) {
      y += this.app.textAscent() + this.app.textDescent();
      var message = "Click to request an echo from the Arduino";
      if (pRxLine != null) {
        message = pRxLine;
        int age = this.app.millis() - portConnection.pRxTime;
        if (age >= 1000) {
          message += String.format(" (%s ago)", Utils.humanTime(age));
        }
      }
      this.app.text("RX: " + message, x, y);
    }
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values at the
   * lower left corner of the canvas.
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
    portConnection.read(mLog);
  }

  /**
   * Request an echo from the Arduino. It will send back the last values that it
   * has received.
   */
  public void requestEcho() {
    serialPort.write("!e\n");
  }

  private PApplet app;
  private String pTxLine;
  private PortConnection portConnection;
  private int pPeriodicEchoRequestTime = 0;
  private boolean mLog = false;

  private void processReceivedLine(String line) {
    if (line.isBlank()) {
      return;
    }
    if (line.startsWith("Warning:") || line.startsWith("Error:")) {
      PGraphics.showWarning(line);
    }
    var values = line.split(",");
    if (values.length != size) {
      var message = String.format("Expected %d value(s), but received %d value(s)",
          size, values.length);
      PGraphics.showWarning(message);
    }
    int n = Math.min(values.length, size);
    for (int i = 0; i < n; i++) {
      String field = values[i];
      fieldNames[i] = null;
      if (field.contains(":")) {
        var split = field.split(":", 2);
        fieldNames[i] = split[0];
        field = split[1];
      }
      try {
        this.values[i] = Integer.parseInt(field);
      } catch (NumberFormatException e) {
        PGraphics.showWarning("Received line contains an invalid value: " +
            field);
        break;
      }
    }
  }
}

/**
 * Mediates the connection between multiple SerialRecords connected to the same
 * port, so that the last received line of text can be tracked on a per-port
 * instead of per-SerialRecord basis.
 */
class PortConnection {
  private static Map<Serial, PortConnection> portMap = new HashMap<Serial, PortConnection>();

  /**
   * Get the instance for the specified port. Create a new instance if none
   * exists.
   */
  static PortConnection get(PApplet app, Serial serial) {
    var connection = portMap.get(serial);
    if (connection == null) {
      connection = new PortConnection(app, serial);
      portMap.put(serial, connection);
    }
    return connection;
  }

  Serial serial;
  String pRxLine;
  int pRxTime;

  private PApplet app;
  private String unprocessedRxLine;
  private boolean firstLine = true;

  public PortConnection(PApplet app, Serial serial) {
    this.app = app;
    this.serial = serial;
  }

  /**
   * If data is available on the serial port, synchronously read a line from
   * the serial port and store the values in the current record.
   */
  public String peek(boolean log) {
    if (unprocessedRxLine != null) {
      return unprocessedRxLine;
    }
    while (serial.available() > 0) {
      var line = serial.readStringUntil('\n');
      if (line != null) {
        pRxTime = this.app.millis();
        line = Utils.trimRight(line);
        while (!line.isEmpty()
            && (line.endsWith("\n") || Character.getNumericValue(line.charAt(line.length() - 1)) == -1)) {
          line = line.substring(0, line.length() - 1);
        }
        if (!firstLine) {
          if (log) {
            PApplet.println("Rx: " + line);
          }
          unprocessedRxLine = line;
          return line;
        }
        firstLine = false;
      }
    }
    return null;
  }

  public String read(boolean log) {
    var line = peek(log);
    if (line != null) {
      pRxLine = line;
      unprocessedRxLine = null;
    }
    return line;
  }
}
