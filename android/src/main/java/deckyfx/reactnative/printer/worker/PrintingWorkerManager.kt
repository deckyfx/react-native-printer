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
import java.util.UUID
import java.util.concurrent.TimeUnit

class PrintingWorkerManager private constructor() {
  companion object {
    @Volatile
    private var instance: PrintingWorkerManager? = null

    const val PRINTING_JOB_TAG = "printing"
    const val PRINTING_JOB_NAME_PREFIX = "printing-job-"

    fun getInstance() =
      instance ?: synchronized(this) {
        instance ?: PrintingWorkerManager().also { instance = it }
      }
  }

  fun doSomething() = "Doing something"

  fun enqueuePrint(
    context: ReactContext,
    data: Data,
    jobId: UUID = UUID.randomUUID(),
  ): UUID {
    val constraints = Constraints.Builder()
      .setRequiredNetworkType(NetworkType.CONNECTED)
      .setRequiresBatteryNotLow(true)
      .build()
    val jobName = "${PRINTING_JOB_NAME_PREFIX}${jobId}"
    val jobTag = PRINTING_JOB_TAG
    val inputData = Data.Builder()
      .putAll(data)
      .putString(WorkerEventData.FIELD_JOB_ID, jobId.toString())
      .putString(WorkerEventData.FIELD_JOB_NAME, jobName)
      .putString(WorkerEventData.FIELD_JOB_TAG, jobTag)
      .build()
    val workRequest: OneTimeWorkRequest =
      OneTimeWorkRequestBuilder<PrintingWorker>()
        .setInitialDelay(1, TimeUnit.SECONDS)
        .setBackoffCriteria(BackoffPolicy.LINEAR, 1, TimeUnit.MINUTES)
        .setInputData(inputData)
        .setConstraints(constraints)
        .addTag(jobTag)
        .setId(jobId)
        .build()
    WorkManager.getInstance(context).enqueueUniqueWork(
      jobName,
      ExistingWorkPolicy.APPEND_OR_REPLACE,
      workRequest
    )
    return jobId
  }

  fun enqueuePrint(context: ReactContext, jobBuilderData: JobBuilderData): UUID {
    val argument = WorkerArgument.file(jobBuilderData.file)
    return enqueuePrint(context, argument.data, jobBuilderData.uuid)
  }

  fun cancelWork(context: ReactContext, uuid: UUID) {
    WorkManager.getInstance(context).cancelWorkById(uuid);
  }
}
