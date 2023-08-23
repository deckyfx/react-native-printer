import type { ColumnConfig, RowConfig, TableConfig } from './ColumnConfig';
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
   * Get design preview
   *
   * @readonly
   */
  public get preview(): string {
    return `\n${this.design}`;
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
   * Add blank white space
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
    const totalWidth = configs.reduce((total, column) => {
      return total + column.width;
    }, 0);
    if (totalWidth > this.maxChar) {
      console.warn(
        `sum of column width ${totalWidth} exceed printing max char ${this.maxChar}`
      );
      return [];
    }
    let result: Array<Array<string>> = [];
    let chunkedNums = 0;
    // Chunks all texts
    configs.forEach((config, index) => {
      const width =
        hasSpacer && index < configs.length - 1
          ? config.width - 1
          : config.width;
      const chunked = this.chuckLines(config.text || '', width);
      if (chunked.length > chunkedNums) {
        chunkedNums = chunked.length;
      }
      result.push(chunked);
    });
    // Pad result so all chunked array has same size
    // Also pad line with whitespaces to emulate allignment
    result = result.map((chunkeds, index) => {
      const config = configs[index]!;
      const width =
        hasSpacer && index < configs.length - 1
          ? config.width - 1
          : config.width;
      if (chunkeds.length < chunkedNums) {
        const diff = chunkedNums - chunkeds.length;
        const fill = Array(diff).fill(' '.repeat(config.width));
        chunkeds.push(...fill);
      }
      chunkeds = chunkeds
        .map((line) => {
          if (line.length >= width) {
            return line;
          }
          const diff = width - line.length;
          const fill = ' '.repeat(diff);
          switch (configs[index]?.allignment) {
            case TagHelper.ALLIGNMENT.LEFT:
              return line + fill;
            case TagHelper.ALLIGNMENT.CENTER:
              const leftdiff = Math.floor(diff / 2);
              const rightdiff = diff - leftdiff;
              const leftfill = ' '.repeat(leftdiff);
              const rightfill = ' '.repeat(rightdiff);
              return leftfill + line + rightfill;
            case TagHelper.ALLIGNMENT.RIGHT:
              return fill + line;
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
  public addAutoColumn(columns: RowConfig) {
    const configs = columns.slice(0, 3); // only take maximum three columns
    const line = configs.reduce<string>(
      (construct: string, config: ColumnConfig) => {
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
