package dev.scavazzini.clevent.data.core.repository

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.BufferOverflowException
import javax.inject.Inject

private const val RECORD_DOMAIN = "dev.scavazzini"
private const val RECORD_TYPE = "clevent"

class NdefTagRepository @Inject constructor() : TagRepository {

    override fun read(intent: Intent): ByteArray {
        return intent.getNdefMessage().records[0].payload
    }

    @Suppress("DEPRECATION")
    private fun Intent.getNdefMessage(): NdefMessage {
        val ndefMessages = getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)

        return ndefMessages?.get(0) as? NdefMessage
            ?: throw Exception()
    }

    override suspend fun write(payload: ByteArray, intent: Intent) {
        withContext(Dispatchers.IO) {
            val ndef = intent.getNdef()
            val ndefMessage = createNdefMessage(payload)

            if (ndefMessage.byteArrayLength > ndef.maxSize) {
                throw BufferOverflowException()
            }

            ndef.use {
                it.connect()
                it.writeNdefMessage(ndefMessage)
            }
        }
    }

    @Suppress("DEPRECATION")
    private fun Intent.getNdef(): Ndef {
        val tag = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)
        } else {
            getParcelableExtra(NfcAdapter.EXTRA_TAG)
        }

        return Ndef.get(tag)
    }

    private fun createNdefMessage(payload: ByteArray): NdefMessage {
        return NdefMessage(
            NdefRecord.createExternal(RECORD_DOMAIN, RECORD_TYPE, payload),
            NdefRecord.createApplicationRecord("dev.scavazzini.clevent"),
        )
    }

    override suspend fun erase(intent: Intent) {
        withContext(Dispatchers.IO) {
            val emptyRecord = NdefRecord(
                /* tnf = */ NdefRecord.TNF_EMPTY,
                /* type = */ null,
                /* id = */ null,
                /* payload = */ null,
            )

            val emptyMessage = NdefMessage(emptyRecord)

            intent.getNdef().use {
                it.connect()
                it.writeNdefMessage(emptyMessage)
            }
        }
    }

}
