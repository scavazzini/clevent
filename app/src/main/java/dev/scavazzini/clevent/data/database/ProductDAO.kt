package dev.scavazzini.clevent.data.database;

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.scavazzini.clevent.data.models.Product
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductDAO {
    @Query("SELECT * FROM Product")
    fun getAll(): Flow<List<Product>>

    @Insert
    suspend fun insertAll(products: List<Product>)

    @Query("DELETE FROM Product")
    suspend fun clear()

    @Transaction
    suspend fun clearAndInsert(products: List<Product>) {
        clear()
        insertAll(products)
    }
}
