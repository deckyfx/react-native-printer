package deckyfx.reactnative.printer.helpers

import android.graphics.Bitmap

// @ref https://gist.github.com/douglasjunior/dc3b41908514304f694f1b37cadf2df7
object EscPosHelper {
    /**
     * Collect a slice of 3 bytes with 24 dots for image printing.
     *
     * @param y     row position of the pixel.
     * @param x     column position of the pixel.
     * @param image 2D array of pixels of the image (RGB, row major order).
     * @return 3 byte array with 24 dots (field set).
     */
    @JvmStatic
    fun collectImageSlice(y: Int, x: Int, image: Bitmap): ByteArray {
        val slices = byteArrayOf(0, 0, 0)
        var yy = y
        var i = 0
        while (yy < y + 24 && i < 3) {
            // repeat for 3 cycles
            var slice: Byte = 0
            for (b in 0..7) {
                val yyy = yy + b
                if (yyy >= image.height) {
                    continue
                }
                val color = image.getPixel(x, yyy)
                val v = shouldPrintColor(color)
                slice = (slice.toInt() or ((if (v) 1 else 0) shl 7 - b).toByte().toInt()).toByte()
            }
            slices[i] = slice
            yy += 8
            i++
        }
        return slices
    }

    /**
     * Resizes a Bitmap image.
     *
     * @param image
     * @param width
     * @return new Bitmap image.
     */
    @JvmStatic
    fun resizeImage(image: Bitmap, width: Int): Bitmap {
        val origWidth = image.width
        val origHeight = image.height
        if (origWidth > width) {
            // picture is wider than we want it, we calculate its target height
            val destHeight = origHeight / (origWidth / width)
            // we create an scaled bitmap so it reduces the image, not just trim it
            return Bitmap.createScaledBitmap(image, width, destHeight, false)
        }
        return image
    }

    @JvmStatic
    fun resizeImage(image: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        var image = image
        if (maxHeight > 0 && maxWidth > 0) {
            val width = image.width
            val height = image.height
            val ratioBitmap = width.toFloat() / height.toFloat()
            val ratioMax = maxWidth.toFloat() / maxHeight.toFloat()
            var finalWidth = maxWidth
            var finalHeight = maxHeight
            if (ratioMax > ratioBitmap) {
                finalWidth = (maxHeight.toFloat() * ratioBitmap).toInt()
            } else {
                finalHeight = (maxWidth.toFloat() / ratioBitmap).toInt()
            }
            image = Bitmap.createScaledBitmap(image, finalWidth, finalHeight, true)
        }
        return image
    }

    /**
     * Defines if a color should be printed (burned).
     *
     * @param color RGB color.
     * @return true if should be printed/burned (black), false otherwise (white).
     */
    @JvmStatic
    fun shouldPrintColor(color: Int): Boolean {
        val threshold = 127
        val a: Int
        val r: Int
        val g: Int
        val b: Int
        val luminance: Int
        a = color shr 24 and 0xff
        if (a != 0xff) { // ignore pixels with alpha channel
            return false
        }
        r = color shr 16 and 0xff
        g = color shr 8 and 0xff
        b = color and 0xff
        luminance = (0.299 * r + 0.587 * g + 0.114 * b).toInt()
        return luminance < threshold
    }
}
