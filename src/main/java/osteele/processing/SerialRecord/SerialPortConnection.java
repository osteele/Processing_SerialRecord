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

  private final Serial serial;
  String pTxLine;
  String pRxLine;
  int pRxTime;

  private final PApplet app;
  private String unprocessedRxLine;
  private boolean logToConsole = false;
  boolean logToCanvas = true;

  SerialPortConnection(PApplet app, Serial serial) {
    this.app = app;
    this.serial = serial;
    this.canvasLogger = new CanvasLogger(app, this);
    this.periodicEcho = new PeriodicEchoScheduler(app, this);
  }

  public void logToConsole(boolean flag) {
    this.logToConsole = flag;
  }

  public void logToCanvas(boolean flag) {
    this.logToCanvas = flag;
  }

  public int available() {
    String line = unprocessedRxLine;
    return line == null ? serial.available() : line.length();
  }

  public void clear() {
    unprocessedRxLine = null;
    serial.clear();
  }

  /**
   * If data is available on the serial port, synchronously read a line from
   * the serial port and store the values in the current record.
   */
  String peek() {
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
        if (logToConsole) {
          PApplet.println("Rx: " + line);
        }
        unprocessedRxLine = line;
        return line;
      }
    }
    return null;
  }

  String read() {
    String line = peek();
    if (line != null) {
      pRxLine = line;
      unprocessedRxLine = null;
    }
    return line;
  }

  void writeln(String line) {
    pTxLine = line;
    if (logToConsole) {
      PApplet.println("TX: " + line);
    }
    serial.write(line);
    serial.write('\n');
  }

  //
  // Facade for CanvasLogger
  //

  private final CanvasLogger canvasLogger;

  void drawTxRx() {
    canvasLogger.drawTxRx();
  }

  void drawTxRx(float x, float y) {
    canvasLogger.drawTxRx(x, y);
  }

  //
  // Facade for PeriodicEcho
  //

  private final PeriodicEchoScheduler periodicEcho;

  void periodicEchoRequest(int interval) {
    periodicEcho.interval = interval;
  }

  void requestEcho() {
    serial.write("!e\n");
  }
}
