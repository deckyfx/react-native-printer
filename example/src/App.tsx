/* eslint-disable react-native/no-inline-styles */
import * as React from 'react';

import { StyleSheet, View, Text, TouchableHighlight } from 'react-native';

import DocumentPicker from 'react-native-document-picker';

import type { ReactNativePrinter } from '../../src/definitions/index';

import { RNPrinter as RNPrinterModule } from '../../src/index';

import USBPrinter from './USBPrinter';
import NetworkPrinter from './NetworkPrinter';
import SerialPrinter from './SerialPrinter';

const RNPrinter: ReactNativePrinter.RNPrinter = RNPrinterModule;

export default function App() {
  const [address] = React.useState<string | undefined>('');
  const [imageUri, setImageUri] = React.useState<string | undefined>('');

  React.useEffect(() => {
    return () => {};
  }, []);

  return (
    <View style={styles.container}>
      <USBPrinter />
      <NetworkPrinter />
      <SerialPrinter />
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
                  connection: RNPrinter.PRINTER_CONNECTION_USB,
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
            RNPrinter.prunePrintingWorks();
          }}
        >
          <Text>Prune Jobs</Text>
        </TouchableHighlight>
      </View>
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
