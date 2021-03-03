package dev.scavazzini.clevent.io

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.utilities.crypto.Encryptor
import dev.scavazzini.clevent.utilities.serializers.CustomerSerializer
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class NFCWriterTest {

    private val customer = Customer()
    private val encryptorMock = mock(Encryptor::class.java)
    private val serializerMock = mock(CustomerSerializer::class.java)
    private val tagMock = mock(Tag::class.java)
    private val ndefMock = mock(Ndef::class.java)
    private val ndefRecordMock = mock(NdefRecord::class.java)
    private val ndefMessageMock = mock(NdefMessage::class.java)
    private val nfcWriter = spy(NFCWriter(encryptorMock, serializerMock))

    @Before
    fun setUp() {
        doReturn(ndefMock).`when`(nfcWriter).getNdefFrom(tagMock)
        doReturn(byteArrayOf(1, 2)).`when`(serializerMock).serialize(customer)
        doReturn(byteArrayOf(3, 4)).`when`(tagMock).id
        doReturn(byteArrayOf(5, 6)).`when`(encryptorMock).encrypt(byteArrayOf(1, 2), byteArrayOf(3, 4))
        doReturn(ndefRecordMock).`when`(nfcWriter).createNdefRecord(byteArrayOf(5, 6))
        doReturn(ndefMessageMock).`when`(nfcWriter).createNdefMessage(ndefRecordMock)
    }

    @Test
    fun shouldWriteEncryptedPayloadOnNFCTag() = runBlocking {
        nfcWriter.write(tagMock, customer)

        verify(serializerMock).serialize(customer)
        verify(encryptorMock).encrypt(byteArrayOf(1, 2), byteArrayOf(3, 4))
        verify(ndefMock).connect()
        verify(ndefMock).writeNdefMessage(ndefMessageMock)
        verify(ndefMock).close()
    }
}
