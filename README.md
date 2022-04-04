# Serial Record Library for Processing

Library to send and read single or multiple values to and from the serial port.

The library transmits the values in ASCII. Each record is a sequence of ASCII
representations of numbers, separated by a comma and terminated by a newline.

This library can be used in conjunction with the [Arduino
SerialRecord](https://github.com/osteele/Arduino_SerialRecord) library on
Arduino, but does not require it.

![](docs/screenshot.png "Screenshot")

## Design Goals

- Easy for novice programmers to configure and use
- Easy to inspect the transmitted data
- Detects and provides diagnostics for common errors

### Non-goals

- Efficiency. The library uses an ASCII representation of numbers. This is easy
  to visually inspect without tools, but it is computationally expensive to read
  and write, and requires more bandwidth than a binary representation.
- Flexibility. All records must have the same number of values; only integers
  are supported. This makes it possible to detect errors in code that uses the
  library, but is not appropriate to all communications. If you need more
  flexibility, this is not the library for you. (Consider the Data and I/O
  libraries in the list of [Processing Contributed
  Libraries](https://processing.org/reference/libraries/)).)

## Installation

1. Download a ZIP archive of this repository: Click on the green Code button at
   the top of this page, and press "Download ZIP".
2. Unzip the archive.
3. Move the archive into the folder Process/Libraries, in your home directory.

The next time you start Processing, you will find examples in the File >
Examples menu item. Remember to make a copy of an example before you modify it;
Processing (unlike the Arduino IDE) will not do this for you.

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
