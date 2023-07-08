import type { NativeModule } from 'react-native';

import type { RNPrinter as RNPrinterTypes } from './RNPrinter';
import type { EscPos as EscPosTypes } from './EscPos';
import type { DeviceScanner as DeviceScannerTypes } from './DeviceScanner';

export declare module ReactNativePrinter {
  type RNPrinter = RNPrinterTypes & NativeModule;
  type EscPos = EscPosTypes & NativeModule;
  type DeviceScanner = DeviceScannerTypes & NativeModule;
}
