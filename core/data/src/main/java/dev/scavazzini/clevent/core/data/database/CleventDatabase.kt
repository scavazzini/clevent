package dev.scavazzini.clevent.core.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import dev.scavazzini.clevent.core.data.model.Product;

@Database(entities = [Product::class], version = 1, exportSchema = false)
abstract class CleventDatabase : RoomDatabase() {
    abstract fun productDao(): ProductDao

    companion object {
        @Volatile
        private var instance: CleventDatabase? = null

        fun getInstance(context: Context): CleventDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): CleventDatabase =
            Room.databaseBuilder(context, CleventDatabase::class.java, "clevent.db").build()
    }
}
