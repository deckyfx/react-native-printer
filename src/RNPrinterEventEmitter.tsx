import { NativeEventEmitter, type NativeModule } from 'react-native';

import {
  Constants as RNPrinter,
  type EVENT_PRINTINGS,
  type RNPrinterEventPayload,
} from './RNPrinter';

export default class RNPrinterEventEmitter extends NativeEventEmitter {
  constructor(nativeModule?: NativeModule) {
    super(nativeModule);
  }

  /**
   * Set all event listeners
   */
  onEvents(
    listener: (event: EVENT_PRINTINGS, payload: RNPrinterEventPayload) => void
  ) {
    [RNPrinter.EVENT_PRINTING_JOB].forEach((eventName) => {
      this.addListener(
        eventName as EVENT_PRINTINGS,
        (payload: RNPrinterEventPayload) => {
          listener.apply(this, [eventName as EVENT_PRINTINGS, payload]);
        }
      );
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
