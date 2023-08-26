package deckyfx.reactnative.printer.escposprinter.connection.serial

class SerialPortDigiAndroid {}

/*
import android.R.attr
import android.content.Context
import android_serialport_api.SerialPort
import com.digi.android.serial.ISerialPortEventListener
import com.digi.android.serial.NoSuchPortException
import com.digi.android.serial.PortInUseException
import com.digi.android.serial.SerialPortEvent
import com.digi.android.serial.SerialPortManager
import com.digi.android.serial.UnsupportedCommOperationException
import com.digi.xbee.api.connection.serial.AbstractSerialPort
import com.digi.xbee.api.connection.serial.SerialPortParameters
import com.digi.xbee.api.exceptions.ConnectionException
import com.digi.xbee.api.exceptions.InterfaceInUseException
import com.digi.xbee.api.exceptions.InvalidConfigurationException
import com.digi.xbee.api.exceptions.InvalidInterfaceException
import com.digi.xbee.api.exceptions.PermissionDeniedException
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.TooManyListenersException


/**
 * This class represents a serial port interface making use of the Digi Android
 * Serial Port library based on the RxTx implementation.
 */
class SerialPortDigiAndroid : AbstractSerialPort, ISerialPortEventListener {
  // Variables.
  private var serialPort: SerialPort? = null
  var inputStream: InputStream? = null
    private set
  var outputStream: OutputStream? = null
    private set
  private var breakThread: Thread? = null
  private var breakEnabled = false
  private var logger: org.slf4j.Logger
  private var context: Context

  /**
   * Class constructor. Instantiates a new `SerialPortDigiAndroid`
   * object using the given parameters.
   *
   * @param context The Android application context.
   * @param port Serial port name to instantiate.
   * @param baudRate Serial port baud rate, the rest of parameters will be
   * set by default.
   *
   * @throws IllegalArgumentException if `baudRate < 1`.
   * @throws NullPointerException if `context == null` or
   * if `port == null`.
   *
   * @see .DEFAULT_DATA_BITS
   *
   * @see .DEFAULT_FLOW_CONTROL
   *
   * @see .DEFAULT_PARITY
   *
   * @see .DEFAULT_STOP_BITS
   *
   * @see .DEFAULT_PORT_TIMEOUT
   *
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   */
  constructor(context: Context, port: String?, baudRate: Int) : super(
    port,
    baudRate,
    DEFAULT_PORT_TIMEOUT
  ) {
    this.context = context
    logger = org.slf4j.LoggerFactory.getLogger(SerialPortDigiAndroid::class.java)
  }

  /**
   * Class constructor. Instantiates a new `SerialPortDigiAndroid`
   * object using the given parameters.
   *
   * @param context The Android application context.
   * @param port Serial port name to instantiate.
   * @param baudRate Serial port baud rate, the rest of parameters will be
   * set by default.
   * @param receiveTimeout Serial port receive timeout in milliseconds.
   *
   * @throws IllegalArgumentException if `baudrate < 1` or
   * if `receiveTimeout < 0`.
   * @throws NullPointerException if `context == null` or
   * if `port == null`.
   *
   * @see .DEFAULT_DATA_BITS
   *
   * @see .DEFAULT_FLOW_CONTROL
   *
   * @see .DEFAULT_PARITY
   *
   * @see .DEFAULT_STOP_BITS
   *
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   */
  constructor(context: Context, port: String?, baudRate: Int, receiveTimeout: Int) : super(
    port,
    baudRate,
    receiveTimeout
  ) {
    this.context = context
    logger = org.slf4j.LoggerFactory.getLogger(SerialPortDigiAndroid::class.java)
  }

  /**
   * Class constructor. Instantiates a new `SerialPortDigiAndroid`
   * object using the given parameters.
   *
   * @param context The Android application context.
   * @param port Serial port name to instantiate.
   * @param parameters Serial port parameters to use.
   *
   * @throws NullPointerException if `context == null` or
   * if `port == null` or
   * if `parameters == null`.
   *
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   * @see SerialPortParameters
   */
  constructor(context: Context, port: String?, parameters: SerialPortParameters?) : super(
    port,
    parameters,
    DEFAULT_PORT_TIMEOUT
  ) {
    this.context = context
    logger = org.slf4j.LoggerFactory.getLogger(SerialPortDigiAndroid::class.java)
  }

  /**
   * Class constructor. Instantiates a new `SerialPortDigiAndroid`
   * object using the given parameters.
   *
   * @param context The Android application context.
   * @param port Serial port name to instantiate.
   * @param parameters Serial port parameters to use.
   * @param receiveTimeout Serial port receive timeout in milliseconds.
   *
   * @throws IllegalArgumentException if `receiveTimeout < 0`.
   * @throws NullPointerException if `context == null` or
   * if `port == null` or
   * if `parameters == null`.
   *
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   * @see .SerialPortDigiAndroid
   * @see SerialPortParameters
   */
  constructor(
    context: Context,
    port: String?,
    parameters: SerialPortParameters?,
    receiveTimeout: Int
  ) : super(port, parameters, receiveTimeout) {
    this.context = context
    logger = org.slf4j.LoggerFactory.getLogger(SerialPortDigiAndroid::class.java)
  }

