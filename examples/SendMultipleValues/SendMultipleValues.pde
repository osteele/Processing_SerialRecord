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
  // In order to send a different number of values, modify the number `2` on the
  // next line to the number values to send. The corresponding number in the
  // Arduino sketch should be modified as well.
  serialRecord = new SerialRecord(this, serialPort, 2);

  // Uncomment the `serialRecord.periodicEchoRequest(…)` line to request an echo
  // every 100 ms. See
  // <https://github.com/osteele/Processing_SerialRecord/wiki/Code-Recipes> for
  // more information.

  serialRecord.periodicEchoRequest(100);
}

void draw() {
  background(0);
  circle(mouseX, mouseY, 20);

  // store some values in serialTransport.values, and send them to the Arduino
  serialRecord.values[0] = int(map(mouseX, 0, width - 1, 0, 1023));
  serialRecord.values[1] = int(map(mouseY, 0, height - 1, 0, 1023));
  serialRecord.send();
}

void mouseClicked() {
  serialRecord.requestEcho();
}
