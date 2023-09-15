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
      width: RNPrinter.PRINTING_WIDTH_80_MM,
      maxChars: RNPrinter.PRINTING_LINES_MAX_CHAR_42,
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
    /*
    const designs = RNPrinter.TEST_PRINT_DESIGN.split('\n');
    for (let i = 0; i < designs.length; i++) {
      let line = designs[i]!!;
      await JobBuilder.printLine(jobId, line);
    }
    const table = new TableBuilder(
      {
        width: 5,
        allignment: TagHelper.ALLIGNMENT.LEFT,
        spacer: true,
      },
      {
        width: 0,
        allignment: TagHelper.ALLIGNMENT.CENTER,
      },
      {
        width: 5,
        allignment: TagHelper.ALLIGNMENT.RIGHT,
      }
    )
      .rows(['L'.repeat(5), 'C'.repeat(30), 'R'.repeat(5)])
      .build();
    designBuilder.addTable(table);
    const table2 = new TableBuilder(
      {
        width: 5,
        allignment: TagHelper.ALLIGNMENT.LEFT,
        spacer: true,
      },
      {
        width: 0,
        allignment: TagHelper.ALLIGNMENT.LEFT,
      },
      {
        width: 5,
        allignment: TagHelper.ALLIGNMENT.LEFT,
      }
    )
      .rows(['L', 'L', 'L'])
      .build();
    designBuilder.addTable(table2);
    const table3 = new TableBuilder(
      {
        width: 5,
        allignment: TagHelper.ALLIGNMENT.RIGHT,
        spacer: true,
      },
      {
        width: 0,
        allignment: TagHelper.ALLIGNMENT.RIGHT,
      },
      {
        width: 5,
        allignment: TagHelper.ALLIGNMENT.RIGHT,
      }
    )
      .rows(['R', 'R', 'R'])
      .build();
    designBuilder.addTable(table3);
    const table4 = new TableBuilder(
      {
        width: 6,
        allignment: TagHelper.ALLIGNMENT.CENTER,
        spacer: true,
      },
      {
        width: 0,
        allignment: TagHelper.ALLIGNMENT.CENTER,
      },
      {
        width: 7,
        allignment: TagHelper.ALLIGNMENT.CENTER,
      }
    )
      .rows(['C', 'C', 'C'])
      .build();
    designBuilder.addTable(table4);
    designBuilder.addLine('0123456789'.repeat(5));
    designBuilder.addLine(TagHelper.right('Right'));
    designBuilder.addLine(
      TagHelper.center(
        TagHelper.image(
          'https://sharktest.b-cdn.net/64dc6ab1a30b6a3da3d56157/bill/Genshin_Impact_logo_1694678343.jpg'
        )
      )
    );
    */
    designBuilder.addLine('Normal');
    designBuilder.addLine('Normal');
    designBuilder.addLine(TagHelper.bold('Bold'));
    designBuilder.addLine('Normal');
    designBuilder.addLine(
      TagHelper.underline(TagHelper.font('Underlined', null, null, true))
    );
    designBuilder.addLine('Normal');
    designBuilder.addLine(TagHelper.bold(TagHelper.underline('Underlined')));
    designBuilder.addLine('Normal');
    designBuilder.addLine(
      TagHelper.font('Tall', TagHelper.FONT_SIZE.TALL, null, true)
    );
    designBuilder.addLine('Normal');
    designBuilder.addLine(TagHelper.font('Wide', TagHelper.FONT_SIZE.WIDE));
    designBuilder.addLine('Normal');
    designBuilder.addLine(TagHelper.font('BIG', TagHelper.FONT_SIZE.BIG));
    designBuilder.addLine('Normal');
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addBlankLine();
    designBuilder.addLine('!!!!!!!!!!');
    const designs = designBuilder.designs;
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
