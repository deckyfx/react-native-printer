package deckyfx.reactnative.printer.escposprinter.connection.serial

import android_serialport_api.SerialPort
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SerialConnection
constructor(
  val path: String,
  var baudRate: Int? = DEFAULT_BAUD_RATE
) : DeviceConnection() {

  private var serial: SerialPort? = null

  override var outputStream: OutputStream? = null
    get() = serial?.outputStream

  override var inputStream: InputStream? = null
    get() = serial?.inputStream

  constructor(
    path: String,
  ) : this(path, DEFAULT_BAUD_RATE)

  init {
    if (baudRate == null) {
      baudRate = DEFAULT_BAUD_RATE
    }
    serial = SerialPort(
      File(path),
      baudRate!!,
      0,
    )
  }

  override fun connect(): DeviceConnection {
    if (isConnected) {
      return this
    }
    try {
      serial?.open()
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      serial?.close()
      throw EscPosConnectionException("Unable to connect to Serial device.")
    }
    return this
  }

  override fun disconnect(): DeviceConnection {
    serial?.close()
    return this
  }

  companion object {
    const val DEFAULT_BAUD_RATE = 9600
  }
}
