package com.example.latienditamanager.data.repository

import android.content.Context
import android.net.Uri
import com.example.latienditamanager.data.local.dao.MovimientoDao
import com.example.latienditamanager.data.local.dao.ProductoDao
import com.example.latienditamanager.util.CsvParser
import com.example.latienditamanager.util.HioposMapper

sealed class ResultadoImportacion {
    data class Exito(
        val productosNuevos: Int,
        val movimientosRegistrados: Int
    ) : ResultadoImportacion()

    data class Error(val mensaje: String) : ResultadoImportacion()
}

class ImportacionRepository(
    private val productoDao: ProductoDao,
    private val movimientoDao: MovimientoDao
) {

    suspend fun importarCsv(
        context: Context,
        uri: Uri,
        fechaImportacion: Long = System.currentTimeMillis()
    ): ResultadoImportacion {

        // 1. Parsear el archivo
        val filas = CsvParser.parsear(context, uri).getOrElse {
            return ResultadoImportacion.Error(
                "No se pudo leer el archivo: ${it.message}"
            )
        }

        if (filas.isEmpty()) {
            return ResultadoImportacion.Error(
                "El archivo no contiene ventas válidas"
            )
        }

        // 2. Obtener productos ya existentes en la BD
        val existentes = productoDao.obtenerTodosUnaSola()
            .map { it.codigoBarras }
            .toSet()

        // 3. Mapear filas a entidades
        val resultado = HioposMapper.mapear(filas, existentes, fechaImportacion)

        // 4. Persistir en orden: primero productos nuevos, luego movimientos
        if (resultado.productosNuevos.isNotEmpty()) {
            productoDao.insertarTodos(resultado.productosNuevos)
        }

        resultado.movimientos.forEach { movimiento ->
            val producto = productoDao.obtenerPorCodigo(movimiento.codigoBarras)
                ?: return@forEach

            val nuevoStock = producto.stockActual - movimiento.cantidad
            productoDao.actualizar(producto.copy(stockActual = nuevoStock))
            movimientoDao.insertar(movimiento)
        }

        return ResultadoImportacion.Exito(
            productosNuevos        = resultado.productosNuevos.size,
            movimientosRegistrados = resultado.movimientos.size
        )
    }
}