package deckyfx.reactnative.printer.escposprinter.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothClass
import android.content.Context
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException

class BluetoothPrintersConnectionsManager(private val context: Context) :
  BluetoothConnections(context) {
  /**
   * Get a list of bluetooth printers.
   *
   * @return an array of EscPosPrinterCommands
   */
  @get:SuppressLint("MissingPermission")
  override val list: Array<BluetoothConnection?>?
    get() {
      val bluetoothDevicesList = super.list ?: return null
      var i = 0
      val printersTmp = arrayOfNulls<BluetoothConnection>(bluetoothDevicesList.size)
      for (bluetoothConnection in bluetoothDevicesList) {
        val device = bluetoothConnection!!.device
        val majDeviceCl = device.bluetoothClass.majorDeviceClass
        val deviceCl = device.bluetoothClass.deviceClass
        if (majDeviceCl == BluetoothClass.Device.Major.IMAGING && (deviceCl == 1664 || deviceCl == BluetoothClass.Device.Major.IMAGING)) {
          printersTmp[i++] = BluetoothConnection(context, device)
        }
      }
      val bluetoothPrinters = arrayOfNulls<BluetoothConnection>(i)
      System.arraycopy(printersTmp, 0, bluetoothPrinters, 0, i)
      return bluetoothPrinters
    }

  companion object {
    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @param context Application context
     *
     * @return an EscPosPrinterCommands instance
     */
    fun selectFirstPaired(context: Context): BluetoothConnection? {
      val printers = BluetoothPrintersConnectionsManager(context)
      val bluetoothPrinters = printers.list
      if (!bluetoothPrinters.isNullOrEmpty()) {
        for (printer in bluetoothPrinters) {
          try {
            return printer!!.connect()
          } catch (e: EscPosConnectionException) {
            e.printStackTrace()
          }
        }
      }
      return null
    }

    /**
     * Easy way to get the first bluetooth printer paired / connected.
     *
     * @param context Application context
     * @param address Bluetooth device mac address to connect
     *
     * @return an EscPosPrinterCommands instance
     */
    fun selectByDeviceAddress(context: Context, address: String): BluetoothConnection? {
      val printers = BluetoothPrintersConnectionsManager(context)
      return printers.list?.find {
        it?.device?.address == address
      }?.let {
        try {
          it.connect()
          return it
        } catch (e: EscPosConnectionException) {
          e.printStackTrace()
          return null
        }
      }
    }
  }
}
