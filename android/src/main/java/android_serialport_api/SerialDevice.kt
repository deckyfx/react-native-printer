package android_serialport_api

import java.io.File
import java.io.InputStream
import java.io.OutputStream

class SerialDevice(val device: File, val baudRate: Int = 9600, val flags: Int = 0) {
  private var mSerialPort: SerialPort? = null

  init {
    mSerialPort = SerialPort(device, baudRate, flags)
  }

  /**
   * 获取输入输出流
   */
  val inputStream: InputStream?
    get() = mSerialPort?.inputStream

  val outputStream: OutputStream?
    get() = mSerialPort?.outputStream

  fun openDevice() {

  }

  fun closeDevice() {
    mSerialPort?.closeIOStream()
    mSerialPort?.close()
  }
}
