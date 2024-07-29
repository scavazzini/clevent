package dev.scavazzini.clevent.core.data.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.core.data.database.CleventDatabase
import dev.scavazzini.clevent.core.data.database.ProductDao
import dev.scavazzini.clevent.core.data.datasource.RemoteProductDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataSourceModule {
    @Provides
    fun provideLocalProductDataSource(cleventDatabase: CleventDatabase): ProductDao {
        return cleventDatabase.productDao()
    }

    @Singleton
    @Provides
    fun provideRemoteProductDataSource(): RemoteProductDataSource {
        return RemoteProductDataSource.create()
    }

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
}
