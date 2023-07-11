package deckyfx.reactnative.printer.deprecated

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import com.facebook.react.bridge.ReactApplicationContext
import com.google.zxing.BarcodeFormat
import com.google.zxing.WriterException
import com.google.zxing.qrcode.QRCodeWriter
import deckyfx.reactnative.printer.deprecated.command.PrinterCommand.getBarCodeCommand
import deckyfx.reactnative.printer.deprecated.helpers.EscPosHelper.collectImageSlice
import deckyfx.reactnative.printer.deprecated.helpers.EscPosHelper.resizeImage
import deckyfx.reactnative.printer.deprecated.utils.BitMatrixUtils.convertToBitmap
import io.github.escposjava.PrinterService
import io.github.escposjava.print.Commands
import io.github.escposjava.print.Printer
import io.github.escposjava.print.exceptions.BarcodeSizeError
import io.github.escposjava.print.exceptions.QRCodeException
import java.io.BufferedReader
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.StringReader
import java.io.UnsupportedEncodingException
import java.util.regex.Pattern

class PrinterService {
    private val layoutBuilder = LayoutBuilder()
    private val DEFAULT_QR_CODE_SIZE = 200
    private val DEFAULT_IMG_MAX_HEIGHT = 200
    private val DEFAULT_IMG_WIDTH_OFFSET = 0
    private val DEFAULT_BAR_CODE_HEIGHT = 120
    private val DEFAULT_BAR_CODE_WIDTH = 3
    private val DEFAULT_BAR_CODE_FORMAT = 73 //CODE-128
    private val DEFAULT_BAR_CODE_FONT = 0
    private val DEFAULT_BAR_CODE_POSITION = 2
    private var printingWidth = PRINTING_WIDTH_58_MM
    private var basePrinterService: PrinterService
    private var context: ReactApplicationContext? = null

    constructor(printer: Printer?) {
        basePrinterService = PrinterService(printer)
    }

    constructor(printer: Printer?, printingWidth: Int) {
        basePrinterService = PrinterService(printer)
        this.printingWidth = printingWidth
    }

    fun cutPart() {
        basePrinterService.cutPart()
    }

    fun cutFull() {
        basePrinterService.cutFull()
    }

    @Throws(UnsupportedEncodingException::class)
    fun print(text: String) {
        // TODO: get rid of GBK default!
        write(text.toByteArray(charset("GBK")))
    }

    @Throws(UnsupportedEncodingException::class)
    fun printLn(text: String) {
        print(text + CARRIAGE_RETURN)
    }

    fun lineBreak() {
        basePrinterService.lineBreak()
    }

    fun lineBreak(nbLine: Int) {
        basePrinterService.lineBreak(nbLine)
    }

    // We have to modify the printBarcode method in io.github.escposjava.PrinterService
    // Take a look on the getBytes() function. It works incorrectly.
    @Throws(BarcodeSizeError::class)
    fun printBarcode(code: String?, bc: String?, width: Int, height: Int, pos: String?, font: String?) {
        basePrinterService.printBarcode(code, bc, width, height, pos, font)
    }

    fun printBarcode(str: String?, nType: Int, nWidthX: Int, nHeight: Int, nHriFontType: Int, nHriFontPosition: Int) {
        val printerBarcode = getBarCodeCommand(str!!, nType, nWidthX, nHeight, nHriFontType, nHriFontPosition)
        basePrinterService.write(printerBarcode)
    }

    @Throws(IOException::class)
    fun printSample() {
        val design = """               ABC Inc. {C}
           1234 Main Street {C}
        Anytown, US 12345-6789 {C}
            (555) 123-4567 {C}

          D0004 | Table #: A1 {C}
------------------------------------------
Item            {<>}    Qty  Price  Amount
Chicken Rice    {<>}      2  12.50   25.00
Coke Zero       {<>}      5   3.00   15.00
Fries           {<>}      3   3.00    9.00
Fresh Oyster    {<>}      1   8.00    8.00
Lobster Roll    {<>}      1  16.50   16.50
------------------------------------------
       {QR[Where are the aliens?]}
"""
        printDesign(design)
    }

    @Throws(IOException::class)
    fun printDesign(text: String) {
        val baos = generateDesignByteArrayOutputStream(text)
        write(baos.toByteArray())
    }

    @Throws(IOException::class)
    fun readImage(filePath: String?, reactContext: ReactApplicationContext?): Bitmap? {
        val fileUri = Uri.parse(filePath)
        var image: Bitmap? = null
        val op = BitmapFactory.Options()
        op.inPreferredConfig = Bitmap.Config.ARGB_8888
        image = BitmapFactory.decodeFile(fileUri.path, op)
        return image
    }

