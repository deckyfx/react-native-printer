package deckyfx.reactnative.printer.serialport

import java.util.Locale

object ByteUtil {
    private val hexArray = "0123456789ABCDEF".toCharArray()

    /**
     * 字符串转字节数组
     *
     * @param s 字符串
     * @return 数组
     */
    @JvmStatic
    fun hexStringToByteArray(s: String): ByteArray {
        var s = s
        if (s.length < 2) {
            s = "0$s"
        }
        val len = s.length
        val data = ByteArray(len / 2)
        var i = 0
        while (i < len) {
            data[i / 2] = ((s[i].digitToIntOrNull(16) ?: -1 shl 4)
            + s[i + 1].digitToIntOrNull(16)!!).toByte()
            i += 2
        }
        return data
    }

    /**
     * 字节数组转字符串
     *
     * @param hexBytes 数组
     * @return 字符串
     */
    fun hexBytesToString(hexBytes: ByteArray): String {
        val hexChars = CharArray(hexBytes.size * 2)
        for (j in hexBytes.indices) {
            val v = hexBytes[j].toInt() and 0xFF
            hexChars[j * 2] = hexArray[v ushr 4]
            hexChars[j * 2 + 1] = hexArray[v and 0x0F]
        }
        return String(hexChars)
    }

    /**
     * 16进制字符串转int
     *
     * @param hexString 字符串
     * @return int
     */
    fun hexStringToInt(hexString: String): Int {
        return hexString.toInt(16)
    }

    /**
     * 16进制字符串转byte数组
     *
     * @param hexString the hex string
     * @return byte[]
     */
    fun hexStringToBytes(hexString: String?): ByteArray? {
        var hexString = hexString
        if (hexString == null || hexString == "") {
            return null
        }
        hexString = hexString.uppercase(Locale.getDefault())
        val length = hexString.length / 2
        val hexChars = hexString.toCharArray()
        val d = ByteArray(length)
        for (i in 0 until length) {
            val pos = i * 2
            d[i] =
                (charToByte(hexChars[pos]).toInt() shl 4 or charToByte(hexChars[pos + 1]).toInt()).toByte()
        }
        return d
    }

    /**
     * char转换位byte
     *
     * @param c char
     * @return byte
     */
    private fun charToByte(c: Char): Byte {
        return "0123456789ABCDEF".indexOf(c).toByte()
    }
}
