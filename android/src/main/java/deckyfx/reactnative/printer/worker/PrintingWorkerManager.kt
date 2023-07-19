package deckyfx.reactnative.printer.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.facebook.react.bridge.ReactContext
import com.facebook.react.bridge.ReadableMap
import java.util.UUID
import java.util.concurrent.TimeUnit

class PrintingWorkerManager private constructor() {
  companion object {
    @Volatile
    private var instance: PrintingWorkerManager? = null

    const val PRINTING_JOB_TAG = "printing"

    fun getInstance() =
      instance ?: synchronized(this) {
        instance ?: PrintingWorkerManager().also { instance = it }
      }
  }

  fun doSomething() = "Doing something"

  fun enqueuePrint(
    context: ReactContext,
    config: ReadableMap,
    text: String
  ) {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build()

    val jobId = UUID.randomUUID()

    val jobName = "printing-job-${jobId}"

    val jobTag = PRINTING_JOB_TAG

    val data = Data.Builder()
      .putAll(config.toHashMap())
      .putString("text", text)
      .putString("jobId", jobId.toString())
      .putString("jobName", jobName)
      .putString("jobTag", jobTag)
      .build()

    val workRequest: OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<PrintingWorker>()
        .setInitialDelay(1, TimeUnit.SECONDS)
        .setInputData(data)
        .setConstraints(constraints)
        .addTag(jobTag)
        .setId(jobId)
        .setBackoffCriteria(BackoffPolicy.LINEAR, 10, TimeUnit.SECONDS)
        .build()

    WorkManager.getInstance(context).enqueueUniqueWork(
      jobName,
      ExistingWorkPolicy.APPEND_OR_REPLACE,
      workRequest)
  }
}
