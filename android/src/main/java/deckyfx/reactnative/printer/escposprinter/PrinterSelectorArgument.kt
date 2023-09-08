package deckyfx.reactnative.printer.escposprinter

import androidx.work.Data
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.devicescan.NetworkScanManager
import deckyfx.reactnative.printer.escposprinter.connection.serial.SerialConnection
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
class PrinterSelectorArgument(
  var connection: String,
  var address: String,
  var port: Int,
  var baudrate: Int,
  var dpi: Int,
  var width: Float,
  var maxChars: Int
) {
  constructor(argv: Data) : this(
    argv.getString(FIELD_CONNECTION)!!,
    argv.getString(FIELD_ADDRESS)!!,
    argv.getInt(FIELD_PORT, NetworkScanManager.DEFAULT_PRINTER_PORT),
    argv.getInt(FIELD_BAUD_RATE, SerialConnection.DEFAULT_BAUD_RATE),
    argv.getInt(FIELD_DPI, RNPrinter.PRINTING_DPI_NORMAL),
    argv.getFloat(FIELD_WIDTH, RNPrinter.PRINTING_WIDTH_80_MM),
    argv.getInt(FIELD_MAX_CHARS, RNPrinter.PRINTING_LINES_MAX_CHAR_42)
  )

  constructor(argv: ReadableMap) : this(
    safeString(argv, FIELD_CONNECTION)!!,
    safeString(argv,FIELD_ADDRESS)!!,
    safeInt(argv, FIELD_PORT, NetworkScanManager.DEFAULT_PRINTER_PORT),
    safeInt(argv, FIELD_BAUD_RATE, SerialConnection.DEFAULT_BAUD_RATE),
    safeInt(argv, FIELD_DPI, RNPrinter.PRINTING_DPI_NORMAL),
    safeFloat(argv, FIELD_WIDTH, RNPrinter.PRINTING_WIDTH_80_MM),
    safeInt(argv, FIELD_MAX_CHARS, RNPrinter.PRINTING_LINES_MAX_CHAR_42),
  )
  constructor() : this("", "", 0, 0, 0, 0f, 0)

  val ready: Boolean
    get() = !connection.isNullOrEmpty() && !address.isNullOrEmpty()

  val data: Data
    get() {
      return Data.Builder()
        .putString(FIELD_CONNECTION, connection)
        .putString(FIELD_ADDRESS, address)
        .putInt(FIELD_PORT, port)
        .putInt(FIELD_BAUD_RATE, baudrate)
        .putInt(FIELD_DPI, dpi)
        .putFloat(FIELD_WIDTH, width)
        .putInt(FIELD_MAX_CHARS, maxChars)
        .build()
    }

  val readableMap: ReadableMap
    get() {
      return Arguments.createMap().apply {
        putString(FIELD_CONNECTION, connection)
        putString(FIELD_ADDRESS, address)
        putInt(FIELD_PORT, port)
        putInt(FIELD_BAUD_RATE, baudrate)
        putInt(FIELD_DPI, dpi)
        putDouble(FIELD_WIDTH, width.toDouble())
        putInt(FIELD_MAX_CHARS, maxChars)
      }
    }

  val json: String
    get() {
      val serializer = Json.serializersModule.serializer<PrinterSelectorArgument>()
      return Json.encodeToString(serializer, this)
    }

  val key: String
    get() {
      when (connection) {
        RNPrinter.PRINTER_CONNECTION_NETWORK -> {
          return "${connection}-${address}:${port}"
        }
        RNPrinter.PRINTER_CONNECTION_BLUETOOTH -> {
          return "${connection}-${address}"
        }
        RNPrinter.PRINTER_CONNECTION_USB -> {
          return "${connection}-${address}"
        }
        RNPrinter.PRINTER_CONNECTION_SERIAL -> {
          return "${connection}-${address}:${baudrate}"
        }
      }
      return ""
    }

  companion object {
    fun fromJson(json: String): PrinterSelectorArgument {
      val serializer = Json.serializersModule.serializer<PrinterSelectorArgument>()
      return Json.decodeFromString(serializer, json)
    }

    const val FIELD_CONNECTION = "connection"
    const val FIELD_ADDRESS = "address"
    const val FIELD_PORT = "port"
    const val FIELD_BAUD_RATE = "baudrate"
    const val FIELD_DPI = "dpi"
    const val FIELD_WIDTH = "width"
    const val FIELD_MAX_CHARS = "maxChars"

    fun safeString(argv: ReadableMap, field: String, defaultValue: String = ""): String {
      return try {
        val result = argv.getString(field)
        result!!
      } catch (e: Throwable) {
        defaultValue
      }
    }

    fun safeInt(argv: ReadableMap, field: String, defaultValue: Int = 0): Int {
      return try {
        val result = argv.getInt(field)
        result!!
      } catch (e: Throwable) {
        defaultValue
      }
    }

    fun safeFloat(argv: ReadableMap, field: String, defaultValue: Float = 0f): Float {
      return try {
        val result = argv.getDouble(FIELD_WIDTH).toFloat()
        result!!
      } catch (e: Throwable) {
        defaultValue
      }
    }

    fun safeBoolean(argv: ReadableMap, field: String, defaultValue: Boolean = false): Boolean {
      return try {
        val result = argv.getBoolean(FIELD_WIDTH)
        result!!
      } catch (e: Throwable) {
        defaultValue
      }
    }
  }
}
