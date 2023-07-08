const SCAN_ALL = 0;
const SCAN_NETWORK = 1;
const SCAN_ZEROCONF = 2;
const SCAN_BLUETOOTH = 3;
const SCAN_SERIAL = 4;

const PRINTER_TYPE_NETWORK = 'network';
const PRINTER_TYPE_BLUETOOTH = 'bluetooth';
const PRINTER_TYPE_SERIAL = 'serial';

const EVENT_START_SCAN = 'START_SCAN';
const EVENT_STOP_SCAN = 'STOP_SCAN';
const EVENT_ERROR = 'ERROR';
const EVENT_DEVICE_FOUND = 'DEVICE_FOUND';
const EVENT_OTHER = 'OTHER';

export type DeviceScanner = {
  scan(
    scanType:
      | typeof SCAN_ALL
      | typeof SCAN_NETWORK
      | typeof SCAN_ZEROCONF
      | typeof SCAN_BLUETOOTH
      | typeof SCAN_SERIAL
  ): void;
  stop(
    scanType:
      | typeof SCAN_ALL
      | typeof SCAN_NETWORK
      | typeof SCAN_ZEROCONF
      | typeof SCAN_BLUETOOTH
      | typeof SCAN_SERIAL
  ): void;

  SCAN_ALL: typeof SCAN_ALL;
  SCAN_NETWORK: typeof SCAN_NETWORK;
  SCAN_ZEROCONF: typeof SCAN_ZEROCONF;
  SCAN_BLUETOOTH: typeof SCAN_BLUETOOTH;
  SCAN_SERIAL: typeof SCAN_SERIAL;

  PRINTER_TYPE_NETWORK: typeof PRINTER_TYPE_NETWORK;
  PRINTER_TYPE_BLUETOOTH: typeof PRINTER_TYPE_BLUETOOTH;
  PRINTER_TYPE_SERIAL: typeof PRINTER_TYPE_SERIAL;

  EVENT_START_SCAN: typeof EVENT_START_SCAN;
  EVENT_STOP_SCAN: typeof EVENT_STOP_SCAN;
  EVENT_ERROR: typeof EVENT_ERROR;
  EVENT_DEVICE_FOUND: typeof EVENT_DEVICE_FOUND;
  EVENT_OTHER: typeof EVENT_OTHER;
};
