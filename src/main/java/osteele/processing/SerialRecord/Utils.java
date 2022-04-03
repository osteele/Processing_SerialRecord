package osteele.processing.SerialRecord;

class Utils {
  static String stringInterpolate(int[] array, String separator) {
    var result = new StringBuffer();
    Boolean first = true;
    for (var elt : array) {
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
}
