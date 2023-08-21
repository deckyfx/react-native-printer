/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';

import { View, Text, TouchableHighlight } from 'react-native';

import {
  RNPrinter,
  DeviceScanner,
  DeviceScannerEventEmitter,
  RNPrinterEventEmitter,
} from '@decky.fx/react-native-printer';

import type {
  DeviceScanEventPayload,
  DeviceData,
} from '@decky.fx/react-native-printer/DeviceScanner';
import type { RNPrinterEventPayload } from '@decky.fx/react-native-printer/RNPrinter';

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
          RNPrinterEventEmitter.onEvents(
            (event: string, payload: RNPrinterEventPayload) => {
              console.log('RNPrinterEventEmitter', event, payload);
            }
          );
          DeviceScannerEventEmitter.onEvents(
            (event: string, payload: DeviceScanEventPayload) => {
              console.log('DeviceScannerEventEmitter', event, payload);
              if (event === 'DEVICE_FOUND') {
                const device = payload as DeviceData;
                setAddress(device.address);
              }
            }
          );
          DeviceScanner.scan(DeviceScanner.SCAN_SERIAL);
        }}
      >
        <Text>Scan Serial Devices</Text>
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
            RNPrinter.enqueuePrint2(
              {
                connection: RNPrinter.PRINTER_CONNECTION_SERIAL,
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
