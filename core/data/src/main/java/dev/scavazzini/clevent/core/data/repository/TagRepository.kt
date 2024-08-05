package dev.scavazzini.clevent.core.data.repository

import android.content.Intent
import android.nfc.Tag

interface TagRepository {
    fun getTag(intent: Intent): Tag

    @Throws(NonCleventTagException::class)
    fun read(tag: Tag): ByteArray

    suspend fun write(payload: ByteArray, tag: Tag)
    suspend fun erase(tag: Tag)
}
