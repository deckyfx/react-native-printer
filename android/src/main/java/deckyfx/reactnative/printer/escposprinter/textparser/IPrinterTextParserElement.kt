package deckyfx.reactnative.printer.escposprinter.textparser

import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException

interface IPrinterTextParserElement {
  @Throws(EscPosEncodingException::class)
  fun length(): Int

  @Throws(EscPosEncodingException::class, EscPosConnectionException::class)
  fun print(printerSocket: EscPosPrinterCommands?): IPrinterTextParserElement?
}
