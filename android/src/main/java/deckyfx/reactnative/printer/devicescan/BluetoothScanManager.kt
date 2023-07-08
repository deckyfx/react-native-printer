package deckyfx.reactnative.printer.devicescan

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.os.Parcelable
import androidx.core.app.ActivityCompat

class BluetoothScanManager(
  private val context: Context,
  private val bluetoothAdapter: BluetoothAdapter
) {
  private var onBluetoothScanListener: OnBluetoothScanListener? = null
  private var mIsRunning = false
  private var mConnectedListener: Intent? = null
  private var mDisconnectedListener: Intent? = null
  private val bluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner
  private var scanningLe = false
  private val handler = Handler(Looper.getMainLooper())

  interface OnBluetoothScanListener {
    fun deviceFound(bluetoothDevice: BluetoothDevice?)
    fun deviceStateChanged(bluetoothDevice: BluetoothDevice?, state: BluetoothEvent)
    fun startScan()
    fun stopScan()
    fun error(error: Exception)
  }

  /**
   * Start Scanning for discoverable devices
   */
  fun startScan() {
    if (mIsRunning) return
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_SCAN
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      onBluetoothScanListener?.error(java.lang.Exception("No Permission for BLUETOOTH_SCAN"))
      return
    }
    mIsRunning = true
    onBluetoothScanListener?.startScan()

    startBluetoothConnectionListener()

    // Bluetooth Device
    bluetoothAdapter.startDiscovery()

    // BLE Bluetooth Device
    if (!scanningLe) { // Stops scanning after a pre-defined scan period.
      handler.postDelayed({
        scanningLe = false
        bluetoothLeScanner.stopScan(leScanCallback)
      }, BLE_SCAN_PERIOD)
      scanningLe = true
      bluetoothLeScanner.startScan(leScanCallback)
    } else {
      scanningLe = false
      bluetoothLeScanner.stopScan(leScanCallback)
    }
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    if (ActivityCompat.checkSelfPermission(
        context,
        Manifest.permission.BLUETOOTH_SCAN
      ) != PackageManager.PERMISSION_GRANTED
    ) {
      onBluetoothScanListener?.error(java.lang.Exception("No Permission for BLUETOOTH_SCAN"))
      return
    }

    mIsRunning = false
    onBluetoothScanListener?.stopScan()

    // Stop Bluetooth discovery
    bluetoothAdapter.cancelDiscovery()

    // Stop Bluetooth BLE scan
    bluetoothLeScanner.stopScan(leScanCallback)

    stopBluetoothConnectionListener()
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
    context.unregisterReceiver(broadcastReceiver)
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

  private fun startBluetoothConnectionListener() {
    stopBluetoothConnectionListener()
    mConnectedListener = context.registerReceiver(bluetoothConnectionEventListener,
      IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
    mDisconnectedListener = context.registerReceiver(bluetoothConnectionEventListener,
      IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))

  }

  private fun stopBluetoothConnectionListener() {
    context.unregisterReceiver(bluetoothConnectionEventListener)
    context.unregisterReceiver(bluetoothConnectionEventListener)
  }

  /**
   * Bluetooth Connection Event Listener
   */
  private val bluetoothConnectionEventListener: BroadcastReceiver = object : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
      if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
        return
      }
      val callbackAction = intent.action
      val bluetoothDevice = intent.getParcelableExtra<Parcelable>(BluetoothDevice.EXTRA_DEVICE) as BluetoothDevice?

      // if action or bluetooth data is null
      if (callbackAction == null || bluetoothDevice == null) {
        // do not proceed
        return
      }

      // hold value for bluetooth event
      val bluetoothEvent: BluetoothEvent = when (callbackAction) {
        BluetoothDevice.ACTION_ACL_CONNECTED -> BluetoothEvent.CONNECTED
        BluetoothDevice.ACTION_ACL_DISCONNECTED -> BluetoothEvent.DISCONNECTED
        else -> BluetoothEvent.NONE
      }

      // bluetooth event must not be null
      if (bluetoothEvent != BluetoothEvent.NONE) {
        onBluetoothScanListener?.deviceStateChanged(bluetoothDevice, bluetoothEvent)
      }
    }
  }

  /**
   * Bluetooth BLE Scan Event Listener
   */
  private val leScanCallback: ScanCallback = object : ScanCallback() {
    override fun onScanResult(callbackType: Int, result: ScanResult) {
      super.onScanResult(callbackType, result)
      onBluetoothScanListener?.deviceFound(result.device)
    }

    override fun onBatchScanResults(results: MutableList<ScanResult>?) {
      super.onBatchScanResults(results)
    }

    override fun onScanFailed(errorCode: Int) {
      val error = "BLE scan failed with code: $errorCode"
      onBluetoothScanListener?.error(java.lang.Exception(error))
    }
  }

  companion object {
    private val LOG_TAG = BluetoothScanManager::class.java.simpleName
    private const val BLE_SCAN_PERIOD: Long = 10000
  }

   enum class BluetoothEvent {
    CONNECTED, DISCONNECTED, DEVICE_FOUND, NONE
  }
}
