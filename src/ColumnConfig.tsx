export type ColumnConfig = {
  width: number;
  text?: string | null | undefined;
  allignment?: string | undefined;
  underline?: boolean | undefined;
  bold?: boolean | undefined;
  spacer?: boolean | undefined;
};

export type RowConfig = ColumnConfig[];

export type TableConfig = RowConfig[];

export type RowData = Array<string | null | undefined>;

export type TableData = RowData[];
