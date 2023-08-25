package deckyfx.reactnative.printer.escposprinter.connection.serial

import android.util.Log
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.serialport.SerialDevice
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SerialConnection
constructor(
  val path: String,
  var baudRate: Int? = DEFAULT_BAUD_RATE
) : DeviceConnection() {

  constructor(
    path: String,
  ) : this(path, DEFAULT_BAUD_RATE)

  val device: SerialDevice

  init {
    if (baudRate == null) {
      baudRate = DEFAULT_BAUD_RATE
    }
    device = SerialDevice(path, baudRate!!)
  }

  override var outputStream: OutputStream?
    get() {
      return device.outputStream
    }
    set(stream) {
      Log.d("????", "Attempt to set OutputStream")
    }

  override var inputStream: InputStream?
    get() {
      return device.inputStream
    }
    set(stream) {
      Log.d("????", "Attempt to set InputStream")
    }

  override fun connect(): DeviceConnection {
    if (isConnected) {
      return this
    }
    try {
      device.open()
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      device.close()
      throw EscPosConnectionException("Unable to connect to Serial device.")
    }
    return this
  }

  override fun disconnect(): DeviceConnection {
    device.close()
    return this
  }

  /**
   * Send data to the device.
   */
  @Throws(EscPosConnectionException::class)
  override fun send() {
    send(0)
  }

  /**
   * Send data to the device.
   */
  @Throws(EscPosConnectionException::class)
  override fun send(addWaitingTime: Int) {
    try {
      device?.send(data)
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    }
  }

  companion object {
    const val DEFAULT_BAUD_RATE = 115200
  }
}
