import { NativeModules, NativeEventEmitter, Platform } from 'react-native';

import type { ReactNativePrinter } from './definitions/index';

const LINKING_ERROR =
  `The package 'react-native-printer' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

export const RNPrinter: ReactNativePrinter.RNPrinter = NativeModules.RNPrinter
  ? NativeModules.RNPrinter
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

export const DeviceScanner: ReactNativePrinter.DeviceScanner =
  NativeModules.DeviceScanner
    ? NativeModules.DeviceScanner
    : new Proxy(
        {},
        {
          get() {
            throw new Error(LINKING_ERROR);
          },
        }
      );

// Register API here

export const DeviceScannerEventEmitter = new NativeEventEmitter(DeviceScanner);
export const RNPrinterEventEmitter = new NativeEventEmitter(RNPrinter);
