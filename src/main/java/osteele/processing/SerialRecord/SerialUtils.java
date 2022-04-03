package osteele.processing.SerialRecord;

import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.List;

import processing.core.*;
import processing.serial.*;

public class SerialUtils {
  /**
   * Return the first port name that matches "/dev/cu.usbmodem*" or
   * "/dev/tty.usbmodem*".
   * "/dev/cu.…" is preferred.
   *
   * If there are multiple ports that match "/dev/cu.…", or no ports that match
   * "/dev/cu.…"
   * but multiple ports that match "/dev/tty.…", a message is printed to the
   * console, and the
   * name of the first matching port is returned.
   *
   * If there is no matching port, return null.
   */
  public static String findArduinoPort() {
    List<String> ports = Arrays.asList(Serial.list());
    for (var prefix : new String[] { "/dev/cu.usbmodem", "/dev/tty.usbmodem" }) {
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
    return null;
  }
}
