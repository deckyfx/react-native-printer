/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';

import { View, Text, TouchableHighlight } from 'react-native';

import type { ReactNativePrinter } from '../../src/definitions/index';
import type {
  DeviceScanPayload,
  DeviceData,
} from 'src/definitions/DeviceScanner';
import type DSEventEmitter from '../../src/DeviceScannerEventEmitter';
import type RNPEventEmitter from '../../src/RNPrinterEventEmitter';

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
  const [address, setAddress] = React.useState<string | undefined>('');

  React.useEffect(() => {
    return () => {
      RNPrinterEventEmitter.offEvents();
      DeviceScannerEventEmitter.offEvents();
    };
  }, []);

  return (
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
          RNPrinterEventEmitter.onEvents((event: string, payload: any) => {
            console.log('RNPrinterEventEmitter', event, payload);
          });
          DeviceScannerEventEmitter.onEvents(
            (event: string, payload: DeviceScanPayload) => {
              console.log('DeviceScannerEventEmitter', event, payload);
              if (event === 'DEVICE_FOUND') {
                const device = payload as DeviceData;
                setAddress(device.address);
              }
            }
          );
          DeviceScanner.scan(DeviceScanner.SCAN_USB);
        }}
      >
        <Text>Scan USB Devices</Text>
      </TouchableHighlight>
      <Text>{address}</Text>
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
                connection: RNPrinter.PRINTER_CONNECTION_USB,
                address: address,
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
      <TouchableHighlight
        style={{
          alignItems: 'center',
          backgroundColor: '#DDDDDD',
          padding: 10,
          marginRight: 5,
        }}
        onPress={async () => {
          RNPrinterEventEmitter.offEvents();
          DeviceScannerEventEmitter.offEvents();
        }}
      >
        <Text>Stop Scan</Text>
      </TouchableHighlight>
    </View>
  );
}
