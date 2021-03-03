package dev.scavazzini.clevent.io

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.MultiFormatWriter
import com.google.zxing.common.BitMatrix
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class QRCodeGeneratorTest {

    val bitmapMock = mock(Bitmap::class.java)
    val bitMatrixMock = mock(BitMatrix::class.java)
    val multiFormatWriterMock = mock(MultiFormatWriter::class.java)
    val qrCodeGenerator = spy(QRCodeGenerator(multiFormatWriterMock))

    @Before
    fun setUp() {
        doReturn(bitmapMock).`when`(qrCodeGenerator).createBitmap(3, 3)
        doReturn(bitMatrixMock).`when`(multiFormatWriterMock).encode("hello-world",
                BarcodeFormat.QR_CODE, 3, 3)
    }

    @Test
    fun shouldGenerateQRCodeFromString(): Unit = runBlocking {
        qrCodeGenerator.generate("hello-world", 3, 3)

        verify(qrCodeGenerator).createBitmap(3, 3)
        verify(multiFormatWriterMock).encode("hello-world", BarcodeFormat.QR_CODE, 3, 3)
        verify(bitmapMock, times(9)).setPixel(anyInt(), anyInt(), anyInt())
    }
}
