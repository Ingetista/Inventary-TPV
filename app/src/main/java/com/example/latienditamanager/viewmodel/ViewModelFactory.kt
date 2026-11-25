package com.example.latienditamanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.latienditamanager.data.repository.ImportacionRepository
import com.example.latienditamanager.data.repository.MovimientoRepository
import com.example.latienditamanager.data.repository.ProductoRepository
import com.example.latienditamanager.data.repository.ProveedorRepository

class ViewModelFactory(
    private val productoRepository: ProductoRepository,
    private val proveedorRepository: ProveedorRepository,
    private val movimientoRepository: MovimientoRepository,
    private val importacionRepository: ImportacionRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return when {
            modelClass.isAssignableFrom(ProductoViewModel::class.java) ->
                ProductoViewModel(productoRepository, movimientoRepository) as T
            modelClass.isAssignableFrom(ProveedorViewModel::class.java) ->
                ProveedorViewModel(proveedorRepository) as T
            modelClass.isAssignableFrom(MovimientoViewModel::class.java) ->
                MovimientoViewModel(movimientoRepository) as T
            modelClass.isAssignableFrom(ImportacionViewModel::class.java) ->
                ImportacionViewModel(importacionRepository) as T
            else -> throw IllegalArgumentException("ViewModel desconocido: ${modelClass.name}")
        }
    }
}