package deckyfx.reactnative.printer.escposprinter.barcode

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterSize

class Barcode128(
  printerSize: EscPosPrinterSize?,
  code: String?,
  widthMM: Float,
  heightMM: Float,
  textPosition: Int
) : Barcode(
  printerSize!!, EscPosPrinterCommands.BARCODE_TYPE_128, code!!, widthMM, heightMM, textPosition
) {

  override val codeLength: Int
    get() = this.code.length

  override val colsCount: Int
    get() = (codeLength + 5) * 11
}
