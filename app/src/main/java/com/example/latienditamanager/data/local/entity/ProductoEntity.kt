package com.example.latienditamanager.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "productos")
data class ProductoEntity(
    @PrimaryKey
    val codigoBarras: String,           // El código del producto es PK natural
    val nombre: String,
    val categoria: String = "",
    val stockActual: Double = 0.0,
    val stockMinimo: Double = 0.0,      // Para alertas de stock bajo
    val unidadMedida: String = "ud",    // ud, kg, L, etc.
    val fechaCaducidad: Long? = null,   // Epoch millis — null si no caduca
    val activo: Boolean = true,
    val impuesto: Double = 0.0 // Para archivar sin borrar
)