package dev.scavazzini.clevent.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.utilities.crypto.Encryptor
import dev.scavazzini.clevent.utilities.crypto.FakeEncryptor
import dev.scavazzini.clevent.utilities.serializers.CustomerNFCSerializer
import dev.scavazzini.clevent.utilities.serializers.CustomerSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class UtilitiesModule {
    @Singleton
    @Binds
    abstract fun bindEncryptor(encryptor: FakeEncryptor): Encryptor

    @Singleton
    @Binds
    abstract fun bindCustomerSerializer(customerSerializer: CustomerNFCSerializer): CustomerSerializer
}
