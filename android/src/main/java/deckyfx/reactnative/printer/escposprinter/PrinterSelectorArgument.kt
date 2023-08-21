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
      argv.getString("connection")!!,
      argv.getString("address")!!,
      argv.getInt("port", NetworkScanManager.DEFAULT_PRINTER_PORT),
      argv.getInt("baudrate", SerialConnection.DEFAULT_BAUD_RATE),
      argv.getInt("dpi", RNPrinter.PRINTING_DPI_NORMAL),
      argv.getFloat("width", RNPrinter.PRINTING_WIDTH_80_MM),
      argv.getInt("maxChars", RNPrinter.PRINTING_LINES_MAX_CHAR_42,)
  )

  constructor(argv: ReadableMap) : this(Data.Builder().putAll(argv.toHashMap()).build())
  constructor() : this("", "", 0, 0, 0, 0f, 0)

  val ready: Boolean
    get() = !connection.isNullOrEmpty() && !address.isNullOrEmpty()

  val data: Data
    get() {
      return Data.Builder()
        .putString("connection", connection)
        .putString("address", address)
        .putInt("port", port)
        .putInt("baudrate", baudrate)
        .putInt("dpi", dpi)
        .putFloat("width", width)
        .putInt("maxChars", maxChars)
        .build()
    }

  val readableMap: ReadableMap
    get() {
      return Arguments.createMap().apply {
        putString("connection", connection)
        putString("address", address)
        putInt("port", port)
        putInt("baudrate", baudrate)
        putInt("dpi", dpi)
        putDouble("width", width.toDouble())
        putInt("maxChars", maxChars)
      }
    }

  val json: String
    get() {
      val serializer = Json.serializersModule.serializer<PrinterSelectorArgument>()
      return Json.encodeToString(serializer, this)
    }

  companion object {
    fun fromJson(json: String): PrinterSelectorArgument {
      val serializer = Json.serializersModule.serializer<PrinterSelectorArgument>()
      return Json.decodeFromString(serializer, json)
    }
  }
}
