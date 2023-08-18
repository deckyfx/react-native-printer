import type { PrinterSelector } from "./RNPrinter";

export interface JobBuilderInterface {
  begin(): Promise<boolean>;
  selectPrinter(selector: PrinterSelector): Promise<boolean>;
  printLine(lines: string): Promise<boolean>;
  feedPaper(): Promise<boolean>;
  cutPaper(): Promise<boolean>;
  openCashBox(): Promise<boolean>;
  build(): Promise<string>;
  discard(): Promise<boolean>;
  building(): Boolean;
};