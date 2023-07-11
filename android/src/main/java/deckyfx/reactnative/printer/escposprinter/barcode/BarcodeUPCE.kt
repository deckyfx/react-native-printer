package deckyfx.reactnative.printer.escposprinter.barcode

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterSize
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException

class BarcodeUPCE(
    printerSize: EscPosPrinterSize?,
    code: String?,
    widthMM: Float,
    heightMM: Float,
    textPosition: Int
) : Barcode(
    printerSize!!, EscPosPrinterCommands.BARCODE_TYPE_UPCE, code!!, widthMM, heightMM, textPosition
) {
    init {
        checkCode()
    }

    override val codeLength: Int
        get() = 6
    override val colsCount: Int
        get() = codeLength * 7 + 16

    @Throws(EscPosBarcodeException::class)
    private fun checkCode() {
        val codeLength = codeLength
        if (this.code.length < codeLength) {
            throw EscPosBarcodeException("Code is too short for the barcode type.")
        }
        try {
            this.code = this.code.substring(0, codeLength)
            for (i in 0 until codeLength) {
                this.code.substring(i, i + 1).toInt(10)
            }
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            throw EscPosBarcodeException("Invalid barcode number")
        }
    }
}
