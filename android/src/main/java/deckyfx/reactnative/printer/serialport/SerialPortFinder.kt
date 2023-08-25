package deckyfx.reactnative.printer.serialport

import java.io.File
import java.io.FileReader
import java.io.IOException
import java.io.LineNumberReader
import java.util.Vector

class SerialPortFinder {
  private var mDrivers: Vector<Driver>? = null

  // 设备名称可能存在空格
  @get:Throws(IOException::class)
  val drivers: Vector<Driver>
    get() {
      if (mDrivers == null) {
        mDrivers = Vector()
        val r = LineNumberReader(FileReader("/proc/tty/drivers"))
        var line: String
        try {
          while (r.readLine().also { line = it } != null) {
            // 设备名称可能存在空格
            val driverName = line.substring(0, 0x15).trim { it <= ' ' }
            val w = line.split(" +".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
            if (w.size >= 5 && "serial" == w[w.size - 1]) {
              mDrivers!!.add(Driver(driverName, w[w.size - 4]))
            }
          }
          r.close()
        } catch (e: NullPointerException) {
          // does nothing
        } catch (e: Exception) {
          e.printStackTrace()
        } finally {
          try {
            r.close()
          } catch (e: Exception) {
            e.printStackTrace()
          }
        }
      }
      return mDrivers!!
    }

  // 解析每个设备
  val devices: Array<SerialDeviceFound>
    get() {
      val devices = Vector<SerialDeviceFound>()
      // 解析每个设备
      val itdriv: Iterator<Driver>
      try {
        itdriv = drivers.iterator()
        while (itdriv.hasNext()) {
          val driver = itdriv.next()
          val itdev: Iterator<File> = driver.devices.iterator()
          while (itdev.hasNext()) {
            val device = itdev.next()
            devices.add(SerialDeviceFound(driver, device))
          }
        }
      } catch (e: IOException) {
        e.printStackTrace()
      }
      return devices.toTypedArray()
    }

  inner class Driver(val name: String, val deviceRoot: String) {
    private var mDevices: Vector<File>? = null
    val devices: Vector<File>
      get() {
        if (mDevices == null) {
          mDevices = Vector()
          val dev = File("/dev")
          val files = dev.listFiles() ?: return Vector()
          var i = 0
          while (i < files.size) {
            if (files[i].absolutePath.startsWith(deviceRoot)) {
              mDevices!!.add(files[i])
            }
            i++
          }
        }
        return mDevices!!
      }
  }

  inner class SerialDeviceFound(val driver: Driver, val device: File) {
  }

  companion object {
    private val LOG_TAG = SerialPortFinder::class.simpleName
  }
}
