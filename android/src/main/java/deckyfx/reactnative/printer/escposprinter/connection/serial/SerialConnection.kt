package deckyfx.reactnative.printer.escposprinter.connection.serial

import android_serialport_api.SerialDevice
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SerialConnection
  constructor(
    private var device: SerialDevice?,
  ) : DeviceConnection() {

  constructor(
    file: File,
    baudRate: Int = 9600,
    flag: Int = 0
  ) : this(SerialDevice(file, baudRate, flag))

  constructor(
    path: String,
    baudRate: Int = 9600,
    flag: Int = 0
  ) : this(SerialDevice(File(path), baudRate, flag))

  override var outputStream: OutputStream?
    get() {
      return device?.outputStream
    }
    set(stream) {
    }

  override var inputStream: InputStream?
    get() {
      return device?.inputStream
    }
    set(stream) {
    }

  init {
  }

  override fun connect(): DeviceConnection {
    if (isConnected) {
      return this
    }
    try {
      if (device == null) {
        throw EscPosConnectionException("Unable to connect to Serial device.")
      }
      device?.openDevice()
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      device?.closeDevice()
      throw EscPosConnectionException("Unable to connect to Serial device.")
    }
    return this
  }

  override fun disconnect(): DeviceConnection {
    if (device != null) {
      device!!.closeDevice()
      device = null
    }
    return this
  }

  val connected: Boolean
    get() {
      return device?.outputStream != null && device?.inputStream != null
    }

  /**
   * Send data to the device.
   */
  @Throws(EscPosConnectionException::class)
  override fun send(addWaitingTime: Int) {
    try {
      device?.outputStream?.write(data)
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    }
  }
}
