> **Warning**
> Experimental

# react-native-printer

printing on react-native

modified from https://github.com/DantSu/ESCPOS-ThermalPrinter-Android, 
written in full Kotlin, using latest sdk, and modern language like coroutines etc.

### Notable change from DantSu's
 - Expose it as react-native's Native Module
 - Add devices scan methods
 - Allow to pass image file path uri or URL for `<img>` tag's payload
 ```js
 const data = '<img>file:///sdcard/image.png</img>'
 ```

## Feature
- [x] Scan Local Network using Socket
- [ ] Scan Local Network using Zeroconf
- [ ] Scan Bluetooth Devices
- [x] Scan USB Devices
- [ ] Scan Serial Devices
- [x] Print to Network Printer
- [ ] Print to Bluetooth Printer
- [x] Print to USB Printer
- [ ] Print to  Serial Devices
- [x] Queue Job Printing using AndroidWorker 


## Installation

```sh
npm install @decky.fx/react-native-printer --save
```
or

```sh
yarn add @decky.fx/react-native-printer
```

## Latest Working Version
**1.0.2-j**

## Tested Printer
 - SEWOO SLK-TS100

## Development Instructions

- clone this repo
- `yarn install` 
- `cd example && yarn install`
- if something wrong use `yarn install --ignore-engines`
- recommmended node version are `v16.16.0` or `v18.16.1`
- within example directory: `yarn start`

## Torubleshoots
to use in older react-native project you may need to edit project build.gradle file

edit
```
minCompileSdk
```
add
```
mavenCentral()
maven { url "https://maven.google.com" }
```
and
```
classpath "org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.22"
```

## Usage

```js
// Import modules
import {
  RNPrinter,
  DeviceScanner,
  RNPrinterEventEmitter,
  DeviceScannerEventEmitter,
} from '@decky.fx/react-native-printer';

// Listen various event
RNPrinterEventEmitter.onEvents((event, payload) => {});
DeviceScannerEventEmitter.onEvents((event, payload) => {});

// Scan usb devices
DeviceScanner.scan(DeviceScanner.SCAN_USB);

// Write to usb device 
RNPrinter.write(RNPrinter.PRINTER_CONNECTION_USB, '/dev/usb/001/003', `[C]<img>${imageUri}</img>\n"` + '[L]\n'

// Write to usb device 
RNPrinter.write({
  connection: RNPrinter.PRINTER_CONNECTION_USB,
  address: '/dev/usb/001/003',
}, RNPrinter.TEST_PRINT_DESIGN);

