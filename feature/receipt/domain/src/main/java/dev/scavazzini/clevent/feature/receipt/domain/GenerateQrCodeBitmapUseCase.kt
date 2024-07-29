package dev.scavazzini.clevent.feature.receipt.domain

import android.graphics.Bitmap
import android.graphics.Color
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class GenerateQrCodeBitmapUseCase @Inject constructor(
    private val multiFormatWriter: MultiFormatWriter,
) {
    suspend operator fun invoke(
        content: String,
        width: Int,
        height: Int,
    ): Bitmap {
        return withContext(Dispatchers.Default) {
            val matrix: BitMatrix = multiFormatWriter.encode(
                /* contents = */ content,
                /* format = */ BarcodeFormat.QR_CODE,
                /* width = */ width,
                /* height = */ height,
            )

            return@withContext Bitmap.createBitmap(
                /* width = */ width,
                /* height = */ height,
                /* config = */ Bitmap.Config.RGB_565,
            ).also {
                for (x in 0 until width) {
                    for (y in 0 until height) {
                        val color = if (matrix[x, y]) Color.BLACK else Color.WHITE
                        it.setPixel(x, y, color)
                    }
                }
            }
        }
    }
}
