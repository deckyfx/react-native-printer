package deckyfx.reactnative.printer.escposprinter.textparser

import java.util.Locale


class PreParserRaw(
) {
  fun parse(text: String?): String {
    if (text.isNullOrEmpty()) {
      return ""
    }
    val stringLines = text.split("\n|\r\n".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
    val resultLines = Array(stringLines.size) { "" }
    for ((i, line) in stringLines.withIndex()) {
      resultLines[i] = line
      var textColumn = ""
      if (line.length > 2) {
        when (line.substring(0, 3).uppercase(Locale.getDefault())) {
          "[" + PrinterTextParser.TAGS_ALIGN_LEFT + "]", "[" + PrinterTextParser.TAGS_ALIGN_CENTER + "]", "[" + PrinterTextParser.TAGS_ALIGN_RIGHT + "]" -> {
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
            PrinterTextParser.TAGS_RAW -> {
              val closeTag = "</" + textParserTag.tagName + ">"
              val closeTagPosition = trimmedTextColumn.length - closeTag.length
              if (trimmedTextColumn.substring(closeTagPosition) == closeTag) {
                when (textParserTag.tagName) {
                  PrinterTextParser.TAGS_RAW -> {
                    val rawContents = trimmedTextColumn.substring(openTagEndIndex, closeTagPosition)
                    resultLines[i] = wrapTag(textParserTag.tagName, rawContents)
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

  private fun isHexString(string: String): Boolean {
    return string.all { it in '0'..'9' || it in 'A'..'F' || it in 'a'..'f' }
  }

  private fun wrapTag(tag: String, content: String): String {
    return "<$tag>$content</$tag>"
  }
}
