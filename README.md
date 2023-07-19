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
- [ ] Scan Local Network using Socket
- [ ] Scan Local Network using Zeroconf
- [ ] Scan Bluetooth Devices
- [x] Scan USB Devices
- [ ] Print to Network Printer
- [ ] Print to Bluetooth Printer
- [x] Print to USB Printer


## Installation

```sh
npm install @decky.fx/react-native-printer --save
```
or

```sh
yarn add @decky.fx/react-native-printer
```

## Development Instructions

- clone this repo
- `yarn install` 
- `cd example && yarn install`
- if something wrong use `yarn install --ignore-engines`
- recommmended node version are `v16.16.0` or `v18.16.1`
- within example directory: `yarn start`

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
RNPrinter.write(RNPrinter.PRINTER_TYPE_USB, '/dev/usb/001/003', `[C]<img>${imageUri}</img>\n"` + '[L]\n'

// Write to usb device 
RNPrinter.write({
  type: RNPrinter.PRINTER_TYPE_USB,
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
- [x] Scan Local Network using Zeroconf
- [X] Scan Bluetooth Devices
- [x] Scan USB Devices
- [x] Print to Network Printer
- [x] Print to Bluetooth Printer
- [x] Print to USB Printer
- [x] iOS Implementation


## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
