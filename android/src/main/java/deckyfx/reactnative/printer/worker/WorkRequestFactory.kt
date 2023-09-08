package deckyfx.reactnative.printer.worker

import androidx.work.BackoffPolicy
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.OneTimeWorkRequestBuilder
import java.util.UUID
import java.util.concurrent.TimeUnit

class WorkRequestFactory {
  companion object {
    fun create(
      argument: WorkerArgument,
      jobId: UUID = UUID.randomUUID()
    ): Pair<String, OneTimeWorkRequest> {
      val constraints = Constraints.Builder()
        .setRequiredNetworkType(NetworkType.CONNECTED)
        .setRequiresBatteryNotLow(true)
        .build()
      val jobName = "${PrintingJobsManager.PRINTING_JOB_NAME_PREFIX}${jobId}"
      val jobTag = PrintingJobsManager.PRINTING_JOB_TAG
      val inputData = Data.Builder()
        .putAll(argument.data)
        .putString(WorkerEventData.FIELD_JOB_ID, jobId.toString())
        .putString(WorkerEventData.FIELD_JOB_NAME, jobName)
        .putString(WorkerEventData.FIELD_JOB_TAG, jobTag)
        .build()

      return Pair(
        jobName, OneTimeWorkRequestBuilder<PrintingWorker>()
          .setInitialDelay(1, TimeUnit.SECONDS)
          .setBackoffCriteria(BackoffPolicy.LINEAR, 5, TimeUnit.SECONDS)
          .setInputData(inputData)
          .setConstraints(constraints)
          .addTag(jobTag)
          .setId(jobId)
          .build()
      )
    }
  }
}
