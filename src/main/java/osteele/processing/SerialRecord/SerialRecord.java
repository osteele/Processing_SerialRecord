package osteele.processing.SerialRecord;

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

  /**
   * Constructor.
   *
   * @param app  the PApplet (generally this)
   * @param port the serial port
   * @param size the number of values
   */
  public SerialRecord(PApplet app, Serial port, int size) {
    this.app = app;
    this.portConnection = SerialPortConnection.get(app, port);
    this.serialPort = port;
    this.size = size;
    this.values = new int[size];
    this.fieldNames = new String[size];
  }

  /**
   * Return the first value in the array.
   *
   * @return the first value
   */
  public int get() {
    return values[0];
  }

  /**
   * Set to true to print transmited and received lines to the console.
   *
   * @param flag Enable logging if true.
   */
  public void log(boolean flag) {
    mLog = flag;
  }

  /** Call to print transmited and received lines to the console. */
  public void log() {
    log(true);
  }

  /** Send the values in the urrent record to the serial port. */
  public void send() {
    String record = Utils.stringInterpolate(values, ",");
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
   *
   * @return true if data was available and read.
   */
  public boolean receiveIfAvailable() {
    String line = portConnection.read(mLog);
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
   *
   * @return true if data was available and read.
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
    String pRxLine = portConnection.read(mLog);
    if (pRxLine == null) {
      pRxLine = portConnection.pRxLine;
    }
    if (pTxLine != null) {
      this.app.text("TX: " + pTxLine, x, y);
    }
    if (pRxLine == null || !pRxLine.isEmpty()) {
      y += this.app.textAscent() + this.app.textDescent();
      String message = "Click to request an echo from the Arduino";
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
    float y = this.app.height - 2 * (this.app.textAscent() + this.app.textDescent());
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

  static private String libraryName = "SerialRecord"; // used in error reporting
  private PApplet app;
  private String pTxLine;
  private SerialPortConnection portConnection;
  private int pPeriodicEchoRequestTime = 0;
  private boolean mLog = false;

  private void showWarning(String message) {
    PGraphics.showWarning(String.format("%s: %s", libraryName, message));
  }

  private void processReceivedLine(String line) {
    if (line.isEmpty()) {
      return;
    }
    if (line.startsWith("Warning:") || line.startsWith("Error:")) {
      PGraphics.showWarning("SerialRecord@Arduino: " + line);
    }
    String[] fields = line.split("[,; \t]");
    if (fields.length != size) {
      String message = String.format("Expected %d value(s), but received %d value(s)",
          size, fields.length);
      showWarning(message);
    }
    // Go ahead and read as many fields as fit into the record, even if the
    // number of fields is different from the specified record size. This
    // simplifies incremental development: the user may not need to re-flash the
    // Arduino quite as much.
    int n = Math.min(fields.length, size);
    for (int i = 0; i < n; i++) {
      String field = fields[i];
      fieldNames[i] = null;
      if (field.contains(":")) {
        String[] split = field.split(":", 2);
        fieldNames[i] = split[0];
        field = split[1];
      }
      try {
        this.values[i] = Integer.parseInt(field);
      } catch (NumberFormatException e) {
        showWarning("Received line contains an invalid value: " +
            field);
        break;
      }
    }
  }
}
