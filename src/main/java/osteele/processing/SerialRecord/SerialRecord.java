package osteele.processing.SerialRecord;

import processing.core.*;
import processing.serial.*;

/**
 * A container for an array of ints, that can send them or receive them from
 * the Serial port as comma-separated values terminated by a newline. (This is
 * the format that is used by the Arduino Serial Plotter.)
 */
public class SerialRecord {
  /** The serial port that this SerialRecord uses for sending or receiving. */
  public final Serial serialPort;

  /**
   * The number of values that this record was initialized to.
   * This is also the size of the `values` array.
   */
  public final int size;

  /**
   * The array of values. For a record that is used to send values, values are
   * accumulated into this array, and SerialRecord.send() sends them. For a
   * record that is used to receive values, SerialRecord.read() fills this
   * array, and SerialRecord.values[i] or SerialRecord.get(i) can be used to
   * read the values.
   */
  public int values[];

  /**
   * An array of field names. If the sender includes field names, e.g.
   * "field1:10,field2:20", then this array stores those names, e.g. the first
   * element will be "field1" and the second element will be "field2".
   */
  public String fieldNames[];

  /**
   * Constructor.
   *
   * @param app  the PApplet (generally this)
   * @param port the serial port
   * @param size the number of values
   */
  public SerialRecord(PApplet app, Serial port, int size) {
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
   * Set to true to print transmited and received lines to the console and to
   * the canvas.
   *
   * @param logToConsole Log Tx and Rx to the console if true.
   * @param logToCanvas  Display Tx and Rx on the canvas if true.
   */
  public void log(boolean logToConsole, boolean logToCanvas) {
    portConnection.log(logToConsole, logToCanvas);
  }

  /**
   * Set to true to print transmited and received lines to the console.
   *
   * @param flag Enable logging if true.
   */
  public void log(boolean flag) {
    logToConsole(flag);
  }

  /**
   * Log transmited and received lines to the console.
   */
  public void log() {
    logToConsole(true);
  }

  /**
   * Set to true to display transmited and received lines on the canvas.
   *
   * @param flag Enable logging if true.
   */
  public void logToCanvas(boolean flag) {
    portConnection.logToCanvas(flag);
  }

  /**
   * Display transmited and received lines on the canvas.
   */
  public void logToCanvas() {
    logToCanvas(true);
  }

  /**
   * Set to true to display transmited and received lines on the canvas.
   *
   * @param flag Enable logging if true.
   */
  public void logToConsole(boolean flag) {
    portConnection.logToConsole(flag);
  }

  /**
   * Display transmited and received lines on the canvas.
   */
  public void logToConsole() {
    logToConsole(true);
  }

  /** Send the values in the urrent record to the serial port. */
  public void send() {
    String record = Utils.stringInterpolate(values, ",");
    portConnection.writeln(record);
  }

  /**
   * If data is available on the serial port, synchronously read a line from
   * the serial port and store the values in the current record.
   *
   * @return true if data was available and read.
   */
  public boolean receiveIfAvailable() {
    String line = portConnection.read();
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
    this.portConnection.drawTxRx(x, y);
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values at the
   * lower left corner of the canvas.
   */
  public void draw() {
    this.portConnection.drawTxRx();
  }

  /**
   * Request an echo from the Arduino, if it has been more than interval
   * milliseconds since the last echo that was requested through this means.
   *
   * @param interval the interval, in milliseconds, between requests for an echo
   */
  public void periodicEchoRequest(int interval) {
    portConnection.periodicEchoRequest(interval);
  }

  /**
   * Request an echo from the Arduino. It will send back the last values that it
   * has received.
   */
  public void requestEcho() {
    serialPort.write("!e\n");
  }

  static private String libraryName = "SerialRecord"; // used in error reporting
  private SerialPortConnection portConnection;

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
