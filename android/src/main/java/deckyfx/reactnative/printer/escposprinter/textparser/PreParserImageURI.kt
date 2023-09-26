package deckyfx.reactnative.printer.escposprinter.textparser

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.textparser.PrinterTextParserImg.Companion.bitmapToHexadecimalString
import java.io.ByteArrayInputStream
import java.io.File
import java.io.InputStream
import java.net.URL
import java.util.Locale


class PreParserImageURI(
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
      // IMG tag without alignment is not allowed, default is LEFT
      if (textAlign.isEmpty()) {
        textAlign = "[L]"
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
                    val imageContents = trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)

                    var image = resolveBitmap(imageContents)
                    if (image == null) {
                      if (isHexString(imageContents)) {
                        resultLines[i] = wrapTag(textAlign, textParserTag.tagName, imageContents)
                      } else {
                        resultLines[i] = ""
                      }
                      continue
                    }

                    var height = image.height
                    if (textParserTag.attributes.containsKey(PrinterTextParser.ATTR_BARCODE_HEIGHT)) {                      height = try {
                        val barCodeAttribute = textParserTag.attributes[PrinterTextParser.ATTR_BARCODE_HEIGHT]
                        barCodeAttribute?.toInt() ?: image.height
                      } catch (nfe: NumberFormatException) {
                        image.height
                      }
                    }
                    var width = image.width
                    if (textParserTag.attributes.containsKey(PrinterTextParser.ATTR_BARCODE_WIDTH)) {
                      width = try {
                        val barCodeAttribute =
                          textParserTag.attributes[PrinterTextParser.ATTR_BARCODE_WIDTH]
                        barCodeAttribute?.toInt() ?: image.width
                      } catch (nfe: NumberFormatException) {
                        image.width
                      }
                    }

                    /**
                    // Should we process the image bitmap further?
                    if (width > 0 && height > 0) {
                      image = resizeImage(image, width, height)
                    } else if (width == 0 && height > 0) {
                      image = resizeImageToHeight(image, height)
                    } else if (width > 0 && height == 0) {
                      image = resizeImageToHeight(image, width)
                    }
                    */

                    // Convert URL to Hex String
                    resultLines[i] = wrapTag(textAlign, textParserTag.tagName, bitmapToHexadecimalString(this.escPosPrinter, image))
                    image.recycle()
                  }
                }
              }
            }
          }
        }
      }
    }
    return resultLines.filter { string: String -> string.isNotEmpty() }.joinToString("\n")
  }

  private fun resolveBitmap(imageUri: String): Bitmap? {
    /**
     * Supported URI schemes:
     *
     * * http://
     * * https://
     * * data://
     * * file://
     * * content://
     * * resource://
     * * asset://
     */

    var inputStream: InputStream? = null
    try {
      val colonIndex = imageUri.indexOf(':')
      if (colonIndex == -1) {
        return null
      }
      val scheme = imageUri.substring(0, colonIndex)
      var fileName: String? = null
      when (scheme) {
        "http", "https" -> {
          val url = URL(imageUri)
          // Check if the file exists in context.filesDir
          val file = File(context!!.filesDir, getFileNameFromImageUri(imageUri))
          if (file.exists()) {
            inputStream = file.inputStream()
          } else {
            // Remote file
            fileName = getFileNameFromImageUri(imageUri)
            val connection = url.openConnection()
            inputStream = connection.getInputStream()
          }
        }
        /**
          Data Base64 URL must have format
          data://data:<mime_type>;base64,<data>
          Example:
          data://data:image/jpeg;base64,/9j/4AAQSkZJRgABAgAAZABkAAD
        */
        "data" -> {
          val withoutProtocol = imageUri.replaceFirst("^[a-zA-Z]+://", "")
          val base64Data = withoutProtocol.substringAfter(",")
          val decodedBytes = Base64.decode(base64Data, Base64.DEFAULT)
          inputStream = ByteArrayInputStream(decodedBytes)
        }

        "content", "file", "resource" -> {
          val uri = Uri.parse(imageUri)
          val contentResolver = context!!.contentResolver
          inputStream = contentResolver.openInputStream(uri) ?: throw java.lang.Exception("Failed open input stream")
        }

        else -> {
          inputStream = context!!.contentResolver.openInputStream(Uri.parse(imageUri))
        }
      }

      val bitmap = BitmapFactory.decodeStream(inputStream) ?: throw java.lang.Exception("Failed decode input stream")
      inputStream?.close()

      // Cache downloaded file
      if (!fileName.isNullOrBlank()) {
        // Save the bitmap to local storage
        val file = File(context!!.filesDir, fileName)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, file.outputStream())
      }

      return bitmap
    } catch (e: Exception) {
      inputStream?.close()
      return null
    }
  }

  private fun getFileNameFromImageUri(imageUri: String): String {
    return imageUri.substring(imageUri.lastIndexOf("/") + 1)
  }

  private fun isHexString(string: String): Boolean {
    return string.all { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }
  }

  private fun wrapTag(align: String, tag: String, content: String): String {
    return "$align<$tag>$content</$tag>"
  }

  /**
   * Resizes a Bitmap image.
   *
   * @param image
   * @param width
   * @return new Bitmap image.
   */
  private fun resizeImageToWidth(image: Bitmap, width: Int): Bitmap {
    val origWidth = image.width
    val origHeight = image.height
    if (origWidth > width) {
      // picture is wider than we want it, we calculate its target height
      val destHeight = origHeight / (origWidth / width)
      // we create an scaled bitmap so it reduces the image, not just trim it
      return Bitmap.createScaledBitmap(image, width, destHeight, false)
    }
    return image
  }

  private fun resizeImageToHeight(image: Bitmap, height: Int): Bitmap {
    val origWidth = image.height
    val origHeight = image.height
    if (origHeight > height) {
      // picture is wider than we want it, we calculate its target height
      val destWidth = origWidth / (origHeight / height)
      // we create an scaled bitmap so it reduces the image, not just trim it
      return Bitmap.createScaledBitmap(image, height, destWidth, false)
    }
    return image
  }

  private fun resizeImage(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
    var newImage = image
    if (maxHeight > 0 && maxWidth > 0) {
      val width = newImage.width
      val height = newImage.height
      val ratioBitmap = width.toFloat() / height.toFloat()
      val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
      var finalWidth = maxWidth
      var finalHeight = maxHeight
      if (ratioMax > ratioBitmap) {
        finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
      } else {
        finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
      }
      newImage = Bitmap.createScaledBitmap(newImage, finalWidth, finalHeight, true)
    }
    return newImage
  }
}
