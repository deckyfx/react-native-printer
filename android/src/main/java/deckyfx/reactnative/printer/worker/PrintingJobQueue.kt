package deckyfx.reactnative.printer.worker

import android.content.Context
import androidx.lifecycle.Observer
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.LinkedList

class PrintingJobQueue(context: Context) {
  private val workManager = WorkManager.getInstance(context)

  private val queue = LinkedList<OneTimeWorkRequest>()

  private var running = false

  fun processQueue() {
    if (queue.isEmpty()) {
      running = false
    }

    val currentJob = queue.poll() ?: return
    val workInfoLiveData = workManager.getWorkInfoByIdLiveData(currentJob.id)

    GlobalScope.launch(Dispatchers.Main) {
      workInfoLiveData.observeForever(object : Observer<WorkInfo> {
        override fun onChanged(workInfo: WorkInfo) {
          if (workInfo.state.isFinished) {
            workInfoLiveData.removeObserver(this)
            processQueue()
          }
        }
      })
    }

    // Enqueue the current job outside of GlobalScope
    workManager.enqueue(currentJob)
    running = true
  }

  fun enqueue(job: OneTimeWorkRequest) {
    queue.add(job)
    if (!running) {
      processQueue()
    }
  }
}
