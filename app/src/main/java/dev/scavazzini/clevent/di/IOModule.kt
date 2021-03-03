package dev.scavazzini.clevent.di

import com.google.zxing.MultiFormatWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent

@Module
@InstallIn(ActivityComponent::class)
object IOModule {

    @Provides
    fun provideMultiFormatWriter(): MultiFormatWriter = MultiFormatWriter()

}
