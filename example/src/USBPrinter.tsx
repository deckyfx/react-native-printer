/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';

import { View, Text, TouchableHighlight } from 'react-native';

import {
  RNPrinter,
  DeviceScanner,
  JobBuilder,
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
            RNPrinter.enqueuePrint2(
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
          if (address) {
            await JobBuilder.begin();
            await JobBuilder.selectPrinter({
              connection: RNPrinter.PRINTER_CONNECTION_USB,
              address: address,
            });
            const designs = RNPrinter.TEST_PRINT_DESIGN.split('\n');
            for (let i = 0; i < designs.length; i++) {
              let line = designs[i]!!;
              await JobBuilder.printLine(line);
            };
            await JobBuilder.feedPaper(20);
            await JobBuilder.printLine('------------------');
            await JobBuilder.feedPaper(20);
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.feedPaper(20);
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.feedPaper(20);
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.printLine('--------Sesuatu----------');
            await JobBuilder.feedPaper(20);
            await JobBuilder.printLine('--------Last----------');
            await JobBuilder.feedPaper(100);
            await JobBuilder.printLine(' ');
            await JobBuilder.printLine(' ');
            await JobBuilder.printLine(' ');
            await JobBuilder.printLine(' ');
            await JobBuilder.printLine(' ');
            await JobBuilder.printLine(' ');
            await JobBuilder.cutPaper();
            const job = await JobBuilder.build();
            RNPrinter.enqueuePrint(job);
          }
        }}
      >
        <Text>Print Receipt 2</Text>
      </TouchableHighlight>
      <TouchableHighlight
        style={{
          alignItems: 'center',
          backgroundColor: '#DDDDDD',
          padding: 10,
          marginRight: 5,
        }}
        onPress={async () => {
          DeviceScanner.stop(DeviceScanner.SCAN_ALL);
        }}
      >
        <Text>Stop Scan</Text>
      </TouchableHighlight>
    </View>
  );
}
