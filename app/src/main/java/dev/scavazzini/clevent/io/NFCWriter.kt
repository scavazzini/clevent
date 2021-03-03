package dev.scavazzini.clevent.io

import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.Tag
import android.nfc.tech.Ndef
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.utilities.crypto.Encryptor
import dev.scavazzini.clevent.utilities.serializers.CustomerSerializer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.BufferOverflowException
import javax.inject.Inject

class NFCWriter @Inject constructor(private val encryptor: Encryptor,
                                    private val serializer: CustomerSerializer) {

    suspend fun write(tag: Tag, customer: Customer) = withContext(Dispatchers.IO) {
        val encryptedPayload = encryptor.encrypt(serializer.serialize(customer), tag.id)
        val ndefMessage = createNdefMessage(createNdefRecord(encryptedPayload))

        getNdefFrom(tag).use {
            if (ndefMessage.byteArrayLength > it.maxSize) throw BufferOverflowException()
            it.connect()
            it.writeNdefMessage(ndefMessage)
        }
    }

    suspend fun erase(tag: Tag) = withContext(Dispatchers.IO) {
        getNdefFrom(tag).use {
            it.connect()
            it.writeNdefMessage(createEmptyNdefMessage())
        }
    }

    fun createEmptyNdefMessage() = createNdefMessage(createNdefRecord(ByteArray(0)))

    fun createNdefRecord(encryptedPayload: ByteArray) = NdefRecord
                .createExternal("dev.scavazzini", "clevent", encryptedPayload)

    fun createNdefMessage(ndefRecord: NdefRecord) = NdefMessage(ndefRecord)

    fun getNdefFrom(tag: Tag): Ndef  = Ndef.get(tag)

}
