import { NativeEventEmitter, type NativeModule } from 'react-native';
import {
  Constants as DeviceScanner,
  type DeviceScanEventPayload,
} from './DeviceScanner';

export default class DeviceScannerEventEmitter extends NativeEventEmitter {
  constructor(nativeModule?: NativeModule) {
    super(nativeModule);
  }

  /**
   * Set all event listeners
   */
  onEvents(listener: (even: string, payload: DeviceScanEventPayload) => void) {
    [
      DeviceScanner.EVENT_START_SCAN,
      DeviceScanner.EVENT_STOP_SCAN,
      DeviceScanner.EVENT_DEVICE_FOUND,
      DeviceScanner.EVENT_OTHER,
      DeviceScanner.EVENT_ERROR,
    ].forEach((eventName) => {
      this.addListener(eventName, (...args: any[]) => {
        listener.apply(this, [eventName, args[0]]);
      });
    });
  }

  /**
   * Remove all event listeners
   */
  offEvents() {
    [
      DeviceScanner.EVENT_START_SCAN,
      DeviceScanner.EVENT_STOP_SCAN,
      DeviceScanner.EVENT_DEVICE_FOUND,
      DeviceScanner.EVENT_OTHER,
      DeviceScanner.EVENT_ERROR,
    ].forEach((eventName) => {
      this.removeAllListeners(eventName);
    });
  }
}
