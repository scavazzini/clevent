package dev.scavazzini.clevent.notification

import android.app.NotificationManager
import android.os.Build
import androidx.annotation.RequiresApi

enum class ChannelImportance {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX;

    @RequiresApi(Build.VERSION_CODES.O)
    fun toNotificationManagerImportance(): Int {
        return when (this) {
            MIN -> NotificationManager.IMPORTANCE_MIN
            LOW -> NotificationManager.IMPORTANCE_LOW
            HIGH -> NotificationManager.IMPORTANCE_HIGH
            MAX -> NotificationManager.IMPORTANCE_MAX
            else -> NotificationManager.IMPORTANCE_DEFAULT
        }
    }
}
