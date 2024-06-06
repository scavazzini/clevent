package dev.scavazzini.clevent.domain.receipt.di

import com.google.zxing.MultiFormatWriter
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class ReceiptDomainModule {
    companion object {
        @Provides
        fun provideMultiFormatWriter(): MultiFormatWriter = MultiFormatWriter()
    }
}
