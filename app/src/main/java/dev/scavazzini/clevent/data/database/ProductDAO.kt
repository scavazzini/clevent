package dev.scavazzini.clevent.data.database;

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import dev.scavazzini.clevent.data.models.Product

@Dao
interface ProductDAO {
    @Query("SELECT * FROM Product")
    fun getAll(): LiveData<List<Product>>

    @Query("SELECT * FROM Product WHERE id IN (:ids)")
    suspend fun getProducts(ids: List<Short>): List<Product>

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
