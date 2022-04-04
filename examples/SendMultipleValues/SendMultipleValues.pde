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
