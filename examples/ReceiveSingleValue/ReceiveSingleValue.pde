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
    exit();
    return;
  }
  serialPort = new Serial(this, serialPortName, 9600);
  serialRecord = new SerialRecord(this, serialPort, 1);
}

void draw() {
  background(0);

  serialRecord.read();
  int value = serialRecord.get();

  float y = map(value2, 0, 1024, 0, height);
  line(x, 0, x, height);

  serialRecord.draw();
}
