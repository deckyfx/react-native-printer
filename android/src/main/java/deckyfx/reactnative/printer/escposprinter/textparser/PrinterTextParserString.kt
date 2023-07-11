package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosCharsetEncoding
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Arrays

class PrinterTextParserString(
    printerTextParserColumn: PrinterTextParserColumn,
    private val text: String?,
    private val textSize: ByteArray?,
    private val textColor: ByteArray?,
    private val textReverseColor: ByteArray?,
    private val textBold: ByteArray?,
    private val textUnderline: ByteArray?,
    private val textDoubleStrike: ByteArray?
) : IPrinterTextParserElement {
    private val printer: EscPosPrinter

    init {
        printer = printerTextParserColumn.line.textParser.printer
    }

    @Throws(EscPosEncodingException::class)
    override fun length(): Int {
        if (text.isNullOrEmpty()) return 0
        val charsetEncoding: EscPosCharsetEncoding = printer.encoding
        var coef = 1
        if (Arrays.equals(textSize, EscPosPrinterCommands.TEXT_SIZE_DOUBLE_WIDTH) || Arrays.equals(
                textSize, EscPosPrinterCommands.TEXT_SIZE_BIG
            )
        ) coef = 2 else if (Arrays.equals(
                textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_2
            )
        ) coef = 3 else if (Arrays.equals(
                textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_3
            )
        ) coef = 4 else if (Arrays.equals(
                textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_4
            )
        ) coef = 5 else if (Arrays.equals(
                textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_5
            )
        ) coef = 6 else if (Arrays.equals(
                textSize, EscPosPrinterCommands.TEXT_SIZE_BIG_6
            )
        ) coef = 7
        return if (charsetEncoding != null) {
            try {
                text.toByteArray(Charset.forName(charsetEncoding.name)).size * coef
            } catch (e: UnsupportedEncodingException) {
                throw EscPosEncodingException(e.message)
            }
        } else text.length * coef
    }

    /**
     * Print text
     *
     * @param printerSocket Instance of EscPosPrinterCommands
     * @return this Fluent method
     */
    @Throws(EscPosEncodingException::class)
    override fun print(printerSocket: EscPosPrinterCommands?): PrinterTextParserString {
        printerSocket?.printText(
            text,
            textSize,
            textColor,
            textReverseColor,
            textBold,
            textUnderline,
            textDoubleStrike
        )
        return this
    }
}
