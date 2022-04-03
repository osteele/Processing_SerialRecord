# Serial Record Library for Processing

Library to send and read single or multiple values to and from the serial port.

The library transmits the values in ASCII. Each record is a sequence of ASCII
representations of numbers, separated by a comma and terminated by a newline.
This is the format used by the Arduino IDE Serial Plotter tool.

## Design Goals

- Easy for novice programmers to configure and use
- Easy to inspect the transmitted data
- Detects and provides diagnostics for common errors

### Non-goals

- Efficiency. This use ASCII, which is easy to inspect but computationally expensive to
  read and write, and requires more bandwidth than a binary representation.
- Flexibility. All records must have the same number of values. This makes it
  possible to detect errors in code that uses the library, but is not
  appropriate to all communications. If you need more flexibility, this is not
  the library for you. (See the Alternatives section below.)

## Installation

1. Download a ZIP archive of this repository: Click on the green Code button at
   the top of this page, and press "Download ZIP".
2. Unzip the archive.
3. Move the archive into the folder Process/Libraries, in your home directory.

## Features

- Transmit and received lines can be displayed on the canvas, and/or logged
  to the console.
- Received records that have too few or too many values result in a warning
  in the console.
- When used with the Arduino_SerialRecord library, a command be used to request
  that the Arduino send back the values that it received, for debugging. This
  can be done once, or at periodic intervals.

See the Arduino_SerialRecord library for a matching Arduino library, Arduino
examples, and additional information .

## Acknowledgements

The idea of providing this code as a library was inspired by copy-paste code
provided to students by the NYU Shanghai IMA "Interaction Lab" course.

## License

Copyright (C) 2020-2022 Oliver Steele. This software is made available under the
terms of the GNU LGPL License.
