package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.barcode.Barcode
import deckyfx.reactnative.printer.escposprinter.barcode.Barcode128
import deckyfx.reactnative.printer.escposprinter.barcode.BarcodeEAN13
import deckyfx.reactnative.printer.escposprinter.barcode.BarcodeEAN8
import deckyfx.reactnative.printer.escposprinter.barcode.BarcodeUPCA
import deckyfx.reactnative.printer.escposprinter.barcode.BarcodeUPCE
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException
import java.util.Hashtable

class PrinterTextParserBarcode(
  printerTextParserColumn: PrinterTextParserColumn,
  textAlign: String?,
  barcodeAttributes: Hashtable<String, String>? = Hashtable(),
  code: String
) : IPrinterTextParserElement {
  private var barcode: Barcode? = null
  private val length: Int
  private var align: ByteArray

  init {
    var code = code
    val printer = printerTextParserColumn.line.textParser.printer
    code = code.trim { it <= ' ' }
    align = EscPosPrinterCommands.TEXT_ALIGN_LEFT
    when (textAlign) {
      PrinterTextParser.TAGS_ALIGN_CENTER -> align = EscPosPrinterCommands.TEXT_ALIGN_CENTER
      PrinterTextParser.TAGS_ALIGN_RIGHT -> align = EscPosPrinterCommands.TEXT_ALIGN_RIGHT
    }
    length = printer.printerNbrCharactersPerLine
    var height = 10f
    if (barcodeAttributes?.containsKey(PrinterTextParser.ATTR_BARCODE_HEIGHT) == true) {
      val barCodeAttribute = barcodeAttributes[PrinterTextParser.ATTR_BARCODE_HEIGHT]
        ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_HEIGHT)
      height = try {
        barCodeAttribute.toFloat()
      } catch (nfe: NumberFormatException) {
        throw EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_HEIGHT + " value")
      }
    }
    var width = 0f
    if (barcodeAttributes?.containsKey(PrinterTextParser.ATTR_BARCODE_WIDTH) == true) {
      val barCodeAttribute = barcodeAttributes[PrinterTextParser.ATTR_BARCODE_WIDTH]
        ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_WIDTH)
      width = try {
        barCodeAttribute.toFloat()
      } catch (nfe: NumberFormatException) {
        throw EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_WIDTH + " value")
      }
    }
    var textPosition = EscPosPrinterCommands.BARCODE_TEXT_POSITION_BELOW
    if (barcodeAttributes?.containsKey(PrinterTextParser.ATTR_BARCODE_TEXT_POSITION) == true) {
      val barCodeAttribute = barcodeAttributes[PrinterTextParser.ATTR_BARCODE_TEXT_POSITION]
        ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_TEXT_POSITION)
      when (barCodeAttribute) {
        PrinterTextParser.ATTR_BARCODE_TEXT_POSITION_NONE -> textPosition =
          EscPosPrinterCommands.BARCODE_TEXT_POSITION_NONE

        PrinterTextParser.ATTR_BARCODE_TEXT_POSITION_ABOVE -> textPosition =
          EscPosPrinterCommands.BARCODE_TEXT_POSITION_ABOVE
      }
    }
    var barcodeType: String? = PrinterTextParser.ATTR_BARCODE_TYPE_EAN13
    if (barcodeAttributes?.containsKey(PrinterTextParser.ATTR_BARCODE_TYPE) == true) {
      barcodeType = barcodeAttributes[PrinterTextParser.ATTR_BARCODE_TYPE]
      if (barcodeType == null) {
        throw EscPosParserException("Invalid barcode attribute : " + PrinterTextParser.ATTR_BARCODE_TYPE)
      }
    }
    when (barcodeType) {
      PrinterTextParser.ATTR_BARCODE_TYPE_EAN8 -> barcode =
        BarcodeEAN8(printer, code, width, height, textPosition)

      PrinterTextParser.ATTR_BARCODE_TYPE_EAN13 -> barcode =
        BarcodeEAN13(printer, code, width, height, textPosition)

      PrinterTextParser.ATTR_BARCODE_TYPE_UPCA -> barcode =
        BarcodeUPCA(printer, code, width, height, textPosition)

      PrinterTextParser.ATTR_BARCODE_TYPE_UPCE -> barcode =
        BarcodeUPCE(printer, code, width, height, textPosition)

      PrinterTextParser.ATTR_BARCODE_TYPE_128 -> barcode =
        Barcode128(printer, code, width, height, textPosition)

      else -> throw EscPosParserException("Invalid barcode attribute : " + PrinterTextParser.ATTR_BARCODE_TYPE)
    }
  }

  /**
   * Get the barcode width in char length.
   *
   * @return int
   */
  @Throws(EscPosEncodingException::class)
  override fun length(): Int {
    return length
  }

  /**
   * Print barcode
   *
   * @param printerSocket Instance of EscPosPrinterCommands
   * @return this Fluent method
   */
  override fun print(printerSocket: EscPosPrinterCommands?): PrinterTextParserBarcode {
    printerSocket
      ?.setAlign(align)
      ?.printBarcode(barcode!!)
    return this
  }
}
