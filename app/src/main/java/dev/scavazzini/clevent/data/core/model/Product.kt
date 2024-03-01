package dev.scavazzini.clevent.data.core.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Product(
    @PrimaryKey val id: Short,
    val name: String,
    val price: Int,
    val category: String,
) : Serializable {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        return other is Product && id == other.id
    }

    override fun hashCode(): Int {
        return id.toInt()
    }
}
