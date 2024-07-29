package dev.scavazzini.clevent.core.domain.serializer

import java.nio.ByteBuffer
import java.util.zip.CRC32


class FakeCRC32 : CRC32() {
    override fun update(b: Int) {}
    override fun update(b: ByteArray?, off: Int, len: Int) {}
    override fun update(b: ByteArray?) {}
    override fun update(buffer: ByteBuffer?) {}
    override fun reset() {}
    override fun getValue(): Long = 1234
}
