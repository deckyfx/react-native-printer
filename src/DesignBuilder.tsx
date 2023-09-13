import type {
  AutoColumnConfig,
  AutoRowConfig,
  RowConfig,
  TableConfig,
} from './ColumnConfig';
import TagHelper from './TagHelper';

/**
 * Helper calss to create printable design foor RNPrinter
 *
 * @export
 * @class DesignBuilder
 */
export default class DesignBuilder {
  private maxChar: number;
  private _design: string[] = [];

  /**
   * Creates an instance of DesignBuilder.
   * @param {number} maxChar Max char per line during printing
   * @memberof DesignBuilder
   */
  constructor(maxChar: number) {
    this.maxChar = maxChar;
  }

  private set design(lines: string) {
    this._design = lines.split('\n');
  }

  /**
   * Get design result
   *
   * @readonly
   */
  public get design(): string {
    return this._design.join('\n');
  }

  /**
   * Get design array
   *
   * @readonly
   */
  public get designs(): string[] {
    return this._design;
  }

  /**
   * Display design preview
   *
   * @readonly
   */
  public get preview() {
    this.designs.forEach((line) => {
      console.log(line.trim());
    });
    return this;
  }

  /**
   * Add formated line using TagHelper
   *
   * @param {string} line
   */
  public addLine(line: string) {
    if (!line.endsWith('\n')) {
      line = TagHelper.line(line);
    }
    this._design.push(line);
    return this;
  }

  /**
   * Add array of formated line using TagHelper
   *
   * @param {string} lines
   */
  public addLines(lines: string[]) {
    lines.forEach((line) => this.addLine(line));
    return this;
  }

  /**
   * Add one line of blank white space
   */
  public addBlankLine() {
    this._design.push(' ');
    return this;
  }

  /**
   * Repeat single char for one row
   *
   * @param {string} [char='-'] char to repeat, default is **-**
   */
  public drawSeparator(char: string = '-') {
    this.addLine(
      Array(this.maxChar)
        .fill(char[0] || ' ')
        .join('')
    );
    return this;
  }

  private testWhitespace(char: string): boolean {
    var white = new RegExp(/^\s$/);
    return white.test(char.charAt(0));
  }

  private padLeftRight(str: string, pad: string, length: number): string {
    // Check if the string is longer than n characters.
    if (str.length >= length) {
      return str;
    }

    // Calculate the number of characters to pad on each side.
    const padLength = Math.ceil((length - str.length) / 2);

    // Determine the number of characters to pad on the left and right sides.
    const left = padLength;
    let right = padLength;

    // Decrease the right padding length by one if the total length is greater than n.
    if (padLength * 2 + str.length > length) {
      right--;
    }

    // Create the padded string.
    return `${pad.repeat(left)}${str}${pad.repeat(right)}`;
  }

  /**
   * Split text so it can fit into max char config
   *
   * @param {string} text text to plit
   * @param {number} max char, default is current max char
   * @return {string[]} chunked text
   */
  public chuckLines(text: string, maxLength: number = this.maxChar): string[] {
    // most of this function is credited too
    // https://stackoverflow.com/a/14487422/4825796
    let newLineStr = '\n' + String.fromCharCode(8203);
    let updatedStr = '';
    while (text.length > maxLength) {
      let found = false;
      const testPiece = text.substring(0, maxLength);
      if (testPiece.includes('\n')) {
        const enterIdex = testPiece.indexOf('\n');
        updatedStr = updatedStr + text.slice(0, enterIdex + 1);
        text = text.slice(enterIdex + 1);
      } else {
        // Inserts new line at first whitespace of the line
        for (let i = maxLength - 1; i >= 0; i--) {
          if (this.testWhitespace(text.charAt(i))) {
            // check from back to front for a space
            updatedStr = updatedStr + [text.slice(0, i), newLineStr].join('');
            text = text.slice(i + 1);
            found = true;
            break;
          }
        }
        // Inserts new line at maxWidth position, the word is too long to wrap
        if (!found) {
          updatedStr += [text.slice(0, maxLength), newLineStr].join('');
          text = text.slice(maxLength);
        }
      }
    }
    updatedStr = updatedStr + text;
    return updatedStr.split('\n').map((l) => l.trim());
  }

