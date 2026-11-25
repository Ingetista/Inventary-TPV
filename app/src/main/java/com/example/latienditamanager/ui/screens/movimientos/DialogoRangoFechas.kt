package com.example.latienditamanager.ui.screens.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DialogoRangoFechas(
    desdeInicial: Long,
    hastaInicial: Long,
    onConfirmar: (Long, Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

    var textoDesde by remember { mutableStateOf(sdf.format(Date(desdeInicial))) }
    var textoHasta by remember { mutableStateOf(sdf.format(Date(hastaInicial))) }
    var errorDesde by remember { mutableStateOf(false) }
    var errorHasta by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Seleccionar periodo", fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Text(
                    text  = "Introduce las fechas en formato dd/MM/yyyy",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                OutlinedTextField(
                    value         = textoDesde,
                    onValueChange = {
                        textoDesde = it
                        errorDesde = false
                    },
                    label       = { Text("Desde") },
                    placeholder = { Text("01/10/2026") },
                    modifier    = Modifier.fillMaxWidth(),
                    singleLine  = true,
                    isError     = errorDesde,
                    supportingText = if (errorDesde) {
                        { Text("Formato inválido — usa dd/MM/yyyy") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                OutlinedTextField(
                    value         = textoHasta,
                    onValueChange = {
                        textoHasta = it
                        errorHasta = false
                    },
                    label       = { Text("Hasta") },
                    placeholder = { Text("31/10/2026") },
                    modifier    = Modifier.fillMaxWidth(),
                    singleLine  = true,
                    isError     = errorHasta,
                    supportingText = if (errorHasta) {
                        { Text("Formato inválido — usa dd/MM/yyyy") }
                    } else null,
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Number
                    )
                )

                // Accesos rápidos de periodo
                Text(
                    text  = "Accesos rápidos",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    AccesoRapido("Hoy") {
                        val hoy = Calendar.getInstance()
                        textoDesde = sdf.format(hoy.time)
                        textoHasta = sdf.format(hoy.time)
                    }
                    AccesoRapido("Este mes") {
                        val cal = Calendar.getInstance()
                        cal.set(Calendar.DAY_OF_MONTH, 1)
                        textoDesde = sdf.format(cal.time)
                        textoHasta = sdf.format(Calendar.getInstance().time)
                    }
                    AccesoRapido("30 días") {
                        val hasta = Calendar.getInstance()
                        val desde = Calendar.getInstance().apply {
                            add(Calendar.DAY_OF_MONTH, -30)
                        }
                        textoDesde = sdf.format(desde.time)
                        textoHasta = sdf.format(hasta.time)
                    }
                }
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        val fechaDesde = try {
                            sdf.parse(textoDesde)?.time
                        } catch (e: Exception) { null }

                        val fechaHasta = try {
                            // Hasta el final del día seleccionado
                            sdf.parse(textoHasta)?.let { it.time + 86_400_000L - 1 }
                        } catch (e: Exception) { null }

                        errorDesde = fechaDesde == null
                        errorHasta = fechaHasta == null

                        if (fechaDesde != null && fechaHasta != null) {
                            onConfirmar(fechaDesde, fechaHasta)
                        }
                    }
                ) { Text("Aplicar") }
            }
        }
    )
}

@Composable
private fun AccesoRapido(
    etiqueta: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = false,
        onClick  = onClick,
        label    = { Text(etiqueta, style = MaterialTheme.typography.labelSmall) }
    )
}