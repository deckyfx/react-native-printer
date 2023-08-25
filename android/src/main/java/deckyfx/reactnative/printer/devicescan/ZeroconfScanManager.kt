package deckyfx.reactnative.printer.devicescan

import android.annotation.SuppressLint
import android.content.Context
import android.net.nsd.NsdManager
import android.net.nsd.NsdServiceInfo
import android.net.wifi.WifiManager
import android.os.Build
import com.facebook.react.bridge.WritableArray
import com.facebook.react.bridge.WritableMap
import com.facebook.react.bridge.WritableNativeArray
import com.facebook.react.bridge.WritableNativeMap
import java.io.UnsupportedEncodingException
import java.nio.charset.Charset
import java.util.Locale

class ZeroconfScanManager(
  private val context: Context,
  private val nsdManager: NsdManager
) {
  var onZeroconfScanListener: OnZeroconfScanListener? = null
  private var mIsRunning = false

  private var multicastLock: WifiManager.MulticastLock? = null
  private var mDiscoveryListener: NsdManager.DiscoveryListener? = null

  interface OnZeroconfScanListener {
    fun serviceFound(serviceName: String?)
    fun serviceLost(serviceName: String?)
    fun serviceResolved(service: WritableMap)
    fun startScan()
    fun stopScan()
    fun error(error: Exception)
  }

  /**
   * Start Scanning for discoverable devices
   */
  fun startScan() {
    if (mIsRunning) return
    if (multicastLock == null) {
      @SuppressLint("WifiManagerLeak")
      val wifi = context.applicationContext.getSystemService(
        Context.WIFI_SERVICE
      ) as WifiManager
      multicastLock = wifi.createMulticastLock("multicastLock")
      multicastLock?.setReferenceCounted(true)
      multicastLock?.acquire()
    }
    mDiscoveryListener = object : NsdManager.DiscoveryListener {
      override fun onStartDiscoveryFailed(serviceType: String, errorCode: Int) {
        mIsRunning = false
        val error =
          "Starting service discovery failed with code: $errorCode"
        onZeroconfScanListener?.error(Exception(error))
      }

      override fun onStopDiscoveryFailed(serviceType: String, errorCode: Int) {
        val error =
          "Stopping service discovery failed with code: $errorCode"
        onZeroconfScanListener?.error(Exception(error))
      }

      override fun onDiscoveryStarted(serviceType: String) {
        mIsRunning = true
        onZeroconfScanListener?.startScan()
      }

      override fun onDiscoveryStopped(serviceType: String) {
        mIsRunning = false
        onZeroconfScanListener?.stopScan()
      }

      override fun onServiceFound(serviceInfo: NsdServiceInfo) {
        onZeroconfScanListener?.serviceFound(serviceInfo.serviceName)
        nsdManager.resolveService(serviceInfo, ZeroResolveListener())
      }

      override fun onServiceLost(serviceInfo: NsdServiceInfo) {
        onZeroconfScanListener?.serviceLost(serviceInfo.serviceName)
      }
    }
    val serviceType = String.format("_%s._%s.", SCAN_TYPE, SCAN_PROTOCOL)
    nsdManager.discoverServices(serviceType, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener)
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    if (mDiscoveryListener != null) {
      nsdManager.stopServiceDiscovery(mDiscoveryListener)
    }
    multicastLock?.release()
    mDiscoveryListener = null
    multicastLock = null
    mIsRunning = false
    onZeroconfScanListener?.stopScan()
  }

  private inner class ZeroResolveListener : NsdManager.ResolveListener {
    override fun onResolveFailed(serviceInfo: NsdServiceInfo, errorCode: Int) {
      if (errorCode == NsdManager.FAILURE_ALREADY_ACTIVE) {
        nsdManager.resolveService(serviceInfo, this)
      } else {
        val error = "Resolving service failed with code: $errorCode"
        onZeroconfScanListener?.error(Exception(error))
      }
    }

    override fun onServiceResolved(serviceInfo: NsdServiceInfo) {
      val service = serviceInfoToMap(serviceInfo)
      onZeroconfScanListener?.serviceResolved(service)
    }
  }

  @SuppressLint("ObsoleteSdkInt")
  private fun serviceInfoToMap(serviceInfo: NsdServiceInfo): WritableMap {
    val service: WritableMap = WritableNativeMap()
    service.putString(KEY_SERVICE_NAME, serviceInfo.serviceName)
    val host = serviceInfo.host
    val fullServiceName: String
    if (host == null) {
      fullServiceName = serviceInfo.serviceName
    } else {
      fullServiceName = host.hostName + serviceInfo.serviceType
      service.putString(KEY_SERVICE_HOST, host.hostName)
      val addresses: WritableArray = WritableNativeArray()
      addresses.pushString(host.hostAddress)
      service.putArray(KEY_SERVICE_ADDRESSES, addresses)
    }
    service.putString(KEY_SERVICE_FULL_NAME, fullServiceName)
    service.putInt(KEY_SERVICE_PORT, serviceInfo.port)
    val txtRecords: WritableMap = WritableNativeMap()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      val attributes = serviceInfo.attributes
      for (key in attributes.keys) {
        try {
          val recordValue = attributes[key]
          txtRecords.putString(
            String.format(Locale.getDefault(), "%s", key), String.format(
              Locale.getDefault(),
              "%s",
              if (recordValue != null) String(recordValue, Charset.forName("UTF-8")) else ""
            )
          )
        } catch (e: UnsupportedEncodingException) {
          val error = "Failed to encode txtRecord: $e"
          onZeroconfScanListener?.error(Exception(error))
        }
      }
    }
    service.putMap(KEY_SERVICE_TXT, txtRecords)
    return service
  }

  companion object {
    private val LOG_TAG = ZeroconfScanManager::class.java.simpleName
    const val SCAN_TYPE = "ipp"
    const val SCAN_PROTOCOL = "tcp"
    const val SCAN_DOMAIN = "local."

    const val KEY_SERVICE_NAME = "name"
    const val KEY_SERVICE_FULL_NAME = "fullName"
    const val KEY_SERVICE_HOST = "host"
    const val KEY_SERVICE_PORT = "port"
    const val KEY_SERVICE_ADDRESSES = "addresses"
    const val KEY_SERVICE_TXT = "txt"
  }
}
