package deckyfx.reactnative.printer.escposprinter

class EscPosCommands {
  companion object {
    /**
     * ASCII null control character
     */
    const val NUL: Byte = 0x00

    /**
     * ASCII linefeed control character
     */
    const val LF: Byte = 0x0A

    /**
     * ASCII escape control character
     */
    const val ESC: Byte = 0x1B

    /**
     * ASCII form separator control character
     */
    const val FS: Byte = 0x1C

    /**
     * ASCII form feed control character
     */
    const val FF: Byte = 0x0C

    /**
     * ASCII group separator control character
     */
    const val GS: Byte = 0x1D

    /**
     * ASCII data link escape control character
     */
    const val DLE: Byte = 0x10

    /**
     * ASCII end of transmission control character
     */
    const val EOT: Byte = 0x04

    /**
     * Horizontal tab
     */
    const val HT: Byte = 0x09

    /**
     * Carriage Retur
     */
    const val CR: Byte = 0x0D

    /**
     * !
     */
    const val EXCLAMATION: Byte = 0x21

    /**
     * Indicates UPC-A barcode when used with Printer::barcode
     */
    const val BARCODE_UPCA = 65

    /**
     * Indicates UPC-E barcode when used with Printer::barcode
     */
    const val BARCODE_UPCE = 66

    /**
     * Indicates JAN13 barcode when used with Printer::barcode
     */
    const val BARCODE_JAN13 = 67

    /**
     * Indicates JAN8 barcode when used with Printer::barcode
     */
    const val BARCODE_JAN8 = 68

    /**
     * Indicates CODE39 barcode when used with Printer::barcode
     */
    const val BARCODE_CODE39 = 69

    /**
     * Indicates ITF barcode when used with Printer::barcode
     */
    const val BARCODE_ITF = 70

    /**
     * Indicates CODABAR barcode when used with Printer::barcode
     */
    const val BARCODE_CODABAR = 71

    /**
     * Indicates CODE93 barcode when used with Printer::barcode
     */
    const val BARCODE_CODE93 = 72

    /**
     * Indicates CODE128 barcode when used with Printer::barcode
     */
    const val BARCODE_CODE128 = 73

    /**
     * Indicates that HRI (human-readable interpretation) text should not be
     * printed, when used with Printer::setBarcodeTextPosition
     */
    const val BARCODE_TEXT_NONE = 0

    /**
     * Indicates that HRI (human-readable interpretation) text should be printed
     * above a barcode, when used with Printer::setBarcodeTextPosition
     */
    const val BARCODE_TEXT_ABOVE = 1

    /**
     * Indicates that HRI (human-readable interpretation) text should be printed
     * below a barcode, when used with Printer::setBarcodeTextPosition
     */
    const val BARCODE_TEXT_BELOW = 2

    /**
     * Use the first color (usually black), when used with Printer::setColor
     */
    const val COLOR_1 = 0

    /**
     * Use the second color (usually red or blue), when used with Printer::setColor
     */
    const val COLOR_2 = 1

    /**
     * Make a full cut, when used with Printer::cut
     */
    const val CUT_FULL = 65

    /**
     * Make a partial cut, when used with Printer::cut
     */
    const val CUT_PARTIAL = 66

    /**
     * Use Font A, when used with Printer::setFont
     */
    const val FONT_A = 0

    /**
     * Use Font B, when used with Printer::setFont
     */
    const val FONT_B = 1

    /**
     * Use Font C, when used with Printer::setFont
     */
    const val FONT_C = 2

    /**
     * Use default (high density) image size, when used with Printer::graphics,
     * Printer::bitImage or Printer::bitImageColumnFormat
     */
    const val IMG_DEFAULT = 0

    /**
     * Use lower horizontal density for image printing, when used with Printer::graphics,
     * Printer::bitImage or Printer::bitImageColumnFormat
     */
    const val IMG_DOUBLE_WIDTH = 1

    /**
     * Use lower vertical density for image printing, when used with Printer::graphics,
     * Printer::bitImage or Printer::bitImageColumnFormat
     */
    const val IMG_DOUBLE_HEIGHT = 2

    /**
     * Align text to the left, when used with Printer::setJustification
     */
    const val JUSTIFY_LEFT = 0

    /**
     * Center text, when used with Printer::setJustification
     */
    const val JUSTIFY_CENTER = 1

    /**
     * Align text to the right, when used with Printer::setJustification
     */
    const val JUSTIFY_RIGHT = 2

    /**
     * Use Font A, when used with Printer::selectPrintMode
     */
    const val MODE_FONT_A = 0

    /**
     * Use Font B, when used with Printer::selectPrintMode
     */
    const val MODE_FONT_B = 1

    /**
     * Use text emphasis, when used with Printer::selectPrintMode
     */
    const val MODE_EMPHASIZED = 8

    /**
     * Use double height text, when used with Printer::selectPrintMode
     */
    const val MODE_DOUBLE_HEIGHT = 16

    /**
     * Use double width text, when used with Printer::selectPrintMode
     */
    const val MODE_DOUBLE_WIDTH = 32

    /**
     * Underline text, when used with Printer::selectPrintMode
     */
    const val MODE_UNDERLINE = 128

    /**
     * Indicates standard PDF417 code
     */
    const val PDF417_STANDARD = 0

    /**
     * Indicates truncated PDF417 code
     */
    const val PDF417_TRUNCATED = 1

    /**
     * Indicates error correction level L when used with Printer::qrCode
     */
    const val QR_ECLEVEL_L = 0

    /**
     * Indicates error correction level M when used with Printer::qrCode
     */
    const val QR_ECLEVEL_M = 1

    /**
     * Indicates error correction level Q when used with Printer::qrCode
     */
    const val QR_ECLEVEL_Q = 2

    /**
     * Indicates error correction level H when used with Printer::qrCode
     */
    const val QR_ECLEVEL_H = 3

    /**
     * Indicates QR model 1 when used with Printer::qrCode
     */
    const val QR_MODEL_1 = 1

    /**
     * Indicates QR model 2 when used with Printer::qrCode
     */
    const val QR_MODEL_2 = 2

    /**
     * Indicates micro QR code when used with Printer::qrCode
     */
    const val QR_MICRO = 3

    /**
     * Indicates a request for printer status when used with
     * Printer::getPrinterStatus (experimental)
     */
    const val STATUS_PRINTER = 1

    /**
     * Indicates a request for printer offline cause when used with
     * Printer::getPrinterStatus (experimental)
     */
    const val STATUS_OFFLINE_CAUSE = 2

    /**
     * Indicates a request for error cause when used with Printer::getPrinterStatus
     * (experimental)
     */
    const val STATUS_ERROR_CAUSE = 3

    /**
     * Indicates a request for error cause when used with Printer::getPrinterStatus
     * (experimental)
     */
    const val STATUS_PAPER_ROLL = 4

    /**
     * Indicates a request for ink A status when used with Printer::getPrinterStatus
     * (experimental)
     */
    const val STATUS_INK_A = 7

    /**
     * Indicates a request for ink B status when used with Printer::getPrinterStatus
     * (experimental)
     */
    const val STATUS_INK_B = 6

    /**
     * Indicates a request for peeler status when used with Printer::getPrinterStatus
     * (experimental)
     */
    const val STATUS_PEELER = 8

    /**
     * Indicates no underline when used with Printer::setUnderline
     */
    const val UNDERLINE_NONE = 0

    /**
     * Indicates single underline when used with Printer::setUnderline
     */
    const val UNDERLINE_SINGLE = 1

    /**
     * Indicates double underline when used with Printer::setUnderline
     */
    const val UNDERLINE_DOUBLE = 2

    val PRINTER_ID_1 = arrayOf(GS, 0x49, 0x42) // GS I n
    val PRINTER_ID_2 = arrayOf(GS, 0x49, 0x43) // GS I n
    val PRINTER_ID_1_ALT = arrayOf(GS, 0x49, 0x01) // GS I n
    val PRINTER_ID_2_ALT = arrayOf(GS, 0x49, 0x02) // GS I n

    val INITIALIZE = arrayOf(ESC, 0x40) // ESC @

    fun byteArray(from: Array<Int>): ByteArray {
      return ByteArray(from.size) { from[it].toByte() }
    }

    fun byteArray(from: Int): ByteArray {
      return ByteArray(1) { from.toByte() }
    }
  }
}
