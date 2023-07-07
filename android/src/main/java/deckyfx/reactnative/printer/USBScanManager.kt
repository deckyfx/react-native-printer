package deckyfx.reactnative.printer

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.util.Log

class USBScanManager(private val context: Context, private val usbManager: UsbManager) {
    var onUSBScanListener: OnUSBScanListener? = null

    interface OnUSBScanListener {
        fun deviceFound(usbDevice: UsbDevice)
    }

    /**
     * Start Scanning for discoverable devices
     */
    fun startScan() {
        Log.d(LOG_TAG, "Start Scan.")
        if (onUSBScanListener == null) {
            Log.e(LOG_TAG, "You must call registerCallback(...) first!")
        }
        val devices = usbManager.deviceList;
      // Iterate over all devices
      val it = devices.keys.iterator()
      while (it.hasNext()) {
        val deviceName = it.next()
        val device = devices[deviceName] ?: return
        val VID = Integer.toHexString(device.vendorId).uppercase();
        val PID = Integer.toHexString(device.productId).uppercase();
        if (!usbManager.hasPermission(device)) {
          val mPermissionIntent = PendingIntent.getBroadcast(context, 0, Intent("yourPackageName.USB_PERMISSION"), PendingIntent.FLAG_IMMUTABLE);
          usbManager.requestPermission(device, mPermissionIntent);
          return
        }
        onUSBScanListener!!.deviceFound(device)
      }
    }

    /**
     * To Stop Scanning process
     */
    fun stopScan() {
        Log.d(LOG_TAG, "Stop Scan.")
    }

    companion object {
        private val LOG_TAG = USBScanManager::class.java.simpleName
    }
}
