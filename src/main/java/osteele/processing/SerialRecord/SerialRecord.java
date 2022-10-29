package osteele.processing.SerialRecord;

import java.util.Arrays;
import java.util.List;
import java.util.LinkedList;

import processing.core.*;
import processing.serial.*;

/**
 * A container for an array of ints, that can send them or receive them from
 * the Serial port as comma-separated values terminated by a newline. (This is
 * the format that is used by the Arduino Serial Plotter.)
 */
public class SerialRecord {
  /** The serial port that this SerialRecord uses for sending or receiving. */
  public final Serial serial;

  /**
   * The number of values that this record was initialized to.
   * This is also the size of the `values` array.
   */
  public int size;

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
   * The time in millis() when the record was received.
   */
  public int sampleTime;

  private final PApplet app;
  private List<String> fieldNameList;
  private boolean isFirstLine = true;
  private boolean isResizeable;

  /**
   * Constructor. Construct a serial record that expects
   * `size` number of values.
   *
   * @param app    the PApplet (generally this)
   * @param serial the serial port
   * @param size   the number of values
   */
  public SerialRecord(PApplet app, Serial serial, int size) {
    this.app = app;
    this.serial = serial;
    this.size = size;

    this.values = new int[size];
    this.fieldNames = new String[size];
    this.fieldNameList = Arrays.asList(fieldNames);
    this.isResizeable = size == 0;
    this.portConnection = SerialPortConnection.get(app, serial);
  }

  /**
   * Constructor. Construct a serial record that receives any number of values.
   *
   * @param app    the PApplet (generally this)
   * @param serial the serial port
   */
  public SerialRecord(PApplet app, Serial serial) {
    this(app, serial, 0);
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
   * Return the nth value in the array.
   *
   * @return the first value
   */
  public int get(int index) {
    return values[index];
  }

  /**
   * Return serial record value, indexed by its field name.
   *
   * @param fieldName the name of the field
   * @return the first value
   */
  public int get(String fieldName) {
    int index = fieldNameList.indexOf(fieldName);
    if (index < 0) {
      List<String> nonNullNames = new LinkedList<>(fieldNameList);
      nonNullNames.removeIf(any -> {
        return any == null;
      });
      if (nonNullNames.isEmpty()) {
        throw new RuntimeException("Error: No field names are present");
      } else {
        throw new RuntimeException(String.format(
            "Error: Field name \"%s\" is not present in %s",
            fieldName, nonNullNames));
      }
    }
    return values[index];
  }

  /**
   * Return serial record value, indexed by its field name.
   *
   * @param fieldName    the name of the field
   * @param defaultValue the value to return if fieldName does not occur in
   *                     the record
   * @return the first value
   */
  public int get(String fieldName, int defaultValue) {
    int index = fieldNameList.indexOf(fieldName);
    return index < 0 ? defaultValue : get(fieldName);
  }

  // #region logging

  /**
   * Set to true to print transmited and received lines to the console and to
   * the canvas.
   *
   * @param logToConsole Log Tx and Rx to the console if true.
   * @param logToCanvas  Display Tx and Rx on the canvas if true.
   */
  public void log(boolean logToConsole, boolean logToCanvas) {
    portConnection.logToConsole(logToConsole);
    portConnection.logToCanvas(logToCanvas);
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
  // #endregion

  // #region send-and-receive

  /**
   * Send the values in the current record to the serial port.
   */
  public void send() {
    send(values);
  }

  /**
   * Send the values in the array to the serial port.
   *
   * @param values An array of values to send.
   */
  public void send(int values[]) {
    String record = Utils.stringInterpolate(values, ",");
    portConnection.writeln(record);
  }

  /**
   * Send the values in the current record to the serial port.
   *
   * This method is a synonym for `send()`.
   */
  public void write() {
    send();
  }

  /**
   * Send the values in the current record to the serial port.
   *
   * This method is a synonym for `send()`.
   */
  public void write(int values[]) {
    send(values);
  }

  /**
   * If data is available on the serial port, synchronously read a line from the
   * serial port and store the values in the current record. A synonym for
   * readIfAvailable().
   *
   * @return true if data was available and read.
   */
  public boolean receive() {
    return receiveIfAvailable(values);
  }

  /**
   * If data is available on the serial port, synchronously read a line from
   * the serial port and store the values in the current record.
   *
   * @return true if data was available and read.
   */
  public boolean receiveIfAvailable() {
    return receiveIfAvailable(values);
  }

  private boolean receiveIfAvailable(int[] values) {
    String line = portConnection.read();
    if (line != null) {
      processReceivedLine(line, values);
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
   * If data is available on the serial port, synchronously read a line from the
   * serial port and store the values in the current record. A synonym for
   * readIfAvailable().
   *
   * @return true if data was available and read.
   */
  public boolean read(int[] values) {
    return receiveIfAvailable();
  }
  // #endregion

  // #region serial-facade
  /**
   * Return the number of bytes available from the underlying serial connection.
   * The main use of this is to compare this number to 0, to see whether a
   * call to read() and its synonyms will read a new record.
   *
   * @return The number of bytes available to read.
   */
  public int available() {
    return portConnection.available();
  }

  /**
   * Discard all currently-pending unprocessed incoming serial data.
   */
  public void clear() {
    portConnection.clear();
  }

  // #endregion

  /**
   * Display the most-recently transmitted (TX) and received (RX) values on the
   * canvas.
   *
   * @param x the x-coordinate of the upper-left corner of the display area
   * @param y the y-coordinate of the upper-left corner of the display area
   */
  public void draw(float x, float y) {
    portConnection.drawTxRx(x, y);
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values at the
   * lower left corner of the canvas.
   */
  public void draw() {
    portConnection.drawTxRx();
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
    portConnection.requestEcho();
  }

  // #region private
  private static final String libraryName = "SerialRecord"; // used in error reporting
  private static final String fieldSeparators = "[,; \t]";
  private SerialPortConnection portConnection;

  private void showWarning(String message) {
    PGraphics.showWarning(String.format("%s: %s", libraryName, message));
  }

  private void processReceivedLine(String line, int[] values) {
    // The first line is generally incomplete. It may contain only the end of a
    // record; but also, the prefix may include a random sample of characters
    // from previous lines. Process this line, but don't report errros. If there
    // is a program error, they will almost certainly show up in subsequent
    // lines as well.
    boolean reportInputErrors = !this.isFirstLine && app.millis() < 1000;
    this.isFirstLine = false;

    if (line.isEmpty()) {
      return;
    }
    if (line.startsWith("Warning:") || line.startsWith("Error:")) {
      PGraphics.showWarning("SerialRecord@Arduino: " + line);
      return;
    }

    this.sampleTime = app.millis();
    int size = values.length;
    String[] fields = line.split(fieldSeparators);
    if (fields.length != size && !isResizeable) {
      if (reportInputErrors) {
        String message = String.format("Expected %d value(s), but received %d value(s)",
            size, fields.length);
        showWarning(message);
      }
    }
    if (fields.length != size && isResizeable) {
      this.size = fields.length;
      this.values = new int[size];
      this.fieldNames = new String[size];
      this.fieldNameList = Arrays.asList(fieldNames);
    }
    // Go ahead and read as many fields as fit into the record, even if the
    // number of fields is different from the specified record size. This
    // simplifies incremental development: the user not need re-flash the
    // Arduino quite as frequently.
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
        values[i] = Integer.parseInt(field);
      } catch (NumberFormatException e) {
        if (reportInputErrors) {
          showWarning("Received line contains an invalid value: " +
              field);
        }
        break;
      }
    }
  }
  // #endregion
}
