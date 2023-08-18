package deckyfx.reactnative.printer.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.WorkerParameters
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.serial.SerialConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.tcp.TcpConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager
import java.io.BufferedReader
import java.io.FileReader

class PrintingWorker(private val context: Context, workerParams: WorkerParameters):
  CoroutineWorker(context, workerParams) {

  private val text = inputData.getString("text")
  private val file = inputData.getString("file")
  private val cutPaper = inputData.getBoolean("cutPaper", true)
  private val openCashBox = inputData.getBoolean("openCashBox", true)
  private var printerSelector: PrinterSelectorArgument? = null
  private var printer: EscPosPrinter? = null

  private fun resolvePrinter(config: PrinterSelectorArgument): EscPosPrinter? {
    if (config == null) {
      return null
    }
    var connection: DeviceConnection? = null
    when (config.connection) {
      RNPrinter.PRINTER_CONNECTION_NETWORK -> {
        connection = TcpConnection(config.address, config.port)
      }
      RNPrinter.PRINTER_CONNECTION_BLUETOOTH -> {
        connection = BluetoothPrintersConnectionsManager.selectByDeviceAddress(context, config.address)
      }
      RNPrinter.PRINTER_CONNECTION_USB -> {
        connection = UsbPrintersConnectionsManager.selectByDeviceName(context, config.address)
      }
      RNPrinter.PRINTER_CONNECTION_SERIAL -> {
        connection = SerialConnectionsManager.selectByDeviceName(config.address, config.baudrate)
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

  private fun resolvePrinterFromJSON(line: String) {
    printerSelector = PrinterSelectorArgument.fromJson(line)
    printer = resolvePrinter(printerSelector!!)
  }

  private fun processText() {
    printerSelector = PrinterSelectorArgument(inputData)
    printer = resolvePrinter(printerSelector!!)
    printer?.printFormattedText(text, 0)
    if (cutPaper) {
      printer?.cutPaper()
    }
    if (openCashBox) {
      printer?.openCashBox()
    }
  }

  private fun processFile() {
    // Create a FileReader object.
    val fileReader = FileReader("$JobBuilder.FILE_PATH}${file}")
    // Create a BufferedReader object.
    val bufferedReader = BufferedReader(fileReader)
    // Create a list to store the lines of the file.
    // Read each line of the file.
    var line: String? = bufferedReader.readLine()
    while (line != null) {
      line = bufferedReader.readLine()
      if (printer == null) {
        if (line.startsWith(JobBuilder.COMMAND_SELECT_PRINTER)) {
          resolvePrinterFromJSON(line.replace(JobBuilder.COMMAND_SELECT_PRINTER, ""))
        }
        continue
      } else {
        if (line.startsWith(JobBuilder.COMMAND_SELECT_PRINTER)) {
          resolvePrinterFromJSON(line.replace(JobBuilder.COMMAND_SELECT_PRINTER, ""))
        } else if (line.startsWith(JobBuilder.COMMAND_PRINT)) {
          printer!!.printFormattedText(line.replace(JobBuilder.COMMAND_PRINT, ""))
        } else if (line.startsWith(JobBuilder.COMMAND_FEED_PRINTER)) {
          printer!!.feedPaper(0)
        } else if (line.startsWith(JobBuilder.COMMAND_CUT_PAPER)) {
          printer!!.cutPaper()
        } else if (line.startsWith(JobBuilder.COMMAND_OPEN_CASHBOX)) {
          printer!!.openCashBox()
        }
      }
    }
    // Close the FileReader object.
    bufferedReader.close()
  }

  override suspend fun doWork(): Result {
    val progress = Data.Builder()
      .putAll(inputData)
      .build()
    setProgress(progress)
    // Do the work here
    return try {
      if (!text.isNullOrEmpty()) {
        processText()
      } else if (file.isNullOrEmpty()) {
        processFile()
      }
      // Indicate whether the work finished successfully with the Result
      Result.success(progress)
    } catch (error: Exception) {
      if (file != null) {
        return Result.failure(
          Data.Builder()
            .putAll(progress)
            .putString("error", error.message)
            .build()
        )
      }
      if (runAttemptCount >= 3) {
        return Result.failure(
          Data.Builder()
            .putAll(progress)
            .putString("error", error.message)
            .build()
        )
      } else {
        return Result.retry()
      }
    }
  }

  companion object {}
}
