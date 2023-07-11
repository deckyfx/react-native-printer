package deckyfx.reactnative.printer.escposprinter.connection

import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.io.OutputStream

abstract class DeviceConnection {
  protected open var outputStream: OutputStream? = null

  @JvmField
  protected var inputStream: InputStream? = null

  @JvmField
  protected var data: ByteArray

  init {
    data = ByteArray(0)
  }

  @Throws(EscPosConnectionException::class)
  abstract fun connect(): DeviceConnection?
  abstract fun disconnect(): DeviceConnection?

  /**
   * Check if OutputStream is open.
   *
   * @return true if is connected
   */
  open val isConnected: Boolean
    get() = outputStream != null

  /**
   * Add data to send.
   */
  fun write(bytes: ByteArray) {
    val data = ByteArray(bytes.size + data.size)
    System.arraycopy(this.data, 0, data, 0, this.data.size)
    System.arraycopy(bytes, 0, data, this.data.size, bytes.size)
    this.data = data
  }

  /**
   * Send data to the device.
   */
  @Throws(EscPosConnectionException::class)
  open fun send() {
    send(0)
  }

  /**
   * Send data to the device.
   *
   * @param addWaitingTime add waiting time
   */
  @Throws(EscPosConnectionException::class)
  open fun send(addWaitingTime: Int) {
    if (!isConnected) {
      throw EscPosConnectionException("Unable to send data to device.")
    }
    try {
      outputStream!!.write(data)
      outputStream!!.flush()
      val waitingTime = addWaitingTime + data.size / 16
      data = ByteArray(0)
      if (waitingTime > 0) {
        Thread.sleep(waitingTime.toLong())
      }
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    } catch (e: InterruptedException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    }
  }

  /**
   * Send data to the device. and =wait for response
   */
  @Throws(EscPosConnectionException::class)
  open fun sendAndWaitForResponse() {
    sendAndWaitForResponse(0)
  }

  /**
   * Send data to the device. and =wait for response
   *
   * @param addWaitingTime add waiting time
   */
  @Throws(EscPosConnectionException::class)
  open fun sendAndWaitForResponse(addWaitingTime: Int): String? {
    if (!isConnected) {
      throw EscPosConnectionException("Unable to send data to device.")
    }
    try {
      outputStream!!.write(data)
      outputStream!!.flush()
      val waitingTime = addWaitingTime + data.size / 16
      data = ByteArray(0)
      if (waitingTime > 0) {
        Thread.sleep(waitingTime.toLong())
      }

      // waiting for response
      val input = BufferedReader(InputStreamReader(inputStream))
      val buffer = StringBuilder()
      while (input.ready()) {
        buffer.append(input.readLine())
      }
      buffer.toString()
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    } catch (e: InterruptedException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    }
    return null
  }

  @Throws(EscPosConnectionException::class)
  open fun getPrinterModel(): String? {
    if (!isConnected) {
      throw EscPosConnectionException("Unable to send data to device.")
    }
    if (outputStream == null) {
      return null
    }
    // Standard implementation
    write(byteArrayOf(0x1D, 0x49, 0x42, 0x1D, 0x49, 0x43))
    return sendAndWaitForResponse(100)
  }
}
