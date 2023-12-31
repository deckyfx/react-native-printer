const SCAN_ALL = 0;
const SCAN_NETWORK = 1;
const SCAN_ZEROCONF = 2;
const SCAN_BLUETOOTH = 3;
const SCAN_USB = 4;
const SCAN_SERIAL = 5;

const EVENT_START_SCAN = 'START_SCAN';
const EVENT_STOP_SCAN = 'STOP_SCAN';
const EVENT_ERROR = 'ERROR';
const EVENT_DEVICE_FOUND = 'DEVICE_FOUND';
const EVENT_OTHER = 'OTHER';

export const Constants = {
  SCAN_ALL: SCAN_ALL,
  SCAN_NETWORK: SCAN_NETWORK,
  SCAN_ZEROCONF: SCAN_ZEROCONF,
  SCAN_BLUETOOTH: SCAN_BLUETOOTH,
  SCAN_USB: SCAN_USB,
  SCAN_SERIAL: SCAN_SERIAL,

  EVENT_START_SCAN: EVENT_START_SCAN,
  EVENT_STOP_SCAN: EVENT_STOP_SCAN,
  EVENT_ERROR: EVENT_ERROR,
  EVENT_DEVICE_FOUND: EVENT_DEVICE_FOUND,
  EVENT_OTHER: EVENT_OTHER,
};

export type SCAN_TYPE =
  | typeof SCAN_ALL
  | typeof SCAN_NETWORK
  | typeof SCAN_ZEROCONF
  | typeof SCAN_BLUETOOTH
  | typeof SCAN_USB
  | typeof SCAN_SERIAL;

export type DeviceScanner = {
  /**
   * Start scan nearby devices
   *
   * @param {SCAN_TYPE} scanType What to scan
   * @return {*}  {Promise<void>} Promise Void
   */
  scan(scanType: SCAN_TYPE): Promise<void>;

  /**
   * Stop scan nearby devices
   *
   * @param {SCAN_TYPE} scanType What scan process to stop
   * @return {*}  {Promise<void>} Promise Void
   */
  stop(scanType: SCAN_TYPE): void;

  SCAN_ALL: typeof SCAN_ALL;
  SCAN_NETWORK: typeof SCAN_NETWORK;
  SCAN_ZEROCONF: typeof SCAN_ZEROCONF;
  SCAN_BLUETOOTH: typeof SCAN_BLUETOOTH;
  SCAN_USB: typeof SCAN_USB;
  SCAN_SERIAL: typeof SCAN_SERIAL;

  EVENT_START_SCAN: typeof EVENT_START_SCAN;
  EVENT_STOP_SCAN: typeof EVENT_STOP_SCAN;
  EVENT_ERROR: typeof EVENT_ERROR;
  EVENT_DEVICE_FOUND: typeof EVENT_DEVICE_FOUND;
  EVENT_OTHER: typeof EVENT_OTHER;
};

export interface DeviceScanEventPayload {
  scanType: SCAN_TYPE;
  address?: string | undefined;
  message?: string | undefined;
  [key: string]: any;
}

export type DeviceData = DeviceScanEventPayload & {
  deviceName?: string | undefined;
  port?: number | undefined;
  baudrate?: number | undefined;
};

export type USBDeviceData = DeviceData & {
  PID?: string | undefined;
  VID?: string | undefined;
  deviceId?: string | undefined;
  manufacturerName?: string | undefined;
  serialNumber?: number | undefined;
  status?:
    | {
        attributes?: string[] | undefined;
        interfaces?:
          | {
              no?: number | undefined;
              class?: string | undefined;
              endpoints?:
                | {
                    no?: number | undefined;
                    name?: string | undefined;
                    direction?: string | undefined;
                  }[]
                | undefined;
            }[]
          | undefined;
        length?: number | undefined;
        maxPower?: number | undefined;
      }[]
    | undefined;
  scanType: SCAN_TYPE;
  // "status": {"attributes": ["BusPowered", "SelfPowered", "RemoteWakeup"], "interfaces": [[Object]], "length": 32, "maxPower": 100
};
