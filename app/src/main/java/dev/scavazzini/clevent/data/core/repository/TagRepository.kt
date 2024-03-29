package dev.scavazzini.clevent.data.core.repository

import android.content.Intent

interface TagRepository {
    fun read(intent: Intent): ByteArray
    suspend fun write(payload: ByteArray, intent: Intent)
    suspend fun erase(intent: Intent)
}
