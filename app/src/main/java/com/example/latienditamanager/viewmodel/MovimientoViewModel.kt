package com.example.latienditamanager.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latienditamanager.data.local.dao.ResumenVentas
import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.TipoMovimiento
import com.example.latienditamanager.data.repository.MovimientoRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Calendar

class MovimientoViewModel(
    private val movimientoRepository: MovimientoRepository
) : ViewModel() {

    // Rango de fechas seleccionado — por defecto el mes actual
    private val _desde = MutableStateFlow(inicioDelMes())
    private val _hasta = MutableStateFlow(System.currentTimeMillis())

    val desde: StateFlow<Long> = _desde.asStateFlow()
    val hasta: StateFlow<Long> = _hasta.asStateFlow()

    // Movimientos del rango seleccionado
    val movimientos: StateFlow<List<MovimientoEntity>> = combine(_desde, _hasta) { d, h ->
        Pair(d, h)
    }.flatMapLatest { (d, h) ->
        movimientoRepository.obtenerEnRango(d, h)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Más vendidos en el rango seleccionado
    val masVendidos: StateFlow<List<ResumenVentas>> = combine(_desde, _hasta) { d, h ->
        Pair(d, h)
    }.flatMapLatest { (d, h) ->
        movimientoRepository.obtenerMasVendidos(d, h)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun cambiarRango(desde: Long, hasta: Long) {
        _desde.value = desde
        _hasta.value = hasta
    }

    fun registrarMovimiento(movimiento: MovimientoEntity) = viewModelScope.launch {
        movimientoRepository.registrarMovimiento(movimiento)
    }

    fun registrarMovimientosMasivo(movimientos: List<MovimientoEntity>) =
        viewModelScope.launch {
            movimientoRepository.registrarMovimientosMasivo(movimientos)
        }

    suspend fun totalVendidoDeProducto(codigoBarras: String): Double =
        movimientoRepository.totalVendidoDeProducto(
            codigoBarras,
            _desde.value,
            _hasta.value
        )

    // Helpers de fecha
    private fun inicioDelMes(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}