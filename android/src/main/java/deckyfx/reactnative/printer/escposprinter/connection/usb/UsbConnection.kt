package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import com.facebook.react.bridge.Arguments
import com.facebook.react.bridge.WritableMap
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.nio.charset.StandardCharsets

class UsbConnection
/**
 * Create un instance of UsbConnection.
 *
 * @param usbManager an instance of UsbManager
 * @param usbDevice  an instance of UsbDevice
 */(
  private val usbManager: UsbManager,
  /**
   * Get the instance UsbDevice connected.
   *
   * @return an instance of UsbDevice
   */
  val device: UsbDevice
) : DeviceConnection() {
  var usbOutputStream: UsbOutputStream? = null
  var usbInputStream: UsbInputStream? = null

  override var outputStream: OutputStream? = null
    get() = usbOutputStream

  override var inputStream: InputStream? = null
    get() = usbInputStream

  /**
   * Start socket connection with the usbDevice.
   */
  @Throws(EscPosConnectionException::class)
  override fun connect(): UsbConnection {
    if (isConnected) {
      return this
    }
    try {
      usbOutputStream = UsbOutputStream(usbManager, device)
      usbInputStream = UsbInputStream(usbManager, device)
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      usbOutputStream = null
      throw EscPosConnectionException("Unable to connect to USB device.")
    }
    return this
  }

  /**
   * Close the socket connection with the usbDevice.
   */
  override fun disconnect(): UsbConnection {
    data = ByteArray(0)
    if (isConnected) {
      try {
        usbOutputStream!!.close()
        usbInputStream!!.close()
      } catch (e: IOException) {
        e.printStackTrace()
      }
      usbOutputStream = null
      usbInputStream = null
    }
    return this
  }


  /**
   * Send data to the device. and =wait for response
   *
   * @param addWaitingTime add waiting time
   */
  @Throws(EscPosConnectionException::class)
  override fun sendAndWaitForResponse(addWaitingTime: Int): String? {
    if (!isConnected) {
      throw EscPosConnectionException("Unable to send data to device.")
    }
    var output = ""
    try {
      val bufferOut = PrintWriter(BufferedWriter(OutputStreamWriter(outputStream!!)), true)
      val message = data
      val payload = String(message, StandardCharsets.UTF_8)
      bufferOut.println(payload)
      bufferOut.flush()
      val waitingTime = addWaitingTime + data.size / 16
      data = ByteArray(0)
      if (waitingTime > 0) {
        Thread.sleep(waitingTime.toLong())
      }
      val buffer = ByteArray(1024)
      var read: Int
      usbInputStream!!.close() // USB Connection must be closed before can read
      while (usbInputStream!!.read(buffer).also { read = it } != -1) {
        output = String(buffer, 0, read)
        break
      }
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    } catch (e: InterruptedException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    } finally {
        usbInputStream!!.close()
        usbOutputStream = UsbOutputStream(usbManager, device)
        usbInputStream = UsbInputStream(usbManager, device)
        return output
    }
    return null
  }

  @Throws(EscPosConnectionException::class)
  override fun getPrinterModel(): String? {
    if (!isConnected) {
      throw EscPosConnectionException("Unable to send data to device.")
    }
    if (outputStream == null) {
      return null
    }
    // Standard implementation
    write(byteArrayOf(0x1D, 0x49, 0x42))
    val a = sendAndWaitForResponse(100)
    write(byteArrayOf( 0x1D, 0x49, 0x43))
    val b = sendAndWaitForResponse(100)
    var c = a?.replace("\u0000", "") + b?.replace("\u0000", "")
    if (!c.isNullOrBlank()) {
      c = c.removePrefix("_")
      c = c.replace("_", " ")
    }
    return c
  }

  fun getDeviceStatusMap(): WritableMap {
    if (usbOutputStream == null) {
      return Arguments.createMap()
    }
    return usbOutputStream!!.getDeviceStatusMap()
  }

  fun getDeviceStatus(): String? {
    if (usbOutputStream == null) {
      return null
    }
    return usbOutputStream!!.getDeviceStatus()
  }
}
