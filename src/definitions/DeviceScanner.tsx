const SCAN_ALL = 0;
const SCAN_NETWORK = 1;
const SCAN_ZEROCONF = 2;
const SCAN_BLUETOOTH = 3;
const SCAN_USB = 4;

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
  | typeof SCAN_USB;

export type DeviceScanner = {
  multiply(a: number, b: number): Promise<number>;
  scan(scanType: SCAN_TYPE): Promise<void>;
  stop(scanType: SCAN_TYPE): void;

  SCAN_ALL: typeof SCAN_ALL;
  SCAN_NETWORK: typeof SCAN_NETWORK;
  SCAN_ZEROCONF: typeof SCAN_ZEROCONF;
  SCAN_BLUETOOTH: typeof SCAN_BLUETOOTH;
  SCAN_USB: typeof SCAN_USB;

  EVENT_START_SCAN: typeof EVENT_START_SCAN;
  EVENT_STOP_SCAN: typeof EVENT_STOP_SCAN;
  EVENT_ERROR: typeof EVENT_ERROR;
  EVENT_DEVICE_FOUND: typeof EVENT_DEVICE_FOUND;
  EVENT_OTHER: typeof EVENT_OTHER;
};

export interface DeviceScanPayload {
  scanType: SCAN_TYPE;
  [key: string]: any;
}

export type USBDeviceData = {
  PID: string | undefined;
  VID: string | undefined;
  deviceId: string | undefined;
  deviceName: string | undefined;
  manufacturerName: string | undefined;
  serialNumber: number | undefined;
  status: string | undefined;
  scanType: SCAN_TYPE;
};
