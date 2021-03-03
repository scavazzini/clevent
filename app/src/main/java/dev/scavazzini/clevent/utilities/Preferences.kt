package dev.scavazzini.clevent.utilities

import android.content.SharedPreferences

private const val LAST_SYNC = "last_sync"
private const val ENDPOINT = "endpoint"

class Preferences(private val sharedPreferences: SharedPreferences) :
        SharedPreferences by sharedPreferences {

    private val editor = edit()

    var lastSync: Long
        get() = getLong(LAST_SYNC, -1)
        set(value) = editor.putLong(LAST_SYNC, value).apply()

    val endpoint: String
        get() = getString(ENDPOINT, "") ?: ""
}
