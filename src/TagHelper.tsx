const Allignments = {
  LEFT: '[L]',
  CENTER: '[C]',
  RIGHT: '[R]',
} as const;
const AllignmentArray = [...Object.values(Allignments)] as const;
export type Allignment = (typeof AllignmentArray)[number];

const FontSizes = {
  NORMAL: 'normal',
  WIDE: 'wide',
  TALL: 'tall',
  BIG: 'big',
  BIG2: 'big-2',
  BIG3: 'big-3',
  BIG4: 'big-4',
  BIG5: 'big-5',
  BIG6: 'big-6',
} as const;
const FontSizeArray = [...Object.values(FontSizes)] as const;
export type FontSize = (typeof FontSizeArray)[number];

const FontColors = {
  BLACK: 'black',
  BLACKBG: 'bg-black',
  RED: 'red',
  REDBG: 'bg-red',
} as const;
const FontColorArray = [...Object.values(FontColors)] as const;
export type FontColor = (typeof FontColorArray)[number];

const BarCodeTypes = {
  EAN13: 'ean13', // 12 Numeric char
  EAN8: 'ean8', // 7 Numeric char
  UPCA: 'upca', // 11 Numeric char
  UPCE: 'upce', // 6 Numeric char
  B128: '128', // strings
} as const;
const BarCodeTypeArray = [...Object.values(BarCodeTypes)] as const;
export type BarCodeType = (typeof BarCodeTypeArray)[number];

const BarCodeTextPositions = {
  NONE: 'none',
  ABOVE: 'above',
  BELOW: 'below',
} as const;
const BarCodeTextPositionArray = [
  ...Object.values(BarCodeTextPositions),
] as const;
export type BarCodeTextPosition = (typeof BarCodeTextPositionArray)[number];

const Tags = {
  ALLIGNMENT: Allignments,
  FONT: {
    TAG: 'font',
    ATTRIBUTES: {
      SIZE: 'size',
      COLOR: 'color',
      UNDERLINE: 'underline',
    },
    SIZE: FontSizes,
    COLOR: FontColors,
  },
  BOLD: 'b',
  UNDERLINE: 'u',
  IMAGE: 'img',
  BARCODE: {
    TAG: 'barcode',
    ATTRIBUTES: {
      TYPE: 'type',
      HEIGHT: 'height',
      WIDTH: 'width',
      TEXTPOSITION: 'text',
    },
    TYPE: BarCodeTypes,
    TEXT_POSITION: BarCodeTextPositions,
  },
  QRCODE: {
    TAG: 'qrcode',
    ATTRIBUTES: {
      SIZE: 'size',
    },
  },
  BREAKLINE: '\n',
};

const prepend = (text: string, extra: string) => {
  return extra + text;
};

const append = (text: string, extra: string) => {
  return text + extra;
};

const align = (text: string, direction: Allignment) => {
  return prepend(text, direction);
};

const wrap = (text: string, extra1: string, extra2: string) => {
  return extra1 + text + extra2;
};

const attr = (attribute: string, value: string | number) => {
  return `${attribute}='${value}'`;
};

const wraptag = (
  text: string,
  tag: string,
  attributes: Array<[string, string | number | undefined | null]>
) => {
  const modifiers = attributes
    .filter(
      ([key, value]) =>
        key !== null &&
        value !== null &&
        key !== undefined &&
        value !== undefined
    )
    .map(([key, value]) => {
      return attr(key, value as string | number);
    })
    .join(' ');
  const modifier = modifiers.length > 0 ? ` ${modifiers}` : '';
  return wrap(text, `<${tag}${modifier}>`, `</${tag}>`);
};

