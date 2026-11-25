package com.example.latienditamanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import com.example.latienditamanager.data.repository.ProveedorRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProveedorViewModel(
    private val proveedorRepository: ProveedorRepository
) : ViewModel() {

    val proveedores: StateFlow<List<ProveedorEntity>> = proveedorRepository.todos
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Proveedor seleccionado actualmente para ver su detalle
    private val _proveedorSeleccionado = MutableStateFlow<ProveedorEntity?>(null)
    val proveedorSeleccionado: StateFlow<ProveedorEntity?> =
        _proveedorSeleccionado.asStateFlow()

    // Productos del proveedor seleccionado
    val productosDelProveedor: StateFlow<List<ProductoProveedorEntity>> =
        _proveedorSeleccionado
            .flatMapLatest { proveedor ->
                if (proveedor == null) flowOf(emptyList())
                else proveedorRepository.productosDe(proveedor.id)
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = emptyList()
            )

    fun seleccionarProveedor(proveedor: ProveedorEntity) {
        _proveedorSeleccionado.value = proveedor
    }

    fun insertar(proveedor: ProveedorEntity) = viewModelScope.launch {
        proveedorRepository.insertar(proveedor)
    }

    fun actualizar(proveedor: ProveedorEntity) = viewModelScope.launch {
        proveedorRepository.actualizar(proveedor)
    }

    fun eliminar(proveedor: ProveedorEntity) = viewModelScope.launch {
        proveedorRepository.eliminar(proveedor)
    }

    fun vincularProducto(relacion: ProductoProveedorEntity) = viewModelScope.launch {
        proveedorRepository.vincularProducto(relacion)
    }

    fun desvincularProducto(relacion: ProductoProveedorEntity) = viewModelScope.launch {
        proveedorRepository.desvincularProducto(relacion)
    }

    fun proveedorViewModel_productosDeProveedor(proveedorId: Int) =
        proveedorRepository.productosDe(proveedorId)

    fun obtenerProveedorPrincipalDeProducto(codigoBarras: String) =
        proveedorRepository.proveedoresDe(codigoBarras)
}