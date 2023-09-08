package deckyfx.reactnative.printer.worker

import android.content.Context
import androidx.work.Data
import androidx.work.Worker
import androidx.work.WorkerParameters
import deckyfx.reactnative.printer.RNPrinter
import deckyfx.reactnative.printer.escposprinter.EscPosCommands
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.serial.SerialConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.tcp.TcpConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager
import java.io.BufferedReader
import java.io.FileReader

class PrintingWorker(private val context: Context, workerParams: WorkerParameters) :
  Worker(context, workerParams) {
  private val argument = WorkerArgument(inputData)
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
        connection =
          BluetoothPrintersConnectionsManager.selectByDeviceAddress(context, config.address)
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
    val progress = Data.Builder()
      .putAll(inputData)
    if (printerSelector != null) {
      progress.putAll(printerSelector!!.data)
    }
    setProgressAsync(progress.build())
  }

  private fun processFile() {
    // Create a FileReader object.
    val fileReader = FileReader(argument.file)
    // Create a BufferedReader object.
    val bufferedReader = BufferedReader(fileReader)
    // Create a list to store the lines of the file.
    // Read each line of the file.
    var line: String?
    while (bufferedReader.readLine().also { line = it } != null) {
      if (printer == null) {
        if (line!!.startsWith(JobBuilder.COMMAND_SELECT_PRINTER)) {
          resolvePrinterFromJSON(line!!.replace(JobBuilder.COMMAND_SELECT_PRINTER, ""))
        }
        continue
      } else {
        if (line!!.startsWith(JobBuilder.COMMAND_SELECT_PRINTER)) {
          resolvePrinterFromJSON(line!!.replace(JobBuilder.COMMAND_SELECT_PRINTER, ""))
        } else if (line!!.startsWith(JobBuilder.COMMAND_INITIALIZE)) {
          printer!!.write(EscPosCommands.byteArray(EscPosCommands.INITIALIZE))
        } else if (line!!.startsWith(JobBuilder.COMMAND_PRINT)) {
          printer!!.printFormattedText(line!!.replace(JobBuilder.COMMAND_PRINT, ""), 0)
        } else if (line!!.startsWith(JobBuilder.COMMAND_FEED_PRINTER)) {
          val feed = line!!.replace(JobBuilder.COMMAND_FEED_PRINTER, "").toFloat()
          printer!!.feedPaper(printer!!.mmToPx(feed))
        } else if (line!!.startsWith(JobBuilder.COMMAND_CUT_PAPER)) {
          printer!!.cutPaper()
        } else if (line!!.startsWith(JobBuilder.COMMAND_OPEN_CASHBOX)) {
          printer!!.openCashBox()
        }
      }
    }
    // Close the FileReader object.
    bufferedReader.close()
    printer?.disconnectPrinter()
  }

  override fun doWork(): Result {
    val progress = Data.Builder()
      .putAll(inputData)
    if (printerSelector != null) {
      progress.putAll(printerSelector!!.data)
    }
    return try {
      processFile()
      if (printerSelector != null) {
        progress.putAll(printerSelector!!.data)
      }
      Result.success(progress.build())
    } catch (error: Exception) {
      if (printerSelector != null) {
        progress.putAll(printerSelector!!.data)
      }
      progress.putString(WorkerEventData.FIELD_ERROR, error.message)
      if (runAttemptCount >= 3) {
        Result.failure(progress.build())
      } else {
        progress.putBoolean(RNPrinter.PRINT_JOB_STATE_RETRYING, true)
        setProgressAsync(progress.build())
        Result.retry()
      }
    }
  }

  companion object {}
}
