package deckyfx.reactnative.printer.escposprinter.connection.tcp

import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket

class TcpConnection
  /**
   * Create un instance of TcpConnection.
   *
   * @param address IP address of the device
   * @param port    Port of the device
   */
    @JvmOverloads constructor(
    private val address: String,
    private val port: Int,
    private val timeout: Int = 30
) : DeviceConnection() {
    private var socket: Socket? = null
    /**
     * Create un instance of TcpConnection.
     *
     * Overload of the above function TcpConnection()
     * Include timeout parameter in milliseconds.
     *
     * @param address IP address of the device
     * @param port    Port of the device
     * @param timeout Timeout in milliseconds to establish a connection
     */
    /**
     * Check if the TCP device is connected by socket.
     *
     * @return true if is connected
     */
    override val isConnected: Boolean
        get() = socket != null && socket!!.isConnected && super.isConnected

    /**
     * Start socket connection with the TCP device.
     */
    @Throws(EscPosConnectionException::class)
    override fun connect(): TcpConnection {
        if (isConnected) {
            return this
        }
        try {
            socket = Socket()
            socket!!.connect(InetSocketAddress(InetAddress.getByName(address), port), timeout)
            if (socket == null) {
              throw EscPosConnectionException("Socket is null")
            }
            outputStream = socket!!.getOutputStream()
            inputStream = socket!!.getInputStream()
            data = ByteArray(0)
        } catch (e: IOException) {
            e.printStackTrace()
            disconnect()
            throw EscPosConnectionException("Unable to connect to TCP device.")
        }
        return this
    }

    /**
     * Close the socket connection with the TCP device.
     */
    override fun disconnect(): TcpConnection {
        data = ByteArray(0)
        if (outputStream != null) {
            try {
                outputStream!!.close()
                outputStream = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        if (inputStream != null) {
          try {
            inputStream!!.close()
            inputStream = null
          } catch (e: IOException) {
            e.printStackTrace()
          }
        }
        if (socket != null) {
            try {
                socket!!.close()
                socket = null
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return this
    }
}
