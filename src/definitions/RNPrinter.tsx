import type { SCAN_TYPE } from './DeviceScanner';

export type RNPrinter = {
  multiply(a: number, b: number): Promise<number>;

  checkPermission(scanType: SCAN_TYPE): Promise<boolean>;
  requestPermissions(scanType: SCAN_TYPE): Promise<boolean>;
  getUsbPrintersCount(): Promise<number>;

  write(
    prNumbererType: string,
    address: string,
    text: string
  ): Promise<boolean>;
  write(
    prNumbererType: string,
    address: string,
    port: Number,
    text: string
  ): Promise<boolean>;
  cutPaper(prNumbererType: string, address: string): void;
  cutPaper(prNumbererType: string, address: string, port: Number): void;
  openCashBox(prNumbererType: string, address: string): void;
  openCashBox(prNumbererType: string, address: string, port: Number): void;
  testConnection(prNumbererType: string, address: string): void;
  testConnection(prNumbererType: string, address: string, port: Number): void;
  getPrinterModel(printerType: String, address: String): Promise<string>;
  getPrinterModel(
    printerType: String,
    address: String,
    port: Number
  ): Promise<string>;
  testPrint(
    prNumbererType: string,
    address: string,
    port: Number
  ): Promise<void>;
  testPrint(printerType: String, address: String): Promise<void>;
};
