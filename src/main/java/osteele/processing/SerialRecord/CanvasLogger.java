package osteele.processing.SerialRecord;

import processing.core.*;

public class CanvasLogger {
  private final PApplet app;
  private final SerialPortConnection portConnection;

  public CanvasLogger(PApplet app, SerialPortConnection portConnection) {
    this.app = app;
    this.portConnection = portConnection;
    app.registerMethod("draw", this);
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values on the
   * canvas.
   *
   * @param x the x-coordinate of the upper-left corner of the display area
   * @param y the y-coordinate of the upper-left corner of the display area
   */
  void drawTxRx(float x, float y) {
    String pRxLine = portConnection.read();
    String pTxLine = portConnection.pTxLine;
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
  void drawTxRx() {
    float y = this.app.height - 2 * (this.app.textAscent() + this.app.textDescent());
    drawTxRx(10, y);
  }

  public void draw() {
    if (portConnection.logToCanvas) {
      drawTxRx();
    }
  }
}
