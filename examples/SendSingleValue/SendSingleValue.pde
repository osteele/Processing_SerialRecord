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

  pushStyle();
  textAlign(CENTER, CENTER);
  textSize(20);
  text("Hold the mouse button to send a 1 to the Arduino", 0, 0, width, height);
  popStyle();


  if (mouseButton == LEFT) {
    serialRecord.values[0] = 1;
  } else {
    serialRecord.values[0] = 0;
  }
  serialRecord.send();
}
