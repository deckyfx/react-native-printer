package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.hardware.usb.UsbConstants
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbEndpoint
import android.hardware.usb.UsbInterface

object UsbDeviceHelper {
  /**
   * Find the correct USB interface for printing
   *
   * @param usbDevice USB device
   * @return correct USB interface for printing, null if not found
   */
  @JvmStatic
  fun findPrinterInterface(usbDevice: UsbDevice?): UsbInterface? {
    if (usbDevice == null) {
      return null
    }
    val interfacesCount = usbDevice.interfaceCount
    for (i in 0 until interfacesCount) {
      val usbInterface = usbDevice.getInterface(i)
      if (usbInterface.interfaceClass == UsbConstants.USB_CLASS_PRINTER) {
        return usbInterface
      }
    }
    return null
  }

  /**
   * Find the USB endpoint for device input
   *
   * @param usbInterface USB interface
   * @return Input endpoint or null if not found
   */
  @JvmStatic
  fun findEndpointIn(usbInterface: UsbInterface?): Pair<UsbEndpoint?, UsbEndpoint?>? {
    if (usbInterface != null) {
      val endpointsCount = usbInterface.endpointCount
      var resultEndpoint0: UsbEndpoint? = null
      var resultEndpoint1: UsbEndpoint? = null
      for (i in 0 until endpointsCount) {
        val endpoint = usbInterface.getEndpoint(i)
        if (endpoint.type == UsbConstants.USB_ENDPOINT_XFER_BULK && endpoint.direction == UsbConstants.USB_DIR_OUT) {
          resultEndpoint0 = endpoint
        } else {
          resultEndpoint1 = endpoint
        }
      }
      return Pair(resultEndpoint0, resultEndpoint1)
    }
    return null
  }
}
