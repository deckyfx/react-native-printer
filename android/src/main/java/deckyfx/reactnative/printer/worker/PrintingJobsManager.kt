package deckyfx.reactnative.printer.worker

import android.annotation.SuppressLint
import android.content.Context
import androidx.work.WorkManager
import com.facebook.react.bridge.ReactContext
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import java.util.UUID

class PrintingJobsManager private constructor(private val context: Context) {
  private val workManager = WorkManager.getInstance(context)

  private val jobQueues = mutableMapOf<String, PrintingJobQueue>()

  init {
    // Cancel all works in initial states
    workManager.cancelAllWork()
  }

  companion object {
    @SuppressLint("StaticFieldLeak")
    @Volatile
    private var instance: PrintingJobsManager? = null

    const val PRINTING_JOB_TAG = "printing"
    const val PRINTING_JOB_NAME_PREFIX = "printing-job-"

    fun getInstance(context: ReactContext) =
      instance ?: synchronized(this) {
        instance ?: PrintingJobsManager(context).also { instance = it }
      }
  }

  fun enqueuePrint(jobBuilderData: JobBuilderData, printerSelector: PrinterSelectorArgument? = null): UUID {
    val argument = WorkerArgument(jobBuilderData.file)

    val (_, workRequest) = WorkRequestFactory.create(argument, jobBuilderData.uuid)

    val key = printerSelector?.key ?: PRINTING_JOB_TAG

    val jobQueue = jobQueues.getOrPut(key) { PrintingJobQueue(context) }

    jobQueue.enqueue(workRequest)

    return jobBuilderData.uuid
  }

  fun cancelWork(uuid: UUID) {
    workManager.cancelWorkById(uuid);
  }
}
