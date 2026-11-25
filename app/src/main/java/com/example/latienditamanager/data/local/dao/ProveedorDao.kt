package com.example.latienditamanager.data.local.dao

import androidx.room.*
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ProveedorDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertar(proveedor: ProveedorEntity)

    @Update
    suspend fun actualizar(proveedor: ProveedorEntity)

    @Delete
    suspend fun eliminar(proveedor: ProveedorEntity)

    @Query("SELECT * FROM proveedores ORDER BY nombre ASC")
    fun obtenerTodos(): Flow<List<ProveedorEntity>>

    @Query("SELECT * FROM proveedores WHERE id = :id")
    suspend fun obtenerPorId(id: Int): ProveedorEntity?
}