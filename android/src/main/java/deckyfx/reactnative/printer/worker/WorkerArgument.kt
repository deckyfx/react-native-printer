package deckyfx.reactnative.printer.worker

import androidx.work.Data
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
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
    argv.getString("text"),
    argv.getBoolean("cutPaper", false),
    argv.getBoolean("openCashBox", false),
    argv.getString("file"),

    argv.getString("connection"),
    argv.getString("address"),
    argv.getInt("port", 0),
    argv.getInt("baudrate", 0),
    argv.getInt("dpi", 0),
    argv.getFloat("width", 0f),
    argv.getInt("maxChars", 0)
  )

  constructor(argv: ReadableMap) : this(Data.Builder().putAll(argv.toHashMap()).build())

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
        .putString("text", text)
        .putBoolean("cutPaper", cutPaper)
        .putBoolean("openCashBox", openCashBox)
        .putString("file", file)

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
        putString("text", text)
        putBoolean("cutPaper", cutPaper)
        putBoolean("openCashBox", openCashBox)
        putString("file", file)

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

    fun text(text: String, cutPaper: Boolean = false, openCashBox: Boolean = false): WorkerArgument {
      return WorkerArgument(text, cutPaper, openCashBox, null)
    }
  }
}
