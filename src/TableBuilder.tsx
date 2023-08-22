import type {
  ColumnConfig,
  RowConfig,
  RowData,
  TableConfig,
  TableData,
} from './ColumnConfig';

export default class TableBuilder {
  private configs: RowConfig = [];
  private datas: TableData = [];

  constructor(...columns: RowConfig) {
    if (columns.length > 0) {
      this.columns(...columns);
    }
  }

  public column(column: ColumnConfig) {
    this.configs.push(column);
    return this;
  }

  public columns(...columns: RowConfig) {
    columns.forEach((column) => {
      this.column(column);
    });
    return this;
  }

  public row(text: RowData) {
    this.datas.push(text);
    return this;
  }

  public rows(...rows: TableData) {
    rows.forEach((row) => {
      this.row(row);
    });
    return this;
  }

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
