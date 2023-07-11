package deckyfx.reactnative.printer.deprecated

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat
import io.github.escposjava.print.Printer
import java.io.IOException
import java.io.OutputStream
import java.util.UUID

class BluetoothPrinter(address: String, private val context: Context) : Printer {
  private val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
  private val adapter: BluetoothAdapter = bluetoothManager.adapter
  private var printer: OutputStream? = null
  private val address: String

  init {
    this.address = address
  }

  @Throws(IOException::class)
  override fun open() {
    val device = adapter.getRemoteDevice(address)
    val socket = device.createRfcommSocketToServiceRecord(SPP_UUID)
    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
      return
    }
    socket.connect()
    printer = socket.outputStream
  }

  override fun write(command: ByteArray) {
    try {
      printer!!.write(command)
    } catch (e: IOException) {
      // TODO Auto-generated catch block
      e.printStackTrace()
    }
  }

  @Throws(IOException::class)
  override fun close() {
    printer!!.close()
  }

  companion object {
    private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
  }
}
