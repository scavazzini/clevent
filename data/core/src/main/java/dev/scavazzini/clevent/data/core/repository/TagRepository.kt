package dev.scavazzini.clevent.data.core.repository

import android.content.Intent
import android.nfc.Tag

interface TagRepository {
    fun getTag(intent: Intent): Tag
    fun read(tag: Tag): ByteArray
    suspend fun write(payload: ByteArray, tag: Tag)
    suspend fun erase(tag: Tag)
}
