package deckyfx.reactnative.printer.worker

import androidx.work.Data
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
class WorkerArgument(
  val text: String?,
  val cutPaper: Boolean,
  val openCashBox: Boolean,
  val file: String?,

  @Deprecated("deprecated field")
  val connection: String? = null,
  @Deprecated("deprecated field")
  val address: String? = null,
  @Deprecated("deprecated field")
  val port: Int = 0,
  @Deprecated("deprecated field")
  val baudrate: Int = 0,
  @Deprecated("deprecated field")
  val dpi: Int = 0,
  @Deprecated("deprecated field")
  val width: Float = 0f,
  @Deprecated("deprecated field")
  val maxChars: Int = 0,
) {
  constructor(argv: Data) : this(
    argv.getString(FIELD_TEXT),
    argv.getBoolean(FIELD_CUT_PAPER, false),
    argv.getBoolean(FIELD_OPEN_CASH_BOX, false),
    argv.getString(FIELD_FILE),

    argv.getString(FIELD_CONNECTION),
    argv.getString(FIELD_ADDRESS),
    argv.getInt(FIELD_PORT, 0),
    argv.getInt(FIELD_BAUD_RATE, 0),
    argv.getInt(FIELD_DPI, 0),
    argv.getFloat(FIELD_WIDTH, 0f),
    argv.getInt(FIELD_MAX_CHARS, 0)
  )

  constructor(argv: ReadableMap) : this(
    PrinterSelectorArgument.safeString(argv, FIELD_TEXT),
    PrinterSelectorArgument.safeBoolean(argv, FIELD_CUT_PAPER),
    PrinterSelectorArgument.safeBoolean(argv, FIELD_OPEN_CASH_BOX),
    PrinterSelectorArgument.safeString(argv, FIELD_FILE),

    PrinterSelectorArgument.safeString(argv, FIELD_CONNECTION),
    PrinterSelectorArgument.safeString(argv, FIELD_ADDRESS),
    PrinterSelectorArgument.safeInt(argv, FIELD_PORT),
    PrinterSelectorArgument.safeInt(argv, FIELD_BAUD_RATE),
    PrinterSelectorArgument.safeInt(argv, FIELD_DPI),
    PrinterSelectorArgument.safeFloat(argv, FIELD_WIDTH),
    PrinterSelectorArgument.safeInt(argv, FIELD_MAX_CHARS)
  )

  val isFile: Boolean
    get() {
      return !file.isNullOrEmpty()
    }

  val isText: Boolean
    get() {
      return !text.isNullOrEmpty()
    }

  val data: Data
    get() {
      return Data.Builder()
        .putString(FIELD_TEXT, text)
        .putBoolean(FIELD_CUT_PAPER, cutPaper)
        .putBoolean(FIELD_OPEN_CASH_BOX, openCashBox)
        .putString(FIELD_FILE, file)

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
        putString(FIELD_TEXT, text)
        putBoolean(FIELD_CUT_PAPER, cutPaper)
        putBoolean(FIELD_OPEN_CASH_BOX, openCashBox)
        putString(FIELD_FILE, file)

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
      val serializer = Json.serializersModule.serializer<WorkerArgument>()
      return Json.encodeToString(serializer, this)
    }

  companion object {
    fun fromJson(json: String): WorkerArgument {
      val serializer = Json.serializersModule.serializer<WorkerArgument>()
      return Json.decodeFromString(serializer, json)
    }

    fun file(file: String): WorkerArgument {
      return WorkerArgument(null, false, false, file)
    }

    fun text(
      text: String,
      cutPaper: Boolean = false,
      openCashBox: Boolean = false
    ): WorkerArgument {
      return WorkerArgument(text, cutPaper, openCashBox, null)
    }

    const val FIELD_TEXT = "text"
    const val FIELD_CUT_PAPER = "cutPaper"
    const val FIELD_OPEN_CASH_BOX = "openCashBox"
    const val FIELD_FILE = "file"

    const val FIELD_CONNECTION = "connection"
    const val FIELD_ADDRESS = "address"
    const val FIELD_PORT = "port"
    const val FIELD_BAUD_RATE = "baudrate"
    const val FIELD_DPI = "dpi"
    const val FIELD_WIDTH = "width"
    const val FIELD_MAX_CHARS = "maxChars"
  }
}
