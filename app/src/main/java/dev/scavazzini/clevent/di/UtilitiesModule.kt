package dev.scavazzini.clevent.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.components.ActivityComponent
import dev.scavazzini.clevent.io.NFCReader
import dev.scavazzini.clevent.utilities.crypto.Encryptor
import dev.scavazzini.clevent.utilities.crypto.FakeEncryptor
import dev.scavazzini.clevent.utilities.serializers.CustomerNFCSerializer
import dev.scavazzini.clevent.utilities.serializers.CustomerSerializer

@Module
@InstallIn(ActivityComponent::class)
object UtilitiesModule {

    @Provides
    fun provideEncryptor(): Encryptor = FakeEncryptor()

    @Provides
    fun provideCustomerSerializer(): CustomerSerializer = CustomerNFCSerializer()

    @Provides
    fun provideNFCTagParser(serializer: CustomerSerializer, encryptor: Encryptor): NFCReader {
        return NFCReader(serializer, encryptor)
    }

}
