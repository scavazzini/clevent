package dev.scavazzini.clevent.notification

import androidx.core.app.NotificationCompat

enum class NotificationPriority {
    MIN,
    LOW,
    DEFAULT,
    HIGH,
    MAX;

    fun toNotificationPriority(): Int {
        return when (this) {
            MIN -> NotificationCompat.PRIORITY_MIN
            LOW -> NotificationCompat.PRIORITY_LOW
            HIGH -> NotificationCompat.PRIORITY_HIGH
            MAX -> NotificationCompat.PRIORITY_HIGH
            else -> NotificationCompat.PRIORITY_DEFAULT
        }
    }
}
