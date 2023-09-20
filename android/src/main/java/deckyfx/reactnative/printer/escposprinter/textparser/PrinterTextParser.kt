package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException

class PrinterTextParser(val printer: EscPosPrinter) {
  private var textSize = arrayOf<ByteArray?>(EscPosPrinterCommands.TEXT_SIZE_NORMAL)
  private var textColor = arrayOf<ByteArray?>(EscPosPrinterCommands.TEXT_COLOR_BLACK)
  private var textReverseColor = arrayOf<ByteArray?>(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF)
  private var textBold = arrayOf<ByteArray?>(EscPosPrinterCommands.TEXT_WEIGHT_NORMAL)
  private var textUnderline = arrayOf<ByteArray?>(EscPosPrinterCommands.TEXT_UNDERLINE_OFF)
  private var textDoubleStrike = arrayOf<ByteArray?>(EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_OFF)
  private var text = ""
  fun setFormattedText(text: String): PrinterTextParser {
    this.text = text
    return this
  }

  val lastTextSize: ByteArray?
    get() = textSize[textSize.size - 1]

  fun addTextSize(newTextSize: ByteArray?): PrinterTextParser {
    textSize = arrayBytePush(textSize, newTextSize)
    return this
  }

  fun dropLastTextSize(): PrinterTextParser {
    if (textSize.size > 1) {
      textSize = arrayByteDropLast(textSize)
    }
    return this
  }

  val lastTextColor: ByteArray?
    get() = textColor[textColor.size - 1]

  fun addTextColor(newTextColor: ByteArray?): PrinterTextParser {
    textColor = arrayBytePush(textColor, newTextColor)
    return this
  }

  fun dropLastTextColor(): PrinterTextParser {
    if (textColor.size > 1) {
      textColor = arrayByteDropLast(textColor)
    }
    return this
  }

  val lastTextReverseColor: ByteArray?
    get() = textReverseColor[textReverseColor.size - 1]

  fun addTextReverseColor(newTextReverseColor: ByteArray?): PrinterTextParser {
    textReverseColor = arrayBytePush(textReverseColor, newTextReverseColor)
    return this
  }

  fun dropLastTextReverseColor(): PrinterTextParser {
    if (textReverseColor.size > 1) {
      textReverseColor = arrayByteDropLast(textReverseColor)
    }
    return this
  }

  val lastTextBold: ByteArray?
    get() = textBold[textBold.size - 1]

  fun addTextBold(newTextBold: ByteArray?): PrinterTextParser {
    textBold = arrayBytePush(textBold, newTextBold)
    return this
  }

  fun dropTextBold(): PrinterTextParser {
    if (textBold.size > 1) {
      textBold = arrayByteDropLast(textBold)
    }
    return this
  }

  val lastTextUnderline: ByteArray?
    get() = textUnderline[textUnderline.size - 1]

  fun addTextUnderline(newTextUnderline: ByteArray?): PrinterTextParser {
    textUnderline = arrayBytePush(textUnderline, newTextUnderline)
    return this
  }

  fun dropLastTextUnderline(): PrinterTextParser {
    if (textUnderline.size > 1) {
      textUnderline = arrayByteDropLast(textUnderline)
    }
    return this
  }

  val lastTextDoubleStrike: ByteArray?
    get() = textDoubleStrike[textDoubleStrike.size - 1]

  fun addTextDoubleStrike(newTextDoubleStrike: ByteArray?): PrinterTextParser {
    textDoubleStrike = arrayBytePush(textDoubleStrike, newTextDoubleStrike)
    return this
  }

  fun dropLastTextDoubleStrike(): PrinterTextParser {
    if (textDoubleStrike.size > 1) {
      textDoubleStrike = arrayByteDropLast(textDoubleStrike)
    }
    return this
  }

