package deckyfx.reactnative.printer.escposprinter.textparser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint
import android.net.Uri
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosParserException
import deckyfx.reactnative.printer.escposprinter.textparser.PrinterTextParserImg.Companion.bitmapToHexadecimalString
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.Locale
import kotlin.math.ceil


class PrinterImageUriParser(
  private val context: Context?,
  private val escPosPrinter: EscPosPrinter
) {
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
                    val imageContents =
                      trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                    var imageData: String? = null

                    var height = 0
                    if (textParserTag.attributes.containsKey(PrinterTextParser.ATTR_BARCODE_HEIGHT)) {
                      val barCodeAttribute =
                        textParserTag.attributes[PrinterTextParser.ATTR_BARCODE_HEIGHT]
                          ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_HEIGHT)
                      height = try {
                        barCodeAttribute.toInt()
                      } catch (nfe: NumberFormatException) {
                        throw EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_HEIGHT + " value")
                      }
                    }
                    var width = 0
                    if (textParserTag.attributes.containsKey(PrinterTextParser.ATTR_BARCODE_WIDTH)) {
                      val barCodeAttribute =
                        textParserTag.attributes[PrinterTextParser.ATTR_BARCODE_WIDTH]
                          ?: throw EscPosParserException("Invalid barcode attribute: " + PrinterTextParser.ATTR_BARCODE_WIDTH)
                      width = try {
                        barCodeAttribute.toInt()
                      } catch (nfe: NumberFormatException) {
                        throw EscPosParserException("Invalid barcode " + PrinterTextParser.ATTR_BARCODE_WIDTH + " value")
                      }
                    }

                    imageData = getImageHexadecimalString(imageContents, width, height)
                    if (!imageData.isNullOrEmpty()) {
                      resultLines[i] =
                        textAlign + "<" + textParserTag.tagName + ">" + imageData + "</" + textParserTag.tagName + ">"
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

  private fun getImageHexadecimalString(imageUri: String, maxWidth: Int, maxHeight: Int): String? {
    /**
     * Supported URI schemes:
     *
     * * file://
     * * content://
     * * data://
     * * resource://
     * * asset://
     * * http://
     * * https://
     */
    try {
      val url = URL(imageUri)
      val scheme = url.protocol
      var inputStream: InputStream? = null
      var fileName: String? = null
      if (scheme == "http" || scheme == "https") {
        // Check if the file exists in context.filesDir
        fileName = getFileNameFromImageUri(imageUri)
        val file = File(context!!.filesDir, fileName)
        if (file.exists()) {
          inputStream = file.inputStream()
          val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
          return bitmapToHexadecimalString(this.escPosPrinter, bitmap, false)
        } else {
          // Remote file
          val connection = url.openConnection()
          inputStream = connection.getInputStream()
        }
      } else {
        inputStream = context!!.contentResolver.openInputStream(Uri.parse(imageUri))
      }
      val bitmap = BitmapFactory.decodeStream(inputStream) ?: return null
      val grayscaleBitmap = toGrayscale(bitmap)
      val resizedBitmap = resizeImage(grayscaleBitmap, maxWidth, maxHeight)

      if (!fileName.isNullOrBlank()) {
        // Save the bitmap to local storage
        val file = File(context.filesDir, fileName)
        resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
      }

      val byteArrayOutputStream = ByteArrayOutputStream()
      resizedBitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
      byteArrayOutputStream.close()

      inputStream?.close()
      bitmap.recycle()
      grayscaleBitmap.recycle()

      val byteArrays = byteArrayOutputStream.toByteArray()
      return bitmapToHexadecimalString(this.escPosPrinter, resizedBitmap, false)
    } catch (e: Exception) {
      return null
    }
  }

  private fun resizeImage(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    var image = image
    var finalWidth = 0
    var finalHeight = 0
    if ((maxWidth > 0 && maxHeight > 0) || (maxWidth == 0 && maxHeight == 0) || (maxWidth == 0 && maxHeight > 0) ) {
      finalHeight = if (maxHeight > 250 || maxHeight == 0) {
        250
      } else {
        maxHeight
      }
      val ratioMax = finalHeight.toFloat() / image.height.toFloat()
      finalWidth = ceil(image.width.toFloat() * ratioMax).toInt()
    } else if (maxWidth > 0) {
      finalWidth = if (maxWidth > 250) {
        250
      } else {
        maxWidth
      }
      val ratioMax = finalWidth.toFloat() / image.width.toFloat()
      finalHeight = ceil(image.height.toFloat() * ratioMax).toInt()
    }

    if (finalHeight > 0 && finalWidth > 0) {
      image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
    }
    return image
  }

  private fun toGrayscale(bmpOriginal: Bitmap): Bitmap {
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

  private fun getFileNameFromImageUri(imageUri: String): String {
    val fileName = imageUri.substring(imageUri.lastIndexOf("/") + 1)
    return fileName
  }

  companion object
}
