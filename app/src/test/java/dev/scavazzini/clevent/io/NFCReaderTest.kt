package dev.scavazzini.clevent.io

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.data.models.Product
import dev.scavazzini.clevent.utilities.crypto.Encryptor
import dev.scavazzini.clevent.utilities.serializers.CustomerSerializer
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.Mockito.*

class NFCReaderTest {

    val erasedIntentMock = mock(Intent::class.java)
    val intentMock = mock(Intent::class.java)
    val ndefMessageMock = mock(NdefMessage::class.java)
    val ndefRecordMock = mock(NdefRecord::class.java)
    val serializerMock = mock(CustomerSerializer::class.java)
    val encryptorMock = mock(Encryptor::class.java)
    val nfcReader = spy(NFCReader(serializerMock, encryptorMock))

    @Before
    fun setUp() {
        // TODO: All this mocking stuff seems to be a code smell, but these tests were helpful.
        doReturn(byteArrayOf(0, 0, 0, 0, 0)).`when`(nfcReader).getCustomerPayloadFromNFC(erasedIntentMock)
        doReturn(mock(Tag::class.java)).`when`(erasedIntentMock).getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        doReturn(arrayOf(ndefMessageMock)).`when`(intentMock)
                .getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
        doReturn(arrayOf(ndefRecordMock)).`when`(ndefMessageMock).records
        doReturn(byteArrayOf(1, 2, 3, 4, 5)).`when`(ndefRecordMock).payload
    }

    @Test
    fun shouldExtractCustomerFromNFCIntent() {
        val customer = Customer()
        customer.recharge(1000)
        customer.addProduct(Product(1.toShort(), "p1", 300))
        doReturn(byteArrayOf(1, 2, 3, 4, 5)).`when`(encryptorMock).decrypt(byteArrayOf(1, 2, 3, 4, 5), null)
        doReturn(customer).`when`(serializerMock).deserialize(byteArrayOf(1, 2, 3, 4, 5))
        doReturn(mock(Tag::class.java)).`when`(intentMock).getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG)

        val c1 = nfcReader.extract(intentMock).second
        assertEquals(700, c1.balance)
        assertEquals(1, c1.products.size)
        assertEquals(300, c1.total)
    }

    @Test
    fun shouldReturnNewCustomerWithErasedTag() {
        val customer = nfcReader.extract(erasedIntentMock).second
        assertEquals(0, customer.balance)
        assertEquals(0, customer.products.size)
        assertEquals(0, customer.total)
    }

    @Test
    fun shouldGetPayloadFromNFCIntent() {
        val payload = nfcReader.getCustomerPayloadFromNFC(intentMock)
        assertArrayEquals(byteArrayOf(1, 2, 3, 4, 5), payload)
    }

    @Test
    fun shouldReturnThatTagIsErasedWhenPassAllZeroByteArray() {
        val payload = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        val tagErased = nfcReader.isTagErased(payload)
        assertTrue(tagErased)
    }

    @Test
    fun shouldReturnThatTagIsNotErasedWhenPassNotAllZeroByteArray() {
        val payload = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1)
        val tagErased = nfcReader.isTagErased(payload)
        assertFalse(tagErased)
    }
}
