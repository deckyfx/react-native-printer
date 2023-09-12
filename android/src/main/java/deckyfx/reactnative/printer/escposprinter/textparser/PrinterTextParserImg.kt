package deckyfx.reactnative.printer.escposprinter.textparser

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinterSize
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosEncodingException
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToInt

open class PrinterTextParserImg(
  printerTextParserColumn: PrinterTextParserColumn,
  textAlign: String?,
  image: ByteArray
) : IPrinterTextParserElement {
  private val length: Int
  private val image: ByteArray

  /**
   * Create new instance of PrinterTextParserImg.
   *
   * @param printerTextParserColumn Parent PrinterTextParserColumn instance.
   * @param textAlign Set the image alignment. Use PrinterTextParser.TAGS_ALIGN_... constants.
   * @param hexadecimalString Hexadecimal string of the image data.
   */
  constructor(
    printerTextParserColumn: PrinterTextParserColumn,
    textAlign: String?,
    hexadecimalString: String
  ) : this(printerTextParserColumn, textAlign, hexadecimalStringToBytes(hexadecimalString))

  /**
   * Create new instance of PrinterTextParserImg.
   *
   * @param printerTextParserColumn Parent PrinterTextParserColumn instance.
   * @param textAlign Set the image alignment. Use PrinterTextParser.TAGS_ALIGN_... constants.
   * @param image Bytes contain the image in ESC/POS command.
   */
  init {
    var image = image
    val printer = printerTextParserColumn.line.textParser.printer
    val byteWidth = (image[4].toInt() and 0xFF) + (image[5].toInt() and 0xFF) * 256
    val width = byteWidth * 8
    val height = (image[6].toInt() and 0xFF) + (image[7].toInt() and 0xFF) * 256
    val nbrByteDiff =
      floor(((printer.printerWidthPx - width).toFloat() / 8f).toDouble()).toInt()
    var nbrWhiteByteToInsert = 0
    when (textAlign) {
      PrinterTextParser.TAGS_ALIGN_CENTER -> nbrWhiteByteToInsert =
        (nbrByteDiff.toFloat() / 2f).roundToInt()
      PrinterTextParser.TAGS_ALIGN_RIGHT -> nbrWhiteByteToInsert = nbrByteDiff
    }
    if (nbrWhiteByteToInsert > 0) {
      val newByteWidth = byteWidth + nbrWhiteByteToInsert
      val newImage = EscPosPrinterCommands.initGSv0Command(newByteWidth, height)
      for (i in 0 until height) {
        System.arraycopy(
          image,
          byteWidth * i + 8,
          newImage,
          newByteWidth * i + nbrWhiteByteToInsert + 8,
          byteWidth
        )
      }
      image = newImage
    }
    length =
      ceil((byteWidth.toFloat() * 8 / printer.printerCharSizeWidthPx.toFloat()).toDouble())
        .toInt()
    this.image = image
  }

  /**
   * Get the image width in char length.
   *
   * @return int
   */
  @Throws(EscPosEncodingException::class)
  override fun length(): Int {
    return length
  }

  /**
   * Print image
   *
   * @param printerSocket Instance of EscPosPrinterCommands
   * @return this Fluent method
   */
  @Throws(EscPosConnectionException::class)
  override fun print(printerSocket: EscPosPrinterCommands?): PrinterTextParserImg? {
    printerSocket!!.printImage(image)
    return this
  }

  companion object {
    /**
     * Convert Drawable instance to a hexadecimal string of the image data.
     *
     * @param printerSize A EscPosPrinterSize instance that will print the image.
     * @param drawable Drawable instance to be converted.
     * @return A hexadecimal string of the image data. Empty string if Drawable cannot be cast to BitmapDrawable.
     */
    fun bitmapToHexadecimalString(printerSize: EscPosPrinterSize, drawable: Drawable?): String {
      return if (drawable is BitmapDrawable) {
        bitmapToHexadecimalString(
          printerSize,
          drawable
        )
      } else ""
    }

    /**
     * Convert Drawable instance to a hexadecimal string of the image data.
     *
     * @param printerSize A EscPosPrinterSize instance that will print the image.
     * @param drawable Drawable instance to be converted.
     * @param gradient false : Black and white image, true : Grayscale image
     * @return A hexadecimal string of the image data. Empty string if Drawable cannot be cast to BitmapDrawable.
     */
    fun bitmapToHexadecimalString(
      printerSize: EscPosPrinterSize,
      drawable: Drawable?,
      gradient: Boolean
    ): String {
      return if (drawable is BitmapDrawable) {
        bitmapToHexadecimalString(
          printerSize,
          drawable,
          gradient
        )
      } else ""
    }

    /**
     * Convert BitmapDrawable instance to a hexadecimal string of the image data.
     *
     * @param printerSize A EscPosPrinterSize instance that will print the image.
     * @param bitmapDrawable BitmapDrawable instance to be converted.
     * @return A hexadecimal string of the image data.
     */
    fun bitmapToHexadecimalString(
      printerSize: EscPosPrinterSize,
      bitmapDrawable: BitmapDrawable
    ): String {
      return bitmapToHexadecimalString(printerSize, bitmapDrawable.bitmap)
    }

    /**
     * Convert BitmapDrawable instance to a hexadecimal string of the image data.
     *
     * @param printerSize A EscPosPrinterSize instance that will print the image.
     * @param bitmapDrawable BitmapDrawable instance to be converted.
     * @param gradient false : Black and white image, true : Grayscale image
     * @return A hexadecimal string of the image data.
     */
    fun bitmapToHexadecimalString(
      printerSize: EscPosPrinterSize,
      bitmapDrawable: BitmapDrawable,
      gradient: Boolean
    ): String {
      return bitmapToHexadecimalString(printerSize, bitmapDrawable.bitmap, gradient)
    }

    /**
     * Convert Bitmap instance to a hexadecimal string of the image data.
     *
     * @param printerSize A EscPosPrinterSize instance that will print the image.
     * @param bitmap Bitmap instance to be converted.
     * @return A hexadecimal string of the image data.
     */
    fun bitmapToHexadecimalString(printerSize: EscPosPrinterSize, bitmap: Bitmap?): String {
      return bitmapToHexadecimalString(printerSize, bitmap, true)
    }

    /**
     * Convert Bitmap instance to a hexadecimal string of the image data.
     *
     * @param printerSize A EscPosPrinterSize instance that will print the image.
     * @param bitmap Bitmap instance to be converted.
     * @param gradient false : Black and white image, true : Grayscale image
     * @return A hexadecimal string of the image data.
     */
    fun bitmapToHexadecimalString(
      printerSize: EscPosPrinterSize,
      bitmap: Bitmap?,
      gradient: Boolean
    ): String {
      return bytesToHexadecimalString(
        printerSize.bitmapToBytes(
          bitmap!!, gradient
        )
      )
    }

    /**
     * Convert byte array to a hexadecimal string of the image data.
     *
     * @param bytes Bytes contain the image in ESC/POS command.
     * @return A hexadecimal string of the image data.
     */
    fun bytesToHexadecimalString(bytes: ByteArray): String {
      val imageHexString = StringBuilder()
      for (aByte in bytes) {
        val hexString = Integer.toHexString(aByte.toInt() and 0xFF)
        if (hexString.length == 1) {
          imageHexString.append("0")
        }
        imageHexString.append(hexString)
      }
      return imageHexString.toString()
    }

    /**
     * Convert hexadecimal string of the image data to bytes ESC/POS command.
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
