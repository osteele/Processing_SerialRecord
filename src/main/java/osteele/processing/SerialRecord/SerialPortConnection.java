package osteele.processing.SerialRecord;

import java.util.*;
import processing.core.*;
import processing.serial.*;

/**
 * Mediates the connection between multiple SerialRecords connected to the same
 * port, so that the last received line of text can be tracked on a per-port
 * instead of per-SerialRecord basis.
 */
class SerialPortConnection {
  private static Map<Serial, SerialPortConnection> portMap = new HashMap<Serial, SerialPortConnection>();

  /**
   * Get the instance for the specified port. Create a new instance if none
   * exists.
   */
  static SerialPortConnection get(PApplet app, Serial serial) {
    SerialPortConnection connection = portMap.get(serial);
    if (connection == null) {
      connection = new SerialPortConnection(app, serial);
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

  public SerialPortConnection(PApplet app, Serial serial) {
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
      String line = serial.readStringUntil('\n');
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
    String line = peek(log);
    if (line != null) {
      pRxLine = line;
      unprocessedRxLine = null;
    }
    return line;
  }
}
