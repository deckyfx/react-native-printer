package deckyfx.reactnative.printer.escposprinter.barcode

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterSize

class BarcodeUPCA(
    printerSize: EscPosPrinterSize?,
    code: String?,
    widthMM: Float,
    heightMM: Float,
    textPosition: Int
) : BarcodeNumber(
    printerSize,
    EscPosPrinterCommands.BARCODE_TYPE_UPCA,
    code,
    widthMM,
    heightMM,
    textPosition
) {
    override val codeLength: Int
        get() = 12
}
