package deckyfx.reactnative.printer

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import io.github.escposjava.print.NetworkPrinter
import io.github.escposjava.print.Printer
import io.github.escposjava.print.SerialPrinter
import io.github.escposjava.print.exceptions.BarcodeSizeError
import io.github.escposjava.print.exceptions.QRCodeException
import java.io.IOException
import java.io.UnsupportedEncodingException

class EscPosModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private var printerService: PrinterService? = null
    private var config: ReadableMap? = null

    internal enum class BluetoothEvent {
        CONNECTED, DISCONNECTED, DEVICE_FOUND, NONE
    }

    override fun getConstants(): Map<String, Any>? {
        val constants: MutableMap<String, Any> = HashMap()
        constants[PRINTING_SIZE_58_MM] = PRINTING_SIZE_58_MM
        constants[PRINTING_SIZE_76_MM] = PRINTING_SIZE_76_MM
        constants[PRINTING_SIZE_80_MM] = PRINTING_SIZE_80_MM
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

    init {
    }

    companion object {
        const val PRINTING_SIZE_58_MM = "PRINTING_SIZE_58_MM"
        const val PRINTING_SIZE_76_MM = "PRINTING_SIZE_76_MM"
        const val PRINTING_SIZE_80_MM = "PRINTING_SIZE_80_MM"
    }
}
