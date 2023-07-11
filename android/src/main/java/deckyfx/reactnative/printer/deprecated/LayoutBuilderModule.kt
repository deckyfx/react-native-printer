package deckyfx.reactnative.printer.deprecated

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import java.io.IOException

class LayoutBuilderModule(private val reactContext: ReactApplicationContext) : ReactContextBaseJavaModule(reactContext) {
    private val layoutBuilder = LayoutBuilder()
    override fun getConstants(): Map<String, Any> {
        val constants: MutableMap<String, Any> = HashMap()
        constants[LayoutBuilder.TEXT_ALIGNMENT_LEFT] = LayoutBuilder.TEXT_ALIGNMENT_LEFT
        constants[LayoutBuilder.TEXT_ALIGNMENT_CENTER] = LayoutBuilder.TEXT_ALIGNMENT_CENTER
        constants[LayoutBuilder.TEXT_ALIGNMENT_RIGHT] = LayoutBuilder.TEXT_ALIGNMENT_RIGHT
        return constants
    }

    override fun getName(): String {
        return "LayoutBuilder"
    }

    @ReactMethod
    fun createAccent(text: String?, accent: String, promise: Promise) {
      text?.let{ _text ->
        promise.resolve(layoutBuilder.createAccent(_text, accent[0]))
      }
    }

    @ReactMethod
    @Throws(IOException::class)
    fun createFromDesign(text: String?, promise: Promise) {
        try {
            promise.resolve(layoutBuilder.createFromDesign(text))
        } catch (e: IOException) {
            promise.reject(e)
        }
    }

    @ReactMethod
    fun createDivider(promise: Promise) {
        promise.resolve(layoutBuilder.createDivider())
    }

    @ReactMethod
    fun createDivider(symbol: String, promise: Promise) {
        promise.resolve(layoutBuilder.createDivider(symbol[0].code))
    }

    @ReactMethod
    fun createMenuItem(key: String?, value: String?, space: String, promise: Promise) {
      key?.let { _key ->
        value?.let { _value ->
          promise.resolve(layoutBuilder.createMenuItem(_key, _value, space[0]))
        }
      }
    }

    @ReactMethod
    fun createTextOnLine(text: String?, space: String, alignment: String?, promise: Promise) {
      text?.let { _text ->
          promise.resolve(layoutBuilder.createTextOnLine(_text, space[0], alignment))
      }
    }

    @ReactMethod
    fun setPrintingSize(printingSize: String?) {
      val charsOnLine: Int = when (printingSize) {
          EscPosModule.PRINTING_SIZE_80_MM -> LayoutBuilder.CHARS_ON_LINE_80_MM
          EscPosModule.PRINTING_SIZE_76_MM -> LayoutBuilder.CHARS_ON_LINE_76_MM
          EscPosModule.PRINTING_SIZE_58_MM -> LayoutBuilder.CHARS_ON_LINE_58_MM
          else -> LayoutBuilder.CHARS_ON_LINE_58_MM
      }
      layoutBuilder.charsOnLine = charsOnLine
    }
}
