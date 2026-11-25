package com.example.latienditamanager.util

import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.ProductoEntity
import com.example.latienditamanager.data.local.entity.TipoMovimiento

data class ResultadoMapeo(
    val productosNuevos: List<ProductoEntity>,      // no estaban en la BD
    val movimientos: List<MovimientoEntity>          // ventas del día
)

object HioposMapper {

    // productosExistentes: códigos/nombres que ya están en Room
    // fechaImportacion:    epoch millis del día que se importa
    fun mapear(
        filas: List<FilaCsv>,
        productosExistentes: Set<String>,
        fechaImportacion: Long = System.currentTimeMillis()
    ): ResultadoMapeo {

        val productosNuevos = mutableListOf<ProductoEntity>()
        val movimientos     = mutableListOf<MovimientoEntity>()

        filas.forEach { fila ->
            // La clave de identificación: código de barras si existe, nombre si no
            val clave = fila.codigoBarras ?: fila.nombre

            // Si el producto no existe en la BD, crearlo con stock 0
            // (el movimiento lo ajustará)
            if (clave !in productosExistentes) {
                productosNuevos.add(
                    ProductoEntity(
                        codigoBarras = clave,
                        nombre       = fila.nombre,
                        stockActual  = 0.0
                    )
                )
            }

            // Crear el movimiento de venta
            movimientos.add(
                MovimientoEntity(
                    codigoBarras  = clave,
                    tipo          = TipoMovimiento.VENTA,
                    cantidad      = fila.unidades.toDouble(),
                    fecha         = fechaImportacion,
                    notas         = "Importado desde Hiopos",
                    origenCsv     = true
                )
            )
        }

        return ResultadoMapeo(
            productosNuevos = productosNuevos,
            movimientos     = movimientos
        )
    }
}