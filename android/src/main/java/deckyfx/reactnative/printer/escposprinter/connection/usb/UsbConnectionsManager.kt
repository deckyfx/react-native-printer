package deckyfx.reactnative.printer.escposprinter.connection.usb

import android.content.Context
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbManager

open class UsbConnectionsManager(context: Context) {
    @JvmField
    protected var usbManager: UsbManager?

    /**
     * Create a new instance of UsbConnectionsManager
     *
     * @param context Application context
     */
    init {
        usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    }

    /**
     * Get a list of USB devices available.
     * @return Return an array of UsbConnection instance
     */
    open val list: Array<UsbConnection?>?
        get() {
            if (usbManager == null) {
                return null
            }
            val devicesList: Collection<UsbDevice> = usbManager!!.deviceList.values
            val usbDevices = arrayOfNulls<UsbConnection>(devicesList.size)
            if (devicesList.isNotEmpty()) {
                var i = 0
                for (device in devicesList) {
                    usbDevices[i++] = UsbConnection(usbManager!!, device)
                }
            }
            return usbDevices
        }
}
