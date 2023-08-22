import { NativeEventEmitter, type NativeModule } from 'react-native';

import {
  Constants as RNPrinter,
  type RNPrinterEventPayload,
} from './RNPrinter';

export default class RNPrinterEventEmitter extends NativeEventEmitter {
  constructor(nativeModule?: NativeModule) {
    super(nativeModule);
  }

  /**
   * Set all event listeners
   */
  onEvents(listener: (event: string, payload: RNPrinterEventPayload) => void) {
    [RNPrinter.EVENT_PRINTING_JOB].forEach((eventName) => {
      this.addListener(eventName, (payload: RNPrinterEventPayload) => {
        listener.apply(this, [eventName, payload]);
      });
    });
  }

  /**
   * Remove all event listeners
   */
  offEvents() {
    [RNPrinter.EVENT_PRINTING_JOB].forEach((eventName) => {
      this.removeAllListeners(eventName);
    });
  }
}
