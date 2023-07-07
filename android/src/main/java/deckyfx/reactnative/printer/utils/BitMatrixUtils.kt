package deckyfx.reactnative.printer.utils

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.common.BitMatrix

// @ref https://gist.github.com/adrianoluis/fa9374d7f2f8ca1115b00cc83cd7aacd
object BitMatrixUtils {
    @JvmStatic
    fun convertToBitmap(data: BitMatrix): Bitmap {
        val w = data.width
        val h = data.height
        val pixels = IntArray(w * h)
        for (y in 0 until h) {
            val offset = y * w
            for (x in 0 until w) {
                pixels[offset + x] = if (data[x, y]) Color.BLACK else Color.WHITE
            }
        }
        val bitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        bitmap.setPixels(pixels, 0, w, 0, 0, w, h)
        return bitmap
    }
}
