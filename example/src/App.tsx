import * as React from 'react';

import { StyleSheet, View, Text, Button, TextInput } from 'react-native';

import DocumentPicker from 'react-native-document-picker';

import type { ReactNativePrinter } from '../../src/definitions/index';
import type {
  DeviceFoundPayload,
  USBDeviceData,
} from 'src/definitions/DeviceScanner';
import type ExtendedNativeEventEmitter from '../../src/ExtendedNativeEventEmitter';

// @ts-ignore
import {
  RNPrinter as RNPrinterModule,
  DeviceScanner as DeviceScannerModule,
  RNPrinterEventEmitter as RNPrinterEventEmitterModule,
} from '@decky.fx/react-native-printer';

const RNPrinter: ReactNativePrinter.RNPrinter = RNPrinterModule;
const DeviceScanner: ReactNativePrinter.DeviceScanner = DeviceScannerModule;
const RNPrinterEventEmitter: ExtendedNativeEventEmitter =
  RNPrinterEventEmitterModule;

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [address, setAddress] = React.useState<string | undefined>('');
  const [imageUri, setImageUri] = React.useState<string | undefined>('');

  React.useEffect(() => {
    RNPrinterEventEmitter.onEvents(
      (event: string, payload: DeviceFoundPayload) => {
        console.log(event, payload);
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
            RNPrinter.write(
              DeviceScanner.PRINTER_TYPE_USB,
              address,
              `[C]<img>${imageUri}</img>\n"` + '[L]\n'
            );
          }
        }}
        title="Print USB"
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
