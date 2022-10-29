/**
 * Example sketch for the SerialRecord library for Processing.
 *
 * Sends the x and y position of the mouse on the canvas to the serial port.
 * Reads values back from the serial port, and draws another circle at that
 * position.
 *
 * Click the canvas to request the Arduino to send back the last record that it
 * received.
 *
 * Uncomment the line that contains `periodicEchoRequest` to do this
 * automatically.
 */

import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord sender;
SerialRecord receiver;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  serialPort = new Serial(this, serialPortName, 9600);

  // In order to send a different number of values, modify the number `2` on the
  // next line to the number values to send. The corresponding number in the
  // Arduino sketch should be modified as well.
  sender = new SerialRecord(this, serialPort, 2);

  // If the Arduino sketch sends a different number of values, modify the number
  // `3` on the next line to match the number of values that it sends.
  receiver = new SerialRecord(this, serialPort, 3);
}

void draw() {
  background(0);

  // Send values to the Arduino
  sender.values[0] = mouseX;
  sender.values[1] = mouseY;
  sender.send();

  receiver.receiveIfAvailable();

  // draw a green circle at the mouse position
  fill(0, 255, 0);
  circle(mouseX, mouseY, 20);

  // draw a red circle at the received position
  fill(255, 0, 0);
  circle(receiver.values[0], receiver.values[1], 20);
}
