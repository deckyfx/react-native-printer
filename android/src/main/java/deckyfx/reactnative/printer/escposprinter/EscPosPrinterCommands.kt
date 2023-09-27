package deckyfx.reactnative.printer.escposprinter

import android.graphics.Bitmap
import com.google.zxing.EncodeHintType
import com.google.zxing.WriterException
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import com.google.zxing.qrcode.encoder.ByteMatrix
import com.google.zxing.qrcode.encoder.Encoder
import deckyfx.reactnative.printer.escposprinter.barcode.Barcode
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosBarcodeException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Arrays
import java.util.EnumMap
import kotlin.math.ceil
import kotlin.math.roundToInt


class EscPosPrinterCommands @JvmOverloads constructor(
  printerConnection: DeviceConnection,
  charsetEncoding: EscPosCharsetEncoding? = null
) {
  private val printerConnection: DeviceConnection
  private val charsetEncoding: EscPosCharsetEncoding

  /**
   * Active "ESC *" command for image print.
   *
   */
  var useEscAsteriskCommand = false

  /**
   * Set as dot matrix bypass printing image, barcode, and qrcode
   *
   */
  var isDotMatrixPrinter = false

  val connection: DeviceConnection
    get() = printerConnection

  /**
   * Start socket connection and open stream with the device.
   */
  @Throws(EscPosConnectionException::class)
  fun connect(): EscPosPrinterCommands {
    printerConnection.connect()
    return this
  }

  /**
   * Close the socket connection and stream with the device.
   */
  fun disconnect() {
    printerConnection.disconnect()
  }

  /**
   * Reset printers parameters.
   */
  fun reset(): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    resetState() //  should also reset the printer object state to reset the stylyng flag
    printerConnection.write(RESET_PRINTER)
    return this
  }

  /**
   * Set the alignment of text and barcodes.
   * Don't works with image.
   *
   * @param align Set the alignment of text and barcodes. Use EscPosPrinterCommands.TEXT_ALIGN_... constants
   * @return Fluent interface
   */
  fun setAlign(align: ByteArray?): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    align?.let { printerConnection.write(it) }
    return this
  }

  /**
   * Reset this instance states
   * To fix sometime bold don't work correctly after turned on and off repeatedly
   *
   */
  fun resetState() {
    currentTextSize = ByteArray(0)
    currentTextColor = ByteArray(0)
    currentTextReverseColor = ByteArray(0)
    currentTextBold = ByteArray(0)
    currentTextUnderline = ByteArray(0)
    currentTextDoubleStrike = ByteArray(0)
  }

  private var currentTextSize: ByteArray? = ByteArray(0)
  private var currentTextColor: ByteArray? = ByteArray(0)
  private var currentTextReverseColor: ByteArray? = ByteArray(0)
  private var currentTextBold: ByteArray? = ByteArray(0)
  private var currentTextUnderline: ByteArray? = ByteArray(0)
  private var currentTextDoubleStrike: ByteArray? = ByteArray(0)

  /**
   * Create new instance of EscPosPrinterCommands.
   *
   * @param printerConnection an instance of a class which implement DeviceConnection
   * @param charsetEncoding   Set the charset encoding.
   */
  /**
   * Create new instance of EscPosPrinterCommands.
   *
   * @param printerConnection an instance of a class which implement DeviceConnection
   */
  init {
    this.printerConnection = printerConnection
    this.charsetEncoding =
      charsetEncoding ?: EscPosCharsetEncoding(
        "windows-1252",
        6
      )
  }
  /**
   * Print text with the connected printer.
   *
   * @param text             Text to be printed
   * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
   * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
   * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
   * @param textBold         Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
   * @param textUnderline    Set the underlining of the text. Use EscPosPrinterCommands.TEXT_UNDERLINE_... constants
   * @param textDoubleStrike Set the double strike of the text. Use EscPosPrinterCommands.TEXT_DOUBLE_STRIKE_... constants
   * @return Fluent interface
   */
  /**
   * Print text with the connected printer.
   *
   * @param text             Text to be printed
   * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
   * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
   * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
   * @param textBold         Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
   * @param textUnderline    Set the underlining of the text. Use EscPosPrinterCommands.TEXT_UNDERLINE_... constants
   * @return Fluent interface
   */
  /**
   * Print text with the connected printer.
   *
   * @param text             Text to be printed
   * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
   * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
   * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
   * @param textBold         Set the text weight. Use EscPosPrinterCommands.TEXT_WEIGHT_... constants
   * @return Fluent interface
   */
  /**
   * Print text with the connected printer.
   *
   * @param text             Text to be printed
   * @param textSize         Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
   * @param textColor        Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
   * @param textReverseColor Set the background and text color. Use EscPosPrinterCommands.TEXT_COLOR_REVERSE_... constants
   * @return Fluent interface
   */
  /**
   * Print text with the connected printer.
   *
   * @param text      Text to be printed
   * @param textSize  Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
   * @param textColor Set the text color. Use EscPosPrinterCommands.TEXT_COLOR_... constants
   * @return Fluent interface
   */
  /**
   * Print text with the connected printer.
   *
   * @param text     Text to be printed
   * @param textSize Set the text size. Use EscPosPrinterCommands.TEXT_SIZE_... constants
   * @return Fluent interface
   */
  /**
   * Print text with the connected printer.
   *
   * @param text Text to be printed
   * @return Fluent interface
   */
  @JvmOverloads
  @Throws(EscPosEncodingException::class)
  fun printText(
    text: String? = "",
    textSize: ByteArray? = null,
    textColor: ByteArray? = null,
    textReverseColor: ByteArray? = null,
    textBold: ByteArray? = null,
    textUnderline: ByteArray? = null,
    textDoubleStrike: ByteArray? = null
  ): EscPosPrinterCommands {
    var textSize = textSize
    var textColor = textColor
    var textReverseColor = textReverseColor
    var textBold = textBold
    var textUnderline = textUnderline
    var textDoubleStrike = textDoubleStrike
    if (!printerConnection.isConnected) {
      return this
    }
    if (textSize == null) {
      textSize = if (!useEscAsteriskCommand) {
        TEXT_SIZE_NORMAL
      } else {
        TEXT_SIZE_NORMAL_ALT
      }
    }
    /**
     * ESC * Formatting only can change font size for now, so bypass all other styling
     * */
    if (!useEscAsteriskCommand) {
      if (textColor == null) {
        textColor = TEXT_COLOR_BLACK
      }
      if (textReverseColor == null) {
        textReverseColor = TEXT_COLOR_REVERSE_OFF
      }
      if (textBold == null) {
        textBold = TEXT_WEIGHT_NORMAL
      }
      if (textUnderline == null) {
        textUnderline = TEXT_UNDERLINE_OFF
      }
      if (textDoubleStrike == null) {
        textDoubleStrike = TEXT_DOUBLE_STRIKE_OFF
      }
    } else {
      textColor = null
      textReverseColor = null
      textBold = null
      textUnderline = null
      textDoubleStrike = null
    }
    try {
      if (text.isNullOrEmpty()) return this
      /**
       *  Somehow n ESC * styling, after each line there is weird blank space
       *  Bypass it
       *  */
      val textBytes: ByteArray = text.toByteArray(Charset.forName(charsetEncoding.name))
      /**
       * don't set character encoding for dot-matrix printer
       */
      if (!useEscAsteriskCommand && !isDotMatrixPrinter) {
        printerConnection.write(charsetEncoding.command)
        //this.printerConnection.write(EscPosPrinterCommands.TEXT_FONT_A);
      }
      if (!Arrays.equals(currentTextSize, textSize)) {
        printerConnection.write(textSize)
        currentTextSize = textSize
      }
      if (!Arrays.equals(currentTextDoubleStrike, textDoubleStrike) && textDoubleStrike != null) {
        printerConnection.write(textDoubleStrike)
        currentTextDoubleStrike = textDoubleStrike
      }
      if (!Arrays.equals(currentTextUnderline, textUnderline) && textUnderline != null) {
        printerConnection.write(textUnderline)
        currentTextUnderline = textUnderline
      }
      if (!Arrays.equals(currentTextBold, textBold) && textBold != null) {
        printerConnection.write(textBold)
        currentTextBold = textBold
      }
      if (!Arrays.equals(currentTextColor, textColor) && textColor != null) {
        printerConnection.write(textColor)
        currentTextColor = textColor
      }
      if (!Arrays.equals(currentTextReverseColor, textReverseColor) && textReverseColor != null) {
        printerConnection.write(textReverseColor)
        currentTextReverseColor = textReverseColor
      }
      printerConnection.write(textBytes)
    } catch (e: UnsupportedEncodingException) {
      e.printStackTrace()
      throw EscPosEncodingException(e.message)
    }
    return this
  }

  fun printAllCharsetsEncodingCharacters(): EscPosPrinterCommands {
    for (charsetId in 0..255) {
      printCharsetEncodingCharacters(charsetId)
    }
    return this
  }

  fun printCharsetsEncodingCharacters(charsetsId: IntArray): EscPosPrinterCommands {
    for (charsetId in charsetsId) {
      printCharsetEncodingCharacters(charsetId)
    }
    return this
  }

  fun printCharsetEncodingCharacters(charsetId: Int): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    try {
      if (!useEscAsteriskCommand) {
        printerConnection.write(byteArrayOf(EscPosCommands.ESC, 0x74, charsetId.toByte()))
        printerConnection.write(TEXT_SIZE_NORMAL)
        printerConnection.write(TEXT_COLOR_BLACK)
        printerConnection.write(TEXT_COLOR_REVERSE_OFF)
        printerConnection.write(TEXT_WEIGHT_NORMAL)
        printerConnection.write(TEXT_UNDERLINE_OFF)
        printerConnection.write(TEXT_DOUBLE_STRIKE_OFF)
      } else {
        printerConnection.write(TEXT_SIZE_NORMAL_ALT)
      }
      printerConnection.write(":::: Charset n°$charsetId : ".toByteArray())
      printerConnection.write(
        byteArrayOf(
          0x00.toByte(),
          0x01.toByte(),
          0x02.toByte(),
          0x03.toByte(),
          0x04.toByte(),
          0x05.toByte(),
          0x06.toByte(),
          0x07.toByte(),
          0x08.toByte(),
          0x09.toByte(),
          0x0A.toByte(),
          0x0B.toByte(),
          0x0C.toByte(),
          0x0D.toByte(),
          0x0E.toByte(),
          0x0F.toByte(),
          0x10.toByte(),
          0x11.toByte(),
          0x12.toByte(),
          0x13.toByte(),
          0x14.toByte(),
          0x15.toByte(),
          0x16.toByte(),
          0x17.toByte(),
          0x18.toByte(),
          0x19.toByte(),
          0x1A.toByte(),
          EscPosCommands.ESC,
          0x1C.toByte(),
          EscPosCommands.GS,
          0x1E.toByte(),
          0x1F.toByte(),
          0x20.toByte(),
          EscPosCommands.EXCLAMATION,
          0x22.toByte(),
          0x23.toByte(),
          0x24.toByte(),
          0x25.toByte(),
          0x26.toByte(),
          0x27.toByte(),
          0x28.toByte(),
          0x29.toByte(),
          0x2A.toByte(),
          0x2B.toByte(),
          0x2C.toByte(),
          0x2D.toByte(),
          0x2E.toByte(),
          0x2F.toByte(),
          0x30.toByte(),
          0x31.toByte(),
          0x32.toByte(),
          0x33.toByte(),
          0x34.toByte(),
          0x35.toByte(),
          0x36.toByte(),
          0x37.toByte(),
          0x38.toByte(),
          0x39.toByte(),
          0x3A.toByte(),
          0x3B.toByte(),
          0x3C.toByte(),
          0x3D.toByte(),
          0x3E.toByte(),
          0x3F.toByte(),
          0x40.toByte(),
          0x41.toByte(),
          0x42.toByte(),
          0x43.toByte(),
          0x44.toByte(),
          0x45.toByte(),
          0x46.toByte(),
          0x47.toByte(),
          0x48.toByte(),
          0x49.toByte(),
          0x4A.toByte(),
          0x4B.toByte(),
          0x4C.toByte(),
          0x4D.toByte(),
          0x4E.toByte(),
          0x4F.toByte(),
          0x50.toByte(),
          0x51.toByte(),
          0x52.toByte(),
          0x53.toByte(),
          0x54.toByte(),
          0x55.toByte(),
          0x56.toByte(),
          0x57.toByte(),
          0x58.toByte(),
          0x59.toByte(),
          0x5A.toByte(),
          0x5B.toByte(),
          0x5C.toByte(),
          0x5D.toByte(),
          0x5E.toByte(),
          0x5F.toByte(),
          0x60.toByte(),
          0x61.toByte(),
          0x62.toByte(),
          0x63.toByte(),
          0x64.toByte(),
          0x65.toByte(),
          0x66.toByte(),
          0x67.toByte(),
          0x68.toByte(),
          0x69.toByte(),
          0x6A.toByte(),
          0x6B.toByte(),
          0x6C.toByte(),
          0x6D.toByte(),
          0x6E.toByte(),
          0x6F.toByte(),
          0x70.toByte(),
          0x71.toByte(),
          0x72.toByte(),
          0x73.toByte(),
          0x74.toByte(),
          0x75.toByte(),
          0x76.toByte(),
          0x77.toByte(),
          0x78.toByte(),
          0x79.toByte(),
          0x7A.toByte(),
          0x7B.toByte(),
          0x7C.toByte(),
          0x7D.toByte(),
          0x7E.toByte(),
          0x7F.toByte(),
          0x80.toByte(),
          0x81.toByte(),
          0x82.toByte(),
          0x83.toByte(),
          0x84.toByte(),
          0x85.toByte(),
          0x86.toByte(),
          0x87.toByte(),
          0x88.toByte(),
          0x89.toByte(),
          0x8A.toByte(),
          0x8B.toByte(),
          0x8C.toByte(),
          0x8D.toByte(),
          0x8E.toByte(),
          0x8F.toByte(),
          0x90.toByte(),
          0x91.toByte(),
          0x92.toByte(),
          0x93.toByte(),
          0x94.toByte(),
          0x95.toByte(),
          0x96.toByte(),
          0x97.toByte(),
          0x98.toByte(),
          0x99.toByte(),
          0x9A.toByte(),
          0x9B.toByte(),
          0x9C.toByte(),
          0x9D.toByte(),
          0x9E.toByte(),
          0x9F.toByte(),
          0xA0.toByte(),
          0xA1.toByte(),
          0xA2.toByte(),
          0xA3.toByte(),
          0xA4.toByte(),
          0xA5.toByte(),
          0xA6.toByte(),
          0xA7.toByte(),
          0xA8.toByte(),
          0xA9.toByte(),
          0xAA.toByte(),
          0xAB.toByte(),
          0xAC.toByte(),
          0xAD.toByte(),
          0xAE.toByte(),
          0xAF.toByte(),
          0xB0.toByte(),
          0xB1.toByte(),
          0xB2.toByte(),
          0xB3.toByte(),
          0xB4.toByte(),
          0xB5.toByte(),
          0xB6.toByte(),
          0xB7.toByte(),
          0xB8.toByte(),
          0xB9.toByte(),
          0xBA.toByte(),
          0xBB.toByte(),
          0xBC.toByte(),
          0xBD.toByte(),
          0xBE.toByte(),
          0xBF.toByte(),
          0xC0.toByte(),
          0xC1.toByte(),
          0xC2.toByte(),
          0xC3.toByte(),
          0xC4.toByte(),
          0xC5.toByte(),
          0xC6.toByte(),
          0xC7.toByte(),
          0xC8.toByte(),
          0xC9.toByte(),
          0xCA.toByte(),
          0xCB.toByte(),
          0xCC.toByte(),
          0xCD.toByte(),
          0xCE.toByte(),
          0xCF.toByte(),
          0xD0.toByte(),
          0xD1.toByte(),
          0xD2.toByte(),
          0xD3.toByte(),
          0xD4.toByte(),
          0xD5.toByte(),
          0xD6.toByte(),
          0xD7.toByte(),
          0xD8.toByte(),
          0xD9.toByte(),
          0xDA.toByte(),
          0xDB.toByte(),
          0xDC.toByte(),
          0xDD.toByte(),
          0xDE.toByte(),
          0xDF.toByte(),
          0xE0.toByte(),
          0xE1.toByte(),
          0xE2.toByte(),
          0xE3.toByte(),
          0xE4.toByte(),
          0xE5.toByte(),
          0xE6.toByte(),
          0xE7.toByte(),
          0xE8.toByte(),
          0xE9.toByte(),
          0xEA.toByte(),
          0xEB.toByte(),
          0xEC.toByte(),
          0xED.toByte(),
          0xEE.toByte(),
          0xEF.toByte(),
          0xF0.toByte(),
          0xF1.toByte(),
          0xF2.toByte(),
          0xF3.toByte(),
          0xF4.toByte(),
          0xF5.toByte(),
          0xF6.toByte(),
          0xF7.toByte(),
          0xF8.toByte(),
          0xF9.toByte(),
          0xFA.toByte(),
          0xFB.toByte(),
          0xFC.toByte(),
          0xFD.toByte(),
          0xFE.toByte(),
          0xFF.toByte()
        )
      )
      printerConnection.write(byteArrayOf(LF, LF, LF, LF))
      printerConnection.send()
    } catch (e: EscPosConnectionException) {
      e.printStackTrace()
    }
    return this
  }

  /**
   * Print image with the connected printer.
   *
   * @param image Bytes contain the image in ESC/POS command
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun printImage(image: ByteArray): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    if (isDotMatrixPrinter) {
      println("Bypass printing image due to DOT Matrix Printer")
      return this
    }
    val bytesToPrint: Array<out ByteArray?> =
      if (useEscAsteriskCommand) convertGSv0ToEscAsterisk(image) else arrayOf(image)
    for (bytes in bytesToPrint) {
      bytes?.let { printerConnection.write(it) }
      printerConnection.send()
    }
    return this
  }

  /**
   * Print a barcode with the connected printer.
   *
   * @param barcode Instance of Class that implement Barcode
   * @return Fluent interface
   */
  fun printBarcode(barcode: Barcode): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    if (isDotMatrixPrinter) {
      println("Bypass printing barcode due to DOT Matrix Printer")
      return this
    }
    val code: String = barcode.code
    val barcodeLength: Int = barcode.codeLength
    val barcodeCommand = ByteArray(barcodeLength + 4)
    System.arraycopy(
      byteArrayOf(
        EscPosCommands.GS,
        0x6B,
        barcode.barcodeType.toByte(),
        barcodeLength.toByte()
      ), 0, barcodeCommand, 0, 4
    )
    for (i in 0 until barcodeLength) {
      barcodeCommand[i + 4] = code[i].code.toByte()
    }
    printerConnection.write(byteArrayOf(EscPosCommands.GS, 0x48, barcode.textPosition.toByte()))
    printerConnection.write(byteArrayOf(EscPosCommands.GS, 0x77, barcode.colWidth.toByte()))
    printerConnection.write(byteArrayOf(EscPosCommands.GS, 0x68, barcode.height.toByte()))
    printerConnection.write(barcodeCommand)
    return this
  }

  /**
   * Print a QR code with the connected printer.
   *
   * @param qrCodeType Set the barcode type. Use EscPosPrinterCommands.QRCODE_... constants
   * @param text       String that contains QR code data
   * @param size       dot size of QR code pixel
   * @return Fluent interface
   */
  @Throws(EscPosEncodingException::class)
  fun printQRCode(qrCodeType: Int, text: String, size: Int): EscPosPrinterCommands {
    var size = size
    if (!printerConnection.isConnected) {
      return this
    }
    if (isDotMatrixPrinter) {
      println("Bypass printing qrcode due to DOT Matrix Printer")
      return this
    }
    if (size < 1) {
      size = 1
    } else if (size > 16) {
      size = 16
    }
    try {
      val textBytes = text.toByteArray(charset("UTF-8"))
      val commandLength = textBytes.size + 3
      val pL = commandLength % 256
      val pH = commandLength / 256

      printerConnection.write(
        byteArrayOf(
          EscPosCommands.GS,
          0x28,
          0x6B,
          0x04,
          0x00,
          0x31,
          0x41,
          qrCodeType.toByte(),
          0x00
        )
      )
      printerConnection.write(
        byteArrayOf(
          EscPosCommands.GS,
          0x28,
          0x6B,
          0x03,
          0x00,
          0x31,
          0x43,
          size.toByte()
        )
      )
      printerConnection.write(byteArrayOf(EscPosCommands.GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x45, 0x30))
      val qrCodeCommand = ByteArray(textBytes.size + 8)
      System.arraycopy(
        byteArrayOf(
          EscPosCommands.GS,
          0x28,
          0x6B,
          pL.toByte(),
          pH.toByte(),
          0x31,
          0x50,
          0x30
        ), 0, qrCodeCommand, 0, 8
      )
      System.arraycopy(textBytes, 0, qrCodeCommand, 8, textBytes.size)
      printerConnection.write(qrCodeCommand)
      printerConnection.write(byteArrayOf(EscPosCommands.GS, 0x28, 0x6B, 0x03, 0x00, 0x31, 0x51, 0x30))
    } catch (e: UnsupportedEncodingException) {
      e.printStackTrace()
      throw EscPosEncodingException(e.message)
    }
    return this
  }

  /**
   * Forces the transition to a new line and set the alignment of text and barcodes with the connected printer.
   *
   * @param align Set the alignment of text and barcodes. Use EscPosPrinterCommands.TEXT_ALIGN_... constants
   * @return Fluent interface
   */
  /**
   * Forces the transition to a new line with the connected printer.
   *
   * @return Fluent interface
   */
  @JvmOverloads
  @Throws(EscPosConnectionException::class)
  fun newLine(align: ByteArray? = null): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    printerConnection.write(byteArrayOf(LF))
    printerConnection.send()
    if (align != null) {
      printerConnection.write(align)
    }
    return this
  }

  /**
   * Feed the paper
   *
   * @param dots Number of dots to feed (0 <= dots <= 255)
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun feedPaper(dots: Int): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    if (dots > 0) {
      printerConnection.write(byteArrayOf(EscPosCommands.ESC, 0x4A, dots.toByte()))
      printerConnection.send(dots)
    }
    return this
  }

  /**
   * Cut the paper
   *
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun cutPaper(): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    printerConnection.write(byteArrayOf(EscPosCommands.GS, 0x56, 0x01))
    printerConnection.send(100)
    return this
  }

  /**
   * Open the cash box
   *
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun openCashBox(): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    printerConnection.write(byteArrayOf(EscPosCommands.ESC, 0x70, 0x00, 0x3C, 0xFF.toByte()))
    printerConnection.send(100)
    return this
  }

  /**
   * Get Printer Model
   *
   * @return Fluent interface
   */
  @Throws(EscPosConnectionException::class)
  fun getPrinterModel(): String? {
    if (!printerConnection.isConnected) {
      throw EscPosConnectionException("Not connected")
    }
    return printerConnection.getPrinterModel()
  }


  @Throws(EscPosConnectionException::class)
  fun write(bytes: ByteArray): EscPosPrinterCommands {
    if (!printerConnection.isConnected) {
      return this
    }
    printerConnection.write(bytes)
    return this
  }

  /**
   * @return Charset encoding
   */
  fun getCharsetEncoding(): EscPosCharsetEncoding {
    return charsetEncoding
  }

  companion object {
    const val LF: Byte = 0x0A
    val RESET_PRINTER = byteArrayOf(EscPosCommands.ESC, EscPosCommands.AT)
    val TEXT_ALIGN_LEFT = byteArrayOf(EscPosCommands.ESC, 0x61, 0x00)
    val TEXT_ALIGN_CENTER = byteArrayOf(EscPosCommands.ESC, 0x61, 0x01)
    val TEXT_ALIGN_RIGHT = byteArrayOf(EscPosCommands.ESC, 0x61, 0x02)
    val TEXT_WEIGHT_NORMAL = byteArrayOf(EscPosCommands.ESC, 0x45, 0x00)
    val TEXT_WEIGHT_BOLD = byteArrayOf(EscPosCommands.ESC, 0x45, 0x01)
    val LINE_SPACING_24 = byteArrayOf(EscPosCommands.ESC, 0x33, 0x18)
    val LINE_SPACING_30 = byteArrayOf(EscPosCommands.ESC, 0x33, 0x1e)
    val TEXT_FONT_A = byteArrayOf(EscPosCommands.ESC, 0x4D, 0x00)
    val TEXT_FONT_B = byteArrayOf(EscPosCommands.ESC, 0x4D, 0x01)
    val TEXT_FONT_C = byteArrayOf(EscPosCommands.ESC, 0x4D, 0x02)
    val TEXT_FONT_D = byteArrayOf(EscPosCommands.ESC, 0x4D, 0x03)
    val TEXT_FONT_E = byteArrayOf(EscPosCommands.ESC, 0x4D, 0x04)
    val TEXT_SIZE_NORMAL = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x00)
    val TEXT_SIZE_DOUBLE_HEIGHT = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x01)
    val TEXT_SIZE_DOUBLE_WIDTH = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x10)
    val TEXT_SIZE_BIG = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x11)
    val TEXT_SIZE_BIG_2 = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x22)
    val TEXT_SIZE_BIG_3 = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x33)
    val TEXT_SIZE_BIG_4 = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x44)
    val TEXT_SIZE_BIG_5 = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x55)
    val TEXT_SIZE_BIG_6 = byteArrayOf(EscPosCommands.GS, EscPosCommands.EXCLAMATION, 0x66)

    /**
     *     Use 7x9 font for alternative command for Dot-matrix printer
     *     All bytes is +1
     */
    val TEXT_SIZE_NORMAL_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x01)
    val TEXT_SIZE_DOUBLE_HEIGHT_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x11)
    val TEXT_SIZE_DOUBLE_WIDTH_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x21)
    val TEXT_SIZE_BIG_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x31)
    val TEXT_SIZE_BIG_2_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x31)
    val TEXT_SIZE_BIG_3_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x31)
    val TEXT_SIZE_BIG_4_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x31)
    val TEXT_SIZE_BIG_5_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x31)
    val TEXT_SIZE_BIG_6_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x31)

    val TEXT_SIZE_NORMAL_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x80.toByte())
    val TEXT_SIZE_DOUBLE_HEIGHT_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0x90.toByte())
    val TEXT_SIZE_DOUBLE_WIDTH_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xA0.toByte())
    val TEXT_SIZE_BIG_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xB0.toByte())
    val TEXT_SIZE_BIG_2_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xB0.toByte())
    val TEXT_SIZE_BIG_3_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xB0.toByte())
    val TEXT_SIZE_BIG_4_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xB0.toByte())
    val TEXT_SIZE_BIG_5_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xB0.toByte())
    val TEXT_SIZE_BIG_6_UNDERLINED_ALT = byteArrayOf(EscPosCommands.ESC, EscPosCommands.EXCLAMATION, 0xB0.toByte())

    val TEXT_UNDERLINE_OFF = byteArrayOf(EscPosCommands.ESC, 0x2D, 0x00)
    val TEXT_UNDERLINE_ON = byteArrayOf(EscPosCommands.ESC, 0x2D, 0x01)
    val TEXT_UNDERLINE_LARGE = byteArrayOf(EscPosCommands.ESC, 0x2D, 0x02)
    val TEXT_DOUBLE_STRIKE_OFF = byteArrayOf(EscPosCommands.ESC, 0x47, 0x00)
    val TEXT_DOUBLE_STRIKE_ON = byteArrayOf(EscPosCommands.ESC, 0x47, 0x01)
    val TEXT_COLOR_BLACK = byteArrayOf(EscPosCommands.ESC, 0x72, 0x00)
    val TEXT_COLOR_RED = byteArrayOf(EscPosCommands.ESC, 0x72, 0x01)
    val TEXT_COLOR_REVERSE_OFF = byteArrayOf(EscPosCommands.GS, 0x42, 0x00)
    val TEXT_COLOR_REVERSE_ON = byteArrayOf(EscPosCommands.GS, 0x42, 0x01)
    const val BARCODE_TYPE_UPCA = 65
    const val BARCODE_TYPE_UPCE = 66
    const val BARCODE_TYPE_EAN13 = 67
    const val BARCODE_TYPE_EAN8 = 68
    const val BARCODE_TYPE_ITF = 70
    const val BARCODE_TYPE_128 = 73
    const val BARCODE_TEXT_POSITION_NONE = 0
    const val BARCODE_TEXT_POSITION_ABOVE = 1
    const val BARCODE_TEXT_POSITION_BELOW = 2
    const val QRCODE_1 = 49
    const val QRCODE_2 = 50

    fun initGSv0Command(bytesByLine: Int, bitmapHeight: Int): ByteArray {
      val xH = bytesByLine / 256
      val xL = bytesByLine - xH * 256
      val yH = bitmapHeight / 256
      val yL = bitmapHeight - yH * 256
      val imageBytes = ByteArray(8 + bytesByLine * bitmapHeight)
      imageBytes[0] = EscPosCommands.GS
      imageBytes[1] = 0x76
      imageBytes[2] = 0x30
      imageBytes[3] = 0x00
      imageBytes[4] = xL.toByte()
      imageBytes[5] = xH.toByte()
      imageBytes[6] = yL.toByte()
      imageBytes[7] = yH.toByte()
      return imageBytes
    }

    /**
     * Convert Bitmap instance to a byte array compatible with ESC/POS printer.
     *
     * @param bitmap Bitmap to be convert
     * @param gradient false : Black and white image, true : Grayscale image
     * @return Bytes contain the image in ESC/POS command
     */
    fun bitmapToBytes(bitmap: Bitmap, gradient: Boolean): ByteArray {
      val bitmapWidth: Int = bitmap.width
      val bitmapHeight: Int = bitmap.height
      val bytesByLine = ceil((bitmapWidth.toFloat() / 8f).toDouble()).toInt()

      val imageBytes = initGSv0Command(bytesByLine, bitmapHeight)

      var i = 8
      var greyscaleCoefficientInit = 0
      val gradientStep = 6

      val colorLevelStep = 765.0 / (15 * gradientStep + gradientStep - 1)

      for (posY in 0 until bitmapHeight) {
        var greyscaleCoefficient = greyscaleCoefficientInit
        val greyscaleLine = posY % gradientStep
        var j = 0
        while (j < bitmapWidth) {
          var b = 0
          for (k in 0..7) {
            val posX = j + k
            if (posX < bitmapWidth) {
              val color: Int = bitmap.getPixel(posX, posY)
              val red = color shr 16 and 255
              val green = color shr 8 and 255
              val blue = color and 255
              if (gradient && red + green + blue < (greyscaleCoefficient * gradientStep + greyscaleLine) * colorLevelStep || !gradient && (red < 160 || green < 160 || blue < 160)) {
                b = b or (1 shl 7 - k)
              }
              greyscaleCoefficient += 5
              if (greyscaleCoefficient > 15) {
                greyscaleCoefficient -= 16
              }
            }
          }
          imageBytes[i++] = b.toByte()
          j += 8
        }
        greyscaleCoefficientInit += 2
        if (greyscaleCoefficientInit > 15) {
          greyscaleCoefficientInit = 0
        }
      }

      return imageBytes
    }

    fun convertGSv0ToEscAsterisk(bytes: ByteArray): Array<ByteArray?> {
      val xL = bytes[4].toInt() and 0xFF
      val xH = bytes[5].toInt() and 0xFF
      val yL = bytes[6].toInt() and 0xFF
      val yH = bytes[7].toInt() and 0xFF
      val bytesByLine = xH * 256 + xL
      val dotsByLine = bytesByLine * 8
      val nH = dotsByLine / 256
      val nL = dotsByLine % 256
      val imageHeight = yH * 256 + yL
      val imageLineHeightCount = ceil(imageHeight.toDouble() / 24.0).toInt()
      val imageBytesSize = 6 + bytesByLine * 24
      val returnedBytes = arrayOfNulls<ByteArray>(imageLineHeightCount + 2)
      returnedBytes[0] = LINE_SPACING_24
      for (i in 0 until imageLineHeightCount) {
        val pxBaseRow = i * 24
        val imageBytes = ByteArray(imageBytesSize)
        imageBytes[0] = EscPosCommands.ESC
        imageBytes[1] = EscPosCommands.ASTERISKS
        imageBytes[2] = EscPosCommands.EXCLAMATION
        imageBytes[3] = nL.toByte()
        imageBytes[4] = nH.toByte()
        for (j in 5 until imageBytes.size) {
          val imgByte = j - 5
          val byteRow = imgByte % 3
          val pxColumn = imgByte / 3
          val bitColumn = 1 shl 7 - pxColumn % 8
          val pxRow = pxBaseRow + byteRow * 8
          for (k in 0..7) {
            val indexBytes = bytesByLine * (pxRow + k) + pxColumn / 8 + 8
            if (indexBytes >= bytes.size) {
              break
            }
            val isBlack = bytes[indexBytes].toInt() and bitColumn == bitColumn
            if (isBlack) {
              imageBytes[j] = (imageBytes[j].toInt() or (1 shl 7) - k).toByte()
            }
          }
        }
        imageBytes[imageBytes.size - 1] = LF
        returnedBytes[i + 1] = imageBytes
      }
      returnedBytes[returnedBytes.size - 1] = LINE_SPACING_30
      return returnedBytes
    }

    /**
     * Convert a string to QR Code byte array compatible with ESC/POS printer.
     *
     * @param data String data to convert in QR Code
     * @param size QR code dots size
     * @return Bytes contain the image in ESC/POS command
     */
    @Throws(EscPosBarcodeException::class)
    fun QRCodeDataToBytes(data: String?, size: Int): ByteArray {
      var byteMatrix: ByteMatrix? = null
      try {
        val hints = EnumMap<EncodeHintType, Any?>(
          EncodeHintType::class.java
        )
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        val code = Encoder.encode(data, ErrorCorrectionLevel.L, hints)
        byteMatrix = code!!.matrix
      } catch (e: WriterException) {
        e.printStackTrace()
        throw EscPosBarcodeException("Unable to encode QR code")
      }
      if (byteMatrix == null) {
        return initGSv0Command(0, 0)
      }
      val width = byteMatrix.width
      val height = byteMatrix.height
      val coefficient = (size.toFloat() / width.toFloat()).roundToInt()
      val imageWidth = width * coefficient
      val imageHeight = height * coefficient
      val bytesByLine = ceil((imageWidth.toFloat() / 8f).toDouble()).toInt()
      var i = 8
      if (coefficient < 1) {
        return initGSv0Command(0, 0)
      }
      val imageBytes = initGSv0Command(bytesByLine, imageHeight)
      for (y in 0 until height) {
        val lineBytes = ByteArray(bytesByLine)
        var x = -1
        var multipleX = coefficient
        var isBlack = false
        for (j in 0 until bytesByLine) {
          var b = 0
          for (k in 0..7) {
            if (multipleX == coefficient) {
              isBlack = ++x < width && byteMatrix[x, y].toInt() == 1
              multipleX = 0
            }
            if (isBlack) {
              b = b or (1 shl 7) - k
            }
            ++multipleX
          }
          lineBytes[j] = b.toByte()
        }
        for (multipleY in 0 until coefficient) {
          System.arraycopy(lineBytes, 0, imageBytes, i, lineBytes.size)
          i += lineBytes.size
        }
      }
      return imageBytes
    }
  }
}
