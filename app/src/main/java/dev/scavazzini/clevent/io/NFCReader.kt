package dev.scavazzini.clevent.io

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NfcAdapter
import android.nfc.Tag
import dev.scavazzini.clevent.data.models.Customer
import dev.scavazzini.clevent.utilities.crypto.Encryptor
import dev.scavazzini.clevent.utilities.serializers.CustomerSerializer
import javax.inject.Inject

class NFCReader @Inject constructor(
        private val serializer: CustomerSerializer,
        private val encryptor: Encryptor,
) {

    fun extract(intent: Intent): Pair<Tag, Customer> {
        val tag = intent.getParcelableExtra<Tag>(NfcAdapter.EXTRA_TAG) ?: throw IllegalArgumentException()
        val payload = getCustomerPayloadFromNFC(intent)

        if (isTagErased(payload)) return Pair(tag, Customer())

        val decryptedPayload = encryptor.decrypt(payload, tag.id)
        val customer = serializer.deserialize(decryptedPayload)

        return Pair(tag, customer)
    }

    fun getCustomerPayloadFromNFC(intent: Intent): ByteArray {
        val ndefMessages = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        if (ndefMessages == null || ndefMessages.size != 1 || ndefMessages[0] !is NdefMessage) {
            return ByteArray(0)
        }

        val ndefMessage = ndefMessages[0] as NdefMessage

        if (ndefMessage.records.size != 1) return ByteArray(0)

        return ndefMessage.records[0].payload
    }

    fun isTagErased(payload: ByteArray): Boolean {
        return payload.all { it == 0x0.toByte() }
    }
}
