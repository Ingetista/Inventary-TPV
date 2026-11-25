package com.example.latienditamanager.util

import android.content.Context
import android.net.Uri

// Representa una fila ya limpia del CSV — sin lógica de dominio todavía
data class FilaCsv(
    val codigoBarras: String?,   // null si el producto no tiene código
    val nombre: String,          // ya limpio, sin " / Único"
    val unidades: Int,
    val importe: Double
)

object CsvParser {

    // Entrada: URI del archivo seleccionado por el usuario
    // Salida:  lista de FilaCsv válidas, o error encapsulado
    fun parsear(context: Context, uri: Uri): Result<List<FilaCsv>> {
        return try {
            val lineas = leerLineas(context, uri)
            val filas  = procesarLineas(lineas)
            Result.success(filas)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun leerLineas(context: Context, uri: Uri): List<String> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalStateException("No se pudo abrir el archivo")

        return inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
            reader.readLines()
        }
    }

    private fun procesarLineas(lineas: List<String>): List<FilaCsv> {
        if (lineas.isEmpty()) return emptyList()

        return lineas
            .drop(1)                          // saltar encabezado
            .filter { it.isNotBlank() }       // ignorar líneas vacías
            .mapNotNull { parsearFila(it) }   // parsear cada fila
    }

    private fun parsearFila(linea: String): FilaCsv? {
        val columnas = linea.split("|")

        // El CSV tiene 6 columnas: Referencia|CodBarras|Nombre|Unidades|Importe|%Total
        if (columnas.size < 5) return null

        val nombre = limpiarNombre(columnas[2].trim())

        // Detectar y descartar la fila de totales — "Registros: N"
        if (nombre.startsWith("Registros:")) return null

        // Descartar filas sin nombre útil
        if (nombre.isBlank()) return null

        val codigoBarras = parsearCodigo(columnas[1].trim())
        val unidades     = columnas[3].trim().toIntOrNull() ?: return null
        val importe      = parsearImporte(columnas[4].trim()) ?: return null

        // Descartar filas con unidades 0 o negativas
        if (unidades <= 0) return null

        return FilaCsv(
            codigoBarras = codigoBarras,
            nombre       = nombre,
            unidades     = unidades,
            importe      = importe
        )
    }

    // "CERVEZA MAHOU 330 ml / Único" → "CERVEZA MAHOU 330 ml"
    private fun limpiarNombre(nombre: String): String {
        return nombre
            .removeSuffix("/ Único")
            .removeSuffix("/Único")
            .trim()
    }

    // "8437013112062" o "8.437013e+12" → "8437013112062"
    // null si la celda está vacía
    private fun parsearCodigo(raw: String): String? {
        if (raw.isBlank()) return null
        // Si viene en notación científica, convertir a Long y luego a String
        return if (raw.contains("e", ignoreCase = true) ||
            raw.contains("E")) {
            raw.toDoubleOrNull()?.toLong()?.toString()
        } else {
            raw.ifBlank { null }
        }
    }

    // "33,50" → 33.50 | "33.50" → 33.50
    private fun parsearImporte(raw: String): Double? {
        return raw
            .replace(",", ".")
            .replace("€", "")
            .trim()
            .toDoubleOrNull()
    }
}