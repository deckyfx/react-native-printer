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
  DeviceData,
} from '@decky.fx/react-native-printer/DeviceScanner';
import type { RNPrinterEventPayload } from '@decky.fx/react-native-printer/RNPrinter';

import Row from './Row';
import Button from './Button';

const USBPrinter = () => {
  const [address, setAddress] = React.useState<string | undefined>('');

  const scan = async () => {
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

    const allowed = await RNPrinter.checkPermissions(
      DeviceScanner.SCAN_BLUETOOTH
    );
    if (!allowed) {
      RNPrinter.requestPermissions(DeviceScanner.SCAN_BLUETOOTH);
      return;
    }
    DeviceScanner.scan(DeviceScanner.SCAN_BLUETOOTH);
  };

  const print = async () => {
    if (address) {
      const jobId = await JobBuilder.begin();
      await JobBuilder.selectPrinter(jobId, {
        connection: RNPrinter.PRINTER_CONNECTION_BLUETOOTH,
        address: address,
      });
      await JobBuilder.initializePrinter(jobId);
      const designs = RNPrinter.TEST_PRINT_DESIGN.split('\n');
      for (let i = 0; i < designs.length; i++) {
        let line = designs[i]!!;
        await JobBuilder.printLine(jobId, line);
      }
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
      await JobBuilder.cutPaper(jobId);
      const job = await JobBuilder.build(jobId);
      RNPrinter.enqueuePrint(job);
    }
  };

  const print2 = async () => {
    if (address) {
      RNPrinter.enqueuePrint2(
        {
          connection: RNPrinter.PRINTER_CONNECTION_BLUETOOTH,
          address: address,
        },
        RNPrinter.TEST_PRINT_DESIGN,
        true,
        true
      );
    }
  };

  const stop = async () => {
    DeviceScanner.stop(DeviceScanner.SCAN_BLUETOOTH);
  };

  React.useEffect(() => {
    return () => {
      RNPrinterEventEmitter.offEvents();
      DeviceScannerEventEmitter.offEvents();
    };
  }, []);

  return (
    <Row>
      <Button text="Scan BT Devices" onClick={scan} />
      <Text>{address}</Text>
      <Button text="Print" onClick={print} />
      <Button text="Print (Deprecated)" onClick={print2} />
      <Button text="Stop Scan" onClick={stop} />
    </Row>
  );
};
export default USBPrinter;
