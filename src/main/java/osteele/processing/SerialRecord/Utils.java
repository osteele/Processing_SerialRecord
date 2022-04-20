package osteele.processing.SerialRecord;

/** Utilities that are private to the package. */
interface Utils {
  static String humanTime(int age) {
    if (age < 1000) {
      return String.format("%d ms", age);
    } else if (age < 60 * 1000) {
      return String.format("%d s", age / 1000);
    } else {
      int minutes = age / 60 / 1000;
      String s = String.format("%d minute", minutes);
      if (minutes > 1)
        s += "s";
      return s;
    }
  }

  static String stringInterpolate(int[] array, String separator) {
    StringBuffer result = new StringBuffer();
    Boolean first = true;
    for (int elt : array) {
      if (first) {
        first = false;
      } else {
        result.append(separator);
      }
      result.append(elt);
    }
    return result.toString();
  }

  static String stringInterpolate(int[] array) {
    return stringInterpolate(array, ", ");
  }

  static String trimRight(String line) {
    // optimized for the case where there is 0 or 1 trailing newline
    while (!line.isEmpty()
        && (line.endsWith("\r") || line.endsWith("\n")
            || Character.getNumericValue(line.charAt(line.length() - 1)) == -1)) {
      line = line.substring(0, line.length() - 1);
    }
    return line;
  }
}
