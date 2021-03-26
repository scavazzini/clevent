package dev.scavazzini.clevent.di

import com.google.zxing.MultiFormatWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object IOModule {
    @Provides
    fun provideMultiFormatWriter(): MultiFormatWriter = MultiFormatWriter()
}
