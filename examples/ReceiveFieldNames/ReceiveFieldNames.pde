/**
 * Example sketch for the SerialRecord library for Processing.
 *
 * Receives two integers from the serial port, and uses them to control the x
 * and y position of a circle on the canvas.
 *
 * The values are retrieved by field name, rather than position. The sender is
 * expected to send records of the form "millis:100,analog:200".
 *
 * This choice of field names is designed to match SendFieldNames example in
 * SerialRecord for Arduino. A more sane choice of field names might be "A0" and
 * "A1" to reflect a different pair of Arduino sources for the values, "x" and
 * "y" to reflect the Processing sketch's use of them.
 */

import processing.serial.*;
import osteele.processing.SerialRecord.*;
import java.util.Arrays;

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

  serialRecord.read();
  int value1 = serialRecord.get("millis", -1);
  int value2 = serialRecord.get("analog", -1);

  float x = map(value1, 0, 1024, 0, width);
  float y = map(value2, 0, 1024, 0, height);
  circle(x, y, 20);
}
