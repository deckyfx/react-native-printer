import * as React from 'react';

import { Text } from 'react-native';

import {
  RNPrinter,
  DeviceScanner,
  DeviceScannerEventEmitter,
  RNPrinterEventEmitter,
  JobBuilder,
} from '@decky.fx/react-native-printer';
import type {
  DeviceScanEventPayload,
  USBDeviceData,
} from '@decky.fx/react-native-printer/DeviceScanner';
import type { RNPrinterEventPayload } from '@decky.fx/react-native-printer/RNPrinter';

import Row from './Row';
import Button from './Button';

const USBPrinter = () => {
  const [address, setAddress] = React.useState<string | undefined>('');

  const scan = async () => {
    RNPrinterEventEmitter.offEvents();
    DeviceScannerEventEmitter.offEvents();
    RNPrinterEventEmitter.onEvents(
      (event: string, payload: RNPrinterEventPayload) => {
        switch (payload.state!!) {
          case RNPrinter.PRINT_JOB_STATE_ENQUEUED:
          case RNPrinter.PRINT_JOB_STATE_RUNNING:
          case RNPrinter.PRINT_JOB_STATE_SUCCEEDED:
          case RNPrinter.PRINT_JOB_STATE_FAILED:
          case RNPrinter.PRINT_JOB_STATE_CANCELED:
            console.log(event, payload.state, payload.id);
            break;
        }
      }
    );
    DeviceScannerEventEmitter.onEvents(
      (event: string, payload: DeviceScanEventPayload) => {
        console.log('DeviceScannerEventEmitter', event, payload);
        if (event === 'DEVICE_FOUND') {
          const device = payload as USBDeviceData;
          setAddress(device.address);
        }
      }
    );
    const allowed = await RNPrinter.checkPermissions(DeviceScanner.SCAN_USB);
    if (!allowed) {
      RNPrinter.requestPermissions(DeviceScanner.SCAN_USB);
      return;
    }
    DeviceScanner.scan(DeviceScanner.SCAN_USB);
  };

  const print = async () => {
    if (!address) {
      return;
    }
    if (address) {
      const printer = {
        connection: RNPrinter.PRINTER_CONNECTION_USB,
        address: address,
        width: RNPrinter.PRINTING_WIDTH_76_MM,
        maxChars: RNPrinter.PRINTING_LINES_MAX_CHAR_40,
      };
      const jobId = await JobBuilder.begin();
      await JobBuilder.selectPrinter(jobId, printer);
      await JobBuilder.initializePrinter(jobId);
      const designs = RNPrinter.TEST_PRINT_DESIGN.split('\n');
      for (let i = 0; i < designs.length; i++) {
        let line = designs[i]!!;
        await JobBuilder.printLine(jobId, line);
      }
      /*
      await JobBuilder.feedPaper(jobId, 20);
      await JobBuilder.printLine(jobId, '------------------');
      await JobBuilder.feedPaper(jobId, 20);
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.feedPaper(jobId, 20);
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.feedPaper(jobId, 20);
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.printLine(jobId, '--------Sesuatu----------');
      await JobBuilder.feedPaper(jobId, 20);
      await JobBuilder.printLine(jobId, '--------Last----------');
      await JobBuilder.feedPaper(jobId, 100);
      await JobBuilder.printLine(jobId, ' ');
      await JobBuilder.printLine(jobId, ' ');
      await JobBuilder.printLine(jobId, ' ');
      await JobBuilder.printLine(jobId, ' ');
      await JobBuilder.printLine(jobId, ' ');
      await JobBuilder.printLine(jobId, ' ');
      */
      await JobBuilder.cutPaper(jobId);
      const job = await JobBuilder.build(jobId);
      // RNPrinter.enqueuePrint(job);
      RNPrinter.enqueuePrint(job, printer);
    }
  };

  const stop = async () => {
    DeviceScanner.stop(DeviceScanner.SCAN_USB);
  };

  React.useEffect(() => {
    return () => {
      RNPrinterEventEmitter.offEvents();
      DeviceScannerEventEmitter.offEvents();
    };
  }, []);

  return (
    <Row>
      <Button text="Scan USB Devices" onClick={scan} />
      <Text>{address}</Text>
      <Button text="Print" onClick={print} />
      <Button text="Stop Scan" onClick={stop} />
    </Row>
  );
};
export default USBPrinter;
