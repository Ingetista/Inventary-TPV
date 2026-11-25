package com.example.latienditamanager.data.repository

import com.example.latienditamanager.data.local.dao.MovimientoDao
import com.example.latienditamanager.data.local.dao.ProductoDao
import com.example.latienditamanager.data.local.dao.ResumenVentas
import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.TipoMovimiento
import kotlinx.coroutines.flow.Flow

class MovimientoRepository(
    private val movimientoDao: MovimientoDao,
    private val productoDao: ProductoDao
) {

    fun obtenerPorProducto(codigoBarras: String): Flow<List<MovimientoEntity>> =
        movimientoDao.obtenerPorProducto(codigoBarras)

    fun obtenerEnRango(desde: Long, hasta: Long): Flow<List<MovimientoEntity>> =
        movimientoDao.obtenerEnRango(desde, hasta)

    fun obtenerPorTipoEnRango(
        tipo: TipoMovimiento,
        desde: Long,
        hasta: Long
    ): Flow<List<MovimientoEntity>> =
        movimientoDao.obtenerPorTipoEnRango(tipo, desde, hasta)

    fun obtenerMasVendidos(desde: Long, hasta: Long): Flow<List<ResumenVentas>> =
        movimientoDao.obtenerMasVendidos(desde, hasta)

    suspend fun totalVendidoDeProducto(
        codigoBarras: String,
        desde: Long,
        hasta: Long
    ): Double = movimientoDao.totalVendidoDeProducto(codigoBarras, desde, hasta)

    // Registra el movimiento Y actualiza el stock del producto atomicamente
    suspend fun registrarMovimiento(movimiento: MovimientoEntity) {
        val producto = productoDao.obtenerPorCodigo(movimiento.codigoBarras)
            ?: return

        val nuevoStock = when (movimiento.tipo) {
            TipoMovimiento.VENTA,
            TipoMovimiento.CADUCIDAD   -> producto.stockActual - movimiento.cantidad
            TipoMovimiento.ENTRADA,
            TipoMovimiento.AJUSTE_MANUAL -> producto.stockActual + movimiento.cantidad
        }

        productoDao.actualizar(producto.copy(stockActual = nuevoStock))
        movimientoDao.insertar(movimiento)
    }

    // Importación masiva desde CSV — registra todos los movimientos del día
    suspend fun registrarMovimientosMasivo(movimientos: List<MovimientoEntity>) {
        movimientos.forEach { registrarMovimiento(it) }
    }

    // Añade al final de MovimientoRepository.kt
    suspend fun insertarMovimientoSolo(movimiento: MovimientoEntity) {
        movimientoDao.insertar(movimiento)
    }
}