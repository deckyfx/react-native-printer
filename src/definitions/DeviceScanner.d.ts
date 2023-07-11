const SCAN_ALL = 0;
const SCAN_NETWORK = 1;
const SCAN_ZEROCONF = 2;
const SCAN_BLUETOOTH = 3;
const SCAN_USB = 4;

const PRINTER_TYPE_NETWORK = 'network';
const PRINTER_TYPE_BLUETOOTH = 'bluetooth';
const PRINTER_TYPE_USB = 'usb';

const EVENT_START_SCAN = 'START_SCAN';
const EVENT_STOP_SCAN = 'STOP_SCAN';
const EVENT_ERROR = 'ERROR';
const EVENT_DEVICE_FOUND = 'DEVICE_FOUND';
const EVENT_OTHER = 'OTHER';

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

  PRINTER_TYPE_NETWORK: typeof PRINTER_TYPE_NETWORK;
  PRINTER_TYPE_BLUETOOTH: typeof PRINTER_TYPE_BLUETOOTH;
  PRINTER_TYPE_USB: typeof PRINTER_TYPE_USB;

  EVENT_START_SCAN: typeof EVENT_START_SCAN;
  EVENT_STOP_SCAN: typeof EVENT_STOP_SCAN;
  EVENT_ERROR: typeof EVENT_ERROR;
  EVENT_DEVICE_FOUND: typeof EVENT_DEVICE_FOUND;
  EVENT_OTHER: typeof EVENT_OTHER;
};
