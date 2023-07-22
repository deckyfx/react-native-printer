package deckyfx.reactnative.printer.escposprinter.connection.serial

import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.serialport.SerialPort
import deckyfx.reactnative.printer.serialport.SerialPortFinder
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
      val serial = SerialPort(File(name), baudRate, 0)
      return SerialConnection(serial)
    }
  }
}
