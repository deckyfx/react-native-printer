package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException

open class PrinterTextParserCommand(
  private val command: ByteArray
) : IPrinterTextParserElement {
  init {
  }

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
  }
}