export default {
  ...{
    ALLIGNMENT: Allignments,
    FONT_SIZE: FontSizes,
    FONT_COLOR: FontSizes,
    BARCODE_TYPE: BarCodeTypes,
    BARCODE_TEXT_POSITION: BarCodeTextPositions,
  },
  /**
   * Create left aligned text
   *
   * @param {string} text text to be formated
   * @return formated text
   */
  left: (text: string) => {
    return align(text, Tags.ALLIGNMENT.LEFT);
  },

  /**
   * Create left aligned text
   *
   * @param {string} text text to be formated
   * @return formated text
   */
  center: (text: string) => {
    return align(text, Tags.ALLIGNMENT.CENTER);
  },

  /**
   * Create right aligned text
   *
   * @param {string} text text to be formated
   * @return formated text
   */
  right: (text: string) => {
    return align(text, Tags.ALLIGNMENT.RIGHT);
  },

  /**
   * Create aligned text
   *
   * @param {string} text text to be formated
   * @param {string} direction Select direction size from **.ALLIGNMENT.???**
   * @return formated text
   */
  align: align,

  /**
   * Create text with custom font
   *
   * @param {string} text text to be formated
   * @param {FontSize} [size=Tags.FONT.SIZE.NORMAL] Select text size from **.FONT.SIZE.???** default is **.FONT.SIZE.NORMAL**
   * @param {FontColor} [color=Tags.FONT.COLOR.BLACK] Select text color from **.FONT.COLOR.???** default is **.FONT.SIZE.BLACK**
   * @return formated text
   */
  font: (
    text: string,
    size: FontSize | null | undefined = Tags.FONT.SIZE.NORMAL,
    color: FontColor | null | undefined = Tags.FONT.COLOR.BLACK,
    underline: boolean | null | undefined = false
  ) => {
    return wraptag(text, Tags.FONT.TAG, [
      [Tags.FONT.ATTRIBUTES.SIZE, size],
      [Tags.FONT.ATTRIBUTES.COLOR, color],
      [Tags.FONT.ATTRIBUTES.UNDERLINE, underline ? '1' : null],
    ]);
  },

  /**
   * Create bold text
   *
   * @param {string} text text to be formated
   * @return formated text
   */
  bold: (text: string) => {
    return wraptag(text, Tags.BOLD, []);
  },

  /**
   * Create underlined text
   *
   * @param {string} text text to be formated
   * @return formated text
   */
  underline: (text: string) => {
    return wraptag(text, Tags.UNDERLINE, []);
  },

  /**
   * Add image
   *
   * @param {string} source image source, protocol must be **file://** or **http[s]://**, remote file willbe downloaded
   * @return image tag text
   */
  image: (source: string) => {
    return wraptag(source, Tags.IMAGE, []);
  },

  /**
   * Create barcode tag
   *
   * @param {string} text barcode payload data
   * @param {(BarCodeType | null | undefined)} [type] Select barcode type from **.BARCODE.TYPE.???** default is **.BARCODE.TYPE.EAN13**
   * @param {(number | null | undefined)} [height] Set barcode height
   * @param {(number | null | undefined)} [width] Set barcode width
   * @param {(BarCodeTextPosition | null | undefined)} [text_position] Select text position from **.BARCODE.TEXTPOSITION.???** default is **.BARCODE.TEXTPOSITION.NONE**
   * @return barcode tag text
   */
  barcode: (
    text: string,
    type?: BarCodeType | null | undefined,
    height?: number | null | undefined,
    width?: number | null | undefined,
    text_position?: BarCodeTextPosition | null | undefined
  ) => {
    if (!type) {
      type = Tags.BARCODE.TYPE.EAN13;
    }
    if (!text_position) {
      text_position = Tags.BARCODE.TEXT_POSITION.NONE;
    }
    return wraptag(text, Tags.BARCODE.TAG, [
      [Tags.BARCODE.ATTRIBUTES.TYPE, type],
      [Tags.BARCODE.ATTRIBUTES.HEIGHT, height],
      [Tags.BARCODE.ATTRIBUTES.WIDTH, width],
      [Tags.BARCODE.ATTRIBUTES.TEXTPOSITION, text_position],
    ]);
  },

  /**
   * Create QRCode tag
   *
   * @param {string} text qrcode payload data
   * @param {number} [size=25] Set qrcode size, default is 25
   * @return barcode tag text
   */
  qrcode: (text: string, size: number = 25) => {
    return wraptag(text, Tags.QRCODE.TAG, [
      [Tags.QRCODE.ATTRIBUTES.SIZE, size],
    ]);
  },

  /**
   * Create a line
   *
   * @param {string} text input
   * @return formated text
   */
  line: (text: string) => {
    return append(text, Tags.BREAKLINE);
  },
};
