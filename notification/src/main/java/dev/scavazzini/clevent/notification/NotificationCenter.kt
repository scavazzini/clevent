package dev.scavazzini.clevent.notification

import android.app.PendingIntent
import android.os.Build
import androidx.annotation.RequiresApi

interface NotificationCenter {
    companion object {
        const val DEFAULT_CHANNEL_ID = "clevent"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun createChannel(
        id: String,
        name: String,
        importance: ChannelImportance = ChannelImportance.DEFAULT,
        description: String? = null,
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun deleteChannel(id: String)

    fun postNotification(
        title: String,
        pendingIntent: PendingIntent,
        channelId: String = DEFAULT_CHANNEL_ID,
        priority: NotificationPriority = NotificationPriority.DEFAULT,
        isOngoing: Boolean = false,
        isAutoCancel: Boolean = true,
        id: Int? = null,
        text: String? = null,
        icon: Int? = null,
        progressBar: NotificationProgress? = null,
    ): Int

    fun cancelNotification(id: Int)
}
