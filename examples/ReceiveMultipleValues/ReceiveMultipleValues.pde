/**
* Example sketch for the SerialRecord library for Processing.
 *
 * Receives two integers from the serial port, and uses them to control the x
 * and y position of a circle on the canvas.
 */

import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord serialRecord;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  serialPort = new Serial(this, serialPortName, 9600);

  // If the Arduino sketch sends a different number of values, modify the number
  // `2` on the next line to match the number of values that it sends.
  serialRecord = new SerialRecord(this, serialPort, 2);
}

void draw() {
  background(0);

  serialRecord.read();
  int value1 = serialRecord.values[0];
  int value2 = serialRecord.values[1];

  float x = map(value1, 0, 1024, 0, width);
  float y = map(value2, 0, 1024, 0, height);
  circle(x, y, 20);
}
