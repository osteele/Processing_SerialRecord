/* Example sketch for the SerialRecord library for Processing.
 *
 * Maps the horizontal and vertical position of the mouse on the canvas to
 * the range 0…1023, and sends them to the serial port.
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
SerialRecord serialRecord;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  if (serialPortName == null) {
    exit();
    return;
  }
  serialPort = new Serial(this, serialPortName, 9600);
  // change the number on the next line to send different numbers of values
  serialRecord = new SerialRecord(this, serialPort, 2);
}

void draw() {
  background(0);
  circle(mouseX, mouseY, 20);

  // store some values in serialTransport.values, and send them to the Arduino
  serialRecord.values[0] = int(map(mouseX, 0, width - 1, 0, 1023));
  serialRecord.values[1] = int(map(mouseY, 0, height - 1, 0, 1023));
  serialRecord.send();

  //serialRecord.periodicEchoRequest(100); // uncomment this line to request an echo every 100 ms

  // Display the most recently transmitted (TX) and received (RX) values
  serialRecord.draw();
}

void mouseClicked() {
  serialRecord.requestEcho();
}
