package dev.scavazzini.clevent.core.domain.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.core.domain.serializer.CustomerNFCSerializer
import dev.scavazzini.clevent.core.domain.serializer.CustomerSerializer
import java.util.zip.CRC32
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SerializerCoreDomainModule {
    companion object {
        @Singleton
        @Provides
        fun providesCustomerSerializer(): CustomerSerializer {
            return CustomerNFCSerializer(
                crc = CRC32(),
            )
        }
    }
}

