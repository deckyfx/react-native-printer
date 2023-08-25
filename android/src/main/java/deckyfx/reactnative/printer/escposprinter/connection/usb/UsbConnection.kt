package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

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
      usbInputStream!!.startReadThread()
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

  fun getDeviceStatus(): String? {
    if (usbOutputStream == null) {
      return null
    }
    return usbOutputStream!!.getDeviceStatus()
  }
}
