package deckyfx.reactnative.printer

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import deckyfx.reactnative.printer.devicescan.DeviceScanner
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.tcp.TcpConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager

class RNPrinter(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var mListenerCount: Int = 0

  override fun getName(): String {
    return LOG_TAG
  }

  companion object {
    private val LOG_TAG = RNPrinter::class.java.simpleName
    val TEST_PRINT_DESIGN =
       // "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
      "[L]\n" +
      "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
      "[L]\n" +
      "[C]================================\n" +
      "[L]\n" +
      "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
      "[L]  + Size : S\n" +
      "[L]\n" +
      "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
      "[L]  + Size : 57/58\n" +
      "[L]\n" +
      "[C]--------------------------------\n" +
      "[R]TOTAL PRICE :[R]34.98e\n" +
      "[R]TAX :[R]4.23e\n" +
      "[L]\n" +
      "[C]================================\n" +
      "[L]\n" +
      "[L]<font size='tall'>Customer :</font>\n" +
      "[L]Raymond DUPONT\n" +
      "[L]5 rue des girafes\n" +
      "[L]31547 PERPETES\n" +
      "[L]Tel : +33801201456\n" +
      "[L]\n" +
      "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
      "[C]<qrcode size='20'>https://dantsu.com/</qrcode>\n" +
      "[L]\n"
  }

  @ReactMethod
  fun checkPermission(scanType: Double, promise: Promise) {
    var inferredScanType  = DeviceScanner.SCAN_ALL
    if (scanType > 0.0) {
      inferredScanType = scanType.toInt()
    }
    if (inferredScanType == DeviceScanner.SCAN_NETWORK || inferredScanType == DeviceScanner.SCAN_ZEROCONF) {
      if (!checkPermission(Manifest.permission.INTERNET)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)) {
        promise.resolve(false)
        return
      }
      promise.resolve(true)
      return
    }
    if (inferredScanType == DeviceScanner.SCAN_USB) {
      promise.resolve(false)
      return
    }
    if (inferredScanType == DeviceScanner.SCAN_BLUETOOTH) {
      if (!checkPermission(Manifest.permission.BLUETOOTH)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
        promise.resolve(false)
        return
      }
      promise.resolve(true)
      return
    }
    if (inferredScanType == DeviceScanner.SCAN_ALL) {
      if (!checkPermission(Manifest.permission.INTERNET)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
        promise.resolve(false)
        return
      }
      promise.resolve(true)
      return
    }
  }

  @ReactMethod
  fun requestPermissions(scanType: Double, promise: Promise) {
    var inferredScanType  = DeviceScanner.SCAN_ALL
    if (scanType > 0.0) {
      inferredScanType = scanType.toInt()
    }
    if (inferredScanType == DeviceScanner.SCAN_NETWORK || inferredScanType == DeviceScanner.SCAN_ZEROCONF) {
      val PERMISSION_ALL = 1
      val PERMISSIONS = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
      )
      ActivityCompat.requestPermissions(reactContext.currentActivity!!, PERMISSIONS, PERMISSION_ALL)
      promise.resolve(true)
      return
    }
    if (inferredScanType == DeviceScanner.SCAN_USB) {
      UsbPrintersConnectionsManager(reactContext).requestUSBPermissions(reactContext, usbReceiver)
      promise.resolve(true)
    }
    if (inferredScanType == DeviceScanner.SCAN_BLUETOOTH) {
      val PERMISSION_ALL = 1
      val PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
      )
      ActivityCompat.requestPermissions(reactContext.currentActivity!!, PERMISSIONS, PERMISSION_ALL)
      promise.resolve(true)
      return
    }
    if (inferredScanType == DeviceScanner.SCAN_ALL) {
      val PERMISSION_ALL = 1
      val PERMISSIONS = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
        Manifest.permission.BLUETOOTH_CONNECT,
        Manifest.permission.BLUETOOTH_SCAN,
      )
      ActivityCompat.requestPermissions(reactContext.currentActivity!!, PERMISSIONS, PERMISSION_ALL)

      val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
      UsbPrintersConnectionsManager(reactContext).list?.forEach {
        if (it != null) {
          val permissionIntent = PendingIntent.getBroadcast(
            reactContext,
            0,
            Intent(DeviceScanner.ACTION_USB_PERMISSION),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
          )
          val filter: IntentFilter = IntentFilter(DeviceScanner.ACTION_USB_PERMISSION)
          reactContext.registerReceiver(usbReceiver, filter)
          usbManager.requestPermission(it.device, permissionIntent)
        }
      }

      promise.resolve(true)
      return
    }
    promise.resolve(false)
  }

  @ReactMethod
  fun getUsbPrintersCount(promise: Promise) {
    val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
    UsbPrintersConnectionsManager(reactContext).list?.size?.let {
      promise.resolve(it)
      return
    }
    promise.resolve(0)
  }

  private fun checkPermission(permission: String): Boolean {
    if (ActivityCompat.checkSelfPermission(reactContext, permission) != PackageManager.PERMISSION_GRANTED) {
      return false
    }
    return true
  }

  @ReactMethod
  fun write(printerType: String, address:String, text: String, promise: Promise) {
    write(printerType, address, 0.0, text, promise)
  }

  @ReactMethod
  fun write(printerType: String, address:String, port:Double, text: String, promise: Promise) {
    val deviceConnection = resolvePrinter(printerType, address, port)
    deviceConnection?.let {
      try {
        val printer = EscPosPrinter(reactContext, it, 203, 48f, 32)
        printer.printFormattedText(text, 0)
        return
      } catch (e: Exception) {
        promise.reject(e)
        return
      }
    }
    promise.resolve(true)
  }

  @ReactMethod
  fun cutPaper(printerType: String, address:String) {}

  @ReactMethod
  fun cutPaper(printerType: String, address:String, port:Double) {}

  @ReactMethod
  fun feedPaper(printerType: String, address:String) {}

  @ReactMethod
  fun feedPaper(printerType: String, address:String, port:Double) {}

  @ReactMethod
  fun openCashBox(printerType: String, address:String) {}

  @ReactMethod
  fun openCashBox(printerType: String, address:String, port:Double) {}

  @ReactMethod
  fun testConnection(printerType: String, address:String) {}

  @ReactMethod
  fun testConnection(printerType: String, address:String, port: Double = 9100.0) {
  }

  @ReactMethod
  fun getPrinterModel(printerType: String, address:String, promise: Promise) {
    getPrinterModel(printerType, address, 0.0, promise)
  }

  @ReactMethod
  fun getPrinterModel(printerType: String, address:String, port: Double = 9100.0, promise: Promise) {
    val deviceConnection = resolvePrinter(printerType, address, port)
    deviceConnection?.let {
      try {
        val printer = EscPosPrinter(reactContext, it, 203, 48f, 32)
        val model = printer.getPrinterModel()
        promise.resolve(model)
        return
      } catch (e: Exception) {
        promise.reject(e)
        return
      }
    }
    promise.resolve("")
  }

  @ReactMethod
  fun testPrint(printerType: String, address:String, promise: Promise) {
    testPrint(printerType, address, 0.0, promise)
  }

  @ReactMethod
  fun testPrint(printerType: String, address:String, port: Double = 9100.0, promise: Promise) {
    val deviceConnection = resolvePrinter(printerType, address, port)
    deviceConnection?.let {
      try {
        val printer = EscPosPrinter(reactContext, it, 203, 48f, 32)
        printer.printFormattedTextAndCut(TEST_PRINT_DESIGN, 0)
        return
      } catch (e: Exception) {
        promise.reject(e)
        return
      }
    }
    promise.resolve("")
  }

  // Example method
  // See https://reactnative.dev/docs/native-modules-android
  @ReactMethod
  fun multiply(a: Double, b: Double, promise: Promise) {
    promise.resolve(a * b * 0)
  }

  private fun resolvePrinter(printerType: String, address:String, port: Double? = null): DeviceConnection? {
    when (printerType) {
      DeviceScanner.PRINTER_TYPE_NETWORK -> {
        if (port == null) {
          return null
        }
        return TcpConnection(address, port.toInt())
      }
      DeviceScanner.PRINTER_TYPE_BLUETOOTH -> {
        return BluetoothPrintersConnectionsManager.selectByDeviceAddress(reactContext, address)
      }
      DeviceScanner.PRINTER_TYPE_USB -> {
        return UsbPrintersConnectionsManager.selectByDeviceName(reactContext, address)
      }
    }
    return null
  }

  private fun emitEventToRNSide(eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private fun emitScanOtherEvent(scanType: Int, event: String, serviceName: String?) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    eventParams.putString("event", event)
    eventParams.putString("serviceName", serviceName)
    emitEventToRNSide(DeviceScanner.EVENT_OTHER, eventParams)
  }

  private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (DeviceScanner.ACTION_USB_PERMISSION == action) {
        synchronized(this) {
          val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager?
          val usbDevice = intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            emitScanOtherEvent(DeviceScanner.SCAN_USB, "permissionGranted", usbDevice?.deviceName)
          } else {
            emitScanOtherEvent(DeviceScanner.SCAN_USB, "permissionDenied", usbDevice?.deviceName)
          }
        }
      }
    }
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
}
