package dev.scavazzini.clevent.notification.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.notification.NotificationCenter
import dev.scavazzini.clevent.notification.NotificationCenterImpl
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NotificationModule {
    @Singleton
    @Binds
    abstract fun bindsNotificationCenter(
        notificationCenter: NotificationCenterImpl,
    ): NotificationCenter
}