    @Throws(IOException::class)
    fun printImage(filePath: String?) {
        printImage(readImage(filePath, context))
    }

    @Throws(IOException::class)
    fun printImage(filePath: String?, widthOffset: Int) {
        printImage(readImage(filePath, context), widthOffset)
    }

    @Throws(IOException::class)
    fun printImage(image: Bitmap?) {
        var image = image
        image = resizeImage(image!!, printingWidth - DEFAULT_IMG_WIDTH_OFFSET, DEFAULT_IMG_MAX_HEIGHT)
        val baos = generateImageByteArrayOutputStream(image)
        write(baos.toByteArray())
    }

    @Throws(IOException::class)
    fun printImage(image: Bitmap?, widthOffset: Int) {
        var image = image
        image = resizeImage(image!!, Math.max(printingWidth - Math.abs(widthOffset), 0), DEFAULT_IMG_MAX_HEIGHT)
        val baos = generateImageByteArrayOutputStream(image)
        write(baos.toByteArray())
    }

    @Throws(QRCodeException::class)
    fun printQRCode(value: String, size: Int) {
        val baos = generateQRCodeByteArrayOutputStream(value, size)
        write(baos.toByteArray())
    }

    fun setFontType(type: Int) {
        basePrinterService.write(byteArrayOf(0x1b, 't'.code.toByte(), type.toByte()))
        basePrinterService.write(byteArrayOf(0x1b, 'M'.code.toByte(), type.toByte()))
    }

    fun write(command: ByteArray?) {
        basePrinterService.write(command)
    }

    fun write(command: String) {
        basePrinterService.write(command.toByteArray())
    }

    fun setCharCode(code: String?) {
        basePrinterService.setCharCode(code)
    }

    fun setCharsOnLine(charsOnLine: Int) {
        layoutBuilder.charsOnLine = charsOnLine
    }

    fun setPrintingWidth(printingWidth: Int) {
        this.printingWidth = printingWidth
    }

    fun setTextDensity(density: Int) {
        basePrinterService.setTextDensity(density)
    }

    fun beep() {
        basePrinterService.beep()
    }

    @Throws(IOException::class)
    fun open() {
        basePrinterService.open()
    }

    @Throws(IOException::class)
    fun close() {
        basePrinterService.close()
    }

    fun kickCashDrawerPin2() {
        basePrinterService.write(Commands.CD_KICK_2)
    }

    fun kickCashDrawerPin5() {
        basePrinterService.write(Commands.CD_KICK_5)
    }

