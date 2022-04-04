/* Example sketch for the SerialRecord library for Processing.
 *
 * Receives an integers from the serial port, and use it to control
 * the horizontal positon of a line on the canvas.
 */

import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord serialRecord;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  if (serialPortName == null) {
    println("No Arduino port found. Available serial ports:");
    printArray(Serial.list());
    exit();
    return;
  }
  println("Connect to " + serialPortName);
  serialPort = new Serial(this, serialPortName, 9600);
  serialRecord = new SerialRecord(this, serialPort, 1);
}

void draw() {
  background(0);

  serialRecord.read();
  int value = serialRecord.get();

  float x = value / 100 % width;
  line(x, 0, x, height);

  serialRecord.draw();
}
