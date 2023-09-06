package deckyfx.reactnative.printer.worker

import androidx.work.Data
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.util.UUID


@Serializable
class JobBuilderData(
  val id: String,
  val file: String,
) {
  constructor(argv: Data) : this(
    argv.getString(FIELD_ID)!!,
    argv.getString(FIELD_FILE)!!
  )

  constructor(argv: ReadableMap) : this(
    PrinterSelectorArgument.safeString(argv, FIELD_ID),
    PrinterSelectorArgument.safeString(argv, FIELD_FILE)
  )

  val uuid: UUID
    get() {
      return UUID.fromString(id)
    }

  val data: Data
    get() {
      return Data.Builder()
        .putString(FIELD_ID, id)
        .putString(FIELD_FILE, file)
        .build()
    }

  val readableMap: ReadableMap
    get() {
      return Arguments.createMap().apply {
        putString(FIELD_ID, id)
        putString(FIELD_FILE, file)
      }
    }

  val json: String
    get() {
      val serializer = Json.serializersModule.serializer<JobBuilderData>()
      return Json.encodeToString(serializer, this)
    }

  companion object {
    fun fromJson(json: String): JobBuilderData {
      val serializer = Json.serializersModule.serializer<JobBuilderData>()
      return Json.decodeFromString(serializer, json)
    }

    const val FIELD_ID = "id"
    const val FIELD_FILE = "file"
  }
}
