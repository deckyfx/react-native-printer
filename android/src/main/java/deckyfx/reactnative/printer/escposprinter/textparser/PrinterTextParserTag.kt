package deckyfx.reactnative.printer.escposprinter.textparser

import java.util.Hashtable
import java.util.Locale

class PrinterTextParserTag(tag: String) {
    var tagName = ""
    val attributes = Hashtable<String, String>()
    var length = 0
    var isCloseTag = false

    init {
        var tag = tag
        tag = tag.trim { it <= ' ' }
        if (tag.substring(0, 1) != "<" || tag.substring(tag.length - 1) != ">") {
        } else {
          length = tag.length
          val openTagIndex = tag.indexOf("<")
          val closeTagIndex = tag.indexOf(">")
          val nextSpaceIndex = tag.indexOf(" ")
          if (nextSpaceIndex != -1 && nextSpaceIndex < closeTagIndex) {
            tagName = tag.substring(openTagIndex + 1, nextSpaceIndex).lowercase(Locale.getDefault())
            var attributesString = tag.substring(nextSpaceIndex, closeTagIndex).trim { it <= ' ' }
            while (attributesString.contains("='")) {
              val egalPos = attributesString.indexOf("='")
              val endPos = attributesString.indexOf("'", egalPos + 2)
              val attributeName = attributesString.substring(0, egalPos)
              val attributeValue = attributesString.substring(egalPos + 2, endPos)
              if (attributeName != "") {
                attributes[attributeName] = attributeValue
              }
              attributesString = attributesString.substring(endPos + 1).trim { it <= ' ' }
            }
          } else {
            tagName = tag.substring(openTagIndex + 1, closeTagIndex).lowercase(Locale.getDefault())
          }
          if (tagName.substring(0, 1) == "/") {
            tagName = tagName.substring(1)
            isCloseTag = true
          }
        }
    }

    fun getAttribute(key: String): String? {
        return attributes[key]
    }

    fun hasAttribute(key: String): Boolean {
        return attributes.containsKey(key)
    }
}
