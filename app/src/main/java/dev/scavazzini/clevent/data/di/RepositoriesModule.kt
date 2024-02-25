package dev.scavazzini.clevent.data.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.data.repositories.NdefTagRepository
import dev.scavazzini.clevent.data.repositories.TagRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoriesModule {
    @Singleton
    @Binds
    abstract fun bindTagRepository(ndefTagRepository: NdefTagRepository): TagRepository
}
