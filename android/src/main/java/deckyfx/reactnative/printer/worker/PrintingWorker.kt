package deckyfx.reactnative.printer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.tcp.TcpConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager

class PrintingWorker(private val context: Context, workerParams: WorkerParameters):
  CoroutineWorker(context, workerParams) {

  private val text = inputData.getString("text")
  private val config = RNPrinter.PrinterSelectorArgument(inputData)
  private val cutPaper = inputData.getBoolean("cutPaper", true)
  private val openCashBox = inputData.getBoolean("openCashBox", true)
  private val printer: EscPosPrinter?
  private val retryCount = 0

  init {
    printer = resolvePrinter(config)
  }

  private fun resolvePrinter(config: RNPrinter.PrinterSelectorArgument): EscPosPrinter? {
    var connection: DeviceConnection? = null
    when (config.type) {
      RNPrinter.PRINTER_TYPE_NETWORK -> {
        connection = TcpConnection(config.address, config.port)
      }
      RNPrinter.PRINTER_TYPE_BLUETOOTH -> {
        connection = BluetoothPrintersConnectionsManager.selectByDeviceAddress(context, config.address)
      }
      RNPrinter.PRINTER_TYPE_USB -> {
        connection = UsbPrintersConnectionsManager.selectByDeviceName(context, config.address)
      }
    }
    if (connection == null) {
      return null
    }
    return EscPosPrinter(
      context,
      connection,
      config.dpi,
      config.width,
      config.maxChars
    )
  }

  companion object {
  }

  override suspend fun doWork(): Result {
    val progress = Data.Builder()
      .putAll(config.data)
      .putString("jobId", inputData.getString("jobId"))
      .putString("jobName", inputData.getString("jobName"))
      .putString("jobTag", inputData.getString("jobTag"))
      .build()
    setProgress(progress)
    // Do the work here
    return try {
      printer?.printFormattedText(text, 0)
      if (cutPaper) {
        printer?.cutPaper()
      }
      if (openCashBox) {
        printer?.openCashBox()
      }
      // Indicate whether the work finished successfully with the Result
      Result.success(progress)
    } catch (error: Exception) {
      if (runAttemptCount >= 3) {
        return Result.failure(Data.Builder()
          .putAll(progress)
          .putString("error", error.message)
          .build()
        )
      } else {
        Result.retry()
      }
    }
  }
}
