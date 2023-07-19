/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  TouchableHighlight,
  TextInput,
} from 'react-native';

import DocumentPicker from 'react-native-document-picker';

import type { ReactNativePrinter } from '../../src/definitions/index';
import type {
  DeviceScanPayload,
  DeviceData,
} from 'src/definitions/DeviceScanner';
import type DSEventEmitter from '../../src/DeviceScannerEventEmitter';
import type RNPEventEmitter from '../../src/RNPrinterEventEmitter';

// @ts-ignore
import {
  RNPrinter as RNPrinterModule,
  DeviceScanner as DeviceScannerModule,
  DeviceScannerEventEmitter as DeviceScannerEventEmitterModule,
  RNPrinterEventEmitter as RNPrinterEventEmitterModule,
} from '../../src/index';

const RNPrinter: ReactNativePrinter.RNPrinter = RNPrinterModule;
const DeviceScanner: ReactNativePrinter.DeviceScanner = DeviceScannerModule;
const RNPrinterEventEmitter: RNPEventEmitter = RNPrinterEventEmitterModule;
const DeviceScannerEventEmitter: DSEventEmitter =
  DeviceScannerEventEmitterModule;

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();
  const [address, setAddress] = React.useState<string | undefined>('');
  const [port, setPort] = React.useState<number | undefined>(0);
  const [imageUri, setImageUri] = React.useState<string | undefined>('');

  React.useEffect(() => {
    RNPrinterEventEmitter.onEvents((event: string, payload: any) => {
      console.log('RNPrinterEventEmitter', event, payload);
    });
    DeviceScannerEventEmitter.onEvents(
      (event: string, payload: DeviceScanPayload) => {
        console.log('DeviceScannerEventEmitter', event, payload);
        if (event === 'DEVICE_FOUND') {
          const device = payload as DeviceData;
          setAddress(device.address);
          setPort(device.port);
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
      <View
        style={{
          flexDirection: 'row',
          marginBottom: 5,
        }}
      >
        <TouchableHighlight
          style={{
            alignItems: 'center',
            backgroundColor: '#DDDDDD',
            padding: 10,
            marginRight: 5,
          }}
          onPress={async () => {
            DeviceScanner.scan(DeviceScanner.SCAN_USB);
          }}
        >
          <Text>Scan USB Devices</Text>
        </TouchableHighlight>
        <TextInput
          value={address}
          onChangeText={setAddress}
          style={{ borderColor: 'black', borderWidth: 1, width: 200 }}
        />
      </View>
      <View
        style={{
          flexDirection: 'row',
          marginBottom: 5,
        }}
      >
        <TouchableHighlight
          style={{
            alignItems: 'center',
            backgroundColor: '#DDDDDD',
            padding: 10,
            marginRight: 5,
          }}
          onPress={async () => {
            DeviceScanner.scan(DeviceScanner.SCAN_NETWORK);
          }}
        >
          <Text>Scan Network Devices</Text>
        </TouchableHighlight>
        <TextInput
          value={`${address}:${port}`}
          onChangeText={setAddress}
          style={{ borderColor: 'black', borderWidth: 1, width: 200 }}
        />
      </View>
      <View
        style={{
          flexDirection: 'row',
          marginBottom: 5,
        }}
      >
        <TouchableHighlight
          style={{
            alignItems: 'center',
            backgroundColor: '#DDDDDD',
            padding: 10,
            marginRight: 5,
          }}
          onPress={async () => {
            DocumentPicker.pickSingle({
              presentationStyle: 'fullScreen',
              copyTo: 'cachesDirectory',
              type: ['image/jpg', 'image/png'],
            }).then((_result) => {
              setImageUri(_result.fileCopyUri!!);
            });
          }}
        >
          <Text>Select Image</Text>
        </TouchableHighlight>
        <Text>{imageUri}</Text>
      </View>
      <View
        style={{
          flexDirection: 'row',
          marginBottom: 5,
        }}
      >
        <TouchableHighlight
          style={{
            alignItems: 'center',
            backgroundColor: '#DDDDDD',
            padding: 10,
            marginRight: 5,
          }}
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
        >
          <Text>Print With Image</Text>
        </TouchableHighlight>
        <TouchableHighlight
          style={{
            alignItems: 'center',
            backgroundColor: '#DDDDDD',
            padding: 10,
            marginRight: 5,
          }}
          onPress={async () => {
            if (address) {
              RNPrinter.enqueuePrint(
                {
                  type: RNPrinter.PRINTER_TYPE_NETWORK,
                  address: address,
                  port: port,
                },
                RNPrinter.TEST_PRINT_DESIGN,
                true,
                true
              );
            }
          }}
        >
          <Text>Print Receipt</Text>
        </TouchableHighlight>
      </View>
      <TouchableHighlight
        style={{
          alignItems: 'center',
          backgroundColor: '#DDDDDD',
          padding: 10,
          marginRight: 5,
        }}
        onPress={async () => {
          RNPrinter.prunePrintingWorks();
        }}
      >
        <Text>Prune Jobs</Text>
      </TouchableHighlight>
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