    /**
     * DESIGN 1: Order List                       *
     * D0004 | Table #: A1 {C} {H1}      *
     * ------------------------------------------ *
     * [Dine In] {U} {B}                          *
     * [ ] Espresso {H2}                          *
     * - No sugar, Regular 9oz, Hot           *
     * {H3} {R} x 1 *
     * ------------------------------------------ *
     * [ ] Blueberry Cheesecake {H2}              *
     * - Slice                                *
     * {H3} {R} x 1 *
     * *
     * DESIGN 2: Menu Items                       *
     * ------------------------------------------ *
     * Item         {<>}       Qty  Price  Amount *
     * Pork Rice    {<>}         1  13.80   13.80 *
     * *
     * DESIGN 3: Barcode                          *
     * {QR[Love me, hate me.]} {C}                *
     * {BC[Your Barcode here]} {C}                *
     */
    @Throws(IOException::class)
    private fun generateDesignByteArrayOutputStream(text: String): ByteArrayOutputStream {
        val reader = BufferedReader(StringReader(text.trim { it <= ' ' }))
        val baos = ByteArrayOutputStream()
        var line: String

        // TODO: Shouldn't put it here
        val ESC_LT = byteArrayOf(0x1b, 0x3c, 0x00)
        val ESC_t = byteArrayOf(0x1b, 't'.code.toByte(), 0x00)
        val ESC_M = byteArrayOf(0x1b, 'M'.code.toByte(), 0x00)
        val FS_and = byteArrayOf(0x1c, '&'.code.toByte())
        val TXT_NORMAL_NEW = byteArrayOf(0x1d, '!'.code.toByte(), 0x00)
        val TXT_4SQUARE_NEW = byteArrayOf(0x1d, '!'.code.toByte(), 0x11)
        val TXT_2HEIGHT_NEW = byteArrayOf(0x1d, '!'.code.toByte(), 0x01)
        val TXT_2WIDTH_NEW = byteArrayOf(0x1d, '!'.code.toByte(), 0x10)
        val LINE_SPACE_68 = byteArrayOf(0x1b, 0x33, 68)
        val LINE_SPACE_88 = byteArrayOf(0x1b, 0x33, 120)
        val DEFAULT_LINE_SPACE = byteArrayOf(0x1b, 0x32)
        while (reader.readLine().also { line = it } != null) {
            var qtToWrite: ByteArray? = null
            var imageToWrite: ByteArray? = null
            var fontType: ByteArray? = null
            var lineSpacing: ByteArray? = null
            var bcToWrite: ByteArray? = null
            if (line.matches(".*\\{QR\\[(.+)\\]\\}.*".toRegex())) {
                qtToWrite = try {
                    generateQRCodeByteArrayOutputStream(line.replace(".*\\{QR\\[(.+)\\]\\}.*".toRegex(), "$1"),
                            DEFAULT_QR_CODE_SIZE).toByteArray()
                } catch (e: QRCodeException) {
                    throw IOException(e)
                }
            }
            if (line.matches(".*\\{BC\\[(.+)\\]\\}.*".toRegex())) {
                bcToWrite = getBarCodeCommand(line.replace(".*\\{BC\\[(.+)\\]\\}.*".toRegex(), "$1"), DEFAULT_BAR_CODE_FORMAT, DEFAULT_BAR_CODE_WIDTH, DEFAULT_BAR_CODE_HEIGHT, DEFAULT_BAR_CODE_FONT, DEFAULT_BAR_CODE_POSITION)
            }
            val imgRegex = ".*\\{IMG\\[(.+)\\](?:\\}|:(\\d+)(\\}|:(\\d+))).*"
            val imgPatter = Pattern.compile(imgRegex)
            val imgMatcher = imgPatter.matcher(line)
            if (imgMatcher.find()) {
                try {
                    var maxWidth = printingWidth
                    if (imgMatcher.groupCount() >= 2) {
                        if (imgMatcher.group(2).length > 0) {
                            maxWidth = imgMatcher.group(2).toInt()
                        }
                        if (maxWidth > printingWidth || maxWidth == 0) {
                            maxWidth = printingWidth
                        }
                    }
                    var maxHeight = DEFAULT_IMG_MAX_HEIGHT
                    if (imgMatcher.groupCount() >= 4) {
                        if (imgMatcher.group(4).length > 0) {
                            maxHeight = imgMatcher.group(4).toInt()
                        }
                        if (maxHeight == 0) {
                            maxHeight = DEFAULT_IMG_MAX_HEIGHT
                        }
                    }
                    imageToWrite = generateImageByteArrayOutputStream(
                            resizeImage(
                                    readImage(imgMatcher.group(1), context)!!, maxWidth, maxHeight
                            )
                    ).toByteArray()
                } catch (e: IOException) {
                    throw IOException(e)
                }
            }
            if (line.matches(".*\\{FT\\:(\\d)\\}.*".toRegex())) {
                fontType = try {
                    byteArrayOf(line.replace(".*\\{FT\\:(\\d)\\}.*".toRegex(), "$1").toInt().toByte())
                } catch (e: Exception) {
                    throw IOException(e)
                }
            }
            if (line.matches(".*\\{LS\\:(\\d{1,2})\\}.*".toRegex())) {
                lineSpacing = try {
                    byteArrayOf(line.replace(".*\\{LS\\:(\\d{1,2})\\}.*".toRegex(), "$1").toInt().toByte())
                } catch (e: Exception) {
                    throw IOException(e)
                }
            }
            val bold = line.contains("{B}")
            val underline = line.contains("{U}")
            val h1 = line.contains("{H1}")
            val h2 = line.contains("{H2}")
            val h3 = line.contains("{H3}")
            val lsn = line.contains("{LS:N}")
            val lsm = line.contains("{LS:M}")
            val lsl = line.contains("{LS:L}")
            val ct = line.contains("{C}")
            val rt = line.contains("{R}")
            var charsOnLine = layoutBuilder.charsOnLine
            baos.write(ESC_t)
            baos.write(FS_and)
            baos.write(ESC_M)
            if (fontType != null) {
                baos.write(byteArrayOf(0x1b, 't'.code.toByte(), fontType[0]))
                baos.write(byteArrayOf(0x1b, 'M'.code.toByte(), fontType[0]))
                line = line.replace("\\{FT\\:(\\d)\\}".toRegex(), "")
            }

            // Add tags
            if (bold) {
                baos.write(Commands.TXT_BOLD_ON)
                line = line.replace("{B}", "")
            }
            if (underline) {
                baos.write(Commands.TXT_UNDERL_ON)
                line = line.replace("{U}", "")
            }
            if (h1) {
                baos.write(TXT_4SQUARE_NEW)
                line = line.replace("{H1}", "")
                charsOnLine = charsOnLine / 2
            } else if (h2) {
                baos.write(TXT_2HEIGHT_NEW)
                line = line.replace("{H2}", "")
            } else if (h3) {
                baos.write(TXT_2WIDTH_NEW)
                line = line.replace("{H3}", "")
                charsOnLine = charsOnLine / 2
            }
            if (lsm) {
                baos.write(Commands.LINE_SPACE_24)
                line = line.replace("{LS:M}", "")
            } else if (lsl) {
                baos.write(Commands.LINE_SPACE_30)
                line = line.replace("{LS:L}", "")
            } else if (lsn) {
                baos.write(DEFAULT_LINE_SPACE)
                line = line.replace("{LS:N}", "")
            }
            if (ct) {
                baos.write(Commands.TXT_ALIGN_CT)
                line = line.replace("{C}", "")
            }
            if (rt) {
                baos.write(Commands.TXT_ALIGN_RT)
                line = line.replace("{R}", "")
            }
            if (lineSpacing != null) {
                baos.write(byteArrayOf(0x1b, 0x33, lineSpacing[0]))
                line = line.replace("\\{LS\\:(\\d{1,2})\\}".toRegex(), "")
            }
            try {
                if (qtToWrite != null) {
                    baos.write(qtToWrite)
                }
                if (imageToWrite != null) {
                    baos.write(imageToWrite)
                }
                if (bcToWrite != null) {
                    baos.write(bcToWrite)
                }
                if (qtToWrite == null && imageToWrite == null && bcToWrite == null) {
                    // TODO: get rid of GBK default!
                    baos.write(layoutBuilder.createFromDesign(line, charsOnLine).toByteArray(charset("GBK")))
                }
            } catch (e: UnsupportedEncodingException) {
                // Do nothing?
            }

            // Remove tags
            if (bold) {
                baos.write(Commands.TXT_BOLD_OFF)
            }
            if (underline) {
                baos.write(Commands.TXT_UNDERL_OFF)
            }
            if (h1 || h2 || h3) {
                baos.write(DEFAULT_LINE_SPACE)
                baos.write(TXT_NORMAL_NEW)
            }
            if (lsm || lsl) {
                baos.write(Commands.LINE_SPACE_24)
            }
            if (ct || rt) {
                baos.write(Commands.TXT_ALIGN_LT)
            }
        }
        return baos
    }

