package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.IOException
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

  override var outputStream: OutputStream? = null
    get() = usbOutputStream

  /**
   * Start socket connection with the usbDevice.
   */
  @Throws(EscPosConnectionException::class)
  override fun connect(): UsbConnection {
    if (isConnected) {
      return this
    }
    try {
      usbOutputStream = UsbOutputStream(usbManager, device, true)
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
      } catch (e: IOException) {
        e.printStackTrace()
      }
      usbOutputStream = null
    }
    return this
  }

  /**
   * Send data to the device.
   */
  @Throws(EscPosConnectionException::class)
  override fun send() {
    this.send(0)
  }

  /**
   * Send data to the device.
   */
  @Throws(EscPosConnectionException::class)
  override fun send(addWaitingTime: Int) {
    try {
      usbOutputStream!!.write(data)
      data = ByteArray(0)
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    }
  }


  /**
   * Send data to the device.and wait for response
   */
  @Throws(EscPosConnectionException::class)
  override fun sendAndWaitForResponse() {
    sendAndWaitForResponse(0)
  }

  /**
   * Send data to the device.and wait for response
   */
  override fun sendAndWaitForResponse(addWaitingTime: Int): String? {
    try {
      val result = usbOutputStream!!.write(data, true)
      data = ByteArray(0)
      return result?.let { String(it) }
    } catch (e: IOException) {
      e.printStackTrace()
      throw EscPosConnectionException(e.message)
    }
  }

  fun getDeviceStatus(): String? {
    if (usbOutputStream == null) {
      return null
    }
    return usbOutputStream!!.getDeviceStatus()
  }

  override fun getPrinterModel(): String? {
    return getDeviceStatus()
  }
}
