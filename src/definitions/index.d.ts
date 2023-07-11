import type { NativeModule } from 'react-native';

import type { RNPrinter as RNPrinterTypes } from './RNPrinter';
import type { DeviceScanner as DeviceScannerTypes } from './DeviceScanner';

export declare module ReactNativePrinter {
  type RNPrinter = RNPrinterTypes & NativeModule;
  type DeviceScanner = DeviceScannerTypes & NativeModule;
}
