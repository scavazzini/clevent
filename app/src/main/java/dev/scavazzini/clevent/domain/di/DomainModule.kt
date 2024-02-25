package dev.scavazzini.clevent.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.domain.crypto.Encryptor
import dev.scavazzini.clevent.domain.crypto.FakeEncryptor
import dev.scavazzini.clevent.domain.serializer.CustomerNFCSerializer
import dev.scavazzini.clevent.domain.serializer.CustomerSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Singleton
    @Binds
    abstract fun bindEncryptor(encryptor: FakeEncryptor): Encryptor

    @Singleton
    @Binds
    abstract fun bindCustomerSerializer(customerSerializer: CustomerNFCSerializer): CustomerSerializer
}

