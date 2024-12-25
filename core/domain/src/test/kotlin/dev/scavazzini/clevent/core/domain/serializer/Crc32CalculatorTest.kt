package dev.scavazzini.clevent.core.domain.serializer

import org.junit.Assert.assertEquals
import org.junit.Test

class Crc32CalculatorTest {

    private val crc32Calculator = Crc32Calculator()

    private val validCrcs = mapOf(
        "clevent".toByteArray() to 2777201565.toUInt(),
        "nfc".toByteArray() to 1511310331.toUInt(),
        "android".toByteArray() to 2461840941.toUInt(),
        byteArrayOf() to 0.toUInt(),
    )

    @Test
    fun shouldCalculateIsoHdlcCrcCorrectly() {
        validCrcs.forEach { (payload, expectedCrc) ->
            assertEquals(expectedCrc, crc32Calculator.calculate(payload))
        }
    }

    @Test
    fun shouldReturnTrueForByteArraysThatMatchesCrc() {
        validCrcs.forEach { (payload, expectedCrc) ->
            assertEquals(true, crc32Calculator.matches(payload, expectedCrc))
        }
    }

    @Test
    fun shouldReturnFalseForByteArraysThatMismatchesCrc() {
        val incorrectCrcs = mapOf(
            "string-with-incorrect-crc".toByteArray() to byteArrayOf(0x1, 0x8, 0x7, 0x0),
            "invalid-bytes".toByteArray() to byteArrayOf(0x1, 0x9, 0x2, 0x4),
        )

        incorrectCrcs.forEach { (payload, crc) ->
            assertEquals(false, crc32Calculator.matches(payload, crc))
        }
    }

}
