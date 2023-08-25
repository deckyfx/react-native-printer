package deckyfx.reactnative.printer.escposprinter

import android.graphics.Bitmap
import kotlin.math.roundToInt

abstract class EscPosPrinterSize protected constructor(
  /**
   * Get the printer DPI
   *
   * @return int
   */
  var printerDpi: Int,
  /**
   * Get the printing width in millimeters
   *
   * @return float
   */
  var printerWidthMM: Float,
  /**
   * Get the maximum number of characters that can be printed on a line.
   *
   * @return int
   */
  var printerNbrCharactersPerLine: Int
) {
  /**
   * Get the printing width in dot
   *
   * @return int
   */
  var printerWidthPx: Int
    protected set

  /**
   * Get the number of dot that a printed character contain
   *
   * @return int
   */
  var printerCharSizeWidthPx: Int
    protected set

  init {
    val printingWidthPx = mmToPx(printerWidthMM)
    printerWidthPx = printingWidthPx + printingWidthPx % 8
    printerCharSizeWidthPx = printingWidthPx / printerNbrCharactersPerLine
  }

  /**
   * Convert from millimeters to dot the mmSize variable.
   *
   * @param mmSize Distance in millimeters to be converted
   * @return int
   */
  fun mmToPx(mmSize: Float): Int {
    return (mmSize * printerDpi.toFloat() / INCH_TO_MM).roundToInt()
  }

  /**
   * Convert Bitmap object to ESC/POS image.
   *
   * @param bitmap Instance of Bitmap
   * @param gradient false : Black and white image, true : Grayscale image
   * @return Bytes contain the image in ESC/POS command
   */
  fun bitmapToBytes(bitmap: Bitmap, gradient: Boolean): ByteArray {
    var newBitmap = bitmap
    var isSizeEdit = false
    var bitmapWidth = bitmap.width
    var bitmapHeight = bitmap.height
    val maxWidth = printerWidthPx
    val maxHeight = 256
    if (bitmapWidth > maxWidth) {
      bitmapHeight =
        Math.round(bitmapHeight.toFloat() * maxWidth.toFloat() / bitmapWidth.toFloat())
      bitmapWidth = maxWidth
      isSizeEdit = true
    }
    if (bitmapHeight > maxHeight) {
      bitmapWidth =
        Math.round(bitmapWidth.toFloat() * maxHeight.toFloat() / bitmapHeight.toFloat())
      bitmapHeight = maxHeight
      isSizeEdit = true
    }
    if (isSizeEdit) {
      newBitmap = Bitmap.createScaledBitmap(bitmap, bitmapWidth, bitmapHeight, true)
    }
    return EscPosPrinterCommands.bitmapToBytes(newBitmap, gradient)
  }

  companion object {
    const val INCH_TO_MM = 25.4f
  }
}
