import * as React from 'react';

import { StyleSheet, View, Text, Button, TextInput } from 'react-native';

import DocumentPicker from 'react-native-document-picker';

import type { ReactNativePrinter } from '../../src/definitions/index';
import type {
  DeviceScanPayload,
  USBDeviceData,
} from 'src/definitions/DeviceScanner';
import type DSEventEmitter from '../../src/DeviceScannerEventEmitter';
import type RNPEventEmitter from '../../src/RNPrinterEventEmitter';

// @ts-ignore
import {
  RNPrinter as RNPrinterModule,
  DeviceScanner as DeviceScannerModule,
  DeviceScannerEventEmitter as DeviceScannerEventEmitterModule,
  RNPrinterEventEmitter as RNPrinterEventEmitterModule,
} from '@decky.fx/react-native-printer';

const RNPrinter: ReactNativePrinter.RNPrinter = RNPrinterModule;
const DeviceScanner: ReactNativePrinter.DeviceScanner = DeviceScannerModule;
const RNPrinterEventEmitter: DSEventEmitter = RNPrinterEventEmitterModule;
const DeviceScannerEventEmitter: RNPEventEmitter =
  DeviceScannerEventEmitterModule;

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [address, setAddress] = React.useState<string | undefined>('');
  const [imageUri, setImageUri] = React.useState<string | undefined>('');

  React.useEffect(() => {
    RNPrinterEventEmitter.onEvents(
      (event: string, payload: DeviceScanPayload) => {
        console.log('RNPrinterEventEmitter', event, payload);
        if (
          event === 'DEVICE_FOUND' &&
          payload.scanType === DeviceScanner.SCAN_USB
        ) {
          const usbDevice = payload as USBDeviceData;
          setAddress(usbDevice.deviceName);
        }
      }
    );
    DeviceScannerEventEmitter.onEvents(
      (event: string, payload: DeviceScanPayload) => {
        console.log('DeviceScannerEventEmitter', event, payload);
        if (
          event === 'DEVICE_FOUND' &&
          payload.scanType === DeviceScanner.SCAN_USB
        ) {
          const usbDevice = payload as USBDeviceData;
          setAddress(usbDevice.deviceName);
        }
      }
    );
    RNPrinter.multiply(3, 7).then(setResult);
    return () => {
      RNPrinterEventEmitter.offEvents();
      DeviceScannerEventEmitter.offEvents();
    };
  }, []);

  return (
    <View style={styles.container}>
      <Text>This is invoked from Native Modules: {result}</Text>
      <Button
        onPress={async () => {
          DeviceScanner.scan(DeviceScanner.SCAN_USB);
        }}
        title="Scan USB Devices"
        color="#841584"
      />
      <TextInput
        value={address}
        onChangeText={setAddress}
        // eslint-disable-next-line react-native/no-inline-styles
        style={{ borderColor: 'black' }}
      />
      <Button
        onPress={async () => {
          DocumentPicker.pickSingle({
            presentationStyle: 'fullScreen',
            copyTo: 'cachesDirectory',
            type: ['image/jpg', 'image/png'],
          }).then((_result) => {
            setImageUri(_result.fileCopyUri!!);
          });
        }}
        title="Select Image"
        color="#841584"
      />
      <Text>{imageUri}</Text>
      <Button
        onPress={async () => {
          if (address) {
            RNPrinter.enqueuePrint(
              {
                type: RNPrinter.PRINTER_TYPE_USB,
                address: address,
              },
              `[C]<img>${imageUri}</img>\n"` + '[L]\n'
            );
          }
        }}
        title="Print With Image"
        color="#841584"
      />
      <Button
        onPress={async () => {
          if (address) {
            RNPrinter.enqueuePrint(
              {
                type: RNPrinter.PRINTER_TYPE_USB,
                address: address,
              },
              RNPrinter.TEST_PRINT_DESIGN
            );
          }
        }}
        title="Print Receipt"
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
