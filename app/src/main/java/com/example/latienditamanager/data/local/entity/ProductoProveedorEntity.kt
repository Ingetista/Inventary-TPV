package com.example.latienditamanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "producto_proveedor",
    primaryKeys = ["codigoBarras", "proveedorId"],   // PK compuesta
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["codigoBarras"],
            childColumns = ["codigoBarras"],
            onDelete = ForeignKey.CASCADE            // Si borras producto, se borra la relación
        ),
        ForeignKey(
            entity = ProveedorEntity::class,
            parentColumns = ["id"],
            childColumns = ["proveedorId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class ProductoProveedorEntity(
    val codigoBarras: String,
    val proveedorId: Int,
    val precioCompra: Double,           // Precio que cobra este proveedor
    val precioVenta: Double,            // Precio al que lo vende tu amigo
    val esPrincipal: Boolean = false    // Proveedor preferido para este producto
)