  @Throws(
    InterfaceInUseException::class,
    InvalidInterfaceException::class,
    InvalidConfigurationException::class,
    PermissionDeniedException::class
  )
  fun open() {
    try {
      // Instantiate Serial Port Manager
      val serialPortManager = SerialPortManager(context)
      // Get the serial port.
      serialPort = serialPortManager.openSerialPort(attr.port)
      // Set port as connected.
      connectionOpen = true
      // Configure the port.
      if (parameters == null) parameters = SerialPortParameters(
        baudRate,
        DEFAULT_DATA_BITS,
        DEFAULT_STOP_BITS,
        DEFAULT_PARITY,
        DEFAULT_FLOW_CONTROL
      )
      serialPort.setPortParameters(
        baudRate,
        parameters.dataBits,
        parameters.stopBits,
        parameters.parity,
        parameters.flowControl
      )
      serialPort.enableReceiveTimeout(receiveTimeout)
      // Initialize input and output streams before setting the listener.
      inputStream = serialPort!!.inputStream
      outputStream = serialPort!!.outputStream
      // Activate data received event.
      serialPort.notifyOnDataAvailable(true)
      // Register serial port event listener to be notified when data is available.
      serialPort.registerEventListener(this)
    } catch (e: PortInUseException) {
      throw InterfaceInUseException(
        "Port " + attr.port + " is already in use by other application(s)",
        e
      )
    } catch (e: UnsupportedCommOperationException) {
      throw InvalidConfigurationException(
        "Invalid serial port configuration: " + attr.port + " " + e.getMessage(),
        e
      )
    } catch (e: TooManyListenersException) {
      throw InvalidConfigurationException(
        "Invalid serial port configuration: " + attr.port + " " + e.message,
        e
      )
    } catch (e: IOException) {
      throw InvalidConfigurationException("Error retrieving serial port streams: " + attr.port, e)
    } catch (e: NoSuchPortException) {
      throw InvalidInterfaceException("No such port: " + attr.port, e)
    }
  }

  fun close() {
    try {
      if (inputStream != null) {
        inputStream!!.close()
        inputStream = null
      }
      if (outputStream != null) {
        outputStream!!.close()
        outputStream = null
      }
    } catch (e: IOException) {
      logger.error(e.message, e)
    }
    if (serialPort != null) {
      try {
        serialPort.notifyOnDataAvailable(false)
        serialPort.unregisterEventListener()
        synchronized(serialPort!!) {
          serialPort!!.close()
          serialPort = null
          connectionOpen = false
        }
      } catch (e: Exception) {
      }
    }
  }

  fun serialEvent(event: SerialPortEvent) {
    // Listen only to data available event.
    when (event.getEventType()) {
      DATA_AVAILABLE -> {
        // Check if serial device has been disconnected or not.
        try {
          inputStream!!.available()
        } catch (e: Exception) {
          // Serial device has been disconnected.
          close()
          synchronized(this) {
            //System.out.println("notify");
            this.notify()
          }
          break
        }
        // Notify data is available by waking up the read thread.
        try {
          if (inputStream!!.available() > 0) {
            synchronized(this) {
              //System.out.println("notify");
              this.notify()
            }
          }
        } catch (e: Exception) {
          logger.error(e.message, e)
        }
      }

      else -> {}
    }
  }

  override fun toString(): String {
    return super.toString()
  }

  fun setBreak(enabled: Boolean) {
    breakEnabled = enabled
    if (breakEnabled) {
      if (breakThread == null) {
        breakThread = object : Thread() {
          override fun run() {
            while (breakEnabled && serialPort != null) serialPort.sendBreak(100)
          }
        }
        breakThread.start()
      }
    } else {
      if (breakThread != null) breakThread!!.interrupt()
      breakThread = null
      serialPort.sendBreak(0)
    }
  }

  var readTimeout: Int
    get() = serialPort.getReceiveTimeout()
    set(timeout) {
      serialPort.disableReceiveTimeout()
      try {
        serialPort.enableReceiveTimeout(timeout)
      } catch (e: UnsupportedCommOperationException) {
        e.printStackTrace()
      }
    }

  fun setDTR(state: Boolean) {
    serialPort.setDTR(state)
  }

  fun setRTS(state: Boolean) {
    serialPort.setRTS(state)
  }

  @Throws(InvalidConfigurationException::class, ConnectionException::class)
  fun setPortParameters(
    baudRate: Int,
    dataBits: Int,
    stopBits: Int,
    parity: Int,
    flowControl: Int
  ) {
    parameters = SerialPortParameters(baudRate, dataBits, stopBits, parity, flowControl)
    if (serialPort != null) {
      try {
        serialPort.setPortParameters(baudRate, dataBits, stopBits, parity, flowControl)
      } catch (e: UnsupportedCommOperationException) {
        throw InvalidConfigurationException(e.getMessage(), e)
      }
    }
  }

  fun sendBreak(duration: Int) {
    if (serialPort != null) serialPort.sendBreak(duration)
  }

  val isCTS: Boolean
    get() = serialPort.isCTS()
  val isDSR: Boolean
    get() = serialPort.isDSR()
  val isCD: Boolean
    get() = serialPort.isCD()
}
*/
