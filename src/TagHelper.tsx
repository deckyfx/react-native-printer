const Tags = {
  ALLIGNMENT: {
    LEFT: '[L]',
    CENTER: '[C]',
    RIGHT: '[R]',
  },
  FONT: {
    TAG: 'font',
    ATTRIBUTES: {
      SIZE: 'size',
      COLOR: 'color',
    },
    SIZE: {
      NORMAL: 'normal',
      WIDE: 'wide',
      TALL: 'tall',
      BIG: 'big',
      BIG2: 'big-2',
      BIG3: 'big-3',
      BIG4: 'big-4',
      BIG5: 'big-5',
      BIG6: 'big-6',
    },
    COLOR: {
      BLACK: 'black',
      BLACKBG: 'bg-black',
      RED: 'red',
      REDBG: 'bg-red',
    },
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
    TYPE: {
      EAN13: 'ean13',
      EAN8: 'ean8',
      UPCA: 'upca',
      UPCE: 'upce',
      B128: '128',
    },
    TEXTPOSITION: {
      NONE: 'none',
      ABOVE: 'above',
      BELOW: 'below',
    },
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

const wrap = (text: string, extra1: string, extra2: string) => {
  return extra1 + text + extra2;
};

const attr = (attribute: string, value: string | number) => {
  return `${attribute}='${value}'`;
};

const wraptag = (
  text: string,
  tag: string,
  attributes: Array<[string, string | number | null]>
) => {
  const modifier = attributes
    .filter((attribute) => attribute[0] !== null && attribute[1] !== null)
    .map((attribute) => {
      return attr(attribute[0], attribute[1] as string | number);
    })
    .join(' ');
  return wrap(text, `<${tag} ${modifier}>`, `</${tag}>`);
};

export default {
  ...Tags,
  allignLeft: (text: string) => {
    return prepend(Tags.ALLIGNMENT.LEFT, text);
  },
  allignCenter: (text: string) => {
    return prepend(Tags.ALLIGNMENT.CENTER, text);
  },
  allignRight: (text: string) => {
    return prepend(Tags.ALLIGNMENT.RIGHT, text);
  },
  font: (
    text: string,
    size: string = Tags.FONT.SIZE.NORMAL,
    color: string = Tags.FONT.COLOR.BLACK
  ) => {
    return wraptag(text, Tags.FONT.TAG, [
      [Tags.FONT.ATTRIBUTES.SIZE, size],
      [Tags.FONT.ATTRIBUTES.COLOR, color],
    ]);
  },
  bold: (text: string) => {
    return wraptag(text, Tags.BOLD, []);
  },
  underline: (text: string) => {
    return wraptag(text, Tags.UNDERLINE, []);
  },
  image: (source: string) => {
    return wraptag(source, Tags.IMAGE, []);
  },
  barcode: (
    text: string,
    type: string = Tags.BARCODE.TYPE.EAN13,
    height: number | null,
    width: number | null,
    text_position: string = Tags.BARCODE.TEXTPOSITION.NONE
  ) => {
    return wraptag(text, Tags.BARCODE.TAG, [
      [Tags.BARCODE.ATTRIBUTES.TYPE, type],
      [Tags.BARCODE.ATTRIBUTES.HEIGHT, height],
      [Tags.BARCODE.ATTRIBUTES.WIDTH, width],
      [Tags.BARCODE.ATTRIBUTES.TEXTPOSITION, text_position],
    ]);
  },
  qrcode: (text: string, size: number = 25) => {
    return wraptag(text, Tags.QRCODE.TAG, [
      [Tags.QRCODE.ATTRIBUTES.SIZE, size],
    ]);
  },
  line: (text: string) => {
    return append(text, Tags.BREAKLINE);
  },
};
