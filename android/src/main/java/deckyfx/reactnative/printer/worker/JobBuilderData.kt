package deckyfx.reactnative.printer.worker

import androidx.work.Data
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
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
      argv.getString("id")!!,
      argv.getString("file")!!
  )

  constructor(argv: ReadableMap) : this(Data.Builder().putAll(argv.toHashMap()).build())

  val uuid: UUID
    get() {
      return UUID.fromString(id)
    }

  val data: Data
    get() {
      return Data.Builder()
        .putString("id", id)
        .putString("file", file)
        .build()
    }

  val readableMap: ReadableMap
    get() {
      return Arguments.createMap().apply {
        putString("id", id)
        putString("file", file)
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
  }
}
