package deckyfx.reactnative.printer.worker

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import java.io.File
import java.io.FileOutputStream
import java.util.UUID

class JobBuilder (private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private lateinit var uuid: UUID
  private lateinit var fileos: FileOutputStream
  private var building: Boolean = false

  override fun getName(): String {
    return LOG_TAG
  }

  companion object {
    private val LOG_TAG = JobBuilder::class.java.simpleName
    const val FILE_PATH = "/data/printjob/"

    const val COMMAND_SELECT_PRINTER = "SELECT_PRINTER:"
    const val COMMAND_PRINT = "PRINT:"
    const val COMMAND_FEED_PRINTER = "FEED_PRINTER:"
    const val COMMAND_CUT_PAPER = "CUT_PAPER:"
    const val COMMAND_OPEN_CASHBOX = "OPEN_CASHBOX:"
  }

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants["LOG_TAG"] = LOG_TAG
    return constants
  }

  private val filename: String
    get() {
      return "${FILE_PATH}${uuid.toString()}"
    }

  @ReactMethod
  fun begin(promise: Promise) {
    uuid = UUID.randomUUID()
    val file = File(reactContext.filesDir, filename)
    // If the file doesn't exist, create it.
    if (!file.exists()) {
      file.createNewFile()
    }
    // reset it's contents
    val fos = FileOutputStream(file)
    fos.write("".toByteArray())
    fos.close()

    // Get a FileOutputStream to write to the file.
    fileos = FileOutputStream(file)
    building = true
    promise.resolve(true)
  }

  @ReactMethod()
  fun build(promise: Promise) {
    fileos.close()
    building = false
    promise.resolve(uuid.toString())
  }

  @ReactMethod
  fun discard(promise: Promise) {
    fileos.close()
    val file = File(reactContext.filesDir, filename)
    if (file.exists()) {
      file.delete()
    }
    // Delete the file.
    building = false
    promise.resolve(true)
  }

  @ReactMethod
  fun selectPrinter(selector: ReadableMap, promise: Promise) {
    if (!building) {
      return promise.resolve(false)
    }
    val selector = PrinterSelectorArgument(selector)
    fileos.write("${COMMAND_SELECT_PRINTER}${selector.json}".toByteArray())
    promise.resolve(true)
  }

  @ReactMethod
  fun printLine(line: String, promise: Promise) {
    if (!building) {
      return promise.resolve(false)
    }
    fileos.write("${COMMAND_PRINT}${line}".toByteArray())
    promise.resolve(true)
  }

  @ReactMethod
  fun feedPaper(promise: Promise) {
    if (!building) {
      return promise.resolve(false)
    }
    fileos.write(COMMAND_FEED_PRINTER.toByteArray())
    promise.resolve(true)
  }

  @ReactMethod
  fun cutPaper(promise: Promise) {
    if (!building) {
      return promise.resolve(false)
    }
    fileos.write(COMMAND_CUT_PAPER.toByteArray())
    promise.resolve(true)
  }

  @ReactMethod
  fun openCashBox(promise: Promise) {
    if (!building) {
      return promise.resolve(false)
    }
    fileos.write(COMMAND_OPEN_CASHBOX.toByteArray())
    promise.resolve(true)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  fun building(promise: Promise): Boolean {
    return building
  }
}
