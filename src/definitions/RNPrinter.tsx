import type { SCAN_TYPE } from './DeviceScanner';

const EVENT_PRINTING_JOB = 'PRINTING_JOB';

const PRINTER_TYPE_NETWORK = 'network';
const PRINTER_TYPE_BLUETOOTH = 'bluetooth';
const PRINTER_TYPE_USB = 'usb';

const PRINTING_DPI_NORMAL = 210;

const PRINTING_LINES_MAX_CHAR_33 = 33;
const PRINTING_LINES_MAX_CHAR_40 = 40;
const PRINTING_LINES_MAX_CHAR_42 = 42;
const PRINTING_LINES_MAX_CHAR_56 = 56;

const PRINTING_WIDTH_58_MM = 41.0;
const PRINTING_WIDTH_76_MM = 48.0;
const PRINTING_WIDTH_80_MM = 60.0;

const TEST_PRINT_DESIGN = '';

export const Constants = {
  EVENT_PRINTING_JOB: EVENT_PRINTING_JOB,

  PRINTER_TYPE_NETWORK: PRINTER_TYPE_NETWORK,
  PRINTER_TYPE_BLUETOOTH: PRINTER_TYPE_BLUETOOTH,
  PRINTER_TYPE_USB: PRINTER_TYPE_USB,

  PRINTING_DPI_NORMAL: PRINTING_DPI_NORMAL,

  PRINTING_LINES_MAX_CHAR_33: PRINTING_LINES_MAX_CHAR_33,
  PRINTING_LINES_MAX_CHAR_40: PRINTING_LINES_MAX_CHAR_40,
  PRINTING_LINES_MAX_CHAR_42: PRINTING_LINES_MAX_CHAR_42,
  PRINTING_LINES_MAX_CHAR_56: PRINTING_LINES_MAX_CHAR_56,

  PRINTING_WIDTH_58_MM: PRINTING_WIDTH_58_MM,
  PRINTING_WIDTH_76_MM: PRINTING_WIDTH_76_MM,
  PRINTING_WIDTH_80_MM: PRINTING_WIDTH_80_MM,

  TEST_PRINT_DESIGN: TEST_PRINT_DESIGN,
};

export type PrinterSelector = {
  type:
    | typeof PRINTER_TYPE_NETWORK
    | typeof PRINTER_TYPE_BLUETOOTH
    | typeof PRINTER_TYPE_USB;
  address: string;
  port?: number | undefined;
  dpi?: number | undefined;
  width?: number | undefined;
  maxChars?: number | undefined;
};

export interface RNPrinterPayload {
  scanType: SCAN_TYPE;
  [key: string]: any;
}

export type RNPrinter = {
  EVENT_PRINTING_JOB: typeof EVENT_PRINTING_JOB;

  PRINTER_TYPE_NETWORK: typeof PRINTER_TYPE_NETWORK;
  PRINTER_TYPE_BLUETOOTH: typeof PRINTER_TYPE_BLUETOOTH;
  PRINTER_TYPE_USB: typeof PRINTER_TYPE_USB;

  PRINTING_DPI_NORMAL: typeof PRINTING_DPI_NORMAL;

  PRINTING_LINES_MAX_CHAR_33: typeof PRINTING_LINES_MAX_CHAR_33;
  PRINTING_LINES_MAX_CHAR_40: typeof PRINTING_LINES_MAX_CHAR_40;
  PRINTING_LINES_MAX_CHAR_42: typeof PRINTING_LINES_MAX_CHAR_42;
  PRINTING_LINES_MAX_CHAR_56: typeof PRINTING_LINES_MAX_CHAR_56;

  PRINTING_WIDTH_58_MM: typeof PRINTING_WIDTH_58_MM;
  PRINTING_WIDTH_76_MM: typeof PRINTING_WIDTH_76_MM;
  PRINTING_WIDTH_80_MM: typeof PRINTING_WIDTH_80_MM;

  TEST_PRINT_DESIGN: typeof TEST_PRINT_DESIGN;

  multiply(a: number, b: number): Promise<number>;

  checkPermission(scanType: SCAN_TYPE): Promise<boolean>;
  requestPermissions(scanType: SCAN_TYPE): Promise<boolean>;
  getUsbPrintersCount(): Promise<number>;

  write(selector: PrinterSelector, text: string): Promise<boolean>;
  cutPaper(selector: PrinterSelector): void;
  openCashBox(selector: PrinterSelector): void;
  testConnection(selector: PrinterSelector): void;
  getPrinterModel(selector: PrinterSelector): Promise<string>;
  testPrint(selector: PrinterSelector): Promise<void>;
  enqueuePrint(selector: PrinterSelector, text: string): void;
};
