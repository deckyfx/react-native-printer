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
) {
  constructor(argv: Data) : this(
    argv.getString("text"),
    argv.getBoolean("cutPaper", false),
    argv.getBoolean("openCashBox", false),
    argv.getString("file")
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
        .build()
    }

  val readableMap: ReadableMap
    get() {
      return Arguments.createMap().apply {
        putString("text", text)
        putBoolean("cutPaper", cutPaper)
        putBoolean("openCashBox", openCashBox)
        putString("file", file)
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
