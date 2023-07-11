package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.content.Context
import android.hardware.usb.UsbConstants
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbDeviceHelper.findPrinterInterface

class UsbPrintersConnectionsManager
/**
 * Create a new instance of UsbPrintersConnections
 *
 * @param context Application context
 */
  (context: Context?) : UsbConnectionsManager(context!!) {
  /**
   * Get a list of USB printers.
   *
   * @return an array of UsbConnection
   */
  override val list: Array<UsbConnection?>?
    get() {
      val usbConnections = super.list ?: return null
      var i = 0
      val printersTmp = arrayOfNulls<UsbConnection>(usbConnections.size)
      for (usbConnection in usbConnections) {
        val device = usbConnection!!.device
        var usbClass = device.deviceClass
        if ((usbClass == UsbConstants.USB_CLASS_PER_INTERFACE || usbClass == UsbConstants.USB_CLASS_MISC) && findPrinterInterface(
            device
          ) != null
        ) {
          usbClass = UsbConstants.USB_CLASS_PRINTER
        }
        if (usbClass == UsbConstants.USB_CLASS_PRINTER) {
          printersTmp[i++] = UsbConnection(usbManager!!, device)
        }
      }
      val usbPrinters = arrayOfNulls<UsbConnection>(i)
      System.arraycopy(printersTmp, 0, usbPrinters, 0, i)
      return usbPrinters
    }

  companion object {
    /**
     * Easy way to get the first USB printer paired / connected.
     *
     * @param context an Context
     *
     * @return a UsbConnection instance
     */
    fun selectFirstConnected(context: Context): UsbConnection? {
      val printers = UsbPrintersConnectionsManager(context)
      val bluetoothPrinters = printers.list
      return if (bluetoothPrinters.isNullOrEmpty()) {
        null
      } else bluetoothPrinters[0]
    }

    /**
     * Resolve USB printer paired / connected. with name
     *
     * @param context an Context
     * @param name Printer name to connect
     *
     * @return a UsbConnection instance
     */
    fun selectByDeviceName(context: Context, name: String): UsbConnection? {
      val printers = UsbPrintersConnectionsManager(context)
      val bluetoothPrinters = printers.list
      return if (bluetoothPrinters.isNullOrEmpty()) {
        null
      } else bluetoothPrinters.find {
        it?.device?.deviceName == name
      }
    }
  }
}
