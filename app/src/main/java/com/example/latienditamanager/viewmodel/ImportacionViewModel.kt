package com.example.latienditamanager.viewmodel

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.latienditamanager.data.repository.ImportacionRepository
import com.example.latienditamanager.data.repository.ResultadoImportacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class EstadoImportacion {
    object Inactivo   : EstadoImportacion()
    object Cargando   : EstadoImportacion()
    data class Exito(
        val productosNuevos: Int,
        val movimientosRegistrados: Int
    ) : EstadoImportacion()
    data class Error(val mensaje: String) : EstadoImportacion()
}

class ImportacionViewModel(
    private val importacionRepository: ImportacionRepository
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoImportacion>(EstadoImportacion.Inactivo)
    val estado: StateFlow<EstadoImportacion> = _estado.asStateFlow()

    fun importar(context: Context, uri: Uri) = viewModelScope.launch {
        _estado.value = EstadoImportacion.Cargando

        val resultado = importacionRepository.importarCsv(context, uri)

        _estado.value = when (resultado) {
            is ResultadoImportacion.Exito -> EstadoImportacion.Exito(
                productosNuevos        = resultado.productosNuevos,
                movimientosRegistrados = resultado.movimientosRegistrados
            )
            is ResultadoImportacion.Error -> EstadoImportacion.Error(resultado.mensaje)
        }
    }

    fun resetearEstado() {
        _estado.value = EstadoImportacion.Inactivo
    }
}