// unsubscribe listeners if no longer needed
RNPrinterEventEmitter.offEvents();
DeviceScannerEventEmitter.offEvents();
```

## API

## Constants

## TODO
- [x] Scan Local Network using Socket
- [ ] Scan Local Network using Zeroconf
- [ ] Scan Bluetooth Devices
- [x] Scan USB Devices
- [ ] Scan Serial Devices
- [x] Print to Network Printer
- [ ] Print to Bluetooth Printer
- [x] Print to USB Printer
- [ ] Print to  Serial Devices
- [x] Queue Job Printing using AndroidWorker
- [ ] iOS Implementation

## Formatted text : syntax guide

### New line

Use `\n` to create a new line of text.

### Text alignment and column separation

Add an alignment tag on a same line of text implicitly create a new column.

Column alignment tags :

- `[L]` : left side alignment
- `[C]` : center alignment
- `[R]` : right side alignment

Example :

- `[L]Some text` : One column aligned to left
- `[C]Some text` : One column aligned to center
- `[R]Some text` : One column aligned to right
- `[L]Some text[L]Some other text` : Two columns aligned to left. `Some other text` starts in the center of the paper.
- `[L]Some text[R]Some other text` : Two columns, first aligned to left, second aligned to right. `Some other text` is printed at the right of paper.
- `[L]Some[R]text[R]here` : Three columns.
- `[L][R]text[R]here` : Three columns. The first is empty but it takes a third of the available space.

### Font

#### Size

`<font></font>` tag allows you to change the font size and color. Default size is `normal` / `black`.

- `<font size='normal'>Some text</font>` : Normal size
- `<font size='wide'>Some text</font>` : Double width of medium size
- `<font size='tall'>Some text</font>` : Double height of medium size
- `<font size='big'>Some text</font>` : Double width and height of medium size
- `<font size='big-2'>Some text</font>` : 3 x width and height
- `<font size='big-3'>Some text</font>` : 4 x width and height
- `<font size='big-4'>Some text</font>` : 5 x width and height
- `<font size='big-5'>Some text</font>` : 6 x width and height
- `<font size='big-6'>Some text</font>` : 7 x width and height

- `<font color='black'>Some text</font>` : black text - white background
- `<font color='bg-black'>Some text</font>` : white text - black background
- `<font color='red'>Some text</font>` : red text - white background (Not working on all printer)
- `<font color='bg-red'>Some text</font>` : white text - red background (Not working on all printer)

#### Bold

`<b></b>` tag allows you to change the font weight.

- `<b>Some text</b>`

#### Underline

`<u></u>` tag allows you to underline the text.

- `<u>Some text</u>` text underlined
- `<u type='double'>Some text</u>` text double-strike (Not working on all printer)

### Image

`<img></img>` tag allows you to print image. Inside the tag you need to write a hexadecimal string of an image.

Use `PrinterTextParserImg.bitmapToHexadecimalString` to convert `Drawable`, `BitmapDrawable` or `Bitmap` to hexadecimal string.

- `<img>`hexadecimal string of an image`</img>`

**⚠ WARNING ⚠** : This tag has several constraints :

- A line that contains `<img></img>` can have only one alignment tag and it must be at the beginning of the line.
- `<img>` must be directly preceded by nothing or an alignment tag (`[L][C][R]`).
- `</img>` must be directly followed by a new line `\n`.
- You can't write text on a line that contains `<img></img>`.
- Maximum height of printed image is 256px, If you want to print larger bitmap. Please refer to this solution: [#70](https://github.com/DantSu/ESCPOS-ThermalPrinter-Android/issues/70#issuecomment-714390014)

### Barcode

`<barcode></barcode>` tag allows you to print a barcode. Inside the tag you need to write the code number to print.

- `<barcode>451278452159</barcode>` : **(12 numbers)**  
Prints a EAN13 barcode (height: 10mm, width: ~70% printer width, text: displayed below).
- `<barcode type='ean8'>4512784</barcode>` : **(7 numbers)**  
Prints a EAN8 barcode (height: 10mm, width: ~70% printer width, text: displayed below).
- `<barcode type='upca' height='20'>4512784521</barcode>` : **(11 numbers)**  
Prints a UPC-A barcode (height: 20mm, width: ~70% printer width, text: displayed below).
- `<barcode type='upce' height='25' width='50' text='none'>512789</barcode>` : **(6 numbers)**  
Prints a UPC-E barcode (height: 25mm, width: ~50mm, text: hidden).
- `<barcode type='128' width='40' text='above'>DantSu</barcode>` : **(string)**  
Prints a barcode 128 (height: 10mm, width: ~40mm, text: displayed above).

**⚠ WARNING ⚠** : This tag has several constraints :

- A line that contains `<barcode></barcode>` can have only one alignment tag and it must be at the beginning of the line.
- `<barcode>` must be directly preceded by nothing or an alignment tag (`[L][C][R]`).
- `</barcode>` must be directly followed by a new line `\n`.
- You can't write text on a line that contains `<barcode></barcode>`.

### QR Code

`<qrcode></qrcode>` tag allows you to print a QR code. Inside the tag you need to write the QR code data.

- `<qrcode>https://dantsu.com/</qrcode>` :
Prints a QR code with a width and height of 20 millimeters.
- `<qrcode size='25'>123456789</qrcode>` :
Prints a QR code with a width and height of 25 millimeters.

**⚠ WARNING ⚠** : This tag has several constraints :

- A line that contains `<qrcode></qrcode>` can have only one alignment tag and it must be at the beginning of the line.
- `<qrcode>` must be directly preceded by nothing or an alignment tag (`[L][C][R]`).
- `</qrcode>` must be directly followed by a new line `\n`.
- You can't write text on a line that contains `<qrcode></qrcode>`.


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
