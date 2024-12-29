package dev.scavazzini.clevent.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.scavazzini.clevent.notification.NotificationCenter.Companion.DEFAULT_CHANNEL_ID
import javax.inject.Inject
import kotlin.random.Random

class NotificationCenterImpl @Inject constructor(
    @ApplicationContext private val context: Context,
) : NotificationCenter {
    private val notificationManager = context
        .getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && notificationManager.getNotificationChannel(DEFAULT_CHANNEL_ID) == null
        ) {
            createChannel(
                id = DEFAULT_CHANNEL_ID,
                name = "Clevent",
                description = "Clevent notifications",
            )
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun createChannel(
        id: String,
        name: String,
        importance: ChannelImportance,
        description: String?,
    ) {
        if (notificationManager.getNotificationChannel(id) != null) {
            return
        }

        val channel = NotificationChannel(id, name, importance.toNotificationManagerImportance())

        if (description != null) {
            channel.description = description
        }

        notificationManager.createNotificationChannel(channel)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun deleteChannel(id: String) {
        notificationManager.deleteNotificationChannel(id)
    }

    override fun postNotification(
        title: String,
        pendingIntent: PendingIntent,
        channelId: String,
        priority: NotificationPriority,
        isOngoing: Boolean,
        isAutoCancel: Boolean,
        id: Int?,
        text: String?,
        icon: Int?,
        progressBar: NotificationProgress?,
    ): Int {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(icon ?: R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(text)
            .setPriority(priority.toNotificationPriority())
            .setContentIntent(pendingIntent)
            .setAutoCancel(isAutoCancel)
            .setOngoing(isOngoing)

        if (progressBar != null) {
            builder.setProgress(progressBar.max, progressBar.progress, progressBar.indefinite)
        }

        val notificationId = id ?: Random.nextInt()
        notificationManager.notify(notificationId, builder.build())
        return notificationId
    }

    override fun cancelNotification(id: Int) {
        notificationManager.cancel(id)
    }
}
