import type { SCAN_TYPE } from './DeviceScanner';

import type { JobData } from './JobBuilder';

const EVENT_PRINTING_JOB = 'PRINTING_JOB';

const PRINTER_CONNECTION_NETWORK = 'network';
const PRINTER_CONNECTION_BLUETOOTH = 'bluetooth';
const PRINTER_CONNECTION_USB = 'usb';
const PRINTER_CONNECTION_SERIAL = 'serial';

const PRINTER_TYPE_THERMAL = 'thermal';
const PRINTER_TYPE_DOTMATRIX = 'dotmatrix';

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

  PRINTER_CONNECTION_NETWORK: PRINTER_CONNECTION_NETWORK,
  PRINTER_CONNECTION_BLUETOOTH: PRINTER_CONNECTION_BLUETOOTH,
  PRINTER_CONNECTION_USB: PRINTER_CONNECTION_USB,
  PRINTER_CONNECTION_SERIAL: PRINTER_CONNECTION_SERIAL,

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

export type PrinterConnectionType =
  | typeof PRINTER_CONNECTION_NETWORK
  | typeof PRINTER_CONNECTION_BLUETOOTH
  | typeof PRINTER_CONNECTION_USB
  | typeof PRINTER_CONNECTION_SERIAL;

export type PrinterType =
  | typeof PRINTER_TYPE_THERMAL
  | typeof PRINTER_TYPE_DOTMATRIX;

export type PrintingLinesMaxCharType =
  | typeof PRINTING_LINES_MAX_CHAR_33
  | typeof PRINTING_LINES_MAX_CHAR_40
  | typeof PRINTING_LINES_MAX_CHAR_42
  | typeof PRINTING_LINES_MAX_CHAR_56;

export type PrintingWidthType =
  | typeof PRINTING_WIDTH_58_MM
  | typeof PRINTING_WIDTH_76_MM
  | typeof PRINTING_WIDTH_80_MM;

export type ConnectionSelector = {
  connection: PrinterConnectionType;
};

export type PrinterSelector = ConnectionSelector & {
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

export type RNPrinterEventPayload = {
  connection?: string | null;
  address?: string | null;
  port?: number;
  baudrate?: number;
  dpi?: number;
  width?: number;
  maxChars?: number;
  file?: string | null;
  jobId?: string | null;
  jobName?: string | null;
  jobTag?: string | null;
  state?: string | null;
  id?: string | null;
  tags?: Array<string>;
  generation?: number;
  runAttemptCount?: number;
  error?: string | null;
};

export type RNPrinter = {
  EVENT_PRINTING_JOB: typeof EVENT_PRINTING_JOB;

  PRINTER_CONNECTION_NETWORK: typeof PRINTER_CONNECTION_NETWORK;
  PRINTER_CONNECTION_BLUETOOTH: typeof PRINTER_CONNECTION_BLUETOOTH;
  PRINTER_CONNECTION_USB: typeof PRINTER_CONNECTION_USB;
  PRINTER_CONNECTION_SERIAL: typeof PRINTER_CONNECTION_SERIAL;

  PRINTER_TYPE_THERMAL: typeof PRINTER_TYPE_THERMAL;
  PRINTER_TYPE_DOTMATRIX: typeof PRINTER_TYPE_DOTMATRIX;

  PRINTING_DPI_NORMAL: typeof PRINTING_DPI_NORMAL;

  PRINTING_LINES_MAX_CHAR_33: typeof PRINTING_LINES_MAX_CHAR_33;
  PRINTING_LINES_MAX_CHAR_40: typeof PRINTING_LINES_MAX_CHAR_40;
  PRINTING_LINES_MAX_CHAR_42: typeof PRINTING_LINES_MAX_CHAR_42;
  PRINTING_LINES_MAX_CHAR_56: typeof PRINTING_LINES_MAX_CHAR_56;

  PRINTING_WIDTH_58_MM: typeof PRINTING_WIDTH_58_MM;
  PRINTING_WIDTH_76_MM: typeof PRINTING_WIDTH_76_MM;
  PRINTING_WIDTH_80_MM: typeof PRINTING_WIDTH_80_MM;

  TEST_PRINT_DESIGN: typeof TEST_PRINT_DESIGN;

  checkPermission(selector: ConnectionSelector): Promise<boolean>;
  requestPermissions(selector: ConnectionSelector): Promise<boolean>;
  getUsbPrintersCount(): Promise<number>;

  testConnection(selector: PrinterSelector): void;
  getPrinterModel(selector: PrinterSelector): Promise<string>;
  getAllJobs(): Promise<Array<RNPrinterEventPayload>>;
  prunePrintingWorks(): void;

  /**
   * Enqueue printing job
   *
   * @param {JobData} jobData data yielded by JobBuilder.build()
   * @return {*}  {Promise<string>}
   */
  enqueuePrint(jobData: JobData): Promise<string>;

  /**
   * @deprecated use enqueuePrint(jobData: JobData) is preferred
   */
  write(selector: PrinterSelector, text: string): Promise<boolean>;

  /**
   * @deprecated use enqueuePrint(jobData: JobData) is preferred
   */
  cutPaper(selector: PrinterSelector): void;

  /**
   * @deprecated use enqueuePrint(jobData: JobData) is preferred
   */
  openCashBox(selector: PrinterSelector): void;

  /**
   * @deprecated use enqueuePrint(jobData: JobData) is preferred
   */
  testPrint(selector: PrinterSelector): Promise<void>;

  /**
   * @deprecated use enqueuePrint(jobData: JobData) is preferred
   */
  enqueuePrint2(
    selector: PrinterSelector,
    text: string,
    cutPaper?: boolean,
    openCashBox?: boolean
  ): Promise<string>;
};
