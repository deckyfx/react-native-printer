package deckyfx.reactnative.printer.escposprinter.textparser

import android.content.Context
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.webkit.URLUtil
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException
import deckyfx.reactnative.printer.escposprinter.textparser.PrinterTextParserImg.Companion.bitmapToHexadecimalString
import java.io.File
import java.util.Locale


class PrinterImageUriParser(private val context: Context?, private val escPosPrinter: EscPosPrinter) {
  fun parse(text: String?): String {
    if (text.isNullOrEmpty()) {
      return ""
    }
    val stringLines = text.split("\n|\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val resultLines = Array(stringLines.size) { "" }
    for ((i, line) in stringLines.withIndex()) {
      resultLines[i] = line
      var textAlign = ""
      var textColumn = ""
      if (line.length > 2) {
        when (line.substring(0, 3).uppercase(Locale.getDefault())) {
          "[" + PrinterTextParser.TAGS_ALIGN_LEFT + "]", "[" + PrinterTextParser.TAGS_ALIGN_CENTER + "]", "[" + PrinterTextParser.TAGS_ALIGN_RIGHT + "]" -> {
            textAlign = "[" + line.substring(1, 2).uppercase(Locale.getDefault()) + "]"
            textColumn = line.substring(3)
          }
        }
      }
      val trimmedTextColumn = textColumn.trim { it <= ' ' }
      if (trimmedTextColumn.indexOf("<") == 0) {
        val openTagIndex = trimmedTextColumn.indexOf("<")
        val openTagEndIndex = trimmedTextColumn.indexOf(">", openTagIndex + 1) + 1
        if (openTagIndex < openTagEndIndex) {
          val textParserTag =
            PrinterTextParserTag(trimmedTextColumn.substring(openTagIndex, openTagEndIndex))
          when (textParserTag.tagName) {
            PrinterTextParser.TAGS_IMAGE -> {
              val closeTag = "</" + textParserTag.tagName + ">"
              val closeTagPosition = trimmedTextColumn.length - closeTag.length
              if (trimmedTextColumn.substring(closeTagPosition) == closeTag) {
                when (textParserTag.tagName) {
                  PrinterTextParser.TAGS_IMAGE -> {
                    // textAlign
                    // textParserTag
                    // textParserTag.attributes
                    val imageContents = trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                    var imageData: String? = null

                    var height = 0
                    if (textParserTag.attributes.containsKey(PrinterTextParser.ATTR_BARCODE_HEIGHT)) {
                      val barCodeAttribute = textParserTag.attributes[PrinterTextParser.ATTR_BARCODE_HEIGHT]
                        ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_HEIGHT)
                      height = try {
                        barCodeAttribute.toInt()
                      } catch (nfe: NumberFormatException) {
                        throw EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_HEIGHT + " value")
                      }
                    }
                    var width = 0
                    if (textParserTag.attributes.containsKey(PrinterTextParser.ATTR_BARCODE_WIDTH)) {
                      val barCodeAttribute = textParserTag.attributes[PrinterTextParser.ATTR_BARCODE_WIDTH]
                        ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_WIDTH)
                      width = try {
                        barCodeAttribute.toInt()
                      } catch (nfe: NumberFormatException) {
                        throw EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_WIDTH + " value")
                      }
                    }

                    if (URLUtil.isContentUrl(imageContents)) {
                      // content:// is not handlee for now
                    } else if (URLUtil.isFileUrl(imageContents) || URLUtil.isAssetUrl(imageContents)) {
                      imageData = getImageHexadecimalString(imageContents, width, height)
                    } else if (URLUtil.isHttpUrl(imageContents) || URLUtil.isHttpsUrl(imageContents)) {
                      imageData = getImageHexadecimalString(imageContents, width, height)
                    }
                    if (!imageData.isNullOrEmpty()) {
                      resultLines[i] = textAlign + "<" + textParserTag.tagName + ">" + imageData + "</" + textParserTag.tagName + ">"
                    }
                  }
                }
              }
            }
          }
        }
      }
      // if contain <img> tag get its contents
      // if contain file:// resize image, convert to monochrome, then get its bitmap
      // if contains http[s?]:// download, resize image, convert to monochrome then get its bitmap
      // otherwise return as is
    }
    return resultLines.joinToString("\n")
  }

  private fun resizeImage(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    var image = image
    if (maxHeight > 0 && maxWidth > 0) {
      val width = image.width
      val height = image.height
      val ratioBitmap = width.toFloat() / height.toFloat()
      val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
      var finalWidth = maxWidth
      var finalHeight = maxHeight
      if (ratioMax > ratioBitmap) {
        finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
      } else {
        finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
      }
      image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
    }
    return image
  }

  private fun toGrayscale(bmpOriginal: Bitmap): Bitmap? {
    val height: Int = bmpOriginal.height
    val width: Int = bmpOriginal.width
    val bmpGrayscale = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
    val c = Canvas(bmpGrayscale)
    val paint = Paint()
    val cm = ColorMatrix()
    cm.setSaturation(0f)
    val f = ColorMatrixColorFilter(cm)
    paint.colorFilter = f
    c.drawBitmap(bmpOriginal, 0f, 0f, paint)
    return bmpGrayscale
  }

  private fun getImageHexadecimalString(filePath: String, maxWidth: Int, maxHeight: Int): String? {
    val fileUri = Uri.parse(filePath)
    var image: Bitmap? = null
    val op = BitmapFactory.Options()
    op.inPreferredConfig = Bitmap.Config.ARGB_8888
    image = BitmapFactory.decodeFile(fileUri.path, op)
    if (image == null) {
      return null
    }
    val width = when(maxWidth > 0) {
      true -> maxWidth
      false -> 250
    }
    val height = when(maxHeight > 0) {
      true -> maxHeight
      false -> 250
    }
    val resizedImage = resizeImage(image, width, height)
    image.recycle()
    val grayscaleImage = toGrayscale(resizedImage)
    resizedImage.recycle()
    return  bitmapToHexadecimalString(this.escPosPrinter, grayscaleImage)
  }

  companion object
}
