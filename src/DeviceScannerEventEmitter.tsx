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
  onEvents(listener: (event: string, payload: DeviceScanEventPayload) => void) {
    [
      DeviceScanner.EVENT_START_SCAN,
      DeviceScanner.EVENT_STOP_SCAN,
      DeviceScanner.EVENT_DEVICE_FOUND,
      DeviceScanner.EVENT_OTHER,
      DeviceScanner.EVENT_ERROR,
    ].forEach((eventName) => {
      this.addListener(eventName, (payload: DeviceScanEventPayload) => {
        // Bypass scan network error due to ping connection failed
        if (
          eventName === DeviceScanner.EVENT_ERROR &&
          payload.scanType === DeviceScanner.SCAN_NETWORK &&
          payload.message?.startsWith('failed to connect to')
        ) {
          return;
        }
        listener.apply(this, [eventName, payload]);
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
