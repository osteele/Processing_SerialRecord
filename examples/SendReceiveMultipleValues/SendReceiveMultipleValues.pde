/* Example sketch for the SerialRecord library for Processing.
 *
 * Sends the x and y position of the mouse on the canvas to
 * the serial port. Reads values back from the serial port, and draws
 * another circle at that position.
 *
 * Click the canvas to request the Arduino to send back the last record that
 * it received.
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
  if (serialPortName == null) {
    exit();
    return;
  }
  serialPort = new Serial(this, serialPortName, 9600);
  // change the number on the next line to send different numbers of values
  sender = new SerialRecord(this, serialPort, 2);
  receiver = new SerialRecord(this, serialPort, 2);
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

  //serialRecord.periodicEchoRequest(100); // uncomment this line to request an echo every 100 ms

  // Display the most recently transmitted (TX) and received (RX) values
  fill(255);
  sender.draw();
}

void mouseClicked() {
  sender.requestEcho();
}
