package deckyfx.reactnative.printer.escposprinter.connection.serial

import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import deckyfx.reactnative.printer.serialport.SerialPortFinder

class SerialConnectionsManager {
    private val mSerialPortFinder: SerialPortFinder = SerialPortFinder()

    init {
    }

    val list: Array<SerialPortFinder.SerialDeviceFound>
      get() = mSerialPortFinder.devices

  companion object {
    @Throws(EscPosConnectionException::class)
    fun selectByDeviceName(
      name: String,
      baudRate: Int? = SerialConnection.DEFAULT_BAUD_RATE
    ): SerialConnection {
      return SerialConnection(name, baudRate)
    }
  }
}
