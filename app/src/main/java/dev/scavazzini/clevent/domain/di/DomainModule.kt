package dev.scavazzini.clevent.domain.di

import com.google.zxing.MultiFormatWriter
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.domain.crypto.Encryptor
import dev.scavazzini.clevent.domain.crypto.FakeEncryptor
import dev.scavazzini.clevent.domain.serializer.CustomerProtobufSerializer
import dev.scavazzini.clevent.domain.serializer.CustomerSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {

    companion object {
        @Provides
        fun provideMultiFormatWriter(): MultiFormatWriter = MultiFormatWriter()
    }

    @Singleton
    @Binds
    abstract fun bindEncryptor(encryptor: FakeEncryptor): Encryptor

    @Singleton
    @Binds
    abstract fun bindCustomerSerializer(customerSerializer: CustomerProtobufSerializer): CustomerSerializer
}

