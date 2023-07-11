package deckyfx.reactnative.printer.devicescan

import android.content.Context
import android.hardware.usb.UsbDevice
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager

class USBScanManager(private val context: Context) {
  var onUSBScanListener: OnUSBScanListener? = null
  private var mIsRunning = false

  interface OnUSBScanListener {
    fun deviceFound(usbDevice: UsbDevice)
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
    onUSBScanListener?.startScan()
    val list = UsbPrintersConnectionsManager(context).list
    if (list.isNullOrEmpty()) {
      return
    }
    list.forEach {
      if (it != null) onUSBScanListener?.deviceFound(it.device)
    }
    stopScan()
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    mIsRunning = false
    onUSBScanListener?.stopScan()
  }

  companion object {
    private val LOG_TAG = USBScanManager::class.java.simpleName
  }
}
