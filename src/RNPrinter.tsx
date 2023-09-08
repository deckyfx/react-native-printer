import type { SCAN_TYPE } from './DeviceScanner';

import type { JobData } from './JobBuilder';

const EVENT_PRINTING_JOB = 'PRINTING_JOB';

export type EVENT_PRINTINGS = typeof EVENT_PRINTING_JOB;

const PRINT_JOB_STATE_ENQUEUED = 'ENQUEUED';
const PRINT_JOB_STATE_RUNNING = 'RUNNING';
const PRINT_JOB_STATE_SUCCEEDED = 'SUCCEEDED';
const PRINT_JOB_STATE_FAILED = 'FAILED';
const PRINT_JOB_STATE_CANCELED = 'CANCELED';

export type PRINT_JOB_STATES =
  | typeof PRINT_JOB_STATE_ENQUEUED
  | typeof PRINT_JOB_STATE_RUNNING
  | typeof PRINT_JOB_STATE_SUCCEEDED
  | typeof PRINT_JOB_STATE_FAILED
  | typeof PRINT_JOB_STATE_CANCELED;

const PRINTER_CONNECTION_NETWORK = 'network';
const PRINTER_CONNECTION_BLUETOOTH = 'bluetooth';
const PRINTER_CONNECTION_USB = 'usb';
const PRINTER_CONNECTION_SERIAL = 'serial';

const PRINTER_TYPE_THERMAL = 'thermal';
const PRINTER_TYPE_DOTMATRIX = 'dotmatrix';

const PRINTING_DPI_NORMAL = 210;

const PRINTING_LINES_MAX_CHAR_32 = 32;
const PRINTING_LINES_MAX_CHAR_33 = 33;
const PRINTING_LINES_MAX_CHAR_40 = 40;
const PRINTING_LINES_MAX_CHAR_42 = 42;
const PRINTING_LINES_MAX_CHAR_56 = 56;

const PRINTING_WIDTH_58_MM = 58.0;
const PRINTING_WIDTH_70_MM = 70.0;
const PRINTING_WIDTH_75_MM = 75.0;
const PRINTING_WIDTH_76_MM = 76.0;
const PRINTING_WIDTH_80_MM = 80.0;

const TEST_PRINT_DESIGN = '';

