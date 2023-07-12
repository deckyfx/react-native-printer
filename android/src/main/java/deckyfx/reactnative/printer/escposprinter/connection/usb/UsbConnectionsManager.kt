package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import deckyfx.reactnative.printer.devicescan.DeviceScanner

open class UsbConnectionsManager(context: Context) {
    @JvmField
    protected var usbManager: UsbManager?

    /**
     * Create a new instance of UsbConnectionsManager
     *
     * @param context Application context
     */
    init {
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    /**
     * Get a list of USB devices available.
     * @return Return an array of UsbConnection instance
     */
    open val list: Array<UsbConnection?>?
        get() {
            if (usbManager == null) {
                return null
            }
            val devicesList: Collection<UsbDevice> = usbManager!!.deviceList.values
            val usbDevices = arrayOfNulls<UsbConnection>(devicesList.size)
            if (devicesList.isNotEmpty()) {
                var i = 0
                for (device in devicesList) {
                    usbDevices[i++] = UsbConnection(usbManager!!, device)
                }
            }
            return usbDevices
        }


  open fun requestUSBPermissions(context: Context, receiver: BroadcastReceiver?) {
    if (usbManager == null) {
      return
    }
    list?.forEach {
      if (it != null) {
        val permissionIntent = PendingIntent.getBroadcast(
          context,
          0,
          Intent(DeviceScanner.ACTION_USB_PERMISSION),
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
        )
        val filter = IntentFilter(DeviceScanner.ACTION_USB_PERMISSION)
        context.registerReceiver(receiver, filter)
        usbManager?.requestPermission(it.device, permissionIntent)
      }
    }
  }
}
