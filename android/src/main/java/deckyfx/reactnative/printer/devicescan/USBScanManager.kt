package deckyfx.reactnative.printer.devicescan

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable
import android.util.Log
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager

class USBScanManager(private val context: Context) {
  var onUSBScanListener: OnUSBScanListener? = null
  private var mIsRunning = false
  private var mDetectedDevicesTotal: Int = 0
  private var mDetectedDevicesCount: Int = 0

  interface OnUSBScanListener {
    fun deviceFound(usbDevice: UsbDevice, data: WritableMap)
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
    val manager = UsbPrintersConnectionsManager(context)
    val list = manager.list
    if (list.isNullOrEmpty()) {
      stopScan()
      return
    }
    mDetectedDevicesTotal = list.size
    mDetectedDevicesCount = 0
    manager.requestUSBPermissions(context, usbReceiver)
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    mIsRunning = false
    onUSBScanListener?.stopScan()
    mDetectedDevicesCount = 0
    mDetectedDevicesTotal = 0

  }

  private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (DeviceScanner.ACTION_USB_PERMISSION == action) {
        synchronized(this) {
          if (!mIsRunning) {
            return
          }
          val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager?
          val usbDevice =
            intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
          if (usbDevice != null &&
            usbManager != null &&
            intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)
          ) {
            val connection = UsbConnection(usbManager, usbDevice)
            val eventParams = Arguments.createMap()
            var deviceStatus: WritableMap? = null
            var deviceName: String? = ""
            try {
              connection.connect()
              deviceName = connection.getPrinterModel()
              deviceStatus = connection.getDeviceStatusMap()
              connection.disconnect()
            } catch (error: Exception) {
              error.message?.let {
                Log.d(LOG_TAG, it)
                onUSBScanListener?.error(error)
              }
            }
            eventParams.putString("deviceName", deviceName)
            eventParams.putString("address", usbDevice.deviceName)
            eventParams.putInt("deviceId", usbDevice.deviceId)
            eventParams.putString("manufacturerName", usbDevice.manufacturerName)
            eventParams.putString("serialNumber", usbDevice.serialNumber)
            eventParams.putString("VID", Integer.toHexString(usbDevice.vendorId).uppercase())
            eventParams.putString("PID", Integer.toHexString(usbDevice.productId).uppercase())
            eventParams.putMap("status", deviceStatus)
            onUSBScanListener?.deviceFound(usbDevice, eventParams)
          }
          mDetectedDevicesCount += 1
          if (mDetectedDevicesCount == mDetectedDevicesTotal) {
            stopScan()
          }
        }
      }
    }
  }

  companion object {
    private val LOG_TAG = USBScanManager::class.java.simpleName
  }
}
