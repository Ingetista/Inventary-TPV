package com.example.latienditamanager.data.local.dao

import androidx.room.*
import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

@Dao
interface MovimientoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(movimiento: MovimientoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(movimientos: List<MovimientoEntity>)

    @Delete
    suspend fun eliminar(movimiento: MovimientoEntity)

    // Historial completo de un producto
    @Query("""
        SELECT * FROM movimientos 
        WHERE codigoBarras = :codigoBarras 
        ORDER BY fecha DESC
    """)
    fun obtenerPorProducto(codigoBarras: String): Flow<List<MovimientoEntity>>

    // Movimientos en un rango de fechas — el filtro desde/hasta que pidió tu amigo
    @Query("""
        SELECT * FROM movimientos 
        WHERE fecha BETWEEN :desde AND :hasta 
        ORDER BY fecha DESC
    """)
    fun obtenerEnRango(desde: Long, hasta: Long): Flow<List<MovimientoEntity>>

    // Movimientos por tipo en un rango de fechas
    @Query("""
        SELECT * FROM movimientos 
        WHERE tipo = :tipo 
        AND fecha BETWEEN :desde AND :hasta 
        ORDER BY fecha DESC
    """)
    fun obtenerPorTipoEnRango(
        tipo: TipoMovimiento,
        desde: Long,
        hasta: Long
    ): Flow<List<MovimientoEntity>>

    // Más vendidos — suma de unidades vendidas por producto en un rango
    @Query("""
        SELECT codigoBarras, SUM(ABS(cantidad)) as totalVendido
        FROM movimientos
        WHERE tipo = 'VENTA'
        AND fecha BETWEEN :desde AND :hasta
        GROUP BY codigoBarras
        ORDER BY totalVendido DESC
    """)
    fun obtenerMasVendidos(desde: Long, hasta: Long): Flow<List<ResumenVentas>>

    // Total vendido de un producto específico en un rango
    @Query("""
        SELECT COALESCE(SUM(ABS(cantidad)), 0)
        FROM movimientos
        WHERE codigoBarras = :codigoBarras
        AND tipo = 'VENTA'
        AND fecha BETWEEN :desde AND :hasta
    """)
    suspend fun totalVendidoDeProducto(
        codigoBarras: String,
        desde: Long,
        hasta: Long
    ): Double
}

// Clase auxiliar para la query de más vendidos
data class ResumenVentas(
    val codigoBarras: String,
    val totalVendido: Double
)