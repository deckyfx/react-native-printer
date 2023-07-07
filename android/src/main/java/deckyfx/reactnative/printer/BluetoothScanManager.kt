package deckyfx.reactnative.printer

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

class BluetoothScanManager(private val context: Context, private val bluetoothAdapter: BluetoothAdapter) {
    private var onBluetoothScanListener: OnBluetoothScanListener? = null

    interface OnBluetoothScanListener {
        fun deviceFound(bluetoothDevice: BluetoothDevice?)
    }

    /**
     * Start Scanning for discoverable devices
     */
    fun startScan() {
        Log.d(LOG_TAG, "Start Scan.")
        if (onBluetoothScanListener == null) {
            Log.e(LOG_TAG, "You must call registerCallback(...) first!")
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothAdapter.startDiscovery()
    }

    /**
     * To Stop Scanning process
     */
    fun stopScan() {
        Log.d(LOG_TAG, "Stop Scan.")
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        bluetoothAdapter.cancelDiscovery()
    }

    /**
     * Register Broadcast Receiver that will listen to ACTION_FOUND
     *
     * @param onBluetoothScanListener user's callback implementation
     */
    fun registerCallback(onBluetoothScanListener: OnBluetoothScanListener?) {
        Log.d(LOG_TAG, "Register Callback")
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
        Log.d(LOG_TAG, "Unregister Callback")
        context.unregisterReceiver(broadcastReceiver)
    }

    /**
     * Broadcast Receiver that will receive ACTiON_FOUND and returned with Found
     * Bluetooth Devices
     */
    var broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            // Action must not be null
            // Action must equals to ACTION_FOUND
            if (intent.action != null && BluetoothDevice.ACTION_FOUND == intent.action) {
                // Extract BluetoothDevice found
                val bluetoothDevice = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                // Check if bluetoothDevice is null
                if (bluetoothDevice != null) {
                    // Callback with device found
                    onBluetoothScanListener!!.deviceFound(bluetoothDevice)
                }
            }
        }
    }

    companion object {
        private val LOG_TAG = BluetoothScanManager::class.java.simpleName
    }
}
