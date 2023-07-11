package deckyfx.reactnative.printer.escposprinter.barcode

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterSize

class BarcodeEAN8(
    printerSize: EscPosPrinterSize?,
    code: String?,
    widthMM: Float,
    heightMM: Float,
    textPosition: Int
) : BarcodeNumber(
    printerSize,
    EscPosPrinterCommands.BARCODE_TYPE_EAN8,
    code,
    widthMM,
    heightMM,
    textPosition
) {
    override val codeLength: Int
        get() = 8
}
