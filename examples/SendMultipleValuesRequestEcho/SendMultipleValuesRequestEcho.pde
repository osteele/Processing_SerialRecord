/**
 * Example sketch for the SerialRecord library for Processing.
 *
 * Maps the horizontal and vertical position of the mouse on the canvas to the
 * range 0…1023, and sends them to the serial port.
 *
 * Click the canvas to request the Arduino to send back the last record that it
 * received.
 */

import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord serialRecord;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  serialPort = new Serial(this, serialPortName, 9600);
  serialRecord = new SerialRecord(this, serialPort, 2);
}

void draw() {
  background(0);

  textAlign(CENTER, CENTER);
  textSize(20);
  text("Click to request an echo from the Arduino", 0, 0, width, height);

  circle(mouseX, mouseY, 20);

  // store some values in serialTransport.values, and send them to the Arduino
  serialRecord.values[0] = int(map(mouseX, 0, width - 1, 0, 1023));
  serialRecord.values[1] = int(map(mouseY, 0, height - 1, 0, 1023));
  serialRecord.send();
}

void mouseClicked() {
  serialRecord.requestEcho();
}
