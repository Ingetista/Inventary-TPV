package com.example.latienditamanager.data.local.dao

import androidx.room.*
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoProveedorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(relacion: ProductoProveedorEntity)

    @Update
    suspend fun actualizar(relacion: ProductoProveedorEntity)

    @Delete
    suspend fun eliminar(relacion: ProductoProveedorEntity)

    // Todos los proveedores de un producto
    @Query("""
        SELECT * FROM producto_proveedor 
        WHERE codigoBarras = :codigoBarras
    """)
    fun obtenerProveedoresDe(codigoBarras: String): Flow<List<ProductoProveedorEntity>>

    // Todos los productos de un proveedor
    @Query("""
        SELECT * FROM producto_proveedor 
        WHERE proveedorId = :proveedorId
    """)
    fun obtenerProductosDe(proveedorId: Int): Flow<List<ProductoProveedorEntity>>

    // Proveedor principal de un producto
    @Query("""
        SELECT * FROM producto_proveedor 
        WHERE codigoBarras = :codigoBarras AND esPrincipal = 1
        LIMIT 1
    """)
    suspend fun obtenerPrincipalDe(codigoBarras: String): ProductoProveedorEntity?
}