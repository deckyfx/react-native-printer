import type { PrinterSelector } from './RNPrinter';

export type JobData = {
  id: string,
  file: string,
}

export interface JobBuilderInterface {
  /**
   * Begin a job builder
   *
   * @return {*}  {Promise<boolean>}
   */
  begin(): Promise<boolean>;

  /**
   * Select a printer *Must begin job first
   *
   * @return {*}  {Promise<boolean>}
   */
  selectPrinter(selector: PrinterSelector): Promise<boolean>;

  /**
   * Print one line *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  printLine(line: string): Promise<boolean>;

  /**
   * Feed printer paper *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  feedPaper(dots: number): Promise<boolean>;

  /**
   * Cut paper *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  cutPaper(): Promise<boolean>;

  /**
   * Open cash box, ony for certain type of printer *Must begin job and select a printer first
   *
   * @return {*}  {Promise<boolean>}
   */
  openCashBox(): Promise<boolean>;

  /**
   * Build job
   *
   * @return {*}  {Promise<string>} JobID to be enqueued
   */
  build(): Promise<JobData>;

  /**
   * Cancel and discard current job builder
   *
   * @return {*}  {Promise<boolean>}
   */
  discard(): Promise<boolean>;

  /**
   * Is currently is building
   *
   * @return {*}  {Boolean}
   */
  building(): Boolean;
}
