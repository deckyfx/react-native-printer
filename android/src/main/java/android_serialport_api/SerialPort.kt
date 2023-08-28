/*
 * Copyright 2009 Cedric Priscal
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android_serialport_api

import android.util.Log
import java.io.File
import java.io.FileDescriptor
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class SerialPort(private val device: File, private val baudrate: Int, private val flags: Int) {
  /*
	 * Do not remove or rename the field mFd: it is used by native method close();
	 */
  private var mFd: FileDescriptor? = null
  private val mFileInputStream: FileInputStream
  private val mFileOutputStream: FileOutputStream
  val inputStream: InputStream
    // Getters and setters
    get() = mFileInputStream

  val outputStream: OutputStream
    get() = mFileOutputStream


  fun open() {
    mFd = open(device.absolutePath, baudrate, flags)
    if (mFd == null) {
      Log.e(TAG, "native open returns null")
      throw IOException()
    }
  }

  external fun close()

  external fun open(path: String, baudrate: Int, flags: Int): FileDescriptor?

  init {
    /* Check access permission */
    if (!device.canRead() || !device.canWrite()) {
      try {
        /* Missing read/write permission, trying to chmod the file */
        val su: Process
        su = Runtime.getRuntime().exec("/system/bin/su")
        val cmd = """
                chmod 666 ${device.absolutePath}
                exit

                """.trimIndent()
        su.outputStream.write(cmd.toByteArray())
        if (su.waitFor() != 0 || !device.canRead()
          || !device.canWrite()
        ) {
          throw SecurityException()
        }
      } catch (e: Exception) {
        e.printStackTrace()
        throw SecurityException()
      }
    }
    open()
    mFileInputStream = FileInputStream(mFd)
    mFileOutputStream = FileOutputStream(mFd)
  }

  companion object {
    private const val TAG = "SerialPort"
    init {
      System.loadLibrary("serial_port")
    }
  }
}
