package dev.scavazzini.clevent.domain.core.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.domain.core.crypto.AesEncryptor
import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManager
import dev.scavazzini.clevent.domain.core.crypto.SecretKeyManagerImpl
import dev.scavazzini.clevent.domain.core.crypto.SymmetricEncryptor
import dev.scavazzini.clevent.domain.core.serializer.CustomerProtobufSerializer
import dev.scavazzini.clevent.domain.core.serializer.CustomerSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class CoreDomainModule {
    @Singleton
    @Binds
    abstract fun bindEncryptor(encryptor: AesEncryptor): SymmetricEncryptor

    @Singleton
    @Binds
    abstract fun bindSecretKeyManager(secretKeyManagerImpl: SecretKeyManagerImpl): SecretKeyManager

    @Singleton
    @Binds
    abstract fun bindCustomerSerializer(customerSerializer: CustomerProtobufSerializer): CustomerSerializer
}

