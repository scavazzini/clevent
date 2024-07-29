package dev.scavazzini.clevent.core.data.database;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.scavazzini.clevent.core.data.model.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDao {
    @Query("SELECT * FROM Product")
    fun getAll(): Flow<List<Product>>

    @Transaction
    suspend fun clearAndInsert(products: List<Product>) {
        clear()
        insertAll(products)
    }

    @Insert
    suspend fun insertAll(products: List<Product>)

    @Query("DELETE FROM Product")
    suspend fun clear()
}
