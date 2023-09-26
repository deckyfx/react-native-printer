package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException
import java.util.Hashtable
import java.util.Locale

class PrinterTextParserColumn(textParserLine: PrinterTextParserLine, textColumn: String) {
  val line: PrinterTextParserLine
  var elements = arrayOfNulls<IPrinterTextParserElement>(0)
    private set

  /**
   * Create a new instance of PrinterTextParserColumn.
   *
   * @param textParserLine Parent PrinterTextParserLine instance
   * @param textColumn     Text that the column contain
   */
  init {
    var textColumn = textColumn
    line = textParserLine
    var prependReset = false
    val textParser = line.textParser
    var textAlign = PrinterTextParser.TAGS_ALIGN_LEFT
    val textUnderlineStartColumn = textParser.lastTextUnderline
    val textDoubleStrikeStartColumn = textParser.lastTextDoubleStrike
    val textColorStartColumn = textParser.lastTextColor
    val textReverseColorStartColumn = textParser.lastTextReverseColor

    // =================================================================
    // Reset Printer Setting
    if (textColumn.length > 2) {
      when (textColumn.substring(0, 3).uppercase(Locale.getDefault())) {
        "[" + PrinterTextParser.TAGS_RESET_PRINTER + "]" -> {
          prependReset = true
          textColumn = textColumn.substring(3)
        }
      }
    }

    // =================================================================
    // Check the column alignment
    if (textColumn.length > 2) {
      when (textColumn.substring(0, 3).uppercase(Locale.getDefault())) {
        "[" + PrinterTextParser.TAGS_ALIGN_LEFT + "]", "[" + PrinterTextParser.TAGS_ALIGN_CENTER + "]", "[" + PrinterTextParser.TAGS_ALIGN_RIGHT + "]" -> {
          textAlign = textColumn.substring(1, 2).uppercase(Locale.getDefault())
          textColumn = textColumn.substring(3)
        }
      }
    }
    val trimmedTextColumn = textColumn.trim { it <= ' ' }
    var isImgOrBarcodeLine = false
    if (line.nbrColumns == 1 && trimmedTextColumn.indexOf("<") == 0) {
      // =================================================================
      // Image or Barcode Lines
      val openTagIndex = trimmedTextColumn.indexOf("<")
      val openTagEndIndex = trimmedTextColumn.indexOf(">", openTagIndex + 1) + 1
      if (openTagIndex < openTagEndIndex) {
        val textParserTag =
          PrinterTextParserTag(trimmedTextColumn.substring(openTagIndex, openTagEndIndex))
        when (textParserTag.tagName) {
          PrinterTextParser.TAGS_IMAGE,
          PrinterTextParser.TAGS_BARCODE,
          PrinterTextParser.TAGS_QRCODE,
          PrinterTextParser.TAGS_RAW -> {
            val closeTag = "</" + textParserTag.tagName + ">"
            val closeTagPosition = trimmedTextColumn.length - closeTag.length
            if (trimmedTextColumn.substring(closeTagPosition) == closeTag) {
              when (textParserTag.tagName) {
                PrinterTextParser.TAGS_IMAGE -> appendImage(
                  textAlign,
                  trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                )

                PrinterTextParser.TAGS_BARCODE -> appendBarcode(
                  textAlign,
                  textParserTag.attributes,
                  trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                )

                PrinterTextParser.TAGS_QRCODE -> appendQRCode(
                  textAlign,
                  textParserTag.attributes,
                  trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                )

                PrinterTextParser.TAGS_RAW -> appendRawCommand(
                  trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                )
              }
              isImgOrBarcodeLine = true
            }
          }
        }
      }
    }
    if (!isImgOrBarcodeLine) {
      // =================================================================
      // If the tag is for format text
      var offset = 0
      while (true) {
        var openTagIndex = textColumn.indexOf("<", offset)
        var closeTagIndex = -1
        if (openTagIndex != -1) {
          closeTagIndex = textColumn.indexOf(">", openTagIndex)
        } else {
          openTagIndex = textColumn.length
        }
        this.appendString(textColumn.substring(offset, openTagIndex))
        if (closeTagIndex == -1) {
          break
        }
        closeTagIndex++
        val textParserTag =
          PrinterTextParserTag(textColumn.substring(openTagIndex, closeTagIndex))
        offset = if (PrinterTextParser.isTagTextFormat(textParserTag.tagName)) {
          if (textParserTag.isCloseTag) {
            when (textParserTag.tagName) {
              PrinterTextParser.TAGS_FORMAT_TEXT_BOLD -> textParser.dropTextBold()
              PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE -> {
                textParser.dropLastTextUnderline()
                textParser.dropLastTextDoubleStrike()
              }

              PrinterTextParser.TAGS_FORMAT_TEXT_FONT -> {
                textParser.dropLastTextSize()
                textParser.dropLastTextColor()
                textParser.dropLastTextReverseColor()
              }
            }
          } else {
            when (textParserTag.tagName) {
              PrinterTextParser.TAGS_FORMAT_TEXT_BOLD -> textParser.addTextBold(
                EscPosPrinterCommands.TEXT_WEIGHT_BOLD
              )

              PrinterTextParser.TAGS_FORMAT_TEXT_UNDERLINE -> if (textParserTag.hasAttribute(
                  PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE
                )
              ) {
                when (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE)) {
                  PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE_NORMAL -> {
                    textParser.addTextUnderline(EscPosPrinterCommands.TEXT_UNDERLINE_LARGE)
                    textParser.addTextDoubleStrike(textParser.lastTextDoubleStrike)
                  }

                  PrinterTextParser.ATTR_FORMAT_TEXT_UNDERLINE_TYPE_DOUBLE -> {
                    textParser.addTextUnderline(textParser.lastTextUnderline)
                    textParser.addTextDoubleStrike(EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_ON)
                  }
                }
              } else {
                textParser.addTextUnderline(EscPosPrinterCommands.TEXT_UNDERLINE_LARGE)
                textParser.addTextDoubleStrike(textParser.lastTextDoubleStrike)
              }

              PrinterTextParser.TAGS_FORMAT_TEXT_FONT -> {
                val underline = textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_UNDERLINE) == "1"
                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE)) {
                  when (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE)) {
                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_NORMAL -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_TALL -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_HEIGHT_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_HEIGHT_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_HEIGHT)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_WIDE -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_2 -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_2)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_3 -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_3)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_4 -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_4)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_5 -> {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_5)
                      }
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_SIZE_BIG_6 ->  {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_BIG_6)
                      }
                    }

                    else ->  {
                      if (textParser.printer.useEscAsteriskCommand) {
                        // Using ESC !
                        if (underline) {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL_UNDERLINED_ALT)
                        } else {
                          textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL_ALT)
                        }
                      } else {
                        // Using GS !
                        textParser.addTextSize(EscPosPrinterCommands.TEXT_SIZE_NORMAL)
                      }
                    }
                  }
                } else {
                  textParser.addTextSize(textParser.lastTextSize)
                }
                if (textParserTag.hasAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR)) {
                  when (textParserTag.getAttribute(PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR)) {
                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BLACK -> {
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK)
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF)
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BG_BLACK -> {
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK)
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_ON)
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_RED -> {
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_RED)
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF)
                    }

                    PrinterTextParser.ATTR_FORMAT_TEXT_FONT_COLOR_BG_RED -> {
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_RED)
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_ON)
                    }

                    else -> {
                      textParser.addTextColor(EscPosPrinterCommands.TEXT_COLOR_BLACK)
                      textParser.addTextReverseColor(EscPosPrinterCommands.TEXT_COLOR_REVERSE_OFF)
                    }
                  }
                } else {
                  textParser.addTextColor(textParser.lastTextColor)
                  textParser.addTextReverseColor(textParser.lastTextReverseColor)
                }
              }
            }
          }
          closeTagIndex
        } else {
          this.appendString("<")
          openTagIndex + 1
        }
      }

      // =================================================================
      // Define the number of spaces required for the different alignments
      val nbrCharColumn = line.nbrCharColumn
      var nbrCharForgetted = line.nbrCharForgetted
      var nbrCharColumnExceeded = line.nbrCharColumnExceeded
      var nbrCharTextWithoutTag = 0
      var leftSpace = 0
      var rightSpace = 0
      for (textParserElement in elements) {
        nbrCharTextWithoutTag += textParserElement!!.length()
      }
      when (textAlign) {
        PrinterTextParser.TAGS_ALIGN_LEFT -> rightSpace =
          nbrCharColumn - nbrCharTextWithoutTag

        PrinterTextParser.TAGS_ALIGN_CENTER -> {
          leftSpace =
            Math.floor(((nbrCharColumn.toFloat() - nbrCharTextWithoutTag.toFloat()) / 2f).toDouble())
              .toInt()
          rightSpace = nbrCharColumn - nbrCharTextWithoutTag - leftSpace
        }

        PrinterTextParser.TAGS_ALIGN_RIGHT -> leftSpace =
          nbrCharColumn - nbrCharTextWithoutTag
      }
      if (nbrCharForgetted > 0) {
        nbrCharForgetted -= 1
        rightSpace++
      }
      if (nbrCharColumnExceeded < 0) {
        leftSpace += nbrCharColumnExceeded
        nbrCharColumnExceeded = 0
        if (leftSpace < 1) {
          rightSpace += leftSpace - 1
          leftSpace = 1
        }
      }
      if (leftSpace < 0) {
        nbrCharColumnExceeded += leftSpace
        leftSpace = 0
      }
      if (rightSpace < 0) {
        nbrCharColumnExceeded += rightSpace
        rightSpace = 0
      }
      if (leftSpace > 0) {
        if (!textParser.printer.useEscAsteriskCommand) {
          this.prependString(
            generateSpace(leftSpace),
            EscPosPrinterCommands.TEXT_SIZE_NORMAL_ALT,
            null,
            null,
            null,
            null,
            null
          )
        } else {
          this.prependString(
            generateSpace(leftSpace),
            EscPosPrinterCommands.TEXT_SIZE_NORMAL,
            textColorStartColumn,
            textReverseColorStartColumn,
            EscPosPrinterCommands.TEXT_WEIGHT_NORMAL,
            textUnderlineStartColumn,
            textDoubleStrikeStartColumn
          )
        }
      }
      if (rightSpace > 0) {
        if (!textParser.printer.useEscAsteriskCommand) {
          this.appendString(
            generateSpace(rightSpace),
            EscPosPrinterCommands.TEXT_SIZE_NORMAL,
            textParser.lastTextColor,
            textParser.lastTextReverseColor,
            EscPosPrinterCommands.TEXT_WEIGHT_NORMAL,
            textParser.lastTextUnderline,
            textParser.lastTextDoubleStrike
          )
        }
        /**
         * Don't Append right element for ESC *
         * */
      }

      // =================================================================================================
      // nbrCharForgetted and nbrCharColumnExceeded is use to define number of spaces for the next columns
      line
        .setNbrCharForgetted(nbrCharForgetted)
        .setNbrCharColumnExceeded(nbrCharColumnExceeded)
    }
    if (prependReset) {
      prependElement(PrinterTextParserCommand(EscPosCommands.INITIALIZE.toByteArray()))
    }
  }

  private fun prependString(text: String): PrinterTextParserColumn {
    val textParser = line.textParser
    return this.prependString(
      text,
      textParser.lastTextSize,
      textParser.lastTextColor,
      textParser.lastTextReverseColor,
      textParser.lastTextBold,
      textParser.lastTextUnderline,
      textParser.lastTextDoubleStrike
    )
  }

  private fun prependString(
    text: String?,
    textSize: ByteArray?,
    textColor: ByteArray?,
    textReverseColor: ByteArray?,
    textBold: ByteArray?,
    textUnderline: ByteArray?,
    textDoubleStrike: ByteArray?
  ): PrinterTextParserColumn {
    return prependElement(
      PrinterTextParserString(
        this,
        text,
        textSize,
        textColor,
        textReverseColor,
        textBold,
        textUnderline,
        textDoubleStrike
      )
    )
  }

  private fun appendString(text: String): PrinterTextParserColumn {
    val textParser = line.textParser
    return this.appendString(
      text,
      textParser.lastTextSize,
      textParser.lastTextColor,
      textParser.lastTextReverseColor,
      textParser.lastTextBold,
      textParser.lastTextUnderline,
      textParser.lastTextDoubleStrike
    )
  }

  private fun appendString(
    text: String?,
    textSize: ByteArray?,
    textColor: ByteArray?,
    textReverseColor: ByteArray?,
    textBold: ByteArray?,
    textUnderline: ByteArray?,
    textDoubleStrike: ByteArray?
  ): PrinterTextParserColumn {
    val printer = line.textParser.printer
    return this.appendElement(
      PrinterTextParserString(
        this,
        text,
        textSize,
        textColor,
        textReverseColor,
        textBold,
        textUnderline,
        textDoubleStrike
      )
    )
  }

  private fun prependImage(textAlign: String, hexString: String): PrinterTextParserColumn {
    return prependElement(PrinterTextParserImg(this, textAlign, hexString))
  }

  private fun appendImage(textAlign: String, hexString: String): PrinterTextParserColumn {
    return this.appendElement(PrinterTextParserImg(this, textAlign, hexString))
  }

  @Throws(EscPosParserException::class, EscPosBarcodeException::class)
  private fun prependBarcode(
    textAlign: String,
    barcodeAttributes: Hashtable<String, String>,
    code: String
  ): PrinterTextParserColumn {
    return prependElement(PrinterTextParserBarcode(this, textAlign, barcodeAttributes, code))
  }

  @Throws(EscPosParserException::class, EscPosBarcodeException::class)
  private fun appendBarcode(
    textAlign: String,
    barcodeAttributes: Hashtable<String, String>,
    code: String
  ): PrinterTextParserColumn {
    return this.appendElement(
      PrinterTextParserBarcode(
        this,
        textAlign,
        barcodeAttributes,
        code
      )
    )
  }

  @Throws(EscPosParserException::class, EscPosBarcodeException::class)
  private fun prependQRCode(
    textAlign: String,
    qrCodeAttributes: Hashtable<String, String>,
    data: String
  ): PrinterTextParserColumn {
    return prependElement(PrinterTextParserBarcode(this, textAlign, qrCodeAttributes, data))
  }

  @Throws(EscPosParserException::class, EscPosBarcodeException::class)
  private fun appendQRCode(
    textAlign: String,
    qrCodeAttributes: Hashtable<String, String>,
    data: String
  ): PrinterTextParserColumn {
    return this.appendElement(PrinterTextParserQRCode(this, textAlign, qrCodeAttributes, data))
  }

  @Throws(EscPosParserException::class, EscPosBarcodeException::class)
  private fun appendRawCommand(
    data: String
  ): PrinterTextParserColumn {
    return this.appendElement(PrinterTextParserCommand(data))
  }

  private fun prependElement(element: IPrinterTextParserElement): PrinterTextParserColumn {
    val elementsTmp = arrayOfNulls<IPrinterTextParserElement>(elements.size + 1)
    elementsTmp[0] = element
    System.arraycopy(elements, 0, elementsTmp, 1, elements.size)
    elements = elementsTmp
    return this
  }

  private fun appendElement(element: IPrinterTextParserElement): PrinterTextParserColumn {
    val elementsTmp = arrayOfNulls<IPrinterTextParserElement>(elements.size + 1)
    System.arraycopy(elements, 0, elementsTmp, 0, elements.size)
    elementsTmp[elements.size] = element
    elements = elementsTmp
    return this
  }

  companion object {
    private fun generateSpace(nbrSpace: Int): String {
      val str = StringBuilder()
      for (i in 0 until nbrSpace) {
        str.append(" ")
      }
      return str.toString()
    }
  }
}
