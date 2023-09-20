import type { PrinterSelector } from './RNPrinter';

export type JobData = {
  id: string;
  file: string;
};

export interface JobBuilderInterface {
  /**
   * Begin a job builder
   *
   * @return {string}  jobId {Promise<string>}
   */
  begin(): Promise<string>;

  /**
   * Select a printer *Must begin job first
   *
   * @return {*}  {Promise<boolean>}
   */
  selectPrinter(jobId: string, selector: PrinterSelector): Promise<boolean>;

  /**
   * Initialize a printer, reset all settings
   *
   * @return {*}  {Promise<boolean>}
   */
  initializePrinter(jobId: string): Promise<boolean>;

  /**
   * Set Printer as dot matrix bypass printing image, qrcode, and barcode
   *
   * @return {*}  {Promise<boolean>}
   */
  setAsDotMatrix(jobId: string): Promise<boolean>;

  /**
   * Set Printer to dotmatrix, use 'ESC *' to print rasterized image instead of 'GS v 0'
   *
   * @return {*}  {Promise<boolean>}
   */
  useEscAsterisk(jobId: string): Promise<boolean>;

  /**
   * Print one line *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  printLine(jobId: string, line: string): Promise<boolean>;

  /**
   * Feed printer paper *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  feedPaper(jobId: string, dots: number): Promise<boolean>;

  /**
   * Cut paper *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  cutPaper(jobId: string): Promise<boolean>;

  /**
   * Open cash box, ony for certain type of printer *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  openCashBox(jobId: string): Promise<boolean>;

  /**
   * Build job
   *
   * @return {*}  {Promise<string>} JobID to be enqueued
   */
  build(jobId: string): Promise<JobData>;

  /**
   * Cancel and discard current job builder
   *
   * @return {*}  {Promise<boolean>}
   */
  discard(jobId: string): Promise<boolean>;

  /**
   * Is currently is building
   *
   * @return {*}  {Boolean}
   */
  building(jobId: string): Boolean;

  /**
   * Display design preview
   *
   */
  preview(jobId: string): Promise<string>;
}
