package deckyfx.reactnative.printer.serialport

/**
 * @author Rair
 * @date 2017/10/25
 *
 *
 * desc:数据接收回调
 */
interface OnDataReceiverListener {
    /**
     * 接收数据
     *
     * @param buffer 收到的字节数组
     * @param size   长度
     */
    fun onDataReceiver(buffer: ByteArray?, size: Int)
}
