package osteele.processing.SerialRecord;

import processing.core.*;

/**
 * This class is public in order to use Processing's "registered library
 * method" mechanism. It is not intended for direct use.
 */
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
      app.text("TX: " + pTxLine, x, y);
    }
    if (pRxLine == null || !pRxLine.isEmpty()) {
      y += app.textAscent() + app.textDescent();
      String message = "---";
      if (pRxLine != null) {
        message = pRxLine;
        int age = app.millis() - portConnection.pRxTime;
        if (age >= 1000) {
          message += String.format(" (%s ago)", Utils.humanTime(age));
        }
      }
      app.text("RX: " + message, x, y);
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
      app.resetMatrix();
      app.textSize(12);
      app.textAlign(PApplet.LEFT);
      drawTxRx();
    }
  }
}
