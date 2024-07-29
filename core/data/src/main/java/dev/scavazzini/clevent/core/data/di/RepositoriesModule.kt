package dev.scavazzini.clevent.core.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.core.data.repository.NdefTagRepository
import dev.scavazzini.clevent.core.data.repository.TagRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Singleton
    @Binds
    abstract fun bindTagRepository(ndefTagRepository: NdefTagRepository): TagRepository
}
