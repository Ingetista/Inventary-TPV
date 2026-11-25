package com.example.latienditamanager.data.local.dao

import androidx.room.*
import com.example.latienditamanager.data.local.entity.ProductoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProductoDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(producto: ProductoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertarTodos(productos: List<ProductoEntity>)

    @Update
    suspend fun actualizar(producto: ProductoEntity)

    @Delete
    suspend fun eliminar(producto: ProductoEntity)

    @Query("SELECT * FROM productos WHERE activo = 1 ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<ProductoEntity>>

    @Query("SELECT * FROM productos WHERE codigoBarras = :codigoBarras")
    suspend fun obtenerPorCodigo(codigoBarras: String): ProductoEntity?

    @Query("SELECT * FROM productos WHERE nombre LIKE '%' || :nombre || '%' AND activo = 1")
    fun buscarPorNombre(nombre: String): Flow<List<ProductoEntity>>

    // Productos con stock por debajo del mínimo — para alertas
    @Query("SELECT * FROM productos WHERE stockActual <= stockMinimo AND activo = 1")
    fun obtenerStockBajo(): Flow<List<ProductoEntity>>

    // Agrega esta línea al final de la interfaz ProductoDao
    @Query("SELECT * FROM productos WHERE activo = 1")
    suspend fun obtenerTodosUnaSola(): List<ProductoEntity>

    @Query("UPDATE productos SET stockActual = stockActual + :cantidad WHERE codigoBarras = :codigoBarras")
    suspend fun incrementarStock(codigoBarras: String, cantidad: Double)

    // Productos próximos a caducar — los que caducan en los próximos 'dias' días
    @Query("""
        SELECT * FROM productos 
        WHERE fechaCaducidad IS NOT NULL 
        AND fechaCaducidad <= :fechaLimite 
        AND activo = 1
        ORDER BY fechaCaducidad ASC
    """)
    fun obtenerProximosACaducar(fechaLimite: Long): Flow<List<ProductoEntity>>
}