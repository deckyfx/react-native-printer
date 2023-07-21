package deckyfx.reactnative.printer.serialport

import deckyfx.reactnative.printer.serialport.ByteUtil.hexStringToByteArray
import java.util.Locale

/**
 * @author Rair
 * @date 2018/1/5
 *
 *
 * desc:BCC校验
 */
object BCCVerify {
    /**
     * 计算BCC
     *
     * @param data 数据报文
     */
    private fun bccVal(data: ByteArray): String {
        var ret = ""
        val BCC = ByteArray(1)
        for (datum in data) {
            BCC[0] = (BCC[0].toInt() xor datum.toInt()).toByte()
        }
        var hex = Integer.toHexString(BCC[0].toInt() and 0xFF)
        if (hex.length == 1) {
            hex = "0$hex"
        }
        ret += hex.uppercase(Locale.getDefault())
        return ret
    }

    /**
     * 计算BCC并转为byte数组
     *
     * @param data 数据报文
     */
    fun calcBccBytes(data: ByteArray): ByteArray {
        val bccVal = bccVal(data)
        return hexStringToByteArray(bccVal)
    }
}
