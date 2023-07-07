import { NativeModules, Platform } from 'react-native';

const LINKING_ERROR =
  `The package 'react-native-printer' doesn't seem to be linked. Make sure: \n\n` +
  Platform.select({ ios: "- You have run 'pod install'\n", default: '' }) +
  '- You rebuilt the app after installing the package\n' +
  '- You are not using Expo Go\n';

//console.log(NativeModules.EscPos);
//console.log(NativeModules.TcpSockets);
//console.log(NativeModules.RNPrinter); 
//console.log(NativeModules.RNNetworkInfo);

const RNPrinter = NativeModules.RNPrinter
  ? NativeModules.RNPrinter
  : new Proxy(
      {},
      {
        get() {
          throw new Error(LINKING_ERROR);
        },
      }
    );

// Register API here

export function multiply(a: number, b: number): Promise<number> {
  return RNPrinter.multiply(a, b);
}

export function scanNetworkDevices(): Promise<number> {
  return NativeModules.EscPos.scanNetworkDevices();
}
