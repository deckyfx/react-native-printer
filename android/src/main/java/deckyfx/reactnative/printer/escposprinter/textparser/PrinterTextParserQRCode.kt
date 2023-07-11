package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException
import java.util.Hashtable

class PrinterTextParserQRCode(
    printerTextParserColumn: PrinterTextParserColumn, textAlign: String?,
    qrCodeAttributes: Hashtable<String, String>, data: String
) : PrinterTextParserImg(
    printerTextParserColumn,
    textAlign,
    initConstructor(printerTextParserColumn, qrCodeAttributes, data)
) {
    companion object {
        @Throws(EscPosParserException::class, EscPosBarcodeException::class)
        private fun initConstructor(
            printerTextParserColumn: PrinterTextParserColumn,
            qrCodeAttributes: Hashtable<String, String>,
            data: String
        ): ByteArray {
            var data = data
            val printer = printerTextParserColumn.line.textParser.printer
            data = data.trim { it <= ' ' }
            var size = printer.mmToPx(20f)
            if (qrCodeAttributes.containsKey(PrinterTextParser.ATTR_QRCODE_SIZE)) {
                val qrCodeAttribute = qrCodeAttributes[PrinterTextParser.ATTR_QRCODE_SIZE]
                    ?: throw EscPosParserException("Invalid QR code attribute : " + PrinterTextParser.ATTR_QRCODE_SIZE)
                size = try {
                    printer.mmToPx(qrCodeAttribute.toFloat())
                } catch (nfe: NumberFormatException) {
                    throw EscPosParserException("Invalid QR code " + PrinterTextParser.ATTR_QRCODE_SIZE + " value")
                }
            }
            return EscPosPrinterCommands.QRCodeDataToBytes(data, size)
        }
    }
}