    @Throws(IOException::class)
    private fun generateImageByteArrayOutputStream(image: Bitmap): ByteArrayOutputStream {
        val baos = ByteArrayOutputStream()
        baos.write(Commands.LINE_SPACE_24)
        var y = 0
        while (y < image.height) {
            baos.write(Commands.SELECT_BIT_IMAGE_MODE) // bit mode
            // width, low & high
            baos.write(byteArrayOf((0x00ff and image.width).toByte(), (0xff00 and image.width shr 8).toByte()))
            for (x in 0 until image.width) {
                // For each vertical line/slice must collect 3 bytes (24 bytes)
                baos.write(collectImageSlice(y, x, image))
            }
            baos.write(Commands.CTL_LF)
            y += 24
        }
        return baos
    }

    @Throws(QRCodeException::class)
    private fun generateQRCodeByteArrayOutputStream(value: String, size: Int): ByteArrayOutputStream {
        return try {
            val result = QRCodeWriter().encode(value, BarcodeFormat.QR_CODE, size, size, null)
            val qrcode = convertToBitmap(result)
            generateImageByteArrayOutputStream(qrcode)
        } catch (e: IllegalArgumentException) {
            // Unsupported format
            throw QRCodeException("QRCode generation error", e)
        } catch (e: WriterException) {
            throw QRCodeException("QRCode generation error", e)
        } catch (e: IOException) {
            throw QRCodeException("QRCode generation error", e)
        }
    }

    fun setContext(reactContext: ReactApplicationContext?) {
        context = reactContext
    }

    companion object {
        const val PRINTING_WIDTH_58_MM = 384
        const val PRINTING_WIDTH_76_MM = 450
        const val PRINTING_WIDTH_80_MM = 576
        private val CARRIAGE_RETURN = System.getProperty("line.separator")
    }
}
