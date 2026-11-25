package com.example.latienditamanager.data.repository

import com.example.latienditamanager.data.local.dao.ProductoProveedorDao
import com.example.latienditamanager.data.local.dao.ProveedorDao
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import kotlinx.coroutines.flow.Flow

class ProveedorRepository(
    private val proveedorDao: ProveedorDao,
    private val productoProveedorDao: ProductoProveedorDao
) {

    val todos: Flow<List<ProveedorEntity>> =
        proveedorDao.obtenerTodos()

    suspend fun obtenerPorId(id: Int): ProveedorEntity? =
        proveedorDao.obtenerPorId(id)

    suspend fun insertar(proveedor: ProveedorEntity) =
        proveedorDao.insertar(proveedor)

    suspend fun actualizar(proveedor: ProveedorEntity) =
        proveedorDao.actualizar(proveedor)

    suspend fun eliminar(proveedor: ProveedorEntity) =
        proveedorDao.eliminar(proveedor)

    // Relaciones producto <-> proveedor
    fun proveedoresDe(codigoBarras: String): Flow<List<ProductoProveedorEntity>> =
        productoProveedorDao.obtenerProveedoresDe(codigoBarras)

    fun productosDe(proveedorId: Int): Flow<List<ProductoProveedorEntity>> =
        productoProveedorDao.obtenerProductosDe(proveedorId)

    suspend fun vincularProducto(relacion: ProductoProveedorEntity) =
        productoProveedorDao.insertar(relacion)

    suspend fun actualizarVinculo(relacion: ProductoProveedorEntity) =
        productoProveedorDao.actualizar(relacion)

    suspend fun desvincularProducto(relacion: ProductoProveedorEntity) =
        productoProveedorDao.eliminar(relacion)

    suspend fun proveedorPrincipalDe(codigoBarras: String): ProductoProveedorEntity? =
        productoProveedorDao.obtenerPrincipalDe(codigoBarras)
}