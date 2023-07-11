package deckyfx.reactnative.printer.escposprinter.barcode

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterSize
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException

abstract class Barcode internal constructor(
    printerSize: EscPosPrinterSize,
    barcodeType: Int,
    code: String,
    widthMM: Float,
    heightMM: Float,
    textPosition: Int
) {
    var barcodeType: Int
        protected set
    var code: String
        protected set
    var colWidth: Int
        protected set
    var height: Int
        protected set
    var textPosition: Int
        protected set

    init {
        var widthMM = widthMM
        this.barcodeType = barcodeType
        this.code = code
        height = printerSize.mmToPx(heightMM)
        this.textPosition = textPosition
        if (widthMM == 0f) {
            widthMM = printerSize.printerWidthMM * 0.7f
        }
        val wantedPxWidth =
            if (widthMM > printerSize.printerWidthMM) printerSize.printerWidthPx else printerSize.mmToPx(
                widthMM
            )
        var colWidth = Math.round(wantedPxWidth.toDouble() / colsCount.toDouble()).toInt()
        if (colWidth * colsCount > printerSize.printerWidthPx) {
            --colWidth
        }
        if (colWidth == 0) {
            throw EscPosBarcodeException("Barcode is too long for the paper size.")
        }
        this.colWidth = colWidth
    }

    abstract val codeLength: Int
    abstract val colsCount: Int
}