export const Constants = {
  EVENT_PRINTING_JOB: EVENT_PRINTING_JOB,

  PRINT_JOB_STATE_ENQUEUED: PRINT_JOB_STATE_ENQUEUED,
  PRINT_JOB_STATE_RUNNING: PRINT_JOB_STATE_RUNNING,
  PRINT_JOB_STATE_SUCCEEDED: PRINT_JOB_STATE_SUCCEEDED,
  PRINT_JOB_STATE_FAILED: PRINT_JOB_STATE_FAILED,
  PRINT_JOB_STATE_CANCELED: PRINT_JOB_STATE_CANCELED,

  PRINTER_CONNECTION_NETWORK: PRINTER_CONNECTION_NETWORK,
  PRINTER_CONNECTION_BLUETOOTH: PRINTER_CONNECTION_BLUETOOTH,
  PRINTER_CONNECTION_USB: PRINTER_CONNECTION_USB,
  PRINTER_CONNECTION_SERIAL: PRINTER_CONNECTION_SERIAL,

  PRINTING_DPI_NORMAL: PRINTING_DPI_NORMAL,

  PRINTING_LINES_MAX_CHAR_32: PRINTING_LINES_MAX_CHAR_32,
  PRINTING_LINES_MAX_CHAR_33: PRINTING_LINES_MAX_CHAR_33,
  PRINTING_LINES_MAX_CHAR_40: PRINTING_LINES_MAX_CHAR_40,
  PRINTING_LINES_MAX_CHAR_42: PRINTING_LINES_MAX_CHAR_42,
  PRINTING_LINES_MAX_CHAR_56: PRINTING_LINES_MAX_CHAR_56,

  PRINTING_WIDTH_58_MM: PRINTING_WIDTH_58_MM,
  PRINTING_WIDTH_70_MM: PRINTING_WIDTH_58_MM,
  PRINTING_WIDTH_75_MM: PRINTING_WIDTH_75_MM,
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
  | typeof PRINTING_LINES_MAX_CHAR_32
  | typeof PRINTING_LINES_MAX_CHAR_33
  | typeof PRINTING_LINES_MAX_CHAR_40
  | typeof PRINTING_LINES_MAX_CHAR_42
  | typeof PRINTING_LINES_MAX_CHAR_56;

export type PrintingWidthType =
  | typeof PRINTING_WIDTH_58_MM
  | typeof PRINTING_WIDTH_70_MM
  | typeof PRINTING_WIDTH_75_MM
  | typeof PRINTING_WIDTH_76_MM
  | typeof PRINTING_WIDTH_80_MM;

export type ConnectionSelector = {
  connection: PrinterConnectionType;
};

export type PrinterSelector = ConnectionSelector & {
  address: string;
  port?: number | undefined;
  baudrate?: number | undefined;
  dpi?: number | undefined;
  width?: PrintingWidthType | undefined;
  maxChars?: PrintingLinesMaxCharType | undefined;
};

export interface RNPrinterPayload {
  scanType: SCAN_TYPE;
  [key: string]: any;
}

export type RNPrinterEventPayload = {
  connection?: PrinterConnectionType | null | undefined;
  address?: string | null | undefined;
  port?: number | null | undefined;
  baudrate?: number | null | undefined;
  dpi?: number | null | undefined;
  width?: PrintingWidthType | null | undefined;
  maxChars?: PrintingLinesMaxCharType | null | undefined;
  file?: string | null | undefined;
  jobId?: string | null | undefined;
  jobName?: string | null | undefined;
  jobTag?: string | null | undefined;
  state?: PRINT_JOB_STATES | null | undefined;
  id?: string | null | undefined;
  tags?: Array<string> | null | undefined;
  generation?: number | null | undefined;
  runAttemptCount?: number | null | undefined;
  error?: string | null | undefined;
  message?: string | null | undefined;
};

export type RNPrinter = {
  EVENT_PRINTING_JOB: typeof EVENT_PRINTING_JOB;

  PRINT_JOB_STATE_ENQUEUED: typeof PRINT_JOB_STATE_ENQUEUED;
  PRINT_JOB_STATE_RUNNING: typeof PRINT_JOB_STATE_RUNNING;
  PRINT_JOB_STATE_SUCCEEDED: typeof PRINT_JOB_STATE_SUCCEEDED;
  PRINT_JOB_STATE_FAILED: typeof PRINT_JOB_STATE_FAILED;
  PRINT_JOB_STATE_CANCELED: typeof PRINT_JOB_STATE_CANCELED;

  PRINTER_CONNECTION_NETWORK: typeof PRINTER_CONNECTION_NETWORK;
  PRINTER_CONNECTION_BLUETOOTH: typeof PRINTER_CONNECTION_BLUETOOTH;
  PRINTER_CONNECTION_USB: typeof PRINTER_CONNECTION_USB;
  PRINTER_CONNECTION_SERIAL: typeof PRINTER_CONNECTION_SERIAL;

  PRINTER_TYPE_THERMAL: typeof PRINTER_TYPE_THERMAL;
  PRINTER_TYPE_DOTMATRIX: typeof PRINTER_TYPE_DOTMATRIX;

  PRINTING_DPI_NORMAL: typeof PRINTING_DPI_NORMAL;

  PRINTING_LINES_MAX_CHAR_32: typeof PRINTING_LINES_MAX_CHAR_32;
  PRINTING_LINES_MAX_CHAR_33: typeof PRINTING_LINES_MAX_CHAR_33;
  PRINTING_LINES_MAX_CHAR_40: typeof PRINTING_LINES_MAX_CHAR_40;
  PRINTING_LINES_MAX_CHAR_42: typeof PRINTING_LINES_MAX_CHAR_42;
  PRINTING_LINES_MAX_CHAR_56: typeof PRINTING_LINES_MAX_CHAR_56;

  PRINTING_WIDTH_58_MM: typeof PRINTING_WIDTH_58_MM;
  PRINTING_WIDTH_70_MM: typeof PRINTING_WIDTH_70_MM;
  PRINTING_WIDTH_75_MM: typeof PRINTING_WIDTH_75_MM;
  PRINTING_WIDTH_76_MM: typeof PRINTING_WIDTH_76_MM;
  PRINTING_WIDTH_80_MM: typeof PRINTING_WIDTH_80_MM;

  TEST_PRINT_DESIGN: typeof TEST_PRINT_DESIGN;

  /**
   * Enqueue printing job
   *
   * @param {JobData} jobData Data yield by JobBuilder.build()
   * @return {*} `Promise<string>` Job UUID
   */
  enqueuePrint(jobData: JobData): Promise<string>;

  /**
   * Enqueue printing job to specific printers
   *
   * @param {JobData} jobData Data yield by JobBuilder.build()
   * @param {PrinterSelector} selector Printer Selector Argument
   * @return {*} `Promise<string>` Job UUID
   */
  enqueuePrint(jobData: JobData, selector: PrinterSelector): Promise<string>;

  checkPermissions(scanType: SCAN_TYPE): Promise<boolean>;
  requestPermissions(scanType: SCAN_TYPE): Promise<boolean>;
  getUsbPrintersCount(): Promise<number>;

  testConnection(selector: PrinterSelector): void;
  getPrinterModel(selector: PrinterSelector): Promise<string>;
  getAllJobs(): Promise<Array<RNPrinterEventPayload>>;
  prunePrintingWorks(): void;
};
