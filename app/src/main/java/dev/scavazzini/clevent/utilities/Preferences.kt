package dev.scavazzini.clevent.utilities

import android.content.SharedPreferences
import javax.inject.Inject

private const val LAST_SYNC = "last_sync"
private const val ENDPOINT = "endpoint"

class Preferences @Inject constructor(private val sharedPreferences: SharedPreferences) {

    private val editor = sharedPreferences.edit()

    var lastSync: Long
        get() = sharedPreferences.getLong(LAST_SYNC, -1)
        set(value) = editor.putLong(LAST_SYNC, value).apply()

    val endpoint: String
        get() = sharedPreferences.getString(ENDPOINT, "") ?: ""
}
