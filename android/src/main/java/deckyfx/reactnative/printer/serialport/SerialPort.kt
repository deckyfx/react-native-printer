package deckyfx.reactnative.printer.serialport

import android.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SerialPort(val device: File, val baudRate: Int = 9600, val flags: Int = 0) {
    private var mFileInputStream: FileInputStream? = null
    private var mFileOutputStream: FileOutputStream? = null
    private var mFd: FileDescriptor? = null

    init {
        //检查访问权限
        if (!device.canRead() || !device.canWrite()) {
            try {
                // 没有读/写权限，尝试对文件进行提权
                val su = Runtime.getRuntime().exec("/system/bin/su")
                val cmd = """
                    chmod 777 ${device.absolutePath}
                    exit

                    """.trimIndent()
                su.outputStream.write(cmd.toByteArray())
                if (su.waitFor() != 0 || !device.canRead() || !device.canWrite()) {
                    throw SecurityException()
                }
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.message!!)
                throw SecurityException()
            }
        }
        openDevice()
    }

    /**
     * 获取输入输出流
     */
    val inputStream: InputStream?
        get() = mFileInputStream
    val outputStream: OutputStream?
        get() = mFileOutputStream

    /**
     * 关闭IO流
     */
    fun closeIOStream() {
        try {
            mFileInputStream!!.close()
            mFileOutputStream!!.close()
        } catch (e: IOException) {
            e.printStackTrace()
            Log.e(TAG, e.message!!)
        }
        mFileInputStream = null
        mFileOutputStream = null
    }

  fun openDevice() {
    //不要删除或重命名字段mFd:原生方法close()使用了该字段
    mFd = open(device.absolutePath, baudRate, 0, 0, 0, 0, flags)
    if (mFd == null) {
      Log.i(TAG, "open method return null")
      throw IOException()
    }
    mFileInputStream = FileInputStream(mFd)
    mFileOutputStream = FileOutputStream(mFd)
  }

  fun closeDevice() {
    close()
    mFileInputStream = null
    mFileOutputStream = null
  }

  // JNI
  external fun close()

  external fun open(
    path: String,
    baudrate: Int,
    stopBits: Int,
    dataBits: Int,
    parity: Int,
    flowCon: Int,
    flags: Int
  ): FileDescriptor?

  companion object {
      private val TAG = SerialPort::class.simpleName

      init {
          System.loadLibrary("serialport")
      }
  }
}
