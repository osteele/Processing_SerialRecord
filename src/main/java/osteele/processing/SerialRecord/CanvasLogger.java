package osteele.processing.SerialRecord;

import processing.core.*;

/**
 * This class is public in order to use Processing's "registered library
 * method" mechanism. It is not intended for direct use.
 */
public class CanvasLogger {
  private final PApplet app;
  private final SerialPortConnection portConnection;
  private final float defaultX = 10;
  private final float defaultY = -1;

  private String pRxLine;
  private String pTxLine;
  private int pRxTime;
  private float pBoxWidth = 0;

  CanvasLogger(PApplet app, SerialPortConnection portConnection) {
    this.app = app;
    this.portConnection = portConnection;
    app.registerMethod("draw", this);
  }

  void observe(SerialLineEventType type, int timestamp) {
    assert type == SerialLineEventType.RX_TIME_MSG;
    pRxTime = timestamp;
  }

  void observe(SerialLineEventType type, String line) {
    switch (type) {
      case RX_TIME_MSG:
        assert false;
        break;
      case RX_LINE_MSG:
        pRxLine = line;
        break;
      case TX_LINE_MSG:
        pTxLine = line;
        break;
    }
  }

  void drawTxRx(float x, float y, boolean eraseBackground) {
    String txMessage = pTxLine == null ? null : "TX: " + pTxLine;
    String rxMessage = null;
    if (pRxLine != null && !pRxLine.isEmpty()) {
      rxMessage = "RX: " + pRxLine;
      int age = app.millis() - pRxTime;
      if (age >= 1000) {
        rxMessage += String.format(" (%s ago)", Utils.humanTime(age));
      }
    }
    if (txMessage == null && rxMessage == null)
      return;
    app.pushStyle();
    float lineHeight = app.textAscent() + app.textDescent();
    float boxHeight = 2 * lineHeight;
    if (y < 0) {
      y = this.app.height - boxHeight;
    }
    if (eraseBackground) {
      float boxWidth = PApplet.max(
          rxMessage == null ? 0 : app.textWidth(rxMessage),
          txMessage == null ? 0 : app.textWidth(txMessage));
      app.fill(app.g.backgroundColor);
      app.noStroke();
      app.rect(x, y - app.textAscent(),
          PApplet.max(pBoxWidth, boxWidth), boxHeight);
      app.fill(app.brightness(app.g.backgroundColor) < 128 ? 255 : 0);
      pBoxWidth = boxWidth;
    }
    if (txMessage != null) {
      app.text(txMessage, x, y);
      y += lineHeight;
    }
    if (rxMessage != null) {
      app.text(rxMessage, x, y);
    }
    app.popStyle();
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values on the
   * canvas.
   *
   * @param x the x-coordinate of the upper-left corner of the display area
   * @param y the y-coordinate of the upper-left corner of the display area
   */
  void drawTxRx(float x, float y) {
    drawTxRx(x, y, false);
  }

  /**
   * Display the most-recently transmitted (TX) and received (RX) values at the
   * lower left corner of the canvas.
   */
  void drawTxRx() {
    drawTxRx(defaultX, defaultY, false);
  }

  public void draw() {
    if (portConnection.logToCanvas) {
      app.g.pushStyle();
      app.resetMatrix();
      app.textSize(12);
      app.textAlign(PApplet.LEFT);
      app.fill(app.g.backgroundColor);
      app.fill(255);
      drawTxRx(defaultX, defaultY, true);
      app.popStyle();
    }
  }
}
