import type { Allignment } from './TagHelper';

export type ColumnConfig = {
  width: number;
  text?: string | null | undefined;
  allignment?: Allignment | null | undefined;
  underline?: boolean | undefined;
  bold?: boolean | undefined;
  spacer?: boolean | undefined;
};

export type RowConfig = ColumnConfig[];

export type TableConfig = RowConfig[];

export type RowData = Array<string | null | undefined>;

export type TableData = RowData[];

export type AutoColumnConfig = Omit<
  ColumnConfig,
  'width' | 'underline' | 'bold' | 'spacer'
>;

export type AutoRowConfig = AutoColumnConfig[];
