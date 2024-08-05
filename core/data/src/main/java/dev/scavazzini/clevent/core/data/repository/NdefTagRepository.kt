package dev.scavazzini.clevent.core.data.repository

import android.content.Intent
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NdefRecord.TNF_EXTERNAL_TYPE
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.nfc.tech.Ndef
import android.os.Build
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

private const val RECORD_DOMAIN = "dev.scavazzini"
private const val RECORD_TYPE = "clevent"

class NdefTagRepository @Inject constructor() : TagRepository {

    override fun getTag(intent: Intent): Tag {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            @Suppress("DEPRECATION")
            return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG)!!
        }
        return intent.getParcelableExtra(NfcAdapter.EXTRA_TAG, Tag::class.java)!!
    }

    override fun read(tag: Tag): ByteArray {
        val record = Ndef.get(tag).cachedNdefMessage.records.first()

        if (!record.isCleventRecord()) {
            throw NonCleventTagException()
        }

        return record.payload
    }

    private fun NdefRecord.isCleventRecord(): Boolean {
        return tnf == TNF_EXTERNAL_TYPE
                && type contentEquals "${RECORD_DOMAIN}:${RECORD_TYPE}".toByteArray()
    }

    override suspend fun write(payload: ByteArray, tag: Tag) {
        tag.writePayload(payload)
    }

    private suspend fun Tag.writePayload(payload: ByteArray) {
        val tag = this
        val ndefMessage = createNdefMessage(payload)

        withContext(Dispatchers.IO) {
            Ndef.get(tag).use {
                it.connect()
                it.writeNdefMessage(ndefMessage)
            }
        }
    }

    private fun createNdefMessage(payload: ByteArray): NdefMessage {
        return NdefMessage(
            NdefRecord.createExternal(RECORD_DOMAIN, RECORD_TYPE, payload),
            NdefRecord.createApplicationRecord("dev.scavazzini.clevent"),
        )
    }

    override suspend fun erase(tag: Tag) {
        tag.writePayload(
            payload = ByteArray(0),
        )
    }

}
