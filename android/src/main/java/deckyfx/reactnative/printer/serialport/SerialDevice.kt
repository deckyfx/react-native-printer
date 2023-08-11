package deckyfx.reactnative.printer.serialport

import android_serialport_api.SerialPort
import okhttp3.internal.notify
import tp.xmaihh.serialport.bean.ComBean
import tp.xmaihh.serialport.stick.AbsStickPackageHelper
import tp.xmaihh.serialport.stick.BaseStickPackageHelper
import tp.xmaihh.serialport.utils.ByteUtil
import java.io.File
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.security.InvalidParameterException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class SerialDevice(path: String, baudRate: Int) {
  private var serialPort: SerialPort? = null

  val outputStream: OutputStream?
    get() = serialPort?.outputStream

  val inputStream: InputStream?
    get() = serialPort?.inputStream

  var path = path
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  var port: String
    get() = path
    set(value) {
      path = value
    }

  val file: File
    get() = File(path)

  var baudRate = baudRate
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  var stopBits = 1
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  var dataBits = 8
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  var parity = 0
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  var flowCon = 0
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  var flags = 0
    set(value) {
      if (!_isOpen) {
        field = value
      }
    }

  private var readThread: ReadThread? = null
  private var sendThread: SendThread? = null

  private var _isOpen = false
  val isOpen: Boolean
    get() = _isOpen

  var bLoopData = byteArrayOf(48)

  var delay = 500

  var stickPackageHelper: AbsStickPackageHelper = BaseStickPackageHelper()

  var readDataListener: OnSerialReadListener? = null

  interface OnSerialReadListener {
    fun onData(data: ComBean?)
    fun error(error: Throwable)
  }

  @Throws(SecurityException::class, IOException::class, InvalidParameterException::class)
  fun open() {
    this.serialPort = SerialPort(
      File(path),
      baudRate,
      stopBits,
      dataBits,
      parity,
      flowCon,
      flags
    )
    sendThread = SendThread()
    readThread = ReadThread()
    _isOpen = true
  }

  fun close() {
    if (this.readThread != null) {
      this.readThread!!.interrupt()
    }
    serialPort?.close()
    serialPort = null
    _isOpen = false
  }

  fun send(bOutArray: ByteArray?) {
    try {
      this.outputStream?.write(bOutArray)
      this.outputStream?.flush()
    } catch (error: IOException) {
      error.printStackTrace()
    }
  }

  fun sendHex(sHex: String?) {
    val bOutArray = ByteUtil.HexToByteArr(sHex)
    send(bOutArray)
  }

  fun sendTxt(sTxt: String) {
    val bOutArray = sTxt.toByteArray()
    send(bOutArray)
  }

  fun setTxtLoopData(sTxt: String) {
    bLoopData = sTxt.toByteArray()
  }

  fun setHexLoopData(sHex: String?) {
    bLoopData = ByteUtil.HexToByteArr(sHex)
  }

  fun startSend() {
    sendThread?.resumeThread()
    sendThread?.start()
  }

  fun stopSend() {
    sendThread?.pauseThread()
  }

  fun startRead() {
    readThread?.resumeThread()
    readThread?.start()
  }

  fun stopRead() {
    readThread?.pauseThread()
  }

  private inner class SendThread : Thread() {
    private var suspendFlag = true
    private val lock = ReentrantLock()
    private val condition = lock.newCondition()
    override fun run() {
      super.run()
      while (!isInterrupted) {
        lock.withLock {
          while (suspendFlag) {
            try {
              condition.await()
            } catch (error: InterruptedException) {
              error.printStackTrace()
            }
          }
        }
        send(bLoopData)
        try {
          sleep(delay.toLong())
        } catch (error: InterruptedException) {
          error.printStackTrace()
        }
      }
      lock.unlock()
    }

    fun pauseThread() {
      suspendFlag = true
    }

    @Synchronized
    fun resumeThread() {
      suspendFlag = false
      notify()
    }
  }

  private inner class ReadThread : Thread() {
    private var resume = true
    override fun run() {
      super.run()
      while (!isInterrupted && resume) {
        try {
          if (inputStream == null) {
            return
          }
          val buffer: ByteArray = stickPackageHelper.execute(inputStream)
          if (buffer.isNotEmpty()) {
            val comRecData = ComBean(port, buffer, buffer.size)
            readDataListener?.onData(comRecData)
          }
        } catch (error: Throwable) {
          readDataListener?.error(error)
          error.printStackTrace()
          return
        }
      }
    }

    fun pauseThread() {
      resume = false
    }

    fun resumeThread() {
      resume = true
      notify()
    }
  }
}
