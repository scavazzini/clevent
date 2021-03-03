package dev.scavazzini.clevent.io

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class QRCodeGenerator @Inject constructor(private val multiFormatWriter: MultiFormatWriter) {

    suspend fun generate(content: String, width: Int, height: Int): Bitmap {
        val bitmap = createBitmap(width, height)

        withContext(Dispatchers.Default) {
            val matrix: BitMatrix = multiFormatWriter.encode(content, BarcodeFormat.QR_CODE,
                    width, height)

            for (x in 0 until width) {
                for (y in 0 until height) {
                    bitmap.setPixel(x, y, if (matrix[x, y]) Color.BLACK else Color.WHITE)
                }
            }
        }
        return bitmap
    }

    fun createBitmap(width: Int, height: Int) = Bitmap.createBitmap(width, height,
            Bitmap.Config.RGB_565)

}
