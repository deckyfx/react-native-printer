package deckyfx.reactnative.printer.escposprinter.connection

import deckyfx.reactnative.printer.escposprinter.EscPosCommands
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

abstract class DeviceConnection {
  protected open var outputStream: OutputStream? = null
  protected open var inputStream: InputStream? = null

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
  open fun sendAndWaitForResponse(): String? {
    return sendAndWaitForResponse(0)
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
      val bufferOut = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream!!)), true)
      val message = data
      val payload = String(message, StandardCharsets.UTF_8)
      bufferOut.println(payload)
      bufferOut.flush()
      val waitingTime = addWaitingTime + data.size / 16
      data = ByteArray(0)
      if (waitingTime > 0) {
        Thread.sleep(waitingTime.toLong())
      }
      val buffer = ByteArray(1024)
      var read: Int
      while (inputStream!!.read(buffer).also { read = it } != -1) {
        return String(buffer, 0, read)
      }
      return ""
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
  fun getPrinterModel(): String? {
    if (!isConnected) {
      throw EscPosConnectionException("Unable to send data to device.")
    }
    if (outputStream == null) {
      return null
    }
    write(EscPosCommands.PRINTER_ID_1.toByteArray())
    var manufacturer = sendAndWaitForResponse(100)
    if (!manufacturer.isNullOrEmpty() && manufacturer.length == 1) {
      manufacturer = when (manufacturer[0].code) {
        2 -> "SEWOO"
        13 -> "SEWOO"
        else -> "UNKNOWN"
      }
    }
    write(EscPosCommands.PRINTER_ID_2.toByteArray())
    var model = sendAndWaitForResponse(100)
    if (!model.isNullOrEmpty() && model.length == 1) {
      model = when (model[0].code) {
        2 -> "LK_D30"
        13 -> "LK_D30"
        else -> "UNKNOWN"
      }
    }
    var result = manufacturer + model
    if (!result.isNullOrBlank()) {
      val regex = "[^a-zA-Z0-9-]".toRegex()
      result = result.removePrefix("_").replace("_", " ").replace(regex, "")
      return result
    }
    return result
  }
}
