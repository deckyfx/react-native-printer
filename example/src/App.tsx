/* eslint-disable react-native/no-inline-styles */
import React, { useEffect, useState } from 'react';

import { StyleSheet, View, Text, TouchableHighlight } from 'react-native';

import { TagHelper, DesignBuilder, RNPrinter } from '@decky.fx/react-native-printer';
import type { ColumnConfiguration } from '@decky.fx/react-native-printer/DesignBuilder';

import DocumentPicker from 'react-native-document-picker';

import USBPrinter from './USBPrinter';
import NetworkPrinter from './NetworkPrinter';
import SerialPrinter from './SerialPrinter';

export default function App() {
  const [address] = useState<string | undefined>('');
  const [imageUri, setImageUri] = useState<string | undefined>('');

  const buildDesign = () => {
    const design = new DesignBuilder(RNPrinter.PRINTING_LINES_MAX_CHAR_56);
    const columnData: ColumnConfiguration[] = [
      {
        width: 26,
        text: 'Product Name with very long name that it should break line',
        allignment: TagHelper.ALLIGNMENT.LEFT,
        spacer: true,
        bold: true,
      },
      {
        width: 15,
        text: '20x',
        allignment: TagHelper.ALLIGNMENT.CENTER,
        underline: true
      },
      {
        width: 15,
        text: 'Rp. 3.000',
        allignment: TagHelper.ALLIGNMENT.RIGHT,
      },
    ];
    design.drawSeparator('-');
    design.addFormatedLine(TagHelper.center('Center'));
    design.addFormatedLine(TagHelper.left('Left'));
    design.addFormatedLine(TagHelper.right('Right'));
    design.addFormatedLine(TagHelper.image("file://hajsajkjsajs/uiasauis/kjsa.jpg"));
    design.addFormatedLines(design.columns(columnData));
    design.drawSeparator('-');
    design.addFormatedLine(TagHelper.qrcode('something', 30));
    design.addPrintableCharacters();
    design.drawSeparator('-');
    design.addFormatedLine(TagHelper.barcode('something'));
  }

  useEffect(() => {
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
          onPress={buildDesign}
        >
          <Text>Test Design</Text>
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
