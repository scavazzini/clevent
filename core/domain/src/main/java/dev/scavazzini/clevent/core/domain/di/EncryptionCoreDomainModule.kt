package dev.scavazzini.clevent.core.domain.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.core.domain.crypto.AesEncryptor
import dev.scavazzini.clevent.core.domain.crypto.SecretKeyManager
import dev.scavazzini.clevent.core.domain.crypto.SecretKeyManagerImpl
import dev.scavazzini.clevent.core.domain.crypto.SymmetricEncryptor
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class EncryptionCoreDomainModule {
    @Singleton
    @Binds
    abstract fun bindEncryptor(encryptor: AesEncryptor): SymmetricEncryptor

    @Singleton
    @Binds
    abstract fun bindSecretKeyManager(secretKeyManagerImpl: SecretKeyManagerImpl): SecretKeyManager
}

