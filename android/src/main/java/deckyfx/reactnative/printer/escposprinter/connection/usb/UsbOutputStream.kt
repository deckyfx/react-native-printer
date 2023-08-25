package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbManager
import android.hardware.usb.UsbRequest
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbDeviceHelper.findEndpointIn
import deckyfx.reactnative.printer.escposprinter.connection.usb.UsbDeviceHelper.findPrinterInterface
import java.io.IOException
import java.io.OutputStream
import java.nio.ByteBuffer


class UsbOutputStream(
  usbManager: UsbManager,
  usbDevice: UsbDevice?,
  private val readable: Boolean
) : OutputStream() {
  constructor(usbManager: UsbManager, usbDevice: UsbDevice?) : this(usbManager, usbDevice, false)

  private var usbConnection: UsbDeviceConnection?
  private var usbInterface: UsbInterface?
  private var usbOutEndpoint: UsbEndpoint?
  private var usbInEndpoint: UsbEndpoint?

  init {
    usbInterface = findPrinterInterface(usbDevice)
    if (usbInterface == null) {
      throw IOException("Unable to find USB interface.")
    }
    val endPoints = findEndpointIn(usbInterface)
    usbOutEndpoint = endPoints?.first
    usbInEndpoint = endPoints?.second
    if (usbOutEndpoint == null) {
      throw IOException("Unable to find USB endpoint.")
    }
    usbConnection = usbManager.openDevice(usbDevice)
    if (usbConnection == null) {
      throw IOException("Unable to open USB connection.")
    }
  }

  @Throws(IOException::class)
  override fun write(i: Int) {
    write(byteArrayOf(i.toByte()))
  }

  @Throws(IOException::class)
  override fun write(bytes: ByteArray) {
    write(bytes, 0, bytes.size)
  }

  @Throws(IOException::class)
  override fun write(bytes: ByteArray, offset: Int, length: Int) {
    writeAndWaitResponse(bytes, offset, length, false)
  }

  @Throws(IOException::class)
  fun write(bytes: ByteArray, waitResponse: Boolean): ByteArray? {
    return writeAndWaitResponse(bytes, waitResponse)
  }

  @Throws(IOException::class)
  fun writeAndWaitResponse(bytes: ByteArray, waitResponse: Boolean): ByteArray? {
    return writeAndWaitResponse(bytes, 0, bytes.size, waitResponse)
  }

  @Throws(IOException::class)
  @Suppress("DEPRECATION")
  fun writeAndWaitResponse(
    bytes: ByteArray,
    offset: Int,
    length: Int,
    waitResponse: Boolean
  ): ByteArray? {
    if (usbInterface == null || usbOutEndpoint == null || usbConnection == null) {
      throw IOException("Unable to connect to USB device.")
    }
    if (!usbConnection!!.claimInterface(usbInterface, true)) {
      throw IOException("Error during claim USB interface.")
    }
    val outBuffer = ByteBuffer.wrap(bytes)
    val inBuffer = ByteBuffer.allocate(bytes.size)
    val usbRequest = UsbRequest()
    try {
      usbRequest.initialize(usbConnection, usbOutEndpoint)
      if (!usbRequest.queue(outBuffer, bytes.size)) {
        throw IOException("Error queueing USB request.")
      }
      if (readable && waitResponse) {
        // Receive data from device
        if (usbRequest == usbConnection!!.requestWait()) {
          val inRequest = UsbRequest()
          inRequest.initialize(usbConnection, usbInEndpoint)
          if (inRequest.queue(inBuffer, 100)) {
            usbConnection!!.requestWait()
            return inBuffer.array()
          }
        }
      } else {
        usbConnection!!.requestWait()
      }
    } finally {
      usbRequest.close()
    }
    return null
  }

  @Throws(IOException::class)
  override fun flush() {
  }

  @Throws(IOException::class)
  override fun close() {
    if (usbConnection != null) {
      usbConnection!!.close()
      usbInterface = null
      usbOutEndpoint = null
      usbConnection = null
    }
  }

  // http://www.java2s.com/Tutorials/Android/Android_How_to/USB/Read_USB_device_information.htm
  fun getDeviceStatus(): String? {
    if (usbConnection == null) {
      return null
    }
    //Create a sufficiently large buffer for incoming data
    val buffer = ByteArray(LENGTH)
    usbConnection!!.controlTransfer(
      REQUEST_TYPE, REQUEST, REQ_VALUE, REQ_INDEX,
      buffer, LENGTH, 2000
    )
    //Parse received data into a description
    return parseConfigDescriptor(buffer)
  }

  private fun parseConfigDescriptor(buffer: ByteArray): String {
    val sb = StringBuilder()
    //Parse configuration descriptor header
    var totalLength = buffer[3].toInt() and 0xFF shl 8
    totalLength += buffer[2].toInt() and 0xFF
    //Interface count
    val numInterfaces = buffer[5].toInt() and 0xFF
    //Configuration attributes
    val attributes = buffer[7].toInt() and 0xFF
    //Power is given in 2mA increments
    val maxPower = (buffer[8].toInt() and 0xFF) * 2
    sb.append("Configuration Descriptor:\n")
    sb.append("Length: $totalLength bytes\n")
    sb.append("$numInterfaces Interfaces\n")
    sb.append(
      String.format(
        "Attributes:%s%s%s\n",
        if (attributes and 0x80 == 0x80) " BusPowered" else "",
        if (attributes and 0x40 == 0x40) " SelfPowered" else "",
        if (attributes and 0x20 == 0x20) " RemoteWakeup" else ""
      )
    )
    sb.append(
      """
        Max Power: ${maxPower}mA

        """.trimIndent()
    )

    //The rest of the descriptor is interfaces and endpoints
    var index = DESC_SIZE_CONFIG
    while (index < totalLength) {
      //Read length and type
      val len = buffer[index].toInt() and 0xFF
      val type = buffer[index + 1].toInt() and 0xFF
      when (type) {
        0x04 -> {
          val intfNumber = buffer[index + 2].toInt() and 0xFF
          val numEndpoints = buffer[index + 4].toInt() and 0xFF
          val intfClass = buffer[index + 5].toInt() and 0xFF
          val className = nameForClass(intfClass)
          val data = "- Interface $intfNumber, $className, $numEndpoints Endpoints\n"
          sb.append(data)
        }

        0x05 -> {
          val endpointAddr = buffer[index + 2].toInt() and 0xFF
          //Number is lower 4 bits
          val endpointNum = endpointAddr and 0x0F
          //Direction is high bit
          val direction = endpointAddr and 0x80
          val endpointAttrs = buffer[index + 3].toInt() and 0xFF
          //Type is the lower two bits
          val endpointType = endpointAttrs and 0x3
          val endPointName = nameForEndpointType(endpointType)
          val directionName = nameForDirection(direction)
          val data = "-- Endpoint $endpointNum, $endPointName $directionName\n"
          sb.append(data)
        }
      }
      //Advance to next descriptor
      index += len
    }
    return sb.toString()
  }

  private fun nameForClass(classType: Int): String {
    return when (classType) {
      UsbConstants.USB_CLASS_APP_SPEC -> String.format("Application Specific 0x%02x", classType)
      UsbConstants.USB_CLASS_AUDIO -> "Audio"
      UsbConstants.USB_CLASS_CDC_DATA -> "CDC Control"
      UsbConstants.USB_CLASS_COMM -> "Communications"
      UsbConstants.USB_CLASS_CONTENT_SEC -> "Content Security"
      UsbConstants.USB_CLASS_CSCID -> "Content Smart Card"
      UsbConstants.USB_CLASS_HID -> "Human Interface Device"
      UsbConstants.USB_CLASS_HUB -> "Hub"
      UsbConstants.USB_CLASS_MASS_STORAGE -> "Mass Storage"
      UsbConstants.USB_CLASS_MISC -> "Wireless Miscellaneous"
      UsbConstants.USB_CLASS_PER_INTERFACE -> "(Defined Per Interface)"
      UsbConstants.USB_CLASS_PHYSICA -> "Physical"
      UsbConstants.USB_CLASS_PRINTER -> "Printer"
      UsbConstants.USB_CLASS_STILL_IMAGE -> "Still Image"
      UsbConstants.USB_CLASS_VENDOR_SPEC -> String.format("Vendor Specific 0x%02x", classType)
      UsbConstants.USB_CLASS_VIDEO -> "Video"
      UsbConstants.USB_CLASS_WIRELESS_CONTROLLER -> "Wireless Controller"
      else -> String.format("0x%02x", classType)
    }
  }

  private fun nameForEndpointType(type: Int): String {
    return when (type) {
      UsbConstants.USB_ENDPOINT_XFER_BULK -> "Bulk"
      UsbConstants.USB_ENDPOINT_XFER_CONTROL -> "Control"
      UsbConstants.USB_ENDPOINT_XFER_INT -> "Interrupt"
      UsbConstants.USB_ENDPOINT_XFER_ISOC -> "Isochronous"
      else -> "Unknown Type"
    }
  }

  private fun nameForDirection(direction: Int): String {
    return when (direction) {
      UsbConstants.USB_DIR_IN -> "IN"
      UsbConstants.USB_DIR_OUT -> "OUT"
      else -> "Unknown Direction"
    }
  }

  companion object {
    private val REQUEST_TYPE = 0x80

    //Request: GET_CONFIGURATION_DESCRIPTOR = 0x06
    private val REQUEST = 0x06

    //Value: Descriptor Type (High) and Index (Low)
    // Configuration Descriptor = 0x2
    // Index = 0x0 (First configuration)
    private val REQ_VALUE = 0x200
    private val REQ_INDEX = 0x00
    private val LENGTH = 64

    private const val DESC_SIZE_CONFIG = 9
  }
}
