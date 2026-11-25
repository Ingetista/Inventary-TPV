package com.example.latienditamanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.ProductoEntity
import com.example.latienditamanager.data.local.entity.TipoMovimiento
import com.example.latienditamanager.data.repository.MovimientoRepository
import com.example.latienditamanager.data.repository.ProductoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class ProductoViewModel(
    private val productoRepository: ProductoRepository,
    private val movimientoRepository: MovimientoRepository   // ← añadir

) : ViewModel() {

    // Búsqueda en tiempo real
    private val _textoBusqueda = MutableStateFlow("")
    val textoBusqueda: StateFlow<String> = _textoBusqueda.asStateFlow()

    // Flow base — siempre activo, sin debounce
    private val _todosLosProductos: StateFlow<List<ProductoEntity>> =
        productoRepository.todos
            .stateIn(
                scope        = viewModelScope,
                started      = SharingStarted.Eagerly,
                initialValue = emptyList()
            )

    val productos: StateFlow<List<ProductoEntity>> = _todosLosProductos

    // Alertas de stock bajo para el dashboard
    val stockBajo: StateFlow<List<ProductoEntity>> = productoRepository.stockBajo
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    // Próximos a caducar en los próximos 30 días
    val proximosACaducar: StateFlow<List<ProductoEntity>> = productoRepository
        .proximosACaducar(
            fechaLimite = System.currentTimeMillis() + (30L * 24 * 60 * 60 * 1000)
        )
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    fun onTextoBusquedaCambia(texto: String) {
        _textoBusqueda.value = texto
    }

    fun insertar(producto: ProductoEntity) = viewModelScope.launch {
        productoRepository.insertar(producto)
    }

    fun actualizar(producto: ProductoEntity) = viewModelScope.launch {
        productoRepository.actualizar(producto)
    }

    fun eliminar(producto: ProductoEntity) = viewModelScope.launch {
        productoRepository.eliminar(producto)
    }

    fun registrarEntrada(codigoBarras: String, cantidad: Double) = viewModelScope.launch {
        android.util.Log.d("ENTRADA", ">>> Llamada recibida: $codigoBarras / $cantidad")

        // Verificamos que el producto exista antes de impactarlo
        val producto = productoRepository.obtenerPorCodigo(codigoBarras) ?: run {
            android.util.Log.e("ENTRADA", ">>> Producto NO encontrado para: $codigoBarras")
            return@launch
        }

        android.util.Log.d("ENTRADA", ">>> Stock antes: ${producto.stockActual}")

        // Ejecutamos la actualización atómica directamente en la BD
        productoRepository.incrementarStock(codigoBarras, cantidad)

        android.util.Log.d("ENTRADA", ">>> Stock incrementado en BD con éxito")

        movimientoRepository.insertarMovimientoSolo(
            MovimientoEntity(
                codigoBarras = codigoBarras,
                tipo         = TipoMovimiento.ENTRADA,
                cantidad     = cantidad,
                fecha        = System.currentTimeMillis(),
                notas        = "Entrada manual de mercancía",
                origenCsv    = false
            )
        )

        android.util.Log.d("ENTRADA", ">>> Movimiento insertado OK")
    }
}