package dev.scavazzini.clevent.data.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Product(@PrimaryKey val id: Short, var name: String, var price: Int) : Serializable
