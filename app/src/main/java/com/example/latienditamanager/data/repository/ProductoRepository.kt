package com.example.latienditamanager.data.repository

import com.example.latienditamanager.data.local.dao.ProductoDao
import com.example.latienditamanager.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.onEach

class ProductoRepository(private val productoDao: ProductoDao) {

    val todos: Flow<List<ProductoEntity>> =
        productoDao.obtenerTodos().onEach {
            android.util.Log.d("ROOM_FLOW", ">>> El Flow de productos emitió ${it.size} elementos")
        }


    val stockBajo: Flow<List<ProductoEntity>> =
        productoDao.obtenerStockBajo()

    fun buscarPorNombre(nombre: String): Flow<List<ProductoEntity>> =
        productoDao.buscarPorNombre(nombre)

    fun proximosACaducar(fechaLimite: Long): Flow<List<ProductoEntity>> =
        productoDao.obtenerProximosACaducar(fechaLimite)

    suspend fun obtenerPorCodigo(codigoBarras: String): ProductoEntity? =
        productoDao.obtenerPorCodigo(codigoBarras)

    suspend fun insertar(producto: ProductoEntity) =
        productoDao.insertar(producto)

    suspend fun insertarTodos(productos: List<ProductoEntity>) =
        productoDao.insertarTodos(productos)

    suspend fun actualizar(producto: ProductoEntity) =
        productoDao.actualizar(producto)

    suspend fun eliminar(producto: ProductoEntity) =
        productoDao.eliminar(producto)

    suspend fun incrementarStock(codigoBarras: String, cantidad: Double) =
        productoDao.incrementarStock(codigoBarras, cantidad)


}