package deckyfx.reactnative.printer.devicescan

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import deckyfx.reactnative.printer.escposprinter.connection.bluetooth.BluetoothPrintersConnectionsManager

class BluetoothScanManager(
  private val context: Context,
) {
  private var onBluetoothScanListener: OnBluetoothScanListener? = null
  private var mIsRunning = false

  interface OnBluetoothScanListener {
    fun deviceFound(bluetoothDevice: BluetoothDevice?)
    fun startScan()
    fun stopScan()
    fun error(error: Exception)
  }

  /**
   * Start Scanning for discoverable devices
   */
  fun startScan() {
    if (mIsRunning) return
    val list = BluetoothPrintersConnectionsManager(context).list
    if (list.isNullOrEmpty()) {
      stopScan()
      return
    }
    list.forEach {
      if (it != null) onBluetoothScanListener?.deviceFound(it.device)
    }
    stopScan()
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    mIsRunning = false
    onBluetoothScanListener?.stopScan()
  }

  /**
   * Register Broadcast Receiver that will listen to ACTION_FOUND
   *
   * @param onBluetoothScanListener user's callback implementation
   */
  fun registerCallback(onBluetoothScanListener: OnBluetoothScanListener?) {
    this.onBluetoothScanListener = onBluetoothScanListener
    val intentFilterConnectionState = IntentFilter()
    intentFilterConnectionState.addAction(BluetoothDevice.ACTION_FOUND)
    intentFilterConnectionState.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
    intentFilterConnectionState.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
    context.registerReceiver(broadcastReceiver, intentFilterConnectionState)
  }

  /**
   * You must call this in OnDestroy() to unregister broadcast receiver
   */
  fun unregisterCallback() {
    try {
      context.unregisterReceiver(broadcastReceiver)
    } catch (e: Exception) {
      e.printStackTrace()
    }
  }

  /**
   * Broadcast Receiver that will receive ACTiON_FOUND and returned with Found
   * Bluetooth Devices
   */
  private val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      // Action must not be null
      // Action must equals to ACTION_FOUND
      if (intent.action != null && BluetoothDevice.ACTION_FOUND == intent.action) {
        // Extract BluetoothDevice found
        val bluetoothDevice =
          intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
        // Check if bluetoothDevice is null
        if (bluetoothDevice != null) {
          // Callback with device found
          onBluetoothScanListener?.deviceFound(bluetoothDevice)
        }
      }
    }
  }

  companion object {
    private val LOG_TAG = BluetoothScanManager::class.java.simpleName
  }
}
