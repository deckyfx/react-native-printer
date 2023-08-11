package deckyfx.reactnative.printer.devicescan

import android.content.Context
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import deckyfx.reactnative.printer.serialport.SerialPortFinder
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.connection.serial.SerialConnection
import deckyfx.reactnative.printer.escposprinter.connection.serial.SerialConnectionsManager

class SerialScanManager(private val context: Context) {
  var onSerialScanListener: OnSerialScanListener? = null
  private var mIsRunning = false
  private var mDetectedDevicesTotal: Int = 0
  private var mDetectedDevicesCount: Int = 0

  interface OnSerialScanListener {
    fun deviceFound(device: SerialPortFinder.SerialDeviceFound, data: WritableMap)
    fun startScan()
    fun stopScan()
    fun error(error: Exception)
  }

  /**
   * Start Scanning for discoverable devices
   */
  fun startScan() {
    if (mIsRunning) return
    mIsRunning = true
    onSerialScanListener?.startScan()
    val list: Array<SerialPortFinder.SerialDeviceFound>
    try {
      val manager = SerialConnectionsManager()
      list = manager.list
    } catch(e: Exception) {
      onSerialScanListener?.error(e)
      stopScan()
      return
    }
    if (list.isEmpty()) {
      stopScan()
      return
    }
    mDetectedDevicesTotal = list.size
    mDetectedDevicesCount = 0
    list.forEach {
      val eventParams = Arguments.createMap()
      var deviceStatus: String? = null
      val connection = SerialConnection(it.device.absolutePath)
      try {
        connection.connect()
        if (!connection.device.isOpen) {
          return
        }
        connection.device.sendTxt("asasasa")
        val printer = EscPosPrinter(context, connection, 210, 60f, 42)
        printer.printFormattedText("[L]${it.device.absolutePath}\n")
        connection.disconnect()
      } catch (error: Exception) {
        error.message?.let { message ->
          Log.d(LOG_TAG, message)
          onSerialScanListener?.error(error)
        }
        return
      }
      eventParams.putString("address", it.device.absolutePath)
      eventParams.putString("path", it.device.absolutePath)
      eventParams.putString("deviceName", it.device.name)
      eventParams.putString("driverName", it.driver.name)
      eventParams.putString("deviceRoot", it.driver.deviceRoot)
      onSerialScanListener?.deviceFound(it, eventParams)
    }
    stopScan()
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    mIsRunning = false
    onSerialScanListener?.stopScan()
    mDetectedDevicesCount = 0
    mDetectedDevicesTotal = 0
  }

  companion object {
    private val LOG_TAG = SerialScanManager::class.java.simpleName
  }
}
