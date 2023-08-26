package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbRequest
import android.os.Build
import java.io.IOException
import java.io.InputStream
import java.nio.ByteBuffer


class UsbInputStream(
  usbManager: UsbManager,
  usbDevice: UsbDevice?,
) : InputStream() {
  private var usbConnection: UsbDeviceConnection?
  private var usbInterface: UsbInterface?
  private var usbEndpoint: UsbEndpoint?

  init {
    usbInterface = UsbDeviceHelper.findPrinterInterface(usbDevice)
    if (usbInterface == null) {
      throw IOException("Unable to find USB interface.")
    }
    val endPoints = UsbDeviceHelper.findEndpointIn(usbInterface)
    usbEndpoint = endPoints?.second
    if (usbEndpoint == null) {
      throw IOException("Unable to find USB endpoint.")
    }
    usbConnection = usbManager.openDevice(usbDevice)
    if (usbConnection == null) {
      throw IOException("Unable to open USB connection.")
    }
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read()
	 */
  @Throws(IOException::class)
  override fun read(): Int {
    val buffer = ByteArray(1)
    read(buffer)
    return buffer[0].toInt() and 0xFF
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[])
	 */
  @Throws(IOException::class)
  override fun read(bytes: ByteArray): Int {
    return read(bytes, 0, bytes.size)
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
  @Throws(IOException::class)
  override fun read(bytes: ByteArray, offset: Int, length: Int): Int {
    var readBytes = 0
    val inBuffer = ByteBuffer.wrap(bytes)
    val usbRequest = UsbRequest()
    try {
      usbRequest.initialize(usbConnection, usbEndpoint)
      usbConnection!!.claimInterface (usbInterface, true)
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        usbRequest.queue(inBuffer)
      }
      if (usbConnection!!.requestWait() === usbRequest) {
        readBytes = inBuffer.array().size
        val readData = ByteArray(readBytes)
        System.arraycopy(bytes, offset, readData, 0, readBytes)
      }
    } catch(error: Exception) {
      error.printStackTrace()
    } finally {
        usbRequest.close()
        val readData = ByteArray(readBytes)
        System.arraycopy(bytes, offset, readData, 0, readBytes)
        return readBytes
    }
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
  @Throws(IOException::class)
  override fun available(): Int {
    return 0
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
  @Throws(IOException::class)
  override fun skip(byteCount: Long): Long {
    return 0
  }
}
