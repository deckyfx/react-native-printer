import TagHelper from './TagHelper';

export type ColumnConfiguration = {
  width: number;
  text: string;
  allignment: string | undefined;
  underline: boolean | undefined;
  bold: boolean | undefined;
};

export default class DesignBuilder {
  private maxChar: number;
  private _design: string[] = [];

  constructor(maxChar: number) {
    this.maxChar = maxChar;
  }

  private set design(lines: string) {
    this._design = lines.split('\n');
  }

  public get design(): string {
    return this._design.join('\n');
  }

  public addFormatedLine(line: string) {
    this._design.push(line);
  }

  public addFormatedLines(lines: string[]) {
    this._design = this._design.concat(lines);
  }

  public drawLine(char: string = '-') {
    this.addFormatedLine(
      TagHelper.line(Array(this.maxChar).fill(char).join(''))
    );
  }

  private testWhite(char: string): boolean {
    var white = new RegExp(/^\s$/);
    return white.test(char.charAt(0));
  }

  public chuckLines(text: string, maxLength = this.maxChar): Array<string> {
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
          if (this.testWhite(text.charAt(i))) {
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

  public columns(columns: Array<ColumnConfiguration>): Array<string> {
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
    configs.forEach((config) => {
      const chunked = this.chuckLines(config.text, config.width);
      if (chunked.length > chunkedNums) {
        chunkedNums = chunked.length;
      }
      result.push(chunked);
    });
    result = result.map((chunked, index) => {
      const config = configs[index]!;
      if (chunked.length < chunkedNums) {
        const diff = chunkedNums - chunked.length;
        const fill = Array(diff).fill(' '.repeat(config.width));
        chunked.push(...fill);
      }
      chunked = chunked
        .map((line) => {
          if (line.length >= config.width) {
            return line;
          }
          const diff = config.width - line.length;
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
      return chunked;
    });
    return Array(chunkedNums)
      .fill(null)
      .map((_) => {
        const lines = result.map((chunked, i) => {
          return chunked[i];
        });
        return lines.join('');
      });
  }
}
