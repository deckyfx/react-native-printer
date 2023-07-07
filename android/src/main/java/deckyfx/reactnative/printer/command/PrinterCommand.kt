package deckyfx.reactnative.printer.command

import java.io.UnsupportedEncodingException

object PrinterCommand {
    /**
     * getBarCodeCommand
     *
     * @param str              code
     * @param nType            It is between 65 and 73. {65: UPC-A, 66: UPC-E, 67: EAN13, 68: EAN8, 69: CODE39, 70: ITF, 71: CODABAR, 72: CODE93, 73: CODE128}
     * @param nWidthX          width {2-6}
     * @param nHeight          height {0-255}
     * @param nHriFontType     FontA or FontB
     * @param nHriFontPosition code position (0: none, 1: top, 2: bottom, 3: top - bottom)
     * @return
     * @reference
     * http://www.sam4s.co.kr/_common/ac_downFile.asp?f_url=/files/DOWN/2019012393432_1.pdf&f_name=SAM4S%20Printer%20Control%20Command%20Manual%20REV1_8.pdf (Page 71)
     * https://github.com/januslo/react-native-bluetooth-escpos-printer/blob/master/android/src/main/java/cn/jystudio/bluetooth/escpos/command/sdk/PrinterCommand.java
     */
    @JvmStatic
    fun getBarCodeCommand(str: String, nType: Int, nWidthX: Int, nHeight: Int, nHriFontType: Int, nHriFontPosition: Int): ByteArray? {
        if ((nType < 0x41) or (nType > 0x49) or (nWidthX < 2) or (nWidthX > 6
                        ) or (nHeight < 1) or (nHeight > 255) or (str.length == 0)) return null
        var bCodeData: ByteArray? = null
        bCodeData = try {
            // TODO: get rid of GBK default!
            str.toByteArray(charset("GBK"))
        } catch (e: UnsupportedEncodingException) {
            e.printStackTrace()
            return null
        }
        val command = ByteArray(bCodeData?.size!! + 16)
        command[0] = 29
        command[1] = 119
        command[2] = nWidthX.toByte()
        command[3] = 29
        command[4] = 104
        command[5] = nHeight.toByte()
        command[6] = 29
        command[7] = 102
        command[8] = (nHriFontType and 0x01).toByte()
        command[9] = 29
        command[10] = 72
        command[11] = (nHriFontPosition and 0x03).toByte()
        command[12] = 29
        command[13] = 107
        command[14] = nType.toByte()
        command[15] = bCodeData?.size!!.toByte()
        System.arraycopy(bCodeData, 0, command, 16, bCodeData?.size!!)
        return command
    }
}
