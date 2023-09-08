package deckyfx.reactnative.printer.worker

import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import java.io.BufferedWriter
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.util.UUID


class JobBuilder(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private val files = mutableMapOf<UUID, File>()

  override fun getName(): String {
    return LOG_TAG
  }

  companion object {
    private val LOG_TAG = JobBuilder::class.java.simpleName
    const val COMMAND_SELECT_PRINTER = "SELECT_PRINTER:"
    const val COMMAND_INITIALIZE = "INITIALIZE:"
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

  private val dirPath: String
    get() {
      return "${reactContext.filesDir.absolutePath}/printing"
    }

  private fun appendToFile(uuidString: String, contents: String) {
    val uuid = UUID.fromString(uuidString)
    val file = files[uuid] ?: throw IllegalArgumentException("File with UUID $uuid does not exist")
    // Here true is to append the content to file
    // BufferedWriter writer give better performance
    with(BufferedWriter(FileWriter(file, true))) {
      write(contents)
      close()
    }
  }

  @ReactMethod
  @Suppress("unused")
  fun begin(promise: Promise) {
    val uuid = UUID.randomUUID()

    val directory = File(dirPath)
    val path = "${dirPath}/${uuid}"
    if (!directory.exists()) {
      directory.mkdirs()
    }
    val file = File(path)
    // If the file doesn't exist, create it.
    if (!file.exists()) {
      file.createNewFile()
    }
    // reset it's contents
    val fos = FileOutputStream(file)
    fos.write("".toByteArray())
    fos.close()

    files[uuid] = file

    promise.resolve(uuid.toString())
  }

  @ReactMethod
  @Suppress("unused")
  fun build(uuidString: String, promise: Promise) {
    val uuid = UUID.fromString(uuidString)
    val path = "${dirPath}/${uuid}"
    val file = files[uuid] ?: throw IllegalArgumentException("File with UUID $uuid does not exist")
    files.remove(uuid)
    promise.resolve(JobBuilderData(uuid.toString(), path).readableMap)
  }

  @ReactMethod
  @Suppress("unused")
  fun discard(uuidString: String, promise: Promise) {
    val uuid = UUID.fromString(uuidString)
    val path = "${dirPath}/${uuid}"
    val file = File(path)
    if (file.exists()) {
      file.delete()
    }
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun selectPrinter(uuid: String, selector: ReadableMap, promise: Promise) {
    val selectorArg = PrinterSelectorArgument(selector)
    appendToFile(uuid, "${COMMAND_SELECT_PRINTER}${selectorArg.json}\n")
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun initializePrinter(uuid: String, promise: Promise) {
    appendToFile(uuid, "${COMMAND_INITIALIZE}\n")
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun printLine(uuid: String, line: String, promise: Promise) {
    var lineAdd = line
    if (!line.endsWith("\n")) {
      lineAdd += "\n"
    }
    appendToFile(uuid, "${COMMAND_PRINT}${lineAdd}")
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun feedPaper(uuid: String, dots: Int = 0, promise: Promise) {
    appendToFile(uuid, "${COMMAND_FEED_PRINTER}${dots}\n")
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun cutPaper(uuid: String, promise: Promise) {
    appendToFile(uuid, "${COMMAND_CUT_PAPER}\n")
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun openCashBox(uuid: String, promise: Promise) {
    appendToFile(uuid, "${COMMAND_OPEN_CASHBOX}\n")
    promise.resolve(true)
  }

  @ReactMethod(isBlockingSynchronousMethod = true)
  @Suppress("unused")
  fun building(uuidString: String): Boolean {
    val uuid = UUID.fromString(uuidString)
    val file = files[uuid] ?: return false
    return true
  }
}
