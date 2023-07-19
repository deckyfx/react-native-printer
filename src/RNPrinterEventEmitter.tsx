import { NativeEventEmitter, type NativeModule } from 'react-native';
import { Constants as RNPrinter } from './definitions/RNPrinter';

export default class RNPrinterEventEmitter extends NativeEventEmitter {
  constructor(nativeModule?: NativeModule) {
    super(nativeModule);
  }

  onEvents(listener: (even: string, payload: any) => void) {
    [RNPrinter.EVENT_PRINTING_JOB].forEach((eventName) => {
      this.addListener(eventName, (...args: any[]) => {
        listener.apply(this, [eventName, args[0]]);
      });
    });
  }

  offEvents() {
    [RNPrinter.EVENT_PRINTING_JOB].forEach((eventName) => {
      this.removeAllListeners(eventName);
    });
  }
}
