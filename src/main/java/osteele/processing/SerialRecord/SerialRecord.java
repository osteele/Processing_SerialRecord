package osteele.processing.SerialRecord;

import processing.core.*;
import processing.serial.*;
// import osteele.processing.SerialRecord.Utils;

public class SerialRecord {
  public int size;
  public int values[];
  public Serial serialPort;
  public boolean log = false;

  public SerialRecord(PApplet app, Serial port, int size) {
    this.app = app;
    this.serialPort = port;
    this.values = new int[size];
  }

  public void send() {
    var record = Utils.stringInterpolate(values, ",");
    if (log) {
      PApplet.println("TX: " + record);
    }
    serialPort.write(record);
    serialPort.write('\n');
  }

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

  /** Display the last transmitted (TX) and received (RX) values */
  public void draw(float x, float y) {
    this.app.text("TX: " + Utils.stringInterpolate(values, ","), x, y);
    String rxLine = pRxLine;
    if (rxLine == null || !rxLine.isEmpty()) {
      y += this.app.textAscent() + this.app.textDescent();
      var message = "Click to request an echo from the Arduino";
      if (rxLine != null) {
        message = rxLine;
        String ageString = null;
        int age = this.app.millis() - pRxTime;
        if (age < 1000) {
        } else if (age < 1000) {
          ageString = "" + age + "ms";
        } else if (age < 60 * 1000) {
          ageString = "" + (age / 1000) + "s";
        } else {
          int minutes = age / 60 / 1000;
          ageString = "" + minutes + " minute";
          if (minutes > 1)
            ageString += "s";
        }
        if (ageString != null) {
          message += "(" + ageString + " ago)";
        }
      }
      this.app.text("RX: " + message, x, y);
    }
  }

  /** Display the last transmitted (TX) and received (RX) values */
  public void draw() {
    var y = this.app.height - 2 * (this.app.textAscent() + this.app.textDescent());
    draw(10, y);
  }

  public void periodicEchoRequest(int interval) {
    if (pPeriodicEchoRequestTime + interval < this.app.millis()) {
      this.requestEcho();
      pPeriodicEchoRequestTime = this.app.millis();
    }
  }

  void processReceivedLine(String line) {
  }

  public void requestEcho() {
    serialPort.write("!e\n");
  }

  private PApplet app;
  private String pRxLine;
  private int pRxTime;
  private int pPeriodicEchoRequestTime = 0;
}
