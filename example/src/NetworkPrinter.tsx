import * as React from 'react';

import { Text } from 'react-native';

import {
  RNPrinter,
  DeviceScanner,
  DeviceScannerEventEmitter,
  RNPrinterEventEmitter,
  JobBuilder,
  DesignBuilder,
  TagHelper,
} from '@decky.fx/react-native-printer';
import type {
  DeviceScanEventPayload,
  DeviceData,
} from '@decky.fx/react-native-printer/DeviceScanner';
import type { RNPrinterEventPayload } from '@decky.fx/react-native-printer/RNPrinter';

import Row from './Row';
import Button from './Button';

const NetworkPrinter = () => {
  const [address, setAddress] = React.useState<string | undefined>('');
  const [port, setPort] = React.useState<number | undefined>(0);

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
        if (
          event === DeviceScanner.EVENT_ERROR &&
          payload?.message?.startsWith('Unable to connect')
        ) {
          return;
        }
        console.log('DeviceScannerEventEmitter', event, payload);
        if (event === 'DEVICE_FOUND') {
          const device = payload as DeviceData;
          setAddress(device.address);
          setPort(device.port);
        }
      }
    );

    const allowed = await RNPrinter.checkPermissions(
      DeviceScanner.SCAN_NETWORK
    );
    if (!allowed) {
      RNPrinter.requestPermissions(DeviceScanner.SCAN_NETWORK);
      return;
    }
    DeviceScanner.scan(DeviceScanner.SCAN_NETWORK);
  };

  const print = async () => {
    if (!address) {
      return;
    }
    const printer = {
      connection: RNPrinter.PRINTER_CONNECTION_NETWORK,
      address: address,
      port: port,
      width: RNPrinter.PRINTING_WIDTH_76_MM,
      maxChars: RNPrinter.PRINTING_LINES_MAX_CHAR_40,
    };
    const jobId = await JobBuilder.begin();
    await JobBuilder.selectPrinter(jobId, printer);
    await JobBuilder.initializePrinter(jobId);
    await JobBuilder.setAsDotMatrix(jobId);
    // eslint-disable-next-line react-hooks/rules-of-hooks
    await JobBuilder.useEscAsterisk(jobId);
    const designBuilder = new DesignBuilder(
      RNPrinter.PRINTING_LINES_MAX_CHAR_42
    );

    designBuilder.addRawLine(
      TagHelper.raw(
        TagHelper.hexString(
          '12345678901234567890123456789012345678901234567890'
        )
      )
    );
    designBuilder.addRawLine(
      TagHelper.raw(
        TagHelper.hexString(
          0x1b,
          '!',
          0x11,
          'Hello World -1',
          0x1b,
          '!',
          0x00,
          0x0a
        )
      )
    );
    designBuilder.addLine(
      TagHelper.font('Hello World 1', TagHelper.FONT_SIZE.TALL)
    );
    designBuilder.addBlankLine();
    designBuilder.addLine(
      TagHelper.font('Hello World 2', TagHelper.FONT_SIZE.TALL)
    );
    designBuilder.addLine('12345678901234567890123456789012345678901234567890');
    designBuilder.addLine(
      TagHelper.font('Hello World 3', TagHelper.FONT_SIZE.TALL)
    );
    designBuilder.addLine(
      TagHelper.font('Hello World 4', TagHelper.FONT_SIZE.TALL)
    );

    designBuilder.addLine(
      TagHelper.left(
        TagHelper.image(
          'https://sharktest.b-cdn.net/6512604302babcf4abf1ff28/bill/pandawa_1695704074.jpg'
        )
      )
    );

    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    const designs = designBuilder.designs;
    for (let i = 0; i < designs.length; i++) {
      let line = designs[i]!!;
      await JobBuilder.printLine(jobId, line);
    }

    await JobBuilder.cutPaper(jobId);
    console.log(await JobBuilder.preview(jobId));
    const job = await JobBuilder.build(jobId);
    RNPrinter.enqueuePrint(job, printer);
  };

  const stop = async () => {
    DeviceScanner.stop(DeviceScanner.SCAN_NETWORK);
  };

  React.useEffect(() => {
    return () => {
      RNPrinterEventEmitter.offEvents();
      DeviceScannerEventEmitter.offEvents();
    };
  }, []);

  return (
    <Row>
      <Button text="Scan Network Devices" onClick={scan} />
      <Text>{address && port ? `${address}:${port}` : ''}</Text>
      <Button text="Print" onClick={print} />
      <Button text="Stop Scan" onClick={stop} />
    </Row>
  );
};
export default NetworkPrinter;
