import type {
  RowConfig,
  RowData,
  TableConfig,
  TableData,
} from './ColumnConfig';

/**
 * Create a TableBuilder instance,
 * And add columns configurations, maximum 3, you use array of ColumnConfig or arbitrary number of ColumnConfig as arguments
 *
 * @param {(...RowConfig | RowConfig[])} columns column configuration
 * @export
 * @class TableBuilder
 */
export default class TableBuilder {
  private configs: RowConfig = [];
  private datas: TableData = [];

  constructor(...columns: RowConfig | RowConfig[]) {
    this.column(...columns);
  }

  /**
   * Add columns configuratiosn, maximum 3, you use array of ColumnConfig or arbitrary number of ColumnConfig as arguments
   *
   * @param {(...RowConfig | RowConfig[])} columns column configuration
   * @return {*} current TableBuilder instance
   * @memberof TableBuilder
   */
  public column(...columns: RowConfig | RowConfig[]) {
    if (Array.isArray(columns[0])) {
      (columns[0] as RowConfig).forEach((column) => {
        this.configs.push(column);
      });
    } else if (columns.length > 0) {
      (columns as RowConfig).forEach((column) => {
        this.configs.push(column);
      });
    }
    return this;
  }

  /**
   * Add row data to one column, you use array of RowData or arbitrary number of RowData as arguments
   *
   * @param {(...RowData | RowData[])} texts column configuration
   * @return {*} current TableBuilder instance
   * @memberof TableBuilder
   */
  public row(...texts: RowData | RowData[]) {
    if (Array.isArray(texts[0])) {
      this.datas.push(texts[0] as RowData);
    } else if (texts.length > 0) {
      this.datas.push(texts as RowData);
    }
    return this;
  }

  /**
   * Add row data to multiple columns, you use array of TableData or arbitrary number of TableData as arguments
   *
   * @param {(...TableData | TableData[])} rows column configuration
   * @return {*} current TableBuilder instance
   * @memberof TableBuilder
   */
  public rows(...rows: TableData | TableData[]) {
    if (Array.isArray(rows[0]) && Array.isArray(rows[0][0])) {
      (rows[0] as TableData).forEach((row) => {
        this.row(row);
      });
    } else if (rows.length > 0) {
      (rows as TableData).forEach((row) => {
        this.row(row);
      });
    }
    return this;
  }

  /**
   * Build the table and return the data
   *
   * @return {*} Table data
   * @memberof TableBuilder
   */
  public build(): TableConfig {
    const result: TableConfig = [];
    this.datas.forEach((row) => {
      const rowResult: RowConfig = [];
      this.configs.forEach((column, index) => {
        rowResult.push({
          ...column,
          text: row[index],
        });
      });
      result.push(rowResult);
    });
    return result;
  }
}
