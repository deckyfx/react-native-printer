import { NativeModules, Platform } from 'react-native';

import type { ReactNativePrinter } from './definitions/index';

import DSEventEmitter from './DeviceScannerEventEmitter';
import RNPEventEmitter from './RNPrinterEventEmitter';
import DesignBuilderModule from './DesignBuilder';
import TagHelperModule from './TagHelper';

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

export const DesignBuilder = DesignBuilderModule;
export const TagHelper = TagHelperModule;

// Register API here
export const DeviceScannerEventEmitter = new DSEventEmitter(DeviceScanner);
export const RNPrinterEventEmitter = new RNPEventEmitter(RNPrinter);
