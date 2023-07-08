package deckyfx.reactnative.printer.devicescan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.net.nsd.NsdManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import deckyfx.reactnative.printer.devicescan.NetworkScanManager.OnNetworkScanListener
import deckyfx.reactnative.printer.devicescan.USBScanManager.OnUSBScanListener
import deckyfx.reactnative.printer.devicescan.BluetoothScanManager.OnBluetoothScanListener
import deckyfx.reactnative.printer.devicescan.ZeroconfScanManager.OnZeroconfScanListener

class DeviceScanner(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
  private val mNetworkScanManager: NetworkScanManager = NetworkScanManager()
  private val mUSBScanManager: USBScanManager
  private val mBluetoothScanManager: BluetoothScanManager
  private val mZeroconfScanManager: ZeroconfScanManager
  private var mListenerCount: Int = 0

  init {
    val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
    mUSBScanManager = USBScanManager(usbManager)

    val bluetoothManager = reactContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
    val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    mBluetoothScanManager = BluetoothScanManager(reactContext, bluetoothAdapter)

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
    constants["SCAN_SERIAL"] = SCAN_SERIAL
    constants["PRINTER_TYPE_NETWORK"] = PRINTER_TYPE_NETWORK
    constants["PRINTER_TYPE_BLUETOOTH"] = PRINTER_TYPE_BLUETOOTH
    constants["PRINTER_TYPE_SERIAL"] = PRINTER_TYPE_SERIAL
    return constants
  }

  /**
   * Name of this module for React Native side
   */
  override fun getName(): String {
    return LOG_TAG
  }

  @ReactMethod
  suspend fun scan(scanType: Int = SCAN_ALL) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    if (scanType == SCAN_NETWORK || scanType == SCAN_ALL) {
      mNetworkScanManager.onNetworkScanListener = object : OnNetworkScanListener {
        override fun deviceFound(ip: String, port: Int, deviceName: String) {
          eventParams.putString("ip", ip)
          eventParams.putInt("port", port)
          eventParams.putString("deviceName", deviceName)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitEventToRNSide(EVENT_START_SCAN, eventParams)
        }
        override fun stopScan() {
          emitEventToRNSide(EVENT_STOP_SCAN, eventParams)
        }
        override fun error(error: Exception) {
          eventParams.putString("message", error.message.toString())
          emitEventToRNSide(EVENT_ERROR, eventParams)
        }
      }
      mNetworkScanManager.startScan()
    }
    if (scanType == SCAN_SERIAL || scanType == SCAN_ALL) {
      mUSBScanManager.onUSBScanListener = object : OnUSBScanListener {
        override fun deviceFound(usbDevice: UsbDevice) {
          eventParams.putString("deviceName", usbDevice.deviceName)
          eventParams.putInt("deviceId", usbDevice.deviceId)
          eventParams.putString("manufacturerName", usbDevice.manufacturerName)
          eventParams.putString("serialNumber", usbDevice.serialNumber)
          eventParams.putString("VID", Integer.toHexString(usbDevice.vendorId).uppercase())
          eventParams.putString("PID", Integer.toHexString(usbDevice.productId).uppercase())
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitEventToRNSide(EVENT_START_SCAN, eventParams)
        }
        override fun stopScan() {
          emitEventToRNSide(EVENT_STOP_SCAN, eventParams)
        }
        override fun error(error: Exception) {
          eventParams.putString("message", error.message.toString())
          emitEventToRNSide(EVENT_ERROR, eventParams)
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
          eventParams.putString("name", bluetoothDevice.name)
          eventParams.putString("macAddress", bluetoothDevice.address)
          eventParams.putString("state", BluetoothScanManager.BluetoothEvent.DEVICE_FOUND.name)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun deviceStateChanged(bluetoothDevice: BluetoothDevice?, state: BluetoothScanManager.BluetoothEvent) {
          if (bluetoothDevice == null) return
          if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            return
          }
          eventParams.putString("name", bluetoothDevice.name)
          eventParams.putString("macAddress", bluetoothDevice.address)
          eventParams.putString("state", state.name)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }
        override fun startScan() {
          emitEventToRNSide(EVENT_START_SCAN, eventParams)
        }
        override fun stopScan() {
          emitEventToRNSide(EVENT_STOP_SCAN, eventParams)
        }
        override fun error(error: Exception) {
          eventParams.putString("message", error.message.toString())
          emitEventToRNSide(EVENT_ERROR, eventParams)
        }
      })
      mBluetoothScanManager.startScan()
    }
    if (scanType == SCAN_ZEROCONF || scanType == SCAN_ALL) {
      mZeroconfScanManager.onZeroconfScanListener = object : OnZeroconfScanListener {
        override fun serviceFound(serviceName: String?) {
          eventParams.putString("event", "serviceFound")
          eventParams.putString("serviceName", serviceName)
          emitEventToRNSide(EVENT_OTHER, eventParams)
        }

        override fun serviceLost(serviceName: String?) {
          eventParams.putString("event", "serviceLost")
          eventParams.putString("serviceName", serviceName)
          emitEventToRNSide(EVENT_OTHER, eventParams)
        }

        override fun serviceResolved(service: WritableMap) {
          eventParams.merge(service)
          emitEventToRNSide(EVENT_DEVICE_FOUND, eventParams)
        }

        override fun startScan() {
          emitEventToRNSide(EVENT_START_SCAN, eventParams)
        }

        override fun stopScan() {
          emitEventToRNSide(EVENT_STOP_SCAN, eventParams)
        }

        override fun error(error: Exception) {
          eventParams.putString("message", error.message.toString())
          emitEventToRNSide(EVENT_ERROR, eventParams)
        }
      }
      mZeroconfScanManager.startScan()
    }
  }

  @ReactMethod
  fun stop(scanType: Int = SCAN_ALL) {
    if (scanType == SCAN_NETWORK || scanType == SCAN_ALL) {
      mNetworkScanManager.stopScan()
      mNetworkScanManager.onNetworkScanListener = null
    }
    if (scanType == SCAN_SERIAL || scanType == SCAN_ALL) {
      mUSBScanManager.stopScan()
      mUSBScanManager.onUSBScanListener = null
    }
    if (scanType == SCAN_BLUETOOTH || scanType == SCAN_ALL) {
      mBluetoothScanManager.stopScan()
      mBluetoothScanManager.unregisterCallback()
    }
    if (scanType == SCAN_ZEROCONF || scanType == SCAN_ALL) {
      mZeroconfScanManager.stopScan()
      mZeroconfScanManager.onZeroconfScanListener = null
    }
  }

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

  private fun emitEventToRNSide(eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  companion object {
    private val LOG_TAG = DeviceScanner::class.java.simpleName
    const val SCAN_ALL = 0
    const val SCAN_NETWORK = 1
    const val SCAN_ZEROCONF = 2
    const val SCAN_BLUETOOTH = 3
    const val SCAN_SERIAL = 4

    const val PRINTER_TYPE_NETWORK = "network"
    const val PRINTER_TYPE_BLUETOOTH = "bluetooth"
    const val PRINTER_TYPE_SERIAL = "serial"

    const val EVENT_START_SCAN = "START_SCAN"
    const val EVENT_STOP_SCAN = "STOP_SCAN"
    const val EVENT_ERROR = "ERROR"
    const val EVENT_DEVICE_FOUND = "DEVICE_FOUND"
    const val EVENT_OTHER = "OTHER"
  }
}
