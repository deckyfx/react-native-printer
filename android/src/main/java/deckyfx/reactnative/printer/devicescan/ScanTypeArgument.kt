package deckyfx.reactnative.printer.devicescan

import androidx.work.Data
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableMap
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
class ScanTypeArgument(
  val connection: Int,
) {
  constructor(argv: Data) : this(
      argv.getInt("connection", DeviceScanner.SCAN_ALL)!!
  )

  constructor(argv: ReadableMap) : this(Data.Builder().putAll(argv.toHashMap()).build())

  val data: Data
    get() {
      return Data.Builder()
        .putInt("connection", connection)
        .build()
    }

  val readableMap: ReadableMap
    get() {
      return Arguments.createMap().apply {
        putInt("connection", connection)
      }
    }

  val json: String
    get() {
      val serializer = Json.serializersModule.serializer<ScanTypeArgument>()
      return Json.encodeToString(serializer, this)
    }

  companion object {
    fun fromJson(json: String): ScanTypeArgument {
      val serializer = Json.serializersModule.serializer<ScanTypeArgument>()
      return Json.decodeFromString(serializer, json)
    }
  }
}
