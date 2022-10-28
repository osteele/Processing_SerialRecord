package osteele.processing.SerialRecord;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

import processing.core.*;
import processing.serial.*;

/** Utility functions that make it easier to use the serial port. */
public abstract class SerialUtils {
  SerialUtils() {
  }

  /**
   * Find the serial port that, based on its name, is probably connected to an
   * Arduino.
   *
   * Returns the first port name that matches "/dev/cu.usbmodem*" or
   * "/dev/tty.usbmodem*". "/dev/cu.…" is preferred.
   *
   * If there are multiple ports that match "/dev/cu.…", or no ports that match
   * "/dev/cu.…" but multiple ports that match "/dev/tty.…", a message is
   * printed to the console, and the name of the first matching port is
   * returned.
   *
   * @return the name of the serial port that is probably connected to an
   *         Arduino. If there is no matching port, return null.
   */
  public static String findArduinoPort() {
    List<String> ports = Arrays.asList(Serial.list());
    // macOS: Scan for one of these prefixes.
    // Note: This code will run on other platforms, but is harmless.
    for (String prefix : new String[] { "/dev/cu.usbmodem", "/dev/tty.usbmodem" }) {
      List<String> selected = ports.stream()
          .filter(s -> s.startsWith(prefix))
          .collect(Collectors.toList());
      switch (selected.size()) {
        case 0:
          continue;
        case 1:
          break;
        default:
          PApplet.println("Warning: Multiple serial ports begin with \"" + prefix + "\".");
          PApplet.println("Returning the first one: " + selected.get(0));
          PApplet.print("Other matching ports: ");
          PApplet.println(selected.stream().skip(1).reduce("", (a, b) -> a + (a.isEmpty() ? ""
              : ", ") + b));
      }
      return selected.get(0);
    }
    // Windows: if there is a single COM port, use that.
    // Note: This code differs from the preceding block, in that it will not
    // default to the first matching if there are several.
    // Note: This code will run on other platforms, but is harmless.
    {
      List<String> selected = ports.stream()
          .filter(s -> s.startsWith("COM"))
          .collect(Collectors.toList());
      if (selected.size() == 1) {
        return selected.get(0);
      }
    }
    PApplet.println("SerialUtils: Couldn't determine which serial port is connected to the Arduino.");
    printSerialPorts();
    return null;
  }

  /**
   * Return port if it names a serial port. Otherwise print a list of serial
   * ports, and return null.
   *
   * @param port The name of the serial port to return, if it is present in the
   *             list of serial ports.
   *
   * @return The port argument if it names a serial port, otherwise null.
   */
  public static String findArduinoPort(String port) {
    List<String> ports = Arrays.asList(Serial.list());
    if (ports.contains(port)) {
      return port;
    }
    PApplet.println("SerialUtils: No port with this name is present. Available serial ports:");
    printSerialPorts();
    return null;
  }

  /**
   * Return the index'th port, if the list of serial ports has at least index
   * number of ports. Otherwise return null.
   *
   * @param index The index of the serial port name to return.
   *
   * @return The index'th name for the list of serial ports, if it exists.
   *         Otherwise return null.
   */
  public static String findArduinoPort(int index) {
    String[] ports = Serial.list();
    if (0 <= index && index < ports.length) {
      return ports[index];
    }
    PApplet.println("SerialUtils: No port with this index is present. Available serial ports:");
    printSerialPorts();
    return null;
  }

  private static void printSerialPorts() {
    PApplet.println("Available serial ports:");
    PApplet.printArray(Serial.list());
    String url = "https://github.com/osteele/Processing_SerialRecord/wiki/Find-the-Arduino-Serial-Port";
    PApplet.println("See " + url + " for information on how to identify the appropriate serial port.");
  }
}
