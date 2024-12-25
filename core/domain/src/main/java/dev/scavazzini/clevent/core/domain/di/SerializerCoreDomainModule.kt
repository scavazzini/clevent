package dev.scavazzini.clevent.core.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.core.domain.serializer.Crc32Calculator
import dev.scavazzini.clevent.core.domain.serializer.CrcCalculator
import dev.scavazzini.clevent.core.domain.serializer.CustomerNFCSerializer
import dev.scavazzini.clevent.core.domain.serializer.CustomerSerializer
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SerializerCoreDomainModule {
    companion object {
        @Provides
        fun providesCrcCalculator(): CrcCalculator {
            return Crc32Calculator()
        }

        @Singleton
        @Provides
        fun providesCustomerSerializer(crcCalculator: CrcCalculator): CustomerSerializer {
            return CustomerNFCSerializer(crcCalculator)
        }
    }
}
