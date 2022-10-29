/**
 * Example sketch for the SerialRecord library for Processing.
 *
 * Send a zero or 1 the serial port, depending on whether the mouse is on the
 * left or right half of the canvas.
 *
 * This sketch has the same effect as calling `serialPort.println(0)` and
 * `serialPort.println(1)`.
 *
 * If your sketch needs to send only a single value, consider using that
 * function instead of this library.
 *
 * The only advantage of the approach in this sketch is that it is simple to
 * modify it to transmit a second value, as SendMultipleValues demonstrates.
 */

import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord serialRecord;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  serialPort = new Serial(this, serialPortName, 9600);
  serialRecord = new SerialRecord(this, serialPort, 1);
}

void draw() {
  background(0);

  // display instructions
  pushStyle();
  textAlign(CENTER, CENTER);
  textSize(20);
  text("Hold the mouse button to send a 1 to the Arduino", 0, 0, width, height);
  popStyle();

  if (mouseButton == LEFT) {
    serialRecord.values[0] = 1;
  } else {
    serialRecord.values[0] = 0;
  }
  serialRecord.send();
}
