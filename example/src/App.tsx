import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Button,
  NativeEventEmitter,
  TextInput,
} from 'react-native';

import type { ReactNativePrinter } from '../../src/definitions/index';

// @ts-ignore
import {
  RNPrinter as RNPrinterModule,
  DeviceScanner as DeviceScannerModule,
  DeviceScannerEventEmitter as DeviceScannerEventEmitterModule,
  RNPrinterEventEmitter as RNPrinterEventEmitterModule,
} from '@decky.fx/react-native-printer';

const RNPrinter: ReactNativePrinter.RNPrinter = RNPrinterModule;
const DeviceScanner: ReactNativePrinter.DeviceScanner = DeviceScannerModule;
const DeviceScannerEventEmitter: NativeEventEmitter =
  DeviceScannerEventEmitterModule;
const RNPrinterEventEmitter: NativeEventEmitter = RNPrinterEventEmitterModule;

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [address, setAddress] = React.useState<string | undefined>(
    '/dev/bus/usb/001/003'
  );

  React.useEffect(() => {
    DeviceScannerEventEmitter.addListener(
      DeviceScanner.EVENT_START_SCAN,
      (...args) => {
        console.log(DeviceScanner.EVENT_START_SCAN, ...args);
      }
    );
    DeviceScannerEventEmitter.addListener(
      DeviceScanner.EVENT_STOP_SCAN,
      (...args) => {
        console.log(DeviceScanner.EVENT_STOP_SCAN, ...args);
      }
    );
    DeviceScannerEventEmitter.addListener(
      DeviceScanner.EVENT_DEVICE_FOUND,
      (...args) => {
        console.log(DeviceScanner.EVENT_DEVICE_FOUND, ...args);
      }
    );
    DeviceScannerEventEmitter.addListener(
      DeviceScanner.EVENT_OTHER,
      (...args) => {
        console.log(DeviceScanner.EVENT_OTHER, ...args);
      }
    );
    DeviceScannerEventEmitter.addListener(
      DeviceScanner.EVENT_ERROR,
      (...args) => {
        console.log(DeviceScanner.EVENT_ERROR, ...args);
      }
    );

    RNPrinterEventEmitter.addListener(
      DeviceScanner.EVENT_START_SCAN,
      (...args) => {
        console.log(DeviceScanner.EVENT_START_SCAN, ...args);
      }
    );
    RNPrinterEventEmitter.addListener(
      DeviceScanner.EVENT_STOP_SCAN,
      (...args) => {
        console.log(DeviceScanner.EVENT_STOP_SCAN, ...args);
      }
    );
    RNPrinterEventEmitter.addListener(
      DeviceScanner.EVENT_DEVICE_FOUND,
      (...args) => {
        console.log(DeviceScanner.EVENT_DEVICE_FOUND, ...args);
      }
    );
    RNPrinterEventEmitter.addListener(DeviceScanner.EVENT_OTHER, (...args) => {
      console.log(DeviceScanner.EVENT_OTHER, ...args);
    });
    RNPrinterEventEmitter.addListener(DeviceScanner.EVENT_ERROR, (...args) => {
      console.log(DeviceScanner.EVENT_ERROR, ...args);
    });
    RNPrinter.multiply(3, 7).then(setResult);
    return () => {
      DeviceScannerEventEmitter.removeAllListeners(DeviceScanner.EVENT_ERROR);
      DeviceScannerEventEmitter.removeAllListeners(DeviceScanner.EVENT_OTHER);
      DeviceScannerEventEmitter.removeAllListeners(
        DeviceScanner.EVENT_DEVICE_FOUND
      );
      DeviceScannerEventEmitter.removeAllListeners(
        DeviceScanner.EVENT_STOP_SCAN
      );
      DeviceScannerEventEmitter.removeAllListeners(
        DeviceScanner.EVENT_START_SCAN
      );

      RNPrinterEventEmitter.removeAllListeners(DeviceScanner.EVENT_ERROR);
      RNPrinterEventEmitter.removeAllListeners(DeviceScanner.EVENT_OTHER);
      RNPrinterEventEmitter.removeAllListeners(
        DeviceScanner.EVENT_DEVICE_FOUND
      );
      RNPrinterEventEmitter.removeAllListeners(DeviceScanner.EVENT_STOP_SCAN);
      RNPrinterEventEmitter.removeAllListeners(DeviceScanner.EVENT_START_SCAN);
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text>This is invoked from Native Modules: {result}</Text>
      <TextInput value={address} onChangeText={setAddress} />
      <Button
        onPress={async () => {
          RNPrinter.getUsbPrintersCount()
            .then((count) => {
              console.log('USB Printer detected', count);
              return RNPrinter.requestPermissions(DeviceScanner.SCAN_USB);
            })
            .then((_result: boolean) => {
              if (!_result) {
                console.log('Something wrong');
              }
            });
        }}
        title="Check USB Devices"
        color="#841584"
      />
      <Button
        onPress={async () => {
          if (address) {
            RNPrinter.getPrinterModel(DeviceScanner.PRINTER_TYPE_USB, address)
              .then((_result) => {
                console.log('Device Name', _result);
              })
              .catch((error) => {
                console.error(error);
              });
          }
        }}
        title="Scan USB"
        color="#841584"
      />
    </View>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  box: {
    width: 60,
    height: 60,
    marginVertical: 20,
  },
});
