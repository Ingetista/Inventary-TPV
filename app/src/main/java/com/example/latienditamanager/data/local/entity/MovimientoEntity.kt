package com.example.latienditamanager.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "movimientos",
    foreignKeys = [
        ForeignKey(
            entity = ProductoEntity::class,
            parentColumns = ["codigoBarras"],
            childColumns = ["codigoBarras"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MovimientoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val codigoBarras: String,
    val tipo: TipoMovimiento,           // Enum — ver abajo
    val cantidad: Double,               // Positivo = entrada, negativo = salida
    val fecha: Long,                    // Epoch millis
    val notas: String = "",
    val origenCsv: Boolean = false      // true si vino de importación, false si es manual
)

enum class TipoMovimiento {
    VENTA,          // Descuento por venta (viene del CSV)
    ENTRADA,        // Reposición de mercancía
    AJUSTE_MANUAL,  // Corrección de inventario
    CADUCIDAD       // Baja por producto caducado
}