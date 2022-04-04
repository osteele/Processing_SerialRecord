import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord serialTransport;

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
  serialTransport = new SerialRecord(this, serialPort, 2);
}

void draw() {
  background(0);
  circle(mouseX, mouseY, 20);

  // store some values in serialTransport.values, and send them to the Arduino
  serialTransport.values[0] = int(map(mouseX, 0, width - 1, 0, 1023));
  serialTransport.values[1] = int(map(mouseY, 0, height - 1, 0, 1023));
  serialTransport.send();

  //serialTransport.periodicEchoRequest(100); // uncomment this line to request an echo every 100 ms

  // Receive a line from the Arduino, if it has sent one
  serialTransport.receiveIfAvailable();

  // Display the most recently transmitted (TX) and received (RX) values
  serialTransport.draw();
}

void mouseClicked() {
  serialTransport.requestEcho();
}
