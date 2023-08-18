package deckyfx.reactnative.printer.escposprinter.connection.bluetooth

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothSocket
import android.content.Context
import android.os.ParcelUuid
import deckyfx.reactnative.printer.escposprinter.connection.DeviceConnection
import deckyfx.reactnative.printer.escposprinter.exceptions.EscPosConnectionException
import java.io.IOException
import java.util.UUID

class BluetoothConnection
/**
 * Create un instance of BluetoothConnection.
 *
 * @param device an instance of BluetoothDevice
 */(
  /**
   * Get the instance BluetoothDevice connected.
   *
   * @return an instance of BluetoothDevice
   */
  val context: Context,
  val device: BluetoothDevice
) : DeviceConnection() {
    private var socket: BluetoothSocket? = null

    /**
     * Check if OutputStream is open.
     *
     * @return true if is connected
     */
    override val isConnected: Boolean
        get() = socket != null && socket!!.isConnected && super.isConnected

    /**
     * Start socket connection with the bluetooth device.
     */
    @SuppressLint("MissingPermission")
    @Throws(EscPosConnectionException::class)
    override fun connect(): BluetoothConnection {
        if (isConnected) {
            return this
        }
        if (device == null) {
            throw EscPosConnectionException("Bluetooth device is not connected.")
        }
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter =  bluetoothManager.adapter
        val uuid = deviceUUID
        try {
            socket = device.createRfcommSocketToServiceRecord(uuid)
            bluetoothAdapter.cancelDiscovery()
            if (socket == null) {
              throw IOException("No socket created")
            }
            socket?.connect()
            outputStream = socket?.outputStream
            inputStream = socket?.inputStream
            data = ByteArray(0)
        } catch (e: IOException) {
            e.printStackTrace()
            disconnect()
            throw EscPosConnectionException("Unable to connect to bluetooth device.")
        }
        return this
    }

    /**
     * Get bluetooth device UUID
     */
    private val deviceUUID: UUID
      @SuppressLint("MissingPermission")
      get() {
            val uuids = device.uuids
            return if (!uuids.isNullOrEmpty()) {
                if (listOf(*uuids)
                        .contains(ParcelUuid(SPP_UUID))
                ) {
                    SPP_UUID
                } else uuids[0].uuid
            } else {
                SPP_UUID
            }
        }

    /**
     * Close the socket connection with the bluetooth device.
     */
    override fun disconnect(): BluetoothConnection {
        data = ByteArray(0)
        if (outputStream != null) {
            try {
                outputStream!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            outputStream = null
        }
        if (inputStream != null) {
          try {
            inputStream!!.close()
          } catch (e: IOException) {
            e.printStackTrace()
          }
          inputStream = null
        }
        if (socket != null) {
            try {
                socket!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            }
            socket = null
        }
        return this
    }

    companion object {
        private val SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb")
    }
}
