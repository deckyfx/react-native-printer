package deckyfx.reactnative.printer.devicescan

import android.Manifest
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.net.nsd.NsdManager
import android.util.Log
import android_serialport_api.SerialPortFinder
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import deckyfx.reactnative.printer.devicescan.BluetoothScanManager.OnBluetoothScanListener
import deckyfx.reactnative.printer.devicescan.NetworkScanManager.OnNetworkScanListener
import deckyfx.reactnative.printer.devicescan.USBScanManager.OnUSBScanListener
import deckyfx.reactnative.printer.devicescan.ZeroconfScanManager.OnZeroconfScanListener
import deckyfx.reactnative.printer.devicescan.SerialScanManager.OnSerialScanListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class DeviceScanner(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val mNetworkScanManager: NetworkScanManager = NetworkScanManager()
  private val mUSBScanManager: USBScanManager = USBScanManager(reactContext)
  private val mBluetoothScanManager: BluetoothScanManager = BluetoothScanManager(reactContext)
  private val mZeroconfScanManager: ZeroconfScanManager
  private val mSerialScanManager: SerialScanManager = SerialScanManager(reactContext)
  private var mListenerCount: Int = 0

  init {
    val nsdManager = reactContext.getSystemService(Context.NSD_SERVICE) as NsdManager
    mZeroconfScanManager = ZeroconfScanManager(reactContext, nsdManager)
  }

  /**
   * Exposes constants to React Native side
   */
  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants["SCAN_ALL"] = SCAN_ALL
    constants["SCAN_NETWORK"] = SCAN_NETWORK
    constants["SCAN_ZEROCONF"] = SCAN_ZEROCONF
    constants["SCAN_BLUETOOTH"] = SCAN_BLUETOOTH
    constants["SCAN_USB"] = SCAN_USB
    constants["SCAN_SERIAL"] = SCAN_SERIAL

    constants["EVENT_START_SCAN"] = EVENT_START_SCAN
    constants["EVENT_STOP_SCAN"] = EVENT_STOP_SCAN
    constants["EVENT_ERROR"] = EVENT_ERROR
    constants["EVENT_DEVICE_FOUND"] = EVENT_DEVICE_FOUND
    constants["EVENT_OTHER"] = EVENT_OTHER
    return constants
  }

  /**
   * Name of this module for React Native side
   */
  override fun getName(): String {
    return LOG_TAG
  }

  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b * 0)
  }

  // Exposed react-methods cannot receive Int arguments, we must receive it as Double
  @ReactMethod
  fun scan(scanType: Double, promise: Promise) {
    var inferredScanType  = SCAN_ALL
    if (scanType > 0.0) {
      inferredScanType = scanType.toInt()
    }
    val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    scope.launch {
      try {
        doScan(inferredScanType)
        promise.resolve(true)
      } catch (e: Exception) {
        stop(scanType)
        promise.reject(e)
      }
    }
  }

  // Exposed react-methods cannot receive Int arguments, we must receive it as Double
  @ReactMethod
  fun stop(scanType: Double) {
    var inferredScanType  = SCAN_ALL
    if (scanType > 0.0) {
      inferredScanType = scanType.toInt()
    }
    if (inferredScanType == SCAN_NETWORK || inferredScanType == SCAN_ALL) {
      mNetworkScanManager.stopScan()
      mNetworkScanManager.onNetworkScanListener = null
    }
    if (inferredScanType == SCAN_USB || inferredScanType == SCAN_ALL) {
      mUSBScanManager.stopScan()
      mUSBScanManager.onUSBScanListener = null
    }
    if (inferredScanType == SCAN_BLUETOOTH || inferredScanType == SCAN_ALL) {
      mBluetoothScanManager.stopScan()
      mBluetoothScanManager.unregisterCallback()
    }
    if (inferredScanType == SCAN_ZEROCONF || inferredScanType == SCAN_ALL) {
      mZeroconfScanManager.stopScan()
      mZeroconfScanManager.onZeroconfScanListener = null
    }
    if (inferredScanType == SCAN_SERIAL || inferredScanType == SCAN_ALL) {
      mSerialScanManager.stopScan()
      mSerialScanManager.onSerialScanListener = null
    }
  }

  private fun emitEventToRNSide(eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private suspend fun doScan(scanType: Int = SCAN_ALL) {
    if (scanType == SCAN_NETWORK || scanType == SCAN_ALL) {
      mNetworkScanManager.onNetworkScanListener = object : OnNetworkScanListener {
        override fun deviceFound(ip: String, port: Int, deviceName: String) {
          val eventParams = Arguments.createMap().apply {
            putInt("scanType", scanType)
          }
          eventParams.putString("ip", ip)
          eventParams.putString("address", ip)
          eventParams.putInt("port", port)
          eventParams.putString("deviceName", deviceName)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitStartScan(scanType)
        }
        override fun stopScan() {
          emitStopScan(scanType)
        }
        override fun error(error: Exception) {
          emitErrorScan(scanType, error)
        }
      }
      mNetworkScanManager.startScan()
    }
    if (scanType == SCAN_USB || scanType == SCAN_ALL) {
      mUSBScanManager.onUSBScanListener = object : OnUSBScanListener {
        override fun deviceFound(usbDevice: UsbDevice,  data: WritableMap) {
          val eventParams = Arguments.createMap().apply {
            putInt("scanType", scanType)
          }
          eventParams.merge(data)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitStartScan(scanType)
        }
        override fun stopScan() {
          emitStopScan(scanType)
        }
        override fun error(error: Exception) {
          emitErrorScan(scanType, error)
        }
      }
      mUSBScanManager.startScan()
    }
    if (scanType == SCAN_BLUETOOTH || scanType == SCAN_ALL) {
      mBluetoothScanManager.registerCallback(object : OnBluetoothScanListener {
        override fun deviceFound(bluetoothDevice: BluetoothDevice?) {
          if (bluetoothDevice == null) return
          if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
          }
          val eventParams = Arguments.createMap().apply {
            putInt("scanType", scanType)
          }
          eventParams.putString("name", bluetoothDevice.name)
          eventParams.putString("address", bluetoothDevice.address)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitStartScan(scanType)
        }
        override fun stopScan() {
          emitStopScan(scanType)
        }
        override fun error(error: Exception) {
          emitErrorScan(scanType, error)
        }
      })
      mBluetoothScanManager.startScan()
    }
    if (scanType == SCAN_ZEROCONF || scanType == SCAN_ALL) {
      mZeroconfScanManager.onZeroconfScanListener = object : OnZeroconfScanListener {
        override fun serviceFound(serviceName: String?) {
          emitScanOtherEvent(scanType, "serviceLost", serviceName)
        }
        override fun serviceLost(serviceName: String?) {
          emitScanOtherEvent(scanType, "serviceLost", serviceName)
        }
        override fun serviceResolved(service: WritableMap) {
          emitScanOtherEvent(scanType, "serviceResolved", service)
        }
        override fun startScan() {
          emitStartScan(scanType)
        }
        override fun stopScan() {
          emitStopScan(scanType)
        }
        override fun error(error: Exception) {
          emitErrorScan(scanType, error)
        }
      }
      mZeroconfScanManager.startScan()
    }
    if (scanType == SCAN_SERIAL || scanType == SCAN_ALL) {
      mSerialScanManager.onSerialScanListener = object : OnSerialScanListener {
        override fun deviceFound(serialDevice: SerialPortFinder.SerialDevice, data: WritableMap) {
          val eventParams = Arguments.createMap().apply {
            putInt("scanType", scanType)
          }
          eventParams.merge(data)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitStartScan(scanType)
        }
        override fun stopScan() {
          emitStopScan(scanType)
        }
        override fun error(error: Exception) {
          emitErrorScan(scanType, error)
        }
      }
      mSerialScanManager.startScan()
    }
  }

  private fun emitStartScan(scanType: Int) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    emitEventToRNSide(EVENT_START_SCAN, eventParams)
  }

  private fun emitStopScan(scanType: Int) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    emitEventToRNSide(EVENT_STOP_SCAN, eventParams)
  }

  private fun emitErrorScan(scanType: Int, error: Exception) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    eventParams.putString("message", error.message.toString())
    emitEventToRNSide(EVENT_ERROR, eventParams)
  }

  private fun emitScanOtherEvent(scanType: Int, event: String, service: WritableMap) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    eventParams.putString("event", event)
    eventParams.merge(service)
    emitEventToRNSide(EVENT_OTHER, eventParams)
  }

  private fun emitScanOtherEvent(scanType: Int, event: String, serviceName: String?) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    eventParams.putString("event", event)
    eventParams.putString("serviceName", serviceName)
    emitEventToRNSide(EVENT_OTHER, eventParams)
  }

  // Required for rn built in EventEmitter Calls.
  @ReactMethod
  fun addListener(eventName: String) {
    if (mListenerCount == 0) {
      Log.d(LOG_TAG, "Does Nothing")
    }
    mListenerCount += 1
  }

  @ReactMethod
  fun removeListeners(count: Int) {
    if (mListenerCount > 0 ) mListenerCount -= count
    if (mListenerCount == 0) {
      Log.d(LOG_TAG, "Does Nothing")
    }
  }

  companion object {
    private val LOG_TAG = DeviceScanner::class.java.simpleName
    const val SCAN_ALL = 0
    const val SCAN_NETWORK = 1
    const val SCAN_ZEROCONF = 2
    const val SCAN_BLUETOOTH = 3
    const val SCAN_USB = 4
    const val SCAN_SERIAL = 5

    const val EVENT_START_SCAN = "START_SCAN"
    const val EVENT_STOP_SCAN = "STOP_SCAN"
    const val EVENT_ERROR = "ERROR"
    const val EVENT_DEVICE_FOUND = "DEVICE_FOUND"
    const val EVENT_OTHER = "OTHER"

    const val ACTION_USB_PERMISSION = "deckyfx.reactnative.printer.devicescan.USB_PERMISSION"
  }
}
