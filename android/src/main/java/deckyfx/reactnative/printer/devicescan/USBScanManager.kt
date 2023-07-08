package deckyfx.reactnative.printer.devicescan

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

class USBScanManager(private val usbManager: UsbManager) {
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
    val devices = usbManager.deviceList
    // Iterate over all devices
    val it = devices.keys.iterator()
    while (it.hasNext()) {
      val deviceName = it.next()
      val device = devices[deviceName] ?: return
      onUSBScanListener?.deviceFound(device)
    }
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
