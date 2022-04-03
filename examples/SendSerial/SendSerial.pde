import processing.serial.*;
import osteele.processing.SerialRecord.*;

Serial serialPort;
SerialRecord serialTransport;

void setup() {
  size(500, 500);

  String serialPortName = SerialUtils.findArduinoPort();
  if (serialPortName == null) {
    println("No port found. Ports:");
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

  serialTransport.periodicEchoRequest(100); // uncomment this line to request an echo every 100 ms

  // Receive a line from the Arduino, if it has sent one
  serialTransport.receiveIfAvailable();

  // Display the most recently transmitted (TX) and received (RX) values
  serialTransport.draw();
}

void mouseClicked() {
  serialTransport.requestEcho();
}

// Return the first port name that matches "/dev/cu.usbmodem*" or "/dev/tty.usbmodem*".
// "/dev/cu.…" is preferred.
//
// If there are multiple ports that match "/dev/cu.…", or no ports that match "/dev/cu.…"
// but multiple ports that match "/dev/tty.…", a message is printed to the console, and the
// name of the first matching port is returned.
//
// If there is no matching port, return null.
//String findArduinoPort() {
//  List<String> ports = Arrays.asList(Serial.list());
//  for (var prefix : new String[] {"/dev/cu.usbmodem", "/dev/tty.usbmodem"}) {
//    List<String> selected = ports.stream()
//      .filter(s -> s.startsWith(prefix))
//      .collect(Collectors.toList());
//    switch (selected.size()) {
//    case 0:
//      continue;
//    case 1:
//      break;
//    default:
//      println("Warning: Multiple serial ports begin with \"" + prefix + "\".");
//      println("Returning the first one: " + selected.get(0));
//      print("Other matching ports: ");
//      println(selected.stream().skip(1).reduce("", (a, b) -> a + (a.isEmpty() ? "" : ", ") + b));
//    }
//    return selected.get(0);
//  }
//  return null;
//}

//String stringInterpolate(int[] array, String separator) {
//  var result = new StringBuffer();
//  Boolean first = true;
//  for (var elt: array) {
//    if (first) {
//      first = false;
//    } else {
//      result.append(separator);
//    }
//    result.append(elt);
//  }
//  return result.toString();
//}

//String stringInterpolate(int[] array) {
//  return stringInterpolate(array, ", ");
//}