  @Throws(
    EscPosParserException::class,
    EscPosBarcodeException::class,
    EscPosEncodingException::class
  )
  fun parse(): Array<PrinterTextParserLine?> {
    val stringLines =
      text.split("\n|\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val lines = arrayOfNulls<PrinterTextParserLine>(stringLines.size)
    var i = 0
    for (line in stringLines) {
      lines[i++] = PrinterTextParserLine(this, line)
    }
    return lines
  }

  companion object {
    const val TAGS_ALIGN_LEFT = "L"
    const val TAGS_ALIGN_CENTER = "C"
    const val TAGS_ALIGN_RIGHT = "R"
    const val TAGS_RESET_PRINTER = "X"
    val TAGS_ALIGN = arrayOf(TAGS_ALIGN_LEFT, TAGS_ALIGN_CENTER, TAGS_ALIGN_RIGHT)
    const val TAGS_IMAGE = "img"
    const val TAGS_BARCODE = "barcode"
    const val TAGS_QRCODE = "qrcode"
    const val ATTR_BARCODE_WIDTH = "width"
    const val ATTR_BARCODE_HEIGHT = "height"
    const val ATTR_BARCODE_TYPE = "type"
    const val ATTR_BARCODE_TYPE_EAN8 = "ean8"
    const val ATTR_BARCODE_TYPE_EAN13 = "ean13"
    const val ATTR_BARCODE_TYPE_UPCA = "upca"
    const val ATTR_BARCODE_TYPE_UPCE = "upce"
    const val ATTR_BARCODE_TYPE_128 = "128"
    const val ATTR_BARCODE_TEXT_POSITION = "text"
    const val ATTR_BARCODE_TEXT_POSITION_NONE = "none"
    const val ATTR_BARCODE_TEXT_POSITION_ABOVE = "above"
    const val ATTR_BARCODE_TEXT_POSITION_BELOW = "below"
    const val TAGS_FORMAT_TEXT_FONT = "font"
    const val TAGS_FORMAT_TEXT_BOLD = "b"
    const val TAGS_FORMAT_TEXT_UNDERLINE = "u"
    val TAGS_FORMAT_TEXT =
      arrayOf(TAGS_FORMAT_TEXT_FONT, TAGS_FORMAT_TEXT_BOLD, TAGS_FORMAT_TEXT_UNDERLINE)
    const val ATTR_FORMAT_TEXT_UNDERLINE_TYPE = "type"
    const val ATTR_FORMAT_TEXT_UNDERLINE_TYPE_NORMAL = "normal"
    const val ATTR_FORMAT_TEXT_UNDERLINE_TYPE_DOUBLE = "double"
    const val ATTR_FORMAT_TEXT_FONT_SIZE = "size"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_BIG = "big"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_BIG_2 = "big-2"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_BIG_3 = "big-3"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_BIG_4 = "big-4"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_BIG_5 = "big-5"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_BIG_6 = "big-6"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_TALL = "tall"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_WIDE = "wide"
    const val ATTR_FORMAT_TEXT_FONT_SIZE_NORMAL = "normal"
    const val ATTR_FORMAT_TEXT_FONT_COLOR = "color"
    const val ATTR_FORMAT_TEXT_FONT_UNDERLINE = "underline"
    const val ATTR_FORMAT_TEXT_FONT_COLOR_BLACK = "black"
    const val ATTR_FORMAT_TEXT_FONT_COLOR_BG_BLACK = "bg-black"
    const val ATTR_FORMAT_TEXT_FONT_COLOR_RED = "red"
    const val ATTR_FORMAT_TEXT_FONT_COLOR_BG_RED = "bg-red"
    const val ATTR_QRCODE_SIZE = "size"
    var regexAlignTags: String? = null
      get() {
        if (field == null) {
          val regexAlignTags = StringBuilder()
          for (i in TAGS_ALIGN.indices) {
            regexAlignTags.append("|\\[").append(TAGS_ALIGN[i]).append("\\]")
          }
          field = regexAlignTags.toString().substring(1)
        }
        return field
      }
      private set

    fun isTagTextFormat(tagName: String): Boolean {
      var tagName = tagName
      if (tagName.substring(0, 1) == "/") {
        tagName = tagName.substring(1)
      }
      for (tag in TAGS_FORMAT_TEXT) {
        if (tag == tagName) {
          return true
        }
      }
      return false
    }

    fun arrayByteDropLast(arr: Array<ByteArray?>): Array<ByteArray?> {
      if (arr.isEmpty()) {
        return arr
      }
      val newArr = arrayOfNulls<ByteArray>(arr.size - 1)
      System.arraycopy(arr, 0, newArr, 0, newArr.size)
      return newArr
    }

    fun arrayBytePush(arr: Array<ByteArray?>, add: ByteArray?): Array<ByteArray?> {
      val newArr = arrayOfNulls<ByteArray>(arr.size + 1)
      System.arraycopy(arr, 0, newArr, 0, arr.size)
      newArr[arr.size] = add
      return newArr
    }
  }
}
