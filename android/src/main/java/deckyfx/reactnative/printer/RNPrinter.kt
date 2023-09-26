package deckyfx.reactnative.printer

import android.Manifest
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import android.os.Build
import android.os.Parcelable
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.Promise
import com.facebook.react.bridge.ReactApplicationContext
import com.facebook.react.bridge.ReactContextBaseJavaModule
import com.facebook.react.bridge.ReactMethod
import com.facebook.react.bridge.ReadableMap
import com.facebook.react.bridge.WritableMap
import com.facebook.react.modules.core.DeviceEventManagerModule
import deckyfx.reactnative.printer.devicescan.DeviceScanner
import deckyfx.reactnative.printer.escposprinter.EscPosPrinter
import deckyfx.reactnative.printer.escposprinter.PrinterSelectorArgument
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.serial.SerialConnectionsManager
import deckyfx.reactnative.printer.escposprinter.connection.tcp.TcpConnection
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbPrintersConnectionsManager
import deckyfx.reactnative.printer.worker.JobBuilderData
import deckyfx.reactnative.printer.worker.PrintingJobsManager
import deckyfx.reactnative.printer.worker.WorkerEventData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.io.IOException
import java.util.UUID


class RNPrinter(private val reactContext: ReactApplicationContext) :
  ReactContextBaseJavaModule(reactContext) {

  private var workerListenerInitialized: Boolean = false
  private var mListenerCount: Int = 0

  override fun getName(): String {
    return LOG_TAG
  }

  companion object {
    private val LOG_TAG = RNPrinter::class.java.simpleName

    const val EVENT_PRINTING_JOB = "PRINTING_JOB"

    const val PRINT_JOB_STATE_ENQUEUED = "ENQUEUED"
    const val PRINT_JOB_STATE_RUNNING = "RUNNING"
    const val PRINT_JOB_STATE_SUCCEEDED = "SUCCEEDED"
    const val PRINT_JOB_STATE_FAILED = "FAILED"
    const val PRINT_JOB_STATE_CANCELED = "CANCELED"
    const val PRINT_JOB_STATE_BLOCKED = "BLOCKED"
    const val PRINT_JOB_STATE_RETRYING = "RETRYING"

    const val PRINTER_CONNECTION_NETWORK = "network"
    const val PRINTER_CONNECTION_BLUETOOTH = "bluetooth"
    const val PRINTER_CONNECTION_USB = "usb"
    const val PRINTER_CONNECTION_SERIAL = "serial"

    const val PRINTER_TYPE_THERMAL = "thermal"
    const val PRINTER_TYPE_DOTMATRIX = "dotmatrix"

    const val PRINTING_DPI_NORMAL = 203
    const val PRINTING_LINES_MAX_CHAR_32 = 32
    const val PRINTING_LINES_MAX_CHAR_33 = 33
    const val PRINTING_LINES_MAX_CHAR_40 = 40
    const val PRINTING_LINES_MAX_CHAR_42 = 42
    const val PRINTING_LINES_MAX_CHAR_56 = 56

    // Width is used to calculate image and QR Code placement,
    // It is in mili-meter
    // Somehow need width correction around 6mm,
    // maybe because there are slightly white space in left and right?
    private const val PRINTING_WIDTH_CORRECTION = 10f
    const val PRINTING_WIDTH_58_MM = 58f - PRINTING_WIDTH_CORRECTION
    const val PRINTING_WIDTH_70_MM = 70f - PRINTING_WIDTH_CORRECTION
    const val PRINTING_WIDTH_75_MM = 75f - PRINTING_WIDTH_CORRECTION
    const val PRINTING_WIDTH_76_MM = 76f - PRINTING_WIDTH_CORRECTION
    const val PRINTING_WIDTH_80_MM = 80f - PRINTING_WIDTH_CORRECTION

    const val TEST_PRINT_DESIGN =
      // "[C]<img>" + PrinterTextParserImg.bitmapToHexadecimalString(printer, this.getApplicationContext().getResources().getDrawableForDensity(R.drawable.logo, DisplayMetrics.DENSITY_MEDIUM))+"</img>\n" +
      "[L]\n" +
        "[C]<img>1d763000210000010000000000000000000000000007fffffffffc000000000000000000000000000000000000000000000000000000ffffffffffffe0000000000000000000000000000000000000000000000000000fffffffffffffff00000000000000000000000000000000000000000000000000fffffffffffffffff0000000000000000000000000000000000000000000000007fffffffffffffffffe00000000000000000000000000000000000000000000007fffffffffffffffffffc000000000000000000000000000000000000000000003fffffffffffffffffffff80000000000000000000000000000000000000000001fffffffffffffffffffffff0000000000000000000000000000000000000000007fffffffffffffffffffffffe00000000000000000000000000000000000000003fffffffffffffffffffffffff8000000000000000000000000000000000000000ffffffffffffffffffffffffffe000000000000000000000000000000000000003ffffffffffc000007ffffffffff80000000000000000000000000000000000000fffffffff80000000003fffffffff0000000000000000000000000000000000003ffffffff0000000000001ffffffff800000000000000000000000000000000000ffffffff800000000000001fffffffe00000000000000000000000000000000003fffffffc0000000000000003fffffff80000000000000000000000000000000007ffffffc000000000000000007ffffffe000000000000000000000000000000001fffffff0000000000000000001fffffff800000000000000000000000000000007fffffff0000000000000000001fffffffc0000000000000000000000000000001ffffffff0000000000000000001ffffffff0000000000000000000000000000003ffffffff8000000000000000003ffffffff8000000000000000000000000000007ffffffff8000000000000000003ffffffffc00000000000000000000000000001fffffffffc000000000000000007fffffffff00000000000000000000000000007fffffffffc000000000000000007fffffffffc000000000000000000000000000ffffffffffe000000000000000007fffffffffe000000000000000000000000001ffffffffffe00000000000000000fffffffffff000000000000000000000000003fffff3ffffe00000000000000000fffffcfffff80000000000000000000000000fffffc3fffff00000000000000001fffff87ffffe0000000000000000000000001fffff81fffff00000000000000001fffff81fffff0000000000000000000000003ffffe01fffff80000000000000003fffff00fffff8000000000000000000000007ffff801fffff80000000000000003fffff003ffffc00000000000000000000000fffff000fffffc0000000000000007fffff001ffffe00000000000000000000001ffffe000fffffc0000000000000007ffffe0007ffff00000000000000000000007ffff80007ffffe0000000000000007ffffe0003ffffc0000000000000000000007ffff00007ffffe000000000000000fffffc0001ffffc000000000000000000001ffffe00003fffff000000000000000fffffc00007ffff000000000000000000003ffff800003fffff000000000000001fffff800003ffff800000000000000000007ffff000001fffff000000000000001fffff800001ffffc0000000000000000000ffffe000001fffff800000000000003fffff8000007fffe0000000000000000001ffffc000001fffff800000000000003fffff0000003ffff0000000000000000001ffff8000000fffffc00000000000003fffff0000001ffff0000000000000000003fffe00000007ffffc00000000000007ffffe0000000ffff8000000000000000007fffc00000007ffffe00000000000007ffffc00000007fffc00000000000000000ffff800000007ffffe0000000000000fffffc00000003fffe00000000000000001ffff000000003fffff0000000000000fffffc00000001ffff00000000000000003fffe000000003fffff0000000000001fffff800000000ffff80000000000000007fffc000000003fffff0000000000001fffff8000000007fffc0000000000000007fff8000000001fffff8000000000003fffff0000000003fffe000000000000000ffff0000000000fffff8000000000003fffff0000000001fffe000000000000001fffe0000000000fffffc000000000007fffff0000000000ffff000000000000003fffc0000000000fffffc000000000007ffffe00000000007fff800000000000007fffc00000000007ffffe000000000007ffffe00000000003fffc00000000000007fff800000000007ffffe00000000000fffffc00000000001fffe0000000000000ffff000000000003ffffe00000000000fffffc00000000000fffe0000000000000fffe000000000003fffff00000000001fffff800000000000ffff0000000000001fffc000000000001fffff00000000001fffff8000000000007fff0000000000003fff8000000000001fffff80000000003fffff0000000000003fff8000000000007fff8000000000000fffff80000000003fffff0000000000001fffc000000000007fff0000000000000fffffc0000000007ffffe0000000000000fffe00000000000fffe0000000000000fffffc0000000007ffffe0000000000000fffe00000000001fffe00000000000007ffffe0000000007ffffe00000000000007fff00000000001fffc00000000000007ffffe000000000fffffc0000000000003fff00000000003fff800000000000003fffff000000000fffffc00000000000003fff80000000003fff800000000000003fffff000000001fffff800000000000001fff80000000007fff000000000000001fffff000000001fffff800000000000000fffc0000000007fff000000000000001fffff800000003fffff000000000000000fffe000000000ffff000000000000000fffff800000003fffff000000000000001fffe000000000ffff800000000000000fffffc00000003ffffe000000000000001ffff000000001ffff8000000000000007ffffc00000007ffffe000000000000003ffff000000003ffffc000000000000007ffffe00000007ffffe000000000000003ffff800000003ffffc000000000000003ffffe0000000fffffc000000000000007ffff800000007ffffe000000000000003fffff0000000fffff8000000000000007ffffc00000007ffffe000000000000003fffff0000001fffff800000000000000fffffc0000000fffffe000000000000001fffff0000001fffff800000000000000fffffe0000000ffffff000000000000001fffff8000003fffff000000000000000fffffe0000000ffffff000000000000000fffff8000003fffff000000000000001fffffe0000001ffffff800000000000000fffffc000007ffffe000000000000003ffffff0000001ffffffc000000000000007ffffc000007ffffe000000000000003ffffff0000003ffffffc000000000000007ffffe000007ffffc000000000000007ffffff8000003ffffffc000000000000003ffffe00000fffffc000000000000007ffffff8000003ffffffe000000000000003ffffe00000fffffc000000000000007ffffffc000007fffffff000000000000003fffff00001fffff800000000000000fffffffc000007fffffff000000000000001fffff00001fffff800000000000001fffffffe00000ffffffff800000000000001fffff80003fffff000000000000001fffffffe00000ffffffff800000000000000fffff80003fffff000000000000003fffffffe00000ffffffff800000000000000fffffc0003ffffe000000000000003ffffffff00001ffffffffc000000000000007ffffc0007ffffe000000000000003ffffffff00001ffffffffc000000000000007ffffe000fffffc000000000000007ffffffff00001fff7ffffe000000000000007ffffe000fffffc00000000000000fffffefff80003ffe7ffffe000000000000003fffff000fffffc00000000000000fffffcfff80003ffe3fffff000000000000003fffff001fffff800000000000001fffffc7ff80003ffe3fffff000000000000001fffff001fffff800000000000001fffff87ff80007ffc1fffff800000000000001fffff803fffff000000000000003fffff87ffc0007ffc0fffffc00000000000000fffff803fffff000000000000003fffff03ffc0007ffc0fffffc00000000000000fffffc07ffffe000000000000007fffff03ffc0007ffc0fffffe000000000000007ffffc07ffffe000000000000007ffffe03ffe000fff807ffffe000000000000007ffffe07ffffc00000000000000fffffe01ffe000fff807ffffe000000000000003ffffe0fffffc00000000000000fffffe01ffe000fff803fffff000000000000003fffff0fffff800000000000000fffffc01ffe000fff003fffff000000000000003fffff1fffff800000000000001fffff801ffe001fff001fffff800000000000001fffff9fffff800000000000001fffff800fff001fff001fffff800000000000001fffffffffff000000000000003fffff000fff001fff000fffffc00000000000000fffffffffff000000000000007fffff000fff001fff000fffffc00000000000000ffffffffffe000000000000007ffffe000fff001ffe0007ffffe000000000000007fffffffffe00000000000000fffffe0007ff803ffe0007fffff000000000000007fffffffffc00000000000000fffffc0007ff803ffe0003fffff000000000000003fffffffffc00000000000001fffffc0007ff803ffe0003fffff000000000000003fffffffff800000000000001fffffc0007ff803ffc0001fffff800000000000001fffffffff800000000000003fffff80007ff803ffc0001fffff800000000000001fffffffff000000000000003fffff80003ff803ffc0000fffffc00000000000000fffffffff000000000000003fffff00003ff807ffc0000fffffc00000000000000ffffffffe000000000000007ffffe00003ffc07ffc00007ffffe00000000000000ffffffffe00000000000000fffffe00003ffc07ffc00007ffffe000000000000007fffffffe00000000000000fffffc00003ffc07ff800003fffff000000000000007fffffffc00000000000001fffffc00003ffc07ff800003fffff000000000000003fffffffc00000000000001fffff800003ffc07ff800001fffff800000000000003fffffff800000000000003fffff800003ffc07ff800001fffff800000000000001fffffff800000000000003fffff000001ffc07ff800000fffffc00000000000001fffffff000000000000007fffff000001ffc07ff800000fffffc00000000000000fffffff000000000000007fffff000001ffc07ff8000007ffffe00000000000000fffffff00000000000000fffffe000001ffe0fff8000007ffffe00000000000000ffffffe00000000000000fffffe000001ffe0fff8000003fffff000000000000003fffffc00000000000001fffffc000001ffe0fff8000003fffff00000000000000000000000000000000001ffff8000001ffe0fff8000001fffff80000000000000000000000000000000003fffff8000001ffe0fff8000001fffff80000000000000000000000000000000003fffff8000001ffe0fff8000000fffffc0000000000000000000000000000000007fffff0000001ffe0fff8000000fffffc0000000000000000000000000000000007fffff0000001ffe0fff80000007ffffe000000000000000000000000000000000fffffe0000001ffe0fff80000007ffffe000000000000000000000000000000000fffffc0000001ffe0fff80000003fffff000000000000000000000000000000001fffffc0000001ffe0fff80000003fffff000000000000000000000000000000001fffffc0000001ffe0fff80000001fffff8000000000000ffffffc0000000000003fffff80000001ffe0fff80000001fffff8000000000001ffffffe0000000000003fffff80000001ffe07ff80000000fffffc000000000001ffffffe0000000000007fffff00000001ffe07ff80000000fffffc000000000001fffffff0000000000007ffffe00000001ffe07ff800000007ffffe000000000003fffffff000000000000fffffe00000001ffe07ff800000007ffffe000000000003fffffff000000000000fffffe00000001ffc07ff800000003fffff000000000007fffffff800000000001fffffc00000003ffc07ff800000003fffff000000000007fffffff800000000001fffffc00000003ffc07ff800000001fffff80000000000ffffffffc00000000003fffff800000003ffc07ff800000001fffff80000000000ffffffffc00000000003fffff000000003ffc07ffc00000000fffffc0000000000ffffffffe00000000007fffff000000003ffc07ffc00000000fffffc0000000001ffffffffe00000000007fffff000000003ffc07ffc000000007ffffe0000000001ffffffffe0000000000fffffe000000003ffc03ffc000000007ffffe0000000003fffffffff0000000000fffffe000000003ffc03ffc000000003fffff0000000003fffffffff0000000001fffffc000000007ff803ffc000000003fffff0000000003fffffffff8000000001fffffc000000007ff803ffe000000001fffff8000000007fffffffff8000000003fffff8000000007ff803ffe000000001fffff8000000007fffffffffc000000003fffff8000000007ff803ffe000000000fffffc00000000ffffffffffc000000007fffff0000000007ff801ffe000000000fffffc00000000ffffffffffe000000007ffffe000000000fff801fff0000000007ffffe00000001ffffffffffe00000000fffffe000000000fff001fff0000000007ffffe00000001fffff9fffff00000000fffffc000000000fff001fff0000000003fffff00000003fffff1fffff00000001fffffc000000000fff001fff0000000003fffff00000003fffff0fffff00000001fffffc000000001fff000fff0000000001fffff80000003ffffe0fffff80000003fffff8000000001fff000fff8000000001fffff80000007ffffe07ffff80000003fffff8000000001ffe000fff8000000000fffffc0000007ffffc07ffffc0000007fffff0000000001ffe000fff8000000000fffffc000000fffffc03ffffc0000007fffff0000000003ffe0007ffc0000000007ffffe000000fffffc03ffffe000000fffffe0000000003ffe0007ffc0000000007ffffe000000fffff801ffffe000000fffffe0000000003ffc0007ffc0000000003fffff000001fffff801ffffe000001fffffc0000000007ffc0007ffc0000000003fffff000001fffff001fffff000001fffff80000000007ffc0003ffe0000000001fffff800003fffff000fffff000003fffff80000000007ff80003ffe0000000001fffff800003fffff000fffff800003fffff80000000007ff80003ffe0000000000fffffc00007ffffe0007ffff800007fffff0000000000fff80001fff0000000000fffffc00007ffffe0007ffffc00007fffff0000000000fff80001fff0000000000fffffc00007ffffc0003ffffc00007ffffe0000000001fff00001fff00000000007ffffe0000fffff80003ffffe0000fffffe0000000001fff00000fff80000000003fffff0000fffff80001ffffe0001fffffc0000000001fff00000fff80000000003fffff0001fffff80001ffffe0001fffffc0000000003ffe00000fffc0000000001fffff8001fffff00000fffff0003fffff80000000003ffe000007ffc0000000001fffff8003fffff00000fffff0003fffff80000000007ffe000007ffe0000000000fffffc003ffffe000007ffff8007fffff00000000007ffc000003ffe0000000000fffffc003ffffe000007ffff8007ffffe0000000000fffc000003fff00000000007ffffe007ffffc000007ffffc00fffffe0000000000fff8000003fff00000000007ffffe007ffffc000003ffffc00fffffe0000000001fff8000001fff00000000003fffff00fffffc000003ffffe01fffffc0000000001fff8000001fff80000000003fffff00fffff8000001ffffe01fffffc0000000001fff0000000fff80000000001fffff81fffff8000001fffff03fffff80000000003fff0000000fffc0000000001fffff81fffff0000000fffff03fffff80000000003ffe0000000fffc0000000000fffffc1fffff0000000fffff07fffff00000000007ffe00000007ffe0000000000fffffc3ffffe0000000fffff87fffff0000000000fffc00000007ffe00000000007ffffc3ffffe00000007ffff8fffffe0000000000fffc00000003fff00000000007ffffe7ffffc00000007ffffcfffffe000000001fff800000003fff80000000003ffffffffffc00000003ffffffffffc0000000001fff800000001fff80000000003ffffffffff800000003ffffffffff80000000003fff000000001fffc0000000001ffffffffff800000001ffffffffff80000000003fff000000000fffc0000000001ffffffffff800000001ffffffffff80000000007ffe000000000fffe0000000001ffffffffff000000001ffffffffff0000000000fffe0000000007fff0000000000ffffffffff000000000ffffffffff0000000000fffc0000000003fff00000000007ffffffffe0000000007ffffffffe0000000001fff80000000003fff80000000007ffffffffe0000000007ffffffffe0000000003fff80000000001fffc0000000007ffffffffc0000000007ffffffffc0000000003fff00000000001fffc0000000003ffffffffc0000000003ffffffffc0000000007fff00000000000fffe0000000001ffffffffc0000000003ffffffff8000000000fffe000000000007fff0000000001ffffffff80000000001ffffffff8000000000fffe000000000007fff8000000000ffffffff80000000001ffffffff0000000001fffc000000000003fff8000000000ffffffff00000000000ffffffff0000000003fff8000000000001fffc0000000007fffffff00000000000fffffffe0000000007fff8000000000001fffe0000000007ffffffe000000000007ffffffe000000000ffff0000000000000ffff0000000007ffffffe000000000007ffffffc000000000fffe00000000000007fff8000000003ffffffc000000000007ffffffc000000001fffe00000000000007fff8000000001ffffffc000000000003ffffff8000000003fffc00000000000003fffc000000001ffffffc000000000003ffffff8000000007fff800000000000001fffe000000000ffffff8000000000001ffffff000000000ffff000000000000000ffff000000000ffffff8000000000001ffffff000000001ffff0000000000000007fff8000000007fffff0000000000000fffffe000000003fffe0000000000000007fffc000000007fffff0000000000000fffffe000000007fffc0000000000000003fffe000000003ffffe00000000000007ffffc00000000ffff80000000000000001ffff000000003ffffe00000000000007ffffc00000001ffff00000000000000000ffff800000003ffffc00000000000003ffff800000003fffe000000000000000007fffc00000001ffffc00000000000003ffff800000007fffe000000000000000003fffe00000001ffff800000000000003ffff00000000ffffc000000000000000001ffff00000000ffff800000000000001ffff00000001ffff8000000000000000001ffff80000000ffff000000000000001fffe00000003ffff0000000000000000000ffffe00000007fff000000000000000fffe00000007fffe00000000000000000007ffff00000007fff000000000000000fffc0000001ffffc00000000000000000003ffff80000003ffe0000000000000007ffc0000003ffff800000000000000000001ffffc0000001ffe0000000000000007ff80000007ffff0000000000000000000007ffff0000001ffc0000000000000007ff8000000ffffe0000000000000000000007ffff8000000ffc0000000000000003ff0000003ffffc0000000000000000000003ffffc000000ff80000000000000003ff0000007ffff80000000000000000000000fffff000000ff80000000000000001fe000001fffff000000000000000000000007ffff8000007f00000000000000001fe000003ffffe000000000000000000000003ffffe000003f00000000000000000fc000007ffff8000000000000000000000001fffff000001c000000000000000007800001fffff0000000000000000000000000fffffc000000000000000000000000000007ffffe00000000000000000000000003fffff00000000000000000000000000000fffffc00000000000000000000000001fffff80000000000000000000000000003fffff000000000000000000000000000fffffe000000000000000000000000000fffffe0000000000000000000000000007fffff800000000000000000000000003fffffc0000000000000000000000000001fffffe0000000000000000000000000ffffff000000000000000000000000000007fffff8000000000000000000000003fffffe000000000000000000000000000003ffffff00000000000000000000000ffffff8000000000000000000000000000001ffffffc0000000000000000000007ffffff00000000000000000000000000000007ffffff000000000000000000001ffffffc00000000000000000000000000000003ffffffe0000000000000000000fffffff800000000000000000000000000000000fffffffc000000000000000007ffffffe0000000000000000000000000000000003fffffffc0000000000000003fffffff80000000000000000000000000000000000ffffffff800000000000001fffffffe000000000000000000000000000000000003ffffffff0000000000001ffffffffc000000000000000000000000000000000001fffffffffc0000000007fffffffff00000000000000000000000000000000000003ffffffffffe00000fffffffffff800000000000000000000000000000000000000ffffffffffffffffffffffffffe0000000000000000000000000000000000000003fffffffffffffffffffffffff80000000000000000000000000000000000000000ffffffffffffffffffffffffe00000000000000000000000000000000000000001fffffffffffffffffffffff00000000000000000000000000000000000000000007fffffffffffffffffffffc000000000000000000000000000000000000000000007fffffffffffffffffffc0000000000000000000000000000000000000000000000ffffffffffffffffffe000000000000000000000000000000000000000000000001fffffffffffffffff00000000000000000000000000000000000000000000000001fffffffffffffff0000000000000000000000000000000000000000000000000001fffffffffffff000000000000000000000000000000000000000000000000000000ffffffffffe0000000000000000000000000000</img>\\n\n"+
        "[C]<u><font size='big'>ORDER N°045</font></u>\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<b>BEAUTIFUL SHIRT</b>[R]9.99e\n" +
        "[L]  + Size : S\n" +
        "[L]\n" +
        "[L]<b>AWESOME HAT</b>[R]24.99e\n" +
        "[L]  + Size : 57/58\n" +
        "[L]\n" +
        "[C]--------------------------------\n" +
        "[R]TOTAL PRICE :[R]34.98e\n" +
        "[R]TAX :[R]4.23e\n" +
        "[L]\n" +
        "[C]================================\n" +
        "[L]\n" +
        "[L]<font size='tall'>Customer :</font>\n" +
        "[L]Raymond DUPONT\n" +
        "[L]5 rue des girafes\n" +
        "[L]31547 PERPETES\n" +
        "[L]Tel : +33801201456\n" +
        "012345678901234567890123456789012345678901234567890\n" +
        "[L]\n" +
        "[C]<barcode type='ean13' height='10'>831254784551</barcode>\n" +
        "[C]<qrcode size='20'>https://dantsu.com/</qrcode>\n" +
        "[L]\n" +
        "[L]\n" +
        "[L]\n" +
        "[L]\n" +
        "[L]\n" +
        "[L]\n"
  }

  override fun getConstants(): Map<String, Any> {
    val constants: MutableMap<String, Any> = HashMap()
    constants["EVENT_PRINTING_JOB"] = EVENT_PRINTING_JOB

    constants["PRINT_JOB_STATE_ENQUEUED"] = PRINT_JOB_STATE_ENQUEUED
    constants["PRINT_JOB_STATE_RUNNING"] = PRINT_JOB_STATE_RUNNING
    constants["PRINT_JOB_STATE_SUCCEEDED"] = PRINT_JOB_STATE_SUCCEEDED
    constants["PRINT_JOB_STATE_FAILED"] = PRINT_JOB_STATE_FAILED
    constants["PRINT_JOB_STATE_CANCELED"] = PRINT_JOB_STATE_CANCELED
    constants["PRINT_JOB_STATE_BLOCKED"] = PRINT_JOB_STATE_BLOCKED
    constants["PRINT_JOB_STATE_RETRYING"] = PRINT_JOB_STATE_RETRYING

    constants["PRINTER_CONNECTION_NETWORK"] = PRINTER_CONNECTION_NETWORK
    constants["PRINTER_CONNECTION_BLUETOOTH"] = PRINTER_CONNECTION_BLUETOOTH
    constants["PRINTER_CONNECTION_USB"] = PRINTER_CONNECTION_USB
    constants["PRINTER_CONNECTION_SERIAL"] = PRINTER_CONNECTION_SERIAL

    constants["PRINTER_TYPE_THERMAL"] = PRINTER_TYPE_THERMAL
    constants["PRINTER_TYPE_DOTMATRIX"] = PRINTER_TYPE_DOTMATRIX

    constants["PRINTING_DPI_NORMAL"] = PRINTING_DPI_NORMAL

    constants["PRINTING_LINES_MAX_CHAR_32"] = PRINTING_LINES_MAX_CHAR_32
    constants["PRINTING_LINES_MAX_CHAR_33"] = PRINTING_LINES_MAX_CHAR_33
    constants["PRINTING_LINES_MAX_CHAR_40"] = PRINTING_LINES_MAX_CHAR_40
    constants["PRINTING_LINES_MAX_CHAR_42"] = PRINTING_LINES_MAX_CHAR_42
    constants["PRINTING_LINES_MAX_CHAR_56"] = PRINTING_LINES_MAX_CHAR_56

    constants["PRINTING_WIDTH_58_MM"] = PRINTING_WIDTH_58_MM
    constants["PRINTING_WIDTH_70_MM"] = PRINTING_WIDTH_70_MM
    constants["PRINTING_WIDTH_75_MM"] = PRINTING_WIDTH_75_MM
    constants["PRINTING_WIDTH_76_MM"] = PRINTING_WIDTH_76_MM
    constants["PRINTING_WIDTH_80_MM"] = PRINTING_WIDTH_80_MM

    constants["TEST_PRINT_DESIGN"] = TEST_PRINT_DESIGN
    return constants
  }

  @ReactMethod
  @Suppress("unused")
  fun enqueuePrint(job: ReadableMap, promise: Promise) {
    enqueuePrint(job, null, promise)
  }

  @ReactMethod
  @Suppress("unused")
  fun enqueuePrint(job: ReadableMap, printer: ReadableMap? = null, promise: Promise) {
    val printerSelectorArgument = if (printer != null)  PrinterSelectorArgument(printer) else null
    val jobBuilderData = JobBuilderData(job)
    val uuid = PrintingJobsManager.getInstance(reactContext).enqueuePrint(jobBuilderData, printerSelectorArgument)
    observeWork(uuid)
    promise.resolve(uuid.toString())
  }

  @ReactMethod
  @Suppress("unused")
  fun checkPermissions(scanTypeArgument: Double, promise: Promise) {
    var scanType = DeviceScanner.SCAN_ALL
    if (scanTypeArgument > 0.0) {
      scanType = scanTypeArgument.toInt()
    }
    if (scanType == DeviceScanner.SCAN_NETWORK ||
      scanType == DeviceScanner.SCAN_ZEROCONF ||
      scanType == DeviceScanner.SCAN_ALL
    ) {
      if (!checkPermission(Manifest.permission.INTERNET)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.ACCESS_NETWORK_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.ACCESS_WIFI_STATE)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.CHANGE_WIFI_MULTICAST_STATE)) {
        promise.resolve(false)
        return
      }
    }
    if (scanType == DeviceScanner.SCAN_USB ||
      scanType == DeviceScanner.SCAN_ALL
    ) {
    }
    if (scanType == DeviceScanner.SCAN_SERIAL ||
      scanType == DeviceScanner.SCAN_ALL
    ) {
    }
    if (scanType == DeviceScanner.SCAN_BLUETOOTH ||
      scanType == DeviceScanner.SCAN_ALL
    ) {
      if (!checkPermission(Manifest.permission.BLUETOOTH)) {
        promise.resolve(false)
        return
      }
      if (!checkPermission(Manifest.permission.BLUETOOTH_ADMIN)) {
        promise.resolve(false)
        return
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (!checkPermission(Manifest.permission.BLUETOOTH_CONNECT)) {
          promise.resolve(false)
          return
        }
        if (!checkPermission(Manifest.permission.BLUETOOTH_SCAN)) {
          promise.resolve(false)
          return
        }
      }
    }

    promise.resolve(true)
    return
  }

  @ReactMethod
  @Suppress("unused")
  fun requestPermissions(scanTypeArgument: Double, promise: Promise) {
    var scanType = DeviceScanner.SCAN_ALL
    if (scanTypeArgument > 0.0) {
      scanType = scanTypeArgument.toInt()
    }
    if (scanType == DeviceScanner.SCAN_NETWORK || scanType == DeviceScanner.SCAN_ZEROCONF) {
      val REQUEST_PERMISSION_CODE = 1
      val PERMISSIONS = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
      )
      reactContext.currentActivity?.let {
        ActivityCompat.requestPermissions(it, PERMISSIONS, REQUEST_PERMISSION_CODE)
      }
      promise.resolve(true)
      return
    }
    if (scanType == DeviceScanner.SCAN_USB) {
      UsbPrintersConnectionsManager(reactContext).requestUSBPermissions(reactContext, usbReceiver)
      promise.resolve(true)
    }
    if (scanType == DeviceScanner.SCAN_SERIAL) {
      promise.resolve(true)
      return
    }
    if (scanType == DeviceScanner.SCAN_BLUETOOTH) {
      val REQUEST_PERMISSION_CODE = 100
      val PERMISSIONS = arrayOf(
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
      )
      reactContext.currentActivity?.let {
        ActivityCompat.requestPermissions(it, PERMISSIONS, REQUEST_PERMISSION_CODE)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        reactContext.currentActivity?.let {
          ActivityCompat.requestPermissions(
            it, arrayOf(
              Manifest.permission.BLUETOOTH_CONNECT,
              Manifest.permission.BLUETOOTH_SCAN,
            ), REQUEST_PERMISSION_CODE
          )
        }
      }
      promise.resolve(true)
      return
    }
    if (scanType == DeviceScanner.SCAN_ALL) {
      val REQUEST_PERMISSION_CODE = 1
      val PERMISSIONS = arrayOf(
        Manifest.permission.INTERNET,
        Manifest.permission.ACCESS_WIFI_STATE,
        Manifest.permission.ACCESS_NETWORK_STATE,
        Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
        Manifest.permission.BLUETOOTH,
        Manifest.permission.BLUETOOTH_ADMIN,
      )
      reactContext.currentActivity?.let {
        ActivityCompat.requestPermissions(it, PERMISSIONS, REQUEST_PERMISSION_CODE)
      }
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        reactContext.currentActivity?.let {
          ActivityCompat.requestPermissions(
            it, arrayOf(
              Manifest.permission.BLUETOOTH_CONNECT,
              Manifest.permission.BLUETOOTH_SCAN,
            ), REQUEST_PERMISSION_CODE
          )
        }
      }
      val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
      UsbPrintersConnectionsManager(reactContext).list?.forEach {
        if (it != null) {
          val permissionIntent = PendingIntent.getBroadcast(
            reactContext,
            0,
            Intent(DeviceScanner.ACTION_USB_PERMISSION),
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_MUTABLE else 0
          )
          val filter = IntentFilter(DeviceScanner.ACTION_USB_PERMISSION)
          reactContext.registerReceiver(usbReceiver, filter)
          usbManager.requestPermission(it.device, permissionIntent)
        }
      }
      promise.resolve(true)
      return
    }
  }

  @ReactMethod
  @Suppress("unused")
  fun getUsbPrintersCount(promise: Promise) {
    val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager
    UsbPrintersConnectionsManager(reactContext).list?.size?.let {
      promise.resolve(it)
      return
    }
    promise.resolve(0)
  }

  private fun checkPermission(permission: String): Boolean {
    if (ActivityCompat.checkSelfPermission(
        reactContext,
        permission
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      return false
    }
    return true
  }

  @ReactMethod
  @Suppress("unused")
  fun testConnection(config: ReadableMap, promise: Promise) {
    val printer = resolvePrinter(config)
    printer?.let {
      try {
        return
      } catch (e: Exception) {
        promise.reject(e)
        return
      }
    }
    promise.resolve(true)
  }

  @ReactMethod
  @Suppress("unused")
  fun getPrinterModel(config: ReadableMap, promise: Promise) {
    val printer = resolvePrinter(config)
    printer?.let {
      try {
        val model = printer.getPrinterModel()
        promise.resolve(model)
        return
      } catch (e: Exception) {
        promise.reject(e)
        return
      }
    }
    promise.resolve("")
  }

  @ReactMethod
  @Suppress("unused")
  fun prunePrintingWorks() {
    WorkManager.getInstance(reactContext).cancelAllWork()
    WorkManager.getInstance(reactContext).pruneWork()
  }

  private fun resolvePrinter(config: ReadableMap): EscPosPrinter? {
    return resolvePrinter(PrinterSelectorArgument(config))
  }

  private fun resolvePrinter(config: PrinterSelectorArgument): EscPosPrinter? {
    var connection: DeviceConnection? = null
    when (config.connection) {
      PRINTER_CONNECTION_NETWORK -> {
        connection = TcpConnection(config.address, config.port)
      }

      PRINTER_CONNECTION_BLUETOOTH -> {
        connection =
          BluetoothPrintersConnectionsManager.selectByDeviceAddress(reactContext, config.address)
      }

      PRINTER_CONNECTION_USB -> {
        connection = UsbPrintersConnectionsManager.selectByDeviceName(reactContext, config.address)
      }

      PRINTER_CONNECTION_SERIAL -> {
        connection = SerialConnectionsManager.selectByDeviceName(config.address, config.baudrate)
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

  private fun emitEventToRNSide(eventName: String, params: WritableMap?) {
    reactContext
      .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter::class.java)
      .emit(eventName, params)
  }

  private fun emitScanOtherEvent(scanType: Int, event: String, serviceName: String?) {
    val eventParams = Arguments.createMap().apply {
      putInt("scanType", scanType)
    }
    eventParams.putString("event", event)
    eventParams.putString("serviceName", serviceName)
    emitEventToRNSide(DeviceScanner.EVENT_OTHER, eventParams)
  }

  private val usbReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      val action = intent.action
      if (DeviceScanner.ACTION_USB_PERMISSION == action) {
        synchronized(this) {
          val usbManager = reactContext.getSystemService(Context.USB_SERVICE) as UsbManager?
          val usbDevice =
            intent.getParcelableExtra<Parcelable>(UsbManager.EXTRA_DEVICE) as UsbDevice?
          if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
            emitScanOtherEvent(DeviceScanner.SCAN_USB, "permissionGranted", usbDevice?.deviceName)
          } else {
            emitScanOtherEvent(DeviceScanner.SCAN_USB, "permissionDenied", usbDevice?.deviceName)
          }
        }
      }
    }
  }

  // Required for rn built in EventEmitter Calls.
  @ReactMethod
  @Suppress("unused")
  fun addListener(eventName: String) {
    if (mListenerCount == 0) {
      Log.d(LOG_TAG, "Does Nothing")
    }
    mListenerCount += 1
  }

  @ReactMethod
  @Suppress("unused")
  fun removeListeners(count: Int) {
    if (mListenerCount > 0) mListenerCount -= count
    if (mListenerCount == 0) {
      Log.d(LOG_TAG, "Does Nothing")
    }
  }

  @ReactMethod
  @Suppress("unused")
  fun getAllJobs(promise: Promise) {
    val list = WorkManager.getInstance(reactContext)
      .getWorkInfosByTag(PrintingJobsManager.PRINTING_JOB_TAG)
    val array = Arguments.createArray().apply {
      list.get().forEach { workInfo ->
        pushMap(WorkerEventData.fromWorkInfo(workInfo).writableMap)
      }
    }
    promise.resolve(array)
  }

  private fun observeWork(uuid: UUID) {
    GlobalScope.launch(Dispatchers.Main) {
      WorkManager.getInstance(reactContext)
        // requestId is the WorkRequest id
        .getWorkInfoByIdLiveData(uuid)
        .observe(currentActivity as LifecycleOwner, Observer { workInfo ->
          val workerData = WorkerEventData.fromWorkInfo(workInfo)
          val eventData = workerData.writableMap
          when (workInfo.state) {
            WorkInfo.State.ENQUEUED -> {
              emitEventToRNSide(EVENT_PRINTING_JOB, eventData)
            }

            WorkInfo.State.RUNNING -> {
              emitEventToRNSide(EVENT_PRINTING_JOB, eventData)
            }

            WorkInfo.State.SUCCEEDED -> {
              emitEventToRNSide(EVENT_PRINTING_JOB, eventData)
              removeWork(workInfo.id, workerData.file)
            }

            WorkInfo.State.FAILED -> {
              emitEventToRNSide(EVENT_PRINTING_JOB, eventData)
              removeWork(workInfo.id, workerData.file)
            }

            WorkInfo.State.BLOCKED -> {
              emitEventToRNSide(EVENT_PRINTING_JOB, eventData)
            }

            WorkInfo.State.CANCELLED -> {
              emitEventToRNSide(EVENT_PRINTING_JOB, eventData)
            }
          }
        })
    }
  }

  private fun removeWork(id: UUID, file: String?) {
    PrintingJobsManager.getInstance(reactContext).cancelWork(id)
    if (file.isNullOrEmpty()) {
      return;
    }
    try {
      val fileD = File(file)
      if (fileD.exists()) {
        fileD.delete()
      }
    } catch (error: IOException) {
      error.printStackTrace()
    }
  }
}
