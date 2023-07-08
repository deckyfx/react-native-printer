import * as React from 'react';

import {
  StyleSheet,
  View,
  Text,
  Button,
  NativeEventEmitter,
} from 'react-native';

import type { ReactNativePrinter } from '../../src/definitions/index';

import {
  RNPrinter as RNPrinterModule,
  DeviceScanner as DeviceScannerModule,
  DeviceScannerEventEmitter as DeviceScannerEventEmitterModule,
} from '@decky.fx/react-native-printer';

const RNPrinter: ReactNativePrinter.RNPrinter = RNPrinterModule;
const DeviceScanner: ReactNativePrinter.DeviceScanner = DeviceScannerModule;
const DeviceScannerEventEmitter: NativeEventEmitter =
  DeviceScannerEventEmitterModule;

export default function App() {
  const [result, setResult] = React.useState<number | undefined>();

  React.useEffect(() => {
    RNPrinter.multiply(3, 7).then(setResult);
    DeviceScannerEventEmitter.addListener(
      DeviceScanner.EVENT_DEVICE_FOUND,
      (device) => {
        console.log(device);
      }
    );
  }, []);

  return (
    <View style={styles.container}>
      <Text>Result: {result}</Text>
      <Button
        onPress={async () => {
          DeviceScanner.scan(DeviceScanner.SCAN_NETWORK);
        }}
        title="Scan Network"
        color="#841584"
      />
      <Button
        onPress={async () => {
          DeviceScanner.stop(DeviceScanner.SCAN_NETWORK);
        }}
        title="Stop Network"
        color="#841584"
      />
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
