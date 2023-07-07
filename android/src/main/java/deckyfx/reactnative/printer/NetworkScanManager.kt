package deckyfx.reactnative.printer

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.IOException
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.PrintWriter
import java.net.Inet4Address
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.InterfaceAddress
import java.net.NetworkInterface
import java.net.Socket


class NetworkScanManager {
  var onNetworkScanListener: OnNetworkScanListener? = null

  interface OnNetworkScanListener {
    fun deviceFound(ip: String, port: Int)
  }

  /**
   * Start Scanning for discoverable devices
   */
  fun startScan() {
    Log.d(LOG_TAG, "Start Scan.")
    if (onNetworkScanListener == null) {
      Log.e(LOG_TAG, "You must call registerCallback(...) first!")
    }
    GlobalScope.launch { Dispatchers.IO
      val job = async { getIPV4Address() }
      val ip = job.await()
      ip?.let{ _ip ->
        val ipSegments = _ip.split(".").toMutableList()
        coroutineScope {
          (1..254).map { i ->
            async {
              ipSegments[3] = i.toString()
              val host = ipSegments.joinToString(".")
              val port = PRINTER_PORT
              val connected = socketConnect(host, port)
              if (connected) {
                Log.d(LOG_TAG, "$host:$port is reachable")
                onNetworkScanListener?.deviceFound(host, port)
              } else {
                Log.d(LOG_TAG, "$host:$port is unreachable")
              }
              connected
            }
          }.awaitAll()
          Log.d(LOG_TAG, "scan finished")
        }
      }
    }
  }

  /**
   * To Stop Scanning process
   */
  fun stopScan() {
    Log.d(LOG_TAG, "Stop Scan.")
  }

  @Suppress("SameParameterValue")
  private fun socketConnect(host: String?, port: Int? = PRINTER_PORT): Boolean {
    val socketAddress: InetSocketAddress = if (host != null) {
      InetSocketAddress(InetAddress.getByName(host), port!!)
    } else {
      InetSocketAddress(port!!)
    }
    return try {
      val socket = Socket()
      socket.connect(socketAddress, SOCKET_TIMEOUT)
      PrintWriter(BufferedWriter(OutputStreamWriter(socket.getOutputStream())),true)

      val message = byteArrayOf(0x1d, 0x49, 0x42, 0x1d, 0x49, 0x43)
      val socketOutputStream = socket.getOutputStream()
      socketOutputStream.write(message)

      val input = BufferedReader(InputStreamReader(socket.getInputStream()))
      val buffer = StringBuilder()
      while (input.ready()) {
        buffer.append(input.readLine())
        Log.d(LOG_TAG, "Inside thread : input.readLine() == " + input.readLine())
        Log.d(LOG_TAG, "Inside thread : buffer == $buffer")
      }
      socket.close()
      true
    } catch (e: IOException) {
      false
    }
  }

  private val inetAddresses: List<InterfaceAddress>
    get() {
      val addresses: MutableList<InterfaceAddress> = ArrayList()
      try {
        val en = NetworkInterface.getNetworkInterfaces()
        while (en.hasMoreElements()) {
          @SuppressWarnings("SpellCheckingInspection")
          val networkInterface = en.nextElement()
          for (interface_address in networkInterface.interfaceAddresses) {
            addresses.add(interface_address)
          }
        }
      } catch (ex: Exception) {
        Log.e(LOG_TAG, ex.toString())
      }
      return addresses
    }

  @Suppress("ImplicitThis")
  private fun inDSLITERange(ip: String): Boolean {
    // Fixes issue https://github.com/pusherman/react-native-network-info/issues/43
    // Based on comment
    // https://github.com/pusherman/react-native-network-info/issues/43#issuecomment-358360692
    // added this check in getIPAddress and getIPV4Address
    return DSLITE_LIST.contains(ip)
  }

  private fun getIPV4Address(): String? {
    return try {
      var ipAddress: String? = null
      var tmp = "0.0.0.0"
      for (address in inetAddresses) {
        if (!address.address.isLoopbackAddress && address.address is Inet4Address) {
          address.address.hostAddress?.let { _address ->
            tmp = _address
          }
          if (!inDSLITERange(tmp)) {
            ipAddress = tmp
          }
        }
      }
      ipAddress.toString()
    } catch (e: Exception) {
      Log.d(LOG_TAG, e.message.toString())
      null
    }
  }

  companion object {
    const val PRINTER_PORT = 9100
    const val SOCKET_TIMEOUT = 800
    private val LOG_TAG = NetworkScanManager::class.java.simpleName
    private val DSLITE_LIST = listOf(
      "192.0.0.0",
      "192.0.0.1",
      "192.0.0.2",
      "192.0.0.3",
      "192.0.0.4",
      "192.0.0.5",
      "192.0.0.6",
      "192.0.0.7")
  }
}