  /**
   * Create columned text, chuck each column
   *
   * @param {RowConfig} columns columns configuration
   * @return {*}  {Array<string>} chunked text
   */
  public columns(columns: RowConfig): Array<string> {
    const hasSpacer = columns.some((config) => config.spacer);
    const configs = columns.slice(0, 3); // only take maximum three columns
    let zero_width_count = 0;
    configs.forEach((column) => {
      if (!column.width) {
        zero_width_count += 1;
      }
    });
    if (zero_width_count > 1) {
      console.warn(
        'Invalid configuration!, thee are more than 1 column with 0 width'
      );
      return [];
    }
    if (zero_width_count == 1) {
      // * If there are exatcly one column with 0 width, it will assume it takes the rest of width
      // * Get the ocupied width
      const ocupied = columns.reduce((total, column) => {
        if (column.width) {
          return (total += column.width + (hasSpacer ? 1 : 0));
        }
        return total;
      }, 0);
      // * Get the rest of width
      const rest = this.maxChar - ocupied;
      // * Update the configuration
      columns = columns.map((column) => {
        if (!column.width) {
          column.width = rest;
        }
        return column;
      });
    }
    // * Include spacer to all columns but the last column
    const totalWidth =
      configs.reduce((total, column) => {
        return total + column.width;
      }, 0) + (hasSpacer ? configs.length - 1 : 0);
    if (totalWidth > this.maxChar) {
      console.warn(
        `sum of column width ${totalWidth} exceed printing max char ${this.maxChar}\n`,
        'If you add spacer, the total width will be added by (column_num - 1)'
      );
      return [];
    }
    let result: Array<Array<string>> = [];
    let chunkedNums = 0;
    // * Chunks all texts
    configs.forEach((config) => {
      const width = config.width;
      const chunked = this.chuckLines(config.text || '', width);
      if (chunked.length > chunkedNums) {
        chunkedNums = chunked.length;
      }
      result.push(chunked);
    });
    // * Pad result so all chunked array has same size
    // * Also pad line with whitespaces to emulate allignment
    const STRING_PADDER = ' ';
    result = result.map((chunkeds, index) => {
      const config = configs[index]!;
      const width = config.width;
      if (chunkeds.length < chunkedNums) {
        const diff = chunkedNums - chunkeds.length;
        const fill = Array(diff).fill(STRING_PADDER.repeat(config.width));
        chunkeds.push(...fill);
      }
      chunkeds = chunkeds
        .map((line) => {
          if (line.length >= width) {
            return line;
          }
          switch (configs[index]?.allignment) {
            case TagHelper.ALLIGNMENT.LEFT:
              return line.padEnd(config.width, STRING_PADDER);
            case TagHelper.ALLIGNMENT.CENTER:
              return this.padLeftRight(line, STRING_PADDER, config.width);
            case TagHelper.ALLIGNMENT.RIGHT:
              return line.padStart(config.width, STRING_PADDER);
          }
          return line;
        })
        .map((line) => {
          if (config.underline) {
            line = TagHelper.underline(line);
          }
          if (config.bold) {
            line = TagHelper.bold(line);
          }
          return line;
        });
      return chunkeds;
    });

    // Transpose array;
    result = Array.from(result[0]!).map((_, i) => result.map((row) => row[i]!));
    const result2 = result.map((_) => _.join(hasSpacer ? ' ' : ''));
    return result2;
  }

  /**
   * Add table matrix texts to design
   *
   * @param {TableConfig} rows rows or table data generated by TableBuilder.build()
   * @return {*}  {Array<string>} chunked text
   */
  public addTable(rows: TableConfig) {
    rows.forEach((row) => {
      this.addLines(this.columns(row));
    });
    return this;
  }

  /**
   * Add automated columned line the split width will be equal / automated and handled by the native side
   *
   * @param {RowConfig} columns the required attributes is { allignment, text }
   * @memberof DesignBuilder
   */
  public addAutoColumn(columns: AutoRowConfig) {
    const configs = columns.slice(0, 3); // only take maximum three columns
    const line = configs.reduce<string>(
      (construct: string, config: AutoColumnConfig) => {
        const text = config.text || '';
        switch (config.allignment) {
          case TagHelper.ALLIGNMENT.LEFT:
            return construct + TagHelper.left(text);
          case TagHelper.ALLIGNMENT.CENTER:
            return construct + TagHelper.center(text);
          case TagHelper.ALLIGNMENT.RIGHT:
            return construct + TagHelper.right(text);
        }
        return '';
      },
      ''
    );
    this.addLine(line);
    return this;
  }

  /**
   * Add standard printable characters
   */
  public addPrintableCharacters() {
    this.addLines(
      this.chuckLines(
        'ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-=ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()_+-='
      )
    );
    return this;
  }
}
