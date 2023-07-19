package deckyfx.reactnative.printer.escposprinter

import android.content.Context
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException
import deckyfx.reactnative.printer.escposprinter.textparser.IPrinterTextParserElement
import deckyfx.reactnative.printer.escposprinter.textparser.PrinterImageUriParser
import deckyfx.reactnative.printer.escposprinter.textparser.PrinterTextParser
import deckyfx.reactnative.printer.escposprinter.textparser.PrinterTextParserString

class EscPosPrinter(
  private val context: Context?,
  printer: EscPosPrinterCommands?,
  printerDpi: Int,
  printerWidthMM: Float,
  printerNbrCharactersPerLine: Int
) : EscPosPrinterSize(printerDpi, printerWidthMM, printerNbrCharactersPerLine) {
  private var printer: EscPosPrinterCommands? = null

  /**
   * Create new instance of EscPosPrinter.
   *
   * @param context                     Application context
   * @param printerConnection           Instance of class which implement DeviceConnection
   * @param printerDpi                  DPI of the connected printer
   * @param printerWidthMM              Printing width in millimeters
   * @param printerNbrCharactersPerLine The maximum number of characters that can be printed on a line.
   */
  constructor(
    context: Context?,
    printerConnection: DeviceConnection?,
    printerDpi: Int,
    printerWidthMM: Float,
    printerNbrCharactersPerLine: Int
  ) : this(
    context,
    printerConnection?.let { EscPosPrinterCommands(it) },
    printerDpi,
    printerWidthMM,
    printerNbrCharactersPerLine
  )

  /**
   * Create new instance of EscPosPrinter.
   *
   * @param context                     Application context
   * @param printerConnection           Instance of class which implement DeviceConnection
   * @param printerDpi                  DPI of the connected printer
   * @param printerWidthMM              Printing width in millimeters
   * @param printerNbrCharactersPerLine The maximum number of characters that can be printed on a line.
   * @param charsetEncoding             Set the charset encoding.
   */
  constructor(
    context: Context?,
    printerConnection: DeviceConnection?,
    printerDpi: Int,
    printerWidthMM: Float,
    printerNbrCharactersPerLine: Int,
    charsetEncoding: EscPosCharsetEncoding?
  ) : this(
    context,
    printerConnection?.let { EscPosPrinterCommands(it, charsetEncoding) },
    printerDpi,
    printerWidthMM,
    printerNbrCharactersPerLine
  )

  /**
   * Create new instance of EscPosPrinter.
   *
   * @param context                     Application context
   * @param printer                     Instance of EscPosPrinterCommands
   * @param printerDpi                  DPI of the connected printer
   * @param printerWidthMM              Printing width in millimeters
   * @param printerNbrCharactersPerLine The maximum number of characters that can be printed on a line.
   */
  init {
    if (printer != null) {
      this.printer = printer.connect()
    }
  }

  val commands: EscPosPrinterCommands?
    get() = printer

  val connection: DeviceConnection?
    get() = printer?.connection

  /**
   * Close the connection with the printer.
   *
   * @return Fluent interface
   */
  fun disconnectPrinter(): EscPosPrinter {
    if (printer != null) {
      printer!!.disconnect()
      printer = null
    }
    return this
  }

  /**
   * Active "ESC *" command for image printing.
   *
   * @param enable true to use "ESC *", false to use "GS v 0"
   * @return Fluent interface
   */
  fun useEscAsteriskCommand(enable: Boolean): EscPosPrinter {
    printer!!.useEscAsteriskCommand(enable)
    return this
  }
  /**
   * Print a formatted text. Read the README.md for more information about text formatting options.
   *
   * @param text        Formatted text to be printed.
   * @param mmFeedPaper millimeter distance feed paper at the end.
   * @return Fluent interface
   */

  /**
   * Print a formatted text. Read the README.md for more information about text formatting options.
   *
   * @param text Formatted text to be printed.
   * @return Fluent interface
   */

  /**
   * Print a formatted text. Read the README.md for more information about text formatting options.
   *
   * @param text          Formatted text to be printed.
   * @param dotsFeedPaper distance feed paper at the end.
   * @return Fluent interface
   */
  @Throws(
    EscPosConnectionException::class,
    EscPosParserException::class,
    EscPosEncodingException::class,
    EscPosBarcodeException::class
  )
  fun printFormattedText(text: String?, dotsFeedPaper: Int): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    if (text.isNullOrEmpty()) {
      return this
    }
    val imageParsedText = PrinterImageUriParser(context, this).parse(text)
    if (imageParsedText.isEmpty()) {
      return this
    }
    val textParser = PrinterTextParser(this)
    val linesParsed = textParser
      .setFormattedText(imageParsedText)
      .parse()
    printer!!.reset()
    for (line in linesParsed) {
      val columns = line!!.columns
      var lastElement: IPrinterTextParserElement? = null
      for (column in columns) {
        val elements = column!!.elements
        for (element in elements) {
          element!!.print(printer)
          lastElement = element
        }
      }
      if (lastElement is PrinterTextParserString) {
        printer!!.newLine()
      }
    }
    printer!!.feedPaper(dotsFeedPaper)
    return this
  }

  /**
   * Print a formatted text and cut the paper. Read the README.md for more information about text formatting options.
   *
   * @param text        Formatted text to be printed.
   * @param mmFeedPaper millimeter distance feed paper at the end.
   * @return Fluent interface
   */
  @JvmOverloads
  @Throws(
    EscPosConnectionException::class,
    EscPosParserException::class,
    EscPosEncodingException::class,
    EscPosBarcodeException::class
  )
  fun printFormattedText(text: String?, mmFeedPaper: Float = 20f): EscPosPrinter {
    return this.printFormattedText(text, mmToPx(mmFeedPaper))
  }

  /**
   * Print a formatted text and cut the paper. Read the README.md for more information about text formatting options.
   *
   * @param text Formatted text to be printed.
   * @return Fluent interface
   */
  @JvmOverloads
  @Throws(
    EscPosConnectionException::class,
    EscPosParserException::class,
    EscPosEncodingException::class,
    EscPosBarcodeException::class
  )
  fun printFormattedTextAndCut(text: String?, mmFeedPaper: Float = 20f): EscPosPrinter {
    return this.printFormattedTextAndCut(text, mmToPx(mmFeedPaper))
  }

  /**
   * Print a formatted text and cut the paper. Read the README.md for more information about text formatting options.
   *
   * @param text          Formatted text to be printed.
   * @param dotsFeedPaper distance feed paper at the end.
   * @return Fluent interface
   */
  @Throws(
    EscPosConnectionException::class,
    EscPosParserException::class,
    EscPosEncodingException::class,
    EscPosBarcodeException::class
  )
  fun printFormattedTextAndCut(text: String?, dotsFeedPaper: Int): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    this.printFormattedText(text, dotsFeedPaper)
    printer!!.cutPaper()
    return this
  }

  /**
   * Print a formatted text, cut the paper and open the cash box. Read the README.md for more information about text formatting options.
   *
   * @param text        Formatted text to be printed.
   * @param mmFeedPaper millimeter distance feed paper at the end.
   * @return Fluent interface
   */
  @Throws(
    EscPosConnectionException::class,
    EscPosParserException::class,
    EscPosEncodingException::class,
    EscPosBarcodeException::class
  )
  fun printFormattedTextAndOpenCashBox(text: String?, mmFeedPaper: Float): EscPosPrinter {
    return this.printFormattedTextAndOpenCashBox(text, mmToPx(mmFeedPaper))
  }

  /**
   * Print a formatted text, cut the paper and open the cash box. Read the README.md for more information about text formatting options.
   *
   * @param text          Formatted text to be printed.
   * @param dotsFeedPaper distance feed paper at the end.
   * @return Fluent interface
   */
  @Throws(
    EscPosConnectionException::class,
    EscPosParserException::class,
    EscPosEncodingException::class,
    EscPosBarcodeException::class
  )
  fun printFormattedTextAndOpenCashBox(text: String?, dotsFeedPaper: Int): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    this.printFormattedTextAndCut(text, dotsFeedPaper)
    printer!!.openCashBox()
    return this
  }

  /**
   * Cut paper
   *
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun cutPaper(): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    printer!!.cutPaper()
    return this
  }

  /**
   * Open cash box
   *
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun openCashBox(): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    printer!!.openCashBox()
    return this
  }

  /**
   * Feed paper
   *
   * @param dots dots feed
   *
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun feedPaper(dots: Int): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    printer!!.feedPaper(dots)
    return this
  }

  @Throws(EscPosConnectionException::class)
  fun getPrinterModel(): String? {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return ""
    }
    return printer!!.getPrinterModel()
  }

  @Throws(EscPosConnectionException::class)
  fun write(bytes: ByteArray): EscPosPrinter {
    if (printer == null || printerNbrCharactersPerLine == 0) {
      return this
    }
    printer!!.write(bytes)
    return this
  }

  /**
   * @return Charset encoding
   */
  val encoding: EscPosCharsetEncoding
    get() = printer!!.getCharsetEncoding()

  /**
   * Print all characters of all charset encoding
   *
   * @return Fluent interface
   */
  fun printAllCharsetsEncodingCharacters(): EscPosPrinter {
    printer!!.printAllCharsetsEncodingCharacters()
    return this
  }

  /**
   * Print all characters of selected charsets encoding
   *
   * @param charsetsId Array of charset id to print.
   * @return Fluent interface
   */
  fun printCharsetsEncodingCharacters(charsetsId: IntArray?): EscPosPrinter {
    printer!!.printCharsetsEncodingCharacters(charsetsId!!)
    return this
  }

  /**
   * Print all characters of a charset encoding
   *
   * @param charsetId Charset id to print.
   * @return Fluent interface
   */
  fun printCharsetEncodingCharacters(charsetId: Int): EscPosPrinter {
    printer!!.printCharsetEncodingCharacters(charsetId)
    return this
  }
}
