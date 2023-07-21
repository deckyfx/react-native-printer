package deckyfx.reactnative.printer.escposprinter.connection.serial

import android_serialport_api.SerialDevice
import android_serialport_api.SerialPortFinder
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.File

class SerialConnectionsManager {
    private val mSerialPortFinder: SerialPortFinder = SerialPortFinder()

    init {
    }

    val list: Array<SerialPortFinder.SerialDevice>
      get() = mSerialPortFinder.devices

  companion object {
    @Throws(EscPosConnectionException::class)
    fun selectByDeviceName(
      name: String,
      baudRate: Int = 9600
    ): SerialConnection {
      val serial = SerialDevice(File(name), baudRate, 0)
      return SerialConnection(serial)
    }
  }
}
