package deckyfx.reactnative.printer.worker

import androidx.work.WorkInfo
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.ReadableArray
import com.facebook.react.bridge.WritableMap
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer

@Serializable
class WorkerEventData(
  var selector: PrinterSelectorArgument? = null,
  var file: String? = null,
  var jobId: String? = null,
  var jobName: String? = null,
  var jobTag: String? = null,
  var state: String? = null,
  var id: String? = null,
  var tags: ReadableArray? = null,
  var generation: Int = 0,
  var runAttemptCount: Int = 0,
  var error: String? = null,
) {
  val writableMap: WritableMap
    get() {
      return Arguments.createMap().apply {
        putMap(FIELD_SELECTOR, selector?.readableMap)
        putString(FIELD_FILE, file)
        putString(FIELD_JOB_ID, jobId)
        putString(FIELD_JOB_NAME, jobName)
        putString(FIELD_JOB_TAG, jobTag)
        putString(FIELD_STATE, state)
        putString(FIELD_ID, id)
        putArray(FIELD_TAGS, tags)
        putInt(FIELD_GENERATION, generation)
        putInt(FIELD_RUN_ATTEMPT_COUNT, runAttemptCount)
        putString(FIELD_ERROR, error)
      }
    }

  val json: String
    get() {
      val serializer = Json.serializersModule.serializer<WorkerEventData>()
      return Json.encodeToString(serializer, this)
    }

  companion object {
    fun fromJson(json: String): WorkerEventData {
      val serializer = Json.serializersModule.serializer<WorkerEventData>()
      return Json.decodeFromString(serializer, json)
    }

    fun fromWorkInfo(workInfo: WorkInfo): WorkerEventData {
      val data = WorkerEventData()
      val result = Arguments.createMap().apply {
        if (workInfo.progress.keyValueMap.isNotEmpty()) {
          val selector = PrinterSelectorArgument()
          workInfo.progress.getString(PrinterSelectorArgument.FIELD_CONNECTION)?.let {
            selector.connection = it
          }
          workInfo.progress.getString(PrinterSelectorArgument.FIELD_ADDRESS)?.let {
            selector.address = it
          }
          workInfo.progress.getInt(PrinterSelectorArgument.FIELD_PORT, 0).takeIf { it > 0 }?.let {
            selector.port = it
          }
          workInfo.progress.getInt(PrinterSelectorArgument.FIELD_BAUD_RATE, 0).takeIf { it > 0 }?.let {
            selector.baudrate = it
          }
          workInfo.progress.getInt(PrinterSelectorArgument.FIELD_DPI, 0).takeIf { it > 0 }?.let {
            selector.dpi = it
          }
          workInfo.progress.getFloat(PrinterSelectorArgument.FIELD_WIDTH, 0f).takeIf { it > 0f }?.let {
            selector.width = it
          }
          workInfo.progress.getInt(PrinterSelectorArgument.FIELD_MAX_CHARS, 0).takeIf { it > 0 }?.let {
            selector.maxChars = it
          }
          data.selector = selector
          workInfo.progress.getString(FIELD_FILE)?.let {
            data.file = it
          }
          workInfo.progress.getString(FIELD_JOB_ID)?.let {
            data.jobId = it
          }
          workInfo.progress.getString(FIELD_JOB_NAME)?.let {
            data.jobName = it
          }
          workInfo.progress.getString(FIELD_JOB_TAG)?.let {
            data.jobTag = it
          }
        }
        if (workInfo.outputData.keyValueMap.isNotEmpty()) {
          val selector = PrinterSelectorArgument()
          workInfo.outputData.getString(PrinterSelectorArgument.FIELD_CONNECTION)?.let {
            selector.connection = it
          }
          workInfo.outputData.getString(PrinterSelectorArgument.FIELD_ADDRESS)?.let {
            selector.address = it
          }
          workInfo.outputData.getInt(PrinterSelectorArgument.FIELD_PORT, 0).takeIf { it > 0 }?.let {
            selector.port = it
          }
          workInfo.outputData.getInt(PrinterSelectorArgument.FIELD_BAUD_RATE, 0).takeIf { it > 0 }?.let {
            selector.baudrate = it
          }
          workInfo.outputData.getInt(PrinterSelectorArgument.FIELD_DPI, 0).takeIf { it > 0 }?.let {
            selector.dpi = it
          }
          workInfo.outputData.getFloat(PrinterSelectorArgument.FIELD_WIDTH, 0f).takeIf { it > 0f }?.let {
            selector.width = it
          }
          workInfo.outputData.getInt(PrinterSelectorArgument.FIELD_MAX_CHARS, 0).takeIf { it > 0 }?.let {
            selector.maxChars = it
          }
          data.selector = selector
          workInfo.outputData.getString(FIELD_FILE)?.let {
            data.file = it
          }
          workInfo.outputData.getString(FIELD_JOB_ID)?.let {
            data.jobId = it
          }
          workInfo.outputData.getString(FIELD_JOB_NAME)?.let {
            data.jobName = it
          }
          workInfo.outputData.getString(FIELD_JOB_TAG)?.let {
            data.jobTag = it
          }
        }
        data.state = workInfo.state.name
        data.id = workInfo.id.toString()
        val tags = Arguments.createArray()
        workInfo.tags.forEach {
          tags.pushString(it)
        }
        data.tags = tags
        data.generation = workInfo.generation
        data.runAttemptCount = workInfo.runAttemptCount
      }
      when (workInfo.state) {
        WorkInfo.State.ENQUEUED -> {
        }

        WorkInfo.State.RUNNING -> {
          val isRetrying = workInfo.progress.getBoolean(RNPrinter.PRINT_JOB_STATE_RETRYING, false)
          if (isRetrying) {
            // Notify your UI or perform any desired action when the worker is retrying
            data.state = RNPrinter.PRINT_JOB_STATE_RETRYING
          }
        }

        WorkInfo.State.SUCCEEDED -> {
        }

        WorkInfo.State.FAILED -> {
          val errorMessage = workInfo.outputData.getString(FIELD_ERROR)
          data.error = errorMessage
        }

        WorkInfo.State.BLOCKED -> {
        }

        WorkInfo.State.CANCELLED -> {
        }
      }
      return data
    }

    const val FIELD_SELECTOR = "selector"
    const val FIELD_FILE = "file"
    const val FIELD_JOB_ID = "jobId"
    const val FIELD_JOB_NAME = "jobName"

    const val FIELD_JOB_TAG = "jobTag"
    const val FIELD_STATE = "state"
    const val FIELD_ID = "id"
    const val FIELD_TAGS = "tags"
    const val FIELD_GENERATION = "generation"
    const val FIELD_RUN_ATTEMPT_COUNT = "runAttemptCount"
    const val FIELD_ERROR = "error"
  }
}
