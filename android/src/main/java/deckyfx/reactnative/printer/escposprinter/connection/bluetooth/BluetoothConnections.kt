package deckyfx.reactnative.printer.escposprinter.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context

open class BluetoothConnections(private val context: Context) {
  /**
   * Get a list of bluetooth device.
   *
   * @return an array of EscPosPrinterCommands
   */
  private val bluetoothAdapter: BluetoothAdapter?
    get() {
      val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
      return bluetoothManager.adapter
    }

  init {
  }

  /**
   * Get a list of bluetooth devices available.
   * @return Return an array of BluetoothConnection instance
   */
  @get:SuppressLint("MissingPermission")
  open val list: Array<BluetoothConnection?>?
    get() {
      if (bluetoothAdapter == null) {
        return null
      }
      if (!bluetoothAdapter!!.isEnabled) {
        return null
      }
      val bluetoothDevicesList = bluetoothAdapter!!.bondedDevices
      val bluetoothDevices = arrayOfNulls<BluetoothConnection>(bluetoothDevicesList.size)
      if (bluetoothDevicesList.size > 0) {
        var i = 0
        for (device in bluetoothDevicesList) {
          bluetoothDevices[i++] = BluetoothConnection(context, device)
        }
      }
      return bluetoothDevices
    }
}
