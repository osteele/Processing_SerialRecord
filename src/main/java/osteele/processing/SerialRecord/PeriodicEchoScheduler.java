package osteele.processing.SerialRecord;

import processing.core.*;

public class PeriodicEchoScheduler {
  private final PApplet app;
  private final SerialPortConnection portConnection;
  private int pPeriodicEchoRequestTime = 0;
  int interval = 0;

  PeriodicEchoScheduler(PApplet app, SerialPortConnection portConnection) {
    this.app = app;
    this.portConnection = portConnection;
    app.registerMethod("pre", this);
  }

  public void pre() {
    if (interval == 0)
      return;
    if (pPeriodicEchoRequestTime + interval < this.app.millis()) {
      pPeriodicEchoRequestTime = this.app.millis();
      portConnection.requestEcho();
    }
    portConnection.read();
  }
}
