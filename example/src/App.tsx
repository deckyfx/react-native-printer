import React, { useEffect, useState } from 'react';

import { StyleSheet, View, Text } from 'react-native';

import {
  TagHelper,
  DesignBuilder,
  RNPrinter,
  JobBuilder,
} from '@decky.fx/react-native-printer';
import type { RowConfig } from '@decky.fx/react-native-printer/ColumnConfig';

import DocumentPicker from 'react-native-document-picker';

import USBPrinter from './USBPrinter';
import NetworkPrinter from './NetworkPrinter';
import BluetoothPrinter from './BluetoothPrinter';
import SerialPrinter from './SerialPrinter';
import Row from './Row';
import Button from './Button';

const App = () => {
  const [address] = useState<string | undefined>('');
  const [imageUri, setImageUri] = useState<string | undefined>('');

  const buildDesign = () => {
    const design = new DesignBuilder(RNPrinter.PRINTING_LINES_MAX_CHAR_56);
    const columnData: RowConfig = [
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
        underline: true,
      },
      {
        width: 15,
        text: 'Rp. 3.000',
        allignment: TagHelper.ALLIGNMENT.RIGHT,
      },
    ];
    design.drawSeparator('-');
    design.addLine(TagHelper.center('Center'));
    design.addLine(TagHelper.left('Left'));
    design.addLine(TagHelper.right('Right'));
    // design.addFormatedLine(TagHelper.image("file://hajsajkjsajs/uiasauis/kjsa.jpg"));
    design.addLines(design.columns(columnData));
    design.drawSeparator('-');
    design.addLine(TagHelper.qrcode('something', 30));
    design.addPrintableCharacters();
    design.drawSeparator('-');
    design.addLine(TagHelper.barcode('something'));
  };

  const selectImage = async () => {
    DocumentPicker.pickSingle({
      presentationStyle: 'fullScreen',
      copyTo: 'cachesDirectory',
      type: ['image/jpg', 'image/png'],
    }).then((_result) => {
      setImageUri(_result.fileCopyUri!!);
    });
  };

  const printImage = async () => {
    if (address && imageUri) {
      const printer = {
        connection: RNPrinter.PRINTER_CONNECTION_BLUETOOTH,
        address: address,
        width: RNPrinter.PRINTING_WIDTH_76_MM,
        maxChars: RNPrinter.PRINTING_LINES_MAX_CHAR_40,
      };
      const jobId = await JobBuilder.begin();

      const designBuilder = new DesignBuilder(
        RNPrinter.PRINTING_LINES_MAX_CHAR_40
      );
      designBuilder.addLine(TagHelper.center(TagHelper.image(imageUri)));
      const designs = designBuilder.designs;
      for (let i = 0; i < designs.length; i++) {
        let line = designs[i]!!;
        await JobBuilder.printLine(jobId, line);
      }
      await JobBuilder.cutPaper(jobId);
      const job = await JobBuilder.build(jobId);
      // RNPrinter.enqueuePrint(job);
      RNPrinter.enqueuePrint(job, printer);
    }
  };

  const pruneJobs = async () => {
    RNPrinter.prunePrintingWorks();
  };

  useEffect(() => {
    return () => {};
  }, []);

  return (
    <View style={styles.container}>
      <USBPrinter />
      <NetworkPrinter />
      <BluetoothPrinter />
      <SerialPrinter />
      <Row>
        <Button text={'Select Image'} onClick={selectImage} />
        <Text>{imageUri}</Text>
      </Row>
      <Row>
        <Button text={'Print With Image'} onClick={printImage} />
        <Button text={'Prune Jobs'} onClick={pruneJobs} />
      </Row>
      <Row>
        <Button text={'Test Design'} onClick={buildDesign} />
      </Row>
    </View>
  );
};

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
export default App;
