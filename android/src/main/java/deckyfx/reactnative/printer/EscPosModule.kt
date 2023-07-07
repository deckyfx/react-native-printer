package deckyfx.reactnative.printer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Parcelable
import androidx.core.app.ActivityCompat
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter
import io.github.escposjava.print.NetworkPrinter
import io.github.escposjava.print.Printer
import io.github.escposjava.print.SerialPrinter
import io.github.escposjava.print.exceptions.BarcodeSizeError
import io.github.escposjava.print.exceptions.QRCodeException
import java.io.IOException
import java.io.UnsupportedEncodingException

import deckyfx.reactnative.printer.BluetoothScanManager.OnBluetoothScanListener
import deckyfx.reactnative.printer.USBScanManager.OnUSBScanListener
import deckyfx.reactnative.printer.NetworkScanManager.OnNetworkScanListener

class EscPosModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var printerService: PrinterService? = null
    private var config: ReadableMap? = null
    private val bluetoothScanManager: BluetoothScanManager
    private val usbScanManager: USBScanManager
    private val networkScanManager: NetworkScanManager


    internal enum class BluetoothEvent {
        CONNECTED, DISCONNECTED, DEVICE_FOUND, NONE
    }

    override fun getConstants(): Map<String, Any>? {
        val constants: MutableMap<String, Any> = HashMap()
        constants[PRINTING_SIZE_58_MM] = PRINTING_SIZE_58_MM
        constants[PRINTING_SIZE_76_MM] = PRINTING_SIZE_76_MM
        constants[PRINTING_SIZE_80_MM] = PRINTING_SIZE_80_MM
        constants[BLUETOOTH_CONNECTED] = BluetoothEvent.CONNECTED.name
        constants[BLUETOOTH_DISCONNECTED] = BluetoothEvent.DISCONNECTED.name
        constants[BLUETOOTH_DEVICE_FOUND] = BluetoothEvent.DEVICE_FOUND.name
        return constants
    }

    override fun getName(): String {
        return "EscPos"
    }

    @ReactMethod
    fun cutPart(promise: Promise) {
        printerService!!.cutPart()
        promise.resolve(true)
    }

    @ReactMethod
    fun cutFull(promise: Promise) {
        printerService!!.cutFull()
        promise.resolve(true)
    }

    @ReactMethod
    fun lineBreak(promise: Promise) {
        printerService!!.lineBreak()
        promise.resolve(true)
    }

    @ReactMethod
    fun print(text: String?, promise: Promise) {
        try {
            text?.let { _text ->
              printerService!!.print(_text)
            }
            promise.resolve(true)
        } catch (e: UnsupportedEncodingException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printLn(text: String?, promise: Promise) {
        try {
            text?.let { _text ->
              printerService!!.printLn(_text)
            }
            promise.resolve(true)
        } catch (e: UnsupportedEncodingException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printBarcode(code: String?, bc: String?, width: Int, height: Int, pos: String?, font: String?, promise: Promise) {
        try {
            printerService!!.printBarcode(code, bc, width, height, pos, font)
            promise.resolve(true)
        } catch (e: BarcodeSizeError) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printBarcode(str: String?, nType: Int, nWidthX: Int, nHeight: Int, nHriFontType: Int, nHriFontPosition: Int, promise: Promise) {
        try {
            printerService!!.printBarcode(str, nType, nWidthX, nHeight, nHriFontType, nHriFontPosition)
            promise.resolve(true)
        } catch (e: Exception) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printDesign(text: String?, promise: Promise) {
        try {
            text?.let {_text ->
              printerService!!.printDesign(_text)
            }
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printImage(filePath: String?, promise: Promise) {
        try {
            printerService!!.printImage(filePath)
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printImageWithOffset(filePath: String?, widthOffet: Int, promise: Promise) {
        try {
            printerService!!.printImage(filePath, widthOffet)
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun printQRCode(value: String?, size: Int, promise: Promise) {
        try {
            value?.let { _value ->
              printerService!!.printQRCode(_value, size)
            }
            promise.resolve(true)
        } catch (e: QRCodeException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun setFontType(type: Int, promise: Promise) {
        printerService!!.setFontType(type)
        promise.resolve(true)
    }

    @ReactMethod
    fun printSample(promise: Promise) {
        try {
            printerService!!.printSample()
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun write(command: ByteArray?, promise: Promise) {
        printerService!!.write(command)
        promise.resolve(true)
    }

    @ReactMethod
    fun write(command: String?, promise: Promise) {
        command?.let { _command ->
          printerService!!.write(_command)
        }
        promise.resolve(true)
    }

    @ReactMethod
    fun setCharCode(code: String?) {
        printerService!!.setCharCode(code)
    }

    @ReactMethod
    fun setTextDensity(density: Int) {
        printerService!!.setTextDensity(density)
    }

    @ReactMethod
    fun setPrintingSize(printingSize: String?) {
        val charsOnLine: Int
        val printingWidth: Int
        when (printingSize) {
            PRINTING_SIZE_80_MM -> {
                charsOnLine = LayoutBuilder.CHARS_ON_LINE_80_MM
                printingWidth = PrinterService.PRINTING_WIDTH_80_MM
            }

            PRINTING_SIZE_76_MM -> {
                charsOnLine = LayoutBuilder.CHARS_ON_LINE_76_MM
                printingWidth = PrinterService.PRINTING_WIDTH_76_MM
            }

            PRINTING_SIZE_58_MM -> {
                charsOnLine = LayoutBuilder.CHARS_ON_LINE_58_MM
                printingWidth = PrinterService.PRINTING_WIDTH_58_MM
            }

            else -> {
                charsOnLine = LayoutBuilder.CHARS_ON_LINE_58_MM
                printingWidth = PrinterService.PRINTING_WIDTH_58_MM
            }
        }
        printerService!!.setCharsOnLine(charsOnLine)
        printerService!!.setPrintingWidth(printingWidth)
    }

    @ReactMethod
    fun beep(promise: Promise) {
        printerService!!.beep()
        promise.resolve(true)
    }

    @ReactMethod
    fun setConfig(config: ReadableMap?) {
        this.config = config
    }

    @ReactMethod
    fun kickCashDrawerPin2(promise: Promise) {
        printerService!!.kickCashDrawerPin2()
        promise.resolve(true)
    }

    @ReactMethod
    fun kickCashDrawerPin5(promise: Promise) {
        printerService!!.kickCashDrawerPin5()
        promise.resolve(true)
    }

    @ReactMethod
    fun connectBluetoothPrinter(address: String?, promise: Promise) {
        try {
            if ("bluetooth" != config!!.getString("type")) {
                promise.reject("config.type is not a bluetooth type")
            }
            val printer: Printer = BluetoothPrinter(address!!, reactContext)
            printerService = PrinterService(printer)
            printerService!!.setContext(reactContext)
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun connectNetworkPrinter(address: String?, port: Int, promise: Promise) {
        try {
            if ("network" != config!!.getString("type")) {
                promise.reject("config.type is not a network type")
            }
            val printer: Printer = NetworkPrinter(address, port)
            printerService = PrinterService(printer)
            printerService!!.setContext(reactContext)
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun connectSerialPrinter(address: String?, promise: Promise) {
    try {
      if ("serial" != config!!.getString("type")) {
        promise.reject("config.type is not a network type")
      }
      val printer: Printer = SerialPrinter(address, 9600)
      printerService = PrinterService(printer)
      printerService!!.setContext(reactContext)
      promise.resolve(true)
    } catch (e: IOException) {
      promise.reject(e)
    }
  }

    @ReactMethod
    fun disconnect(promise: Promise) {
        try {
            printerService!!.close()
            promise.resolve(true)
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun scanBluetoothDevices() {
        bluetoothScanManager.registerCallback(object : OnBluetoothScanListener {
            override fun deviceFound(bluetoothDevice: BluetoothDevice?) {
              val deviceInfoParams = Arguments.createMap()
              if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                return
              }
              deviceInfoParams.putString("name", bluetoothDevice!!.name)
                deviceInfoParams.putString("macAddress", bluetoothDevice.address)

                // put deviceInfoParams into callbackParams
                val callbackParams = Arguments.createMap()
                callbackParams.putMap("deviceInfo", deviceInfoParams)
                callbackParams.putString("state", BluetoothEvent.DEVICE_FOUND.name)

                // emit callback to RN code
                reactContext.getJSModule(RCTDeviceEventEmitter::class.java)
                        .emit("bluetoothDeviceFound", callbackParams)
            }
        })
        bluetoothScanManager.startScan()
    }

    @ReactMethod
    fun stopBluetoothScan() {
        bluetoothScanManager.stopScan()
    }

    @ReactMethod
    fun scanUSBDevices() {
      usbScanManager.onUSBScanListener = object : OnUSBScanListener {
        override fun deviceFound(usbDevice: UsbDevice) {
          val deviceInfoParams = Arguments.createMap()
          deviceInfoParams.putString("name", usbDevice.deviceName)
          deviceInfoParams.putString("macAddress", "")

          // put deviceInfoParams into callbackParams
          val callbackParams = Arguments.createMap()
          callbackParams.putMap("deviceInfo", deviceInfoParams)
          callbackParams.putString("state", BluetoothEvent.DEVICE_FOUND.name)

          // emit callback to RN code
          reactContext.getJSModule(RCTDeviceEventEmitter::class.java)
            .emit("usbDeviceFound", callbackParams)
        }
      }
      usbScanManager.startScan()
    }

    @ReactMethod
    fun stopUSBScan() {
      usbScanManager.stopScan()
    }

    @ReactMethod
    fun scanNetworkDevices(promise: Promise) {
      networkScanManager.onNetworkScanListener = object : OnNetworkScanListener {
        override fun deviceFound(ip: String, port: Int) {
            // emit callback to RN code
            reactContext.getJSModule(RCTDeviceEventEmitter::class.java)
              .emit("usbDeviceFound", null)
        }
      }
      networkScanManager.startScan()
      promise.resolve("trigger")
    }

    @ReactMethod
    fun stopNetworkScan() {
      networkScanManager.stopScan()
    }

    @ReactMethod
    fun initBluetoothConnectionListener() {
        // Add listener when bluetooth conencted
        reactContext.registerReceiver(bluetoothConnectionEventListener,
                IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))

        // Add listener when bluetooth disconnected
        reactContext.registerReceiver(bluetoothConnectionEventListener,
                IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
    }

    /**
     * Bluetooth Connection Event Listener
     */
    private val bluetoothConnectionEventListener: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (ActivityCompat.checkSelfPermission(reactContext, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
              return
            }
            val callbackAction = intent.action
            val bluetoothDevice = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?

            // if action or bluetooth data is null
            if (callbackAction == null || bluetoothDevice == null) {
                // do not proceed
                return
            }

            // hold value for bluetooth event
          val bluetoothEvent: BluetoothEvent = when (callbackAction) {
              BluetoothDevice.ACTION_ACL_CONNECTED -> BluetoothEvent.CONNECTED
              BluetoothDevice.ACTION_ACL_DISCONNECTED -> BluetoothEvent.DISCONNECTED
              else -> BluetoothEvent.NONE
          }

            // bluetooth event must not be null
            if (bluetoothEvent != BluetoothEvent.NONE) {
                // extract bluetooth device info and put in deviceInfoParams
                val deviceInfoParams = Arguments.createMap()
                deviceInfoParams.putString("name", bluetoothDevice.name)
                deviceInfoParams.putString("macAddress", bluetoothDevice.address)

                // put deviceInfoParams into callbackParams
                val callbackParams = Arguments.createMap()
                callbackParams.putMap("deviceInfo", deviceInfoParams)
                callbackParams.putString("state", bluetoothEvent.name)

                // emit callback to RN code
                reactContext.getJSModule(RCTDeviceEventEmitter::class.java)
                        .emit("bluetoothStateChanged", callbackParams)
            }
        }
    }

    init {
        val bluetoothManager = reactContext.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val adapter: BluetoothAdapter = bluetoothManager.adapter
        bluetoothScanManager = BluetoothScanManager(reactContext, adapter)

        val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
        usbScanManager = USBScanManager(reactContext, usbManager)

        networkScanManager = NetworkScanManager()
    }

    companion object {
        const val PRINTING_SIZE_58_MM = "PRINTING_SIZE_58_MM"
        const val PRINTING_SIZE_76_MM = "PRINTING_SIZE_76_MM"
        const val PRINTING_SIZE_80_MM = "PRINTING_SIZE_80_MM"
        const val BLUETOOTH_CONNECTED = "BLUETOOTH_CONNECTED"
        const val BLUETOOTH_DISCONNECTED = "BLUETOOTH_DISCONNECTED"
        const val BLUETOOTH_DEVICE_FOUND = "BLUETOOTH_DEVICE_FOUND"
    }
}
