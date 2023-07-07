package deckyfx.reactnative.printer

import org.apache.commons.lang3.StringUtils
import java.io.BufferedReader
import java.io.IOException
import java.io.StringReader
import java.util.regex.Matcher
import java.util.regex.Pattern

// @ref https://github.com/LeeryBit/esc-pos-android/blob/master/library/src/main/java/com/leerybit/escpos/Ticket.java
class LayoutBuilder {
    var charsOnLine = CHARS_ON_LINE_58_MM

    internal constructor() {}
    internal constructor(charsOnLine: Int) {
        this.charsOnLine = charsOnLine
    }

    /**
     * DESIGN 1: Order List                       *
     * D0004 | Table #: A1 {C}           *
     * ------------------------------------------ *
     * [Dine In]                                  *
     * [ ] Espresso                               *
     * - No sugar, Regular 9oz, Hot           *
     * {R} x 1 *
     * ------------------------------------------ *
     * [ ] Blueberry Cheesecake                   *
     * - Slice                                *
     * {R} x 1 *
     * *
     * DESIGN 2: Menu Items                       *
     * ------------------------------------------ *
     * Item         {<>}       Qty  Price  Amount *
     * Pork Rice    {<>}         1  13.80   13.80 *
     */
    @JvmOverloads
    @Throws(IOException::class)
    fun createFromDesign(text: String?, charsOnLine: Int = this.charsOnLine): String {
        val reader = BufferedReader(StringReader(text))
        val designText = StringBuilder()
        var line: String
        while (reader.readLine().also { line = it } != null) {
            if (line.matches(Regex("---.*"))) {
                designText.append(createDivider(charsOnLine))
            } else if (line.matches(Regex("===.*"))) {
                designText.append(createDivider('=', charsOnLine))
            } else if (line.contains("{RP:")) {
                designText.append(duplicateStringSymbol(line))
            } else if (line.contains("{<>}")) {
                val splitLine = line.split("\\{<>\\}".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
                designText.append(createMenuItem(splitLine[0], splitLine[1], ' ', charsOnLine))
            } else {
                designText.append(line)
                designText.append("\n")
                // designText.append(createTextOnLine(line, ' ', TEXT_ALIGNMENT_LEFT, charsOnLine));
            }
        }
        return designText.toString()
    }

    @JvmOverloads
    fun createAccent(text: String, accent: Char, charsOnLine: Int = this.charsOnLine): String {
        var accent = accent
        if (text.length - 4 > charsOnLine) {
            accent = ' '
        }
        return createTextOnLine(" $text ", accent, TEXT_ALIGNMENT_CENTER, charsOnLine)
    }

    fun createDivider(charsOnLine: Int): String {
        return createDivider('-', charsOnLine)
    }

    @JvmOverloads
    fun createDivider(symbol: Char = '-', charsOnLine: Int = this.charsOnLine): String {
        return """
            ${StringUtils.repeat(symbol, charsOnLine)}

            """.trimIndent()
    }

    @JvmOverloads
    fun createMenuItem(key: String, value: String, space: Char, charsOnLine: Int = this.charsOnLine): String {
        return if (key.length + value.length + 2 > charsOnLine) {
            createTextOnLine("$key: $value", ' ', TEXT_ALIGNMENT_LEFT, charsOnLine)
        } else """
     ${StringUtils.rightPad(key, charsOnLine - value.length, space)}$value

     """.trimIndent()
    }

    @JvmOverloads
    fun createTextOnLine(text: String, space: Char, alignment: String?, charsOnLine: Int = this.charsOnLine): String {
        if (text.length > charsOnLine) {
            val out = StringBuilder()
            val len = text.length
            for (i in 0..len / charsOnLine) {
                val str = text.substring(i * charsOnLine, Math.min((i + 1) * charsOnLine, len))
                if (!str.trim { it <= ' ' }.isEmpty()) {
                    out.append(createTextOnLine(str, space, alignment))
                }
            }
            return out.toString()
        }
        return when (alignment) {
            TEXT_ALIGNMENT_RIGHT -> """
                ${StringUtils.leftPad(text, charsOnLine, space)}

                """.trimIndent()

            TEXT_ALIGNMENT_CENTER -> """
                ${StringUtils.center(text, charsOnLine, space)}

                """.trimIndent()

            else -> """
                ${StringUtils.rightPad(text, charsOnLine, space)}

                """.trimIndent()
        }
    }

    fun duplicateStringSymbol(text: String): String {
        var text = text
        val repeatTag = "{RP:"
        val regex = "\\$repeatTag\\d+:.*?\\}"
        val m = Pattern.compile(regex).matcher(text)
        var tagCount = 0
        while (m.find()) {
            tagCount++
        }
        for (x in 0 until tagCount) {
            var symbol = ""
            var count = "0"
            var repeatedSymbol: String? = ""
            val rpIndex = text.indexOf(repeatTag)
            val workingString = text.substring(rpIndex + repeatTag.length, text.indexOf('}'))
            val separatorIdx = workingString.indexOf(':')
            count = workingString.substring(0, separatorIdx)
            symbol = workingString.substring(separatorIdx + 1, workingString.length)
            repeatedSymbol = StringUtils.repeat(symbol, count.toInt())
            val replaceRepeatTag = "$repeatTag$workingString}"
            text = text.replaceFirst(Pattern.quote(text.substring(text.indexOf(replaceRepeatTag), text.indexOf(replaceRepeatTag) + replaceRepeatTag.length)).toRegex(), Matcher.quoteReplacement(repeatedSymbol))
        }
        return """
               $text

               """.trimIndent()
    }

    companion object {
        const val TEXT_ALIGNMENT_LEFT = "LEFT"
        const val TEXT_ALIGNMENT_CENTER = "CENTER"
        const val TEXT_ALIGNMENT_RIGHT = "RIGHT"
        const val CHARS_ON_LINE_58_MM = 32
        const val CHARS_ON_LINE_76_MM = 42
        const val CHARS_ON_LINE_80_MM = 48
    }
}
