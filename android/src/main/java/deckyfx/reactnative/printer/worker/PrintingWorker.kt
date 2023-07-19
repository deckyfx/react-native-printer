package deckyfx.reactnative.printer.worker

import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import com.facebook.react.bridge.ReactContext
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.tcp.TcpConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager

class PrintingWorker(private val reactContext: ReactContext, workerParams: WorkerParameters):
  CoroutineWorker(reactContext, workerParams) {

  private val text = inputData.getString("text")
  private val config = RNPrinter.PrinterSelectorArgument(inputData)
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
        connection = BluetoothPrintersConnectionsManager.selectByDeviceAddress(reactContext, config.address)
      }
      RNPrinter.PRINTER_TYPE_USB -> {
        connection = UsbPrintersConnectionsManager.selectByDeviceName(reactContext, config.address)
      }
    }
    if (connection == null) {
      return null
    }
    return EscPosPrinter(
      reactContext,
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
      // Indicate whether the work finished successfully with the Result
      Result.success(progress)
    } catch (error: Exception) {
      Result.failure(
        Data.Builder()
          .putString("error", error.message)
          .build()
      )
    }
  }
}
