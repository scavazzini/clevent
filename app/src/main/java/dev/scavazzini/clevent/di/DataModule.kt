package dev.scavazzini.clevent.di

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import dev.scavazzini.clevent.data.database.CleventDatabase
import dev.scavazzini.clevent.data.database.ProductDAO
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
    fun provideProductDao(cleventDatabase: CleventDatabase): ProductDAO {
        return cleventDatabase.productDao()
    }
}
