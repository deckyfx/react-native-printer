package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import java.io.IOException
import java.io.InputStream

class UsbInputStream(
  usbManager: UsbManager,
  usbDevice: UsbDevice?,
) : InputStream() {
  private var usbConnection: UsbDeviceConnection?
  private var usbInterface: UsbInterface?
  private var usbEndpoint: UsbEndpoint?
  private var readBuffer: CircularByteBuffer? = null
  private var working = false
  private var receiveThread: Thread? = null

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
  override fun read(buffer: ByteArray): Int {
    return read(buffer, 0, buffer.size)
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#read(byte[], int, int)
	 */
  @Throws(IOException::class)
  override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
    if (readBuffer == null) throw IOException(ERROR_THREAD_NOT_INITIALIZED)
    val deadLine = System.currentTimeMillis() + READ_TIMEOUT
    var readBytes = 0
    while (System.currentTimeMillis() < deadLine && readBytes <= 0) readBytes =
      readBuffer!!.read(buffer, offset, length)
    if (readBytes <= 0) return -1
    val readData = ByteArray(readBytes)
    System.arraycopy(buffer, offset, readData, 0, readBytes)
    return readBytes
  }
  fun doRead(buffer: ByteArray, offset: Int, length: Int) {

  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#available()
	 */
  @Throws(IOException::class)
  override fun available(): Int {
    if (readBuffer == null) throw IOException(ERROR_THREAD_NOT_INITIALIZED)
    return readBuffer!!.availableToRead()
  }

  /*
	 * (non-Javadoc)
	 * @see java.io.InputStream#skip(long)
	 */
  @Throws(IOException::class)
  override fun skip(byteCount: Long): Long {
    if (readBuffer == null) {throw IOException(ERROR_THREAD_NOT_INITIALIZED)}
    return readBuffer!!.skip(byteCount.toInt()).toLong()
  }

  /**
   * Starts the USB input stream read thread to start reading data from the
   * USB Android connection.
   *
   * @see .stopReadThread
   */
  fun startReadThread() {
    if (!working) {
      readBuffer = CircularByteBuffer(READ_BUFFER_SIZE)
      receiveThread = object : Thread() {
        override fun run() {
          working = true
          while (working) {
            val buffer = ByteArray(1024)
            val receivedBytes = usbConnection!!.bulkTransfer(
              usbEndpoint,
              buffer,
              buffer.size,
              READ_TIMEOUT
            ) - OFFSET
            if (receivedBytes > 0) {
              val data = ByteArray(receivedBytes)
              System.arraycopy(buffer, OFFSET, data, 0, receivedBytes)
              readBuffer!!.write(buffer, OFFSET, receivedBytes)
            }
          }
        }
      }
      receiveThread!!.start()
    }
  }

  /**
   * Stops the USB input stream read thread.
   *
   * @see .startReadThread
   */
  fun stopReadThread() {
    working = false
    if (receiveThread != null) receiveThread!!.interrupt()
  }

  companion object {
    // Constants.
    private const val READ_BUFFER_SIZE = 1024
    private const val OFFSET = 0
    private const val READ_TIMEOUT = 100000
    private const val ERROR_THREAD_NOT_INITIALIZED =
      "Read thread not initialized, call first 'startReadThread()'"
  }

  override fun close() {
    super.close()
    stopReadThread()
  }
}
