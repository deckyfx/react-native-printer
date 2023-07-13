# react-native-printer

printing on react native

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

## Usage

```js
// Import modules
import {
  RNPrinter,
  DeviceScanner,
  RNPrinterEventEmitter,
} from '@decky.fx/react-native-printer';

// Listen various event
RNPrinterEventEmitter.onEvents((event, payload) => {});

// Scan usb devices
DeviceScanner.scan(DeviceScanner.SCAN_USB);

// Write to usb device 
RNPrinter.write(DeviceScanner.PRINTER_TYPE_USB, '/dev/usb/001/003', `[C]<img>${imageUri}</img>\n"` + '[L]\n'
);

// unsubscribe listeners if no longer needed
RNPrinterEventEmitter.offEvents();
```

## API

## Constants

## Contributing

See the [contributing guide](CONTRIBUTING.md) to learn how to contribute to the repository and the development workflow.

## License

MIT

---

Made with [create-react-native-library](https://github.com/callstack/react-native-builder-bob)
