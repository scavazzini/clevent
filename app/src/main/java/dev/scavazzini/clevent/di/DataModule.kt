package dev.scavazzini.clevent.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.data.datasource.CleventDatabase
import dev.scavazzini.clevent.data.datasource.LocalProductDataSource
import dev.scavazzini.clevent.data.datasource.RemoteProductDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {
    @Singleton
    @Provides
    fun provideSharedPreferences(@ApplicationContext context: Context): SharedPreferences {
        return PreferenceManager.getDefaultSharedPreferences(context)
    }

    @Singleton
    @Provides
    fun provideDatabase(@ApplicationContext context: Context): CleventDatabase {
        return CleventDatabase.getInstance(context)
    }

    @Provides
    fun provideLocalProductDataSource(cleventDatabase: CleventDatabase): LocalProductDataSource {
        return cleventDatabase.productDao()
    }

    @Singleton
    @Provides
    fun provideRemoteProductDataSource(): RemoteProductDataSource {
        return RemoteProductDataSource.create()
    }
}
