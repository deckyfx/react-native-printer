package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException

open class PrinterTextParserCommand(
  private val command: ByteArray
) : IPrinterTextParserElement {
  constructor(command: String) : this(hexadecimalStringToBytes(command))

  /**
   * Get the image width in char length.
   *
   * @return int
   */
  @Throws(EscPosEncodingException::class)
  override fun length(): Int {
    return command.size
  }

  /**
   * Print image
   *
   * @param printerSocket Instance of EscPosPrinterCommands
   * @return this Fluent method
   */
  @Throws(EscPosConnectionException::class)
  override fun print(printerSocket: EscPosPrinterCommands?): PrinterTextParserCommand? {
    printerSocket!!.write(command)
    if (command.contentEquals(EscPosCommands.INITIALIZE.toByteArray())) {
      printerSocket.resetState()
    }
    return this
  }

  companion object {
    /**
     * Convert hexadecimal string to bytes ESC/POS command.
     *
     * @param hexString Hexadecimal string of the image data.
     * @return Bytes contain the image in ESC/POS command.
     */
    @Throws(NumberFormatException::class)
    fun hexadecimalStringToBytes(hexString: String): ByteArray {
      val bytes = ByteArray(hexString.length / 2)
      for (i in bytes.indices) {
        val pos = i * 2
        bytes[i] = hexString.substring(pos, pos + 2).toInt(16).toByte()
      }
      return bytes
    }
  }
}
