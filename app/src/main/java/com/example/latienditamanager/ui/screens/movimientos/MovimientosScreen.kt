package com.example.latienditamanager.ui.screens.movimientos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.data.local.entity.MovimientoEntity
import com.example.latienditamanager.data.local.entity.TipoMovimiento
import com.example.latienditamanager.ui.theme.*
import com.example.latienditamanager.viewmodel.MovimientoViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovimientosScreen(
    movimientoViewModel: MovimientoViewModel,
    onNavigateBack: () -> Unit
) {
    val movimientos  by movimientoViewModel.movimientos.collectAsState()
    val masVendidos  by movimientoViewModel.masVendidos.collectAsState()
    val desde        by movimientoViewModel.desde.collectAsState()
    val hasta        by movimientoViewModel.hasta.collectAsState()

    var mostrarDialogoFechas by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Movimientos", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {

            // — Selector de rango de fechas —
            item {
                Card(
                    onClick  = { mostrarDialogoFechas = true },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment      = Alignment.CenterVertically,
                            horizontalArrangement  = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector        = Icons.Default.DateRange,
                                contentDescription = null,
                                tint               = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Column {
                                Text(
                                    text  = "Periodo seleccionado",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                        .copy(alpha = 0.7f)
                                )
                                Text(
                                    text  = "${formatearFecha(desde)}  →  ${formatearFecha(hasta)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                )
                            }
                        }
                        Icon(
                            imageVector        = Icons.Default.Edit,
                            contentDescription = "Cambiar fechas",
                            tint               = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }

            // — Ranking más vendidos —
            if (masVendidos.isNotEmpty()) {
                item {
                    Text(
                        text       = "Más vendidos en el periodo",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier   = Modifier.padding(
                            horizontal = 16.dp,
                            vertical   = 4.dp
                        )
                    )
                }
                item {
                    LazyRow(
                        contentPadding        = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        itemsIndexed(masVendidos.take(10)) { index, resumen ->
                            CardMasVendido(
                                posicion      = index + 1,
                                codigoBarras  = resumen.codigoBarras,
                                totalVendido  = resumen.totalVendido
                            )
                        }
                    }
                    Spacer(Modifier.height(12.dp))
                }
            }

            // — Historial de movimientos —
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment     = Alignment.CenterVertically
                ) {
                    Text(
                        text       = "Historial",
                        style      = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text  = "${movimientos.size} registros",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (movimientos.isEmpty()) {
                item {
                    Box(
                        modifier        = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text  = "No hay movimientos en este periodo.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else {
                items(
                    items = movimientos,
                    key   = { it.id }
                ) { movimiento ->
                    FilaMovimiento(
                        movimiento = movimiento,
                        modifier   = Modifier.padding(
                            horizontal = 16.dp,
                            vertical   = 4.dp
                        )
                    )
                }
            }
        }
    }

    // — Diálogo selector de fechas —
    if (mostrarDialogoFechas) {
        DialogoRangoFechas(
            desdeInicial = desde,
            hastaInicial = hasta,
            onConfirmar  = { nuevoDesde, nuevoHasta ->
                movimientoViewModel.cambiarRango(nuevoDesde, nuevoHasta)
                mostrarDialogoFechas = false
            },
            onDismiss = { mostrarDialogoFechas = false }
        )
    }
}

// — Card ranking más vendidos —
@Composable
private fun CardMasVendido(
    posicion: Int,
    codigoBarras: String,
    totalVendido: Double
) {
    Card(
        modifier = Modifier.width(130.dp),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text       = "#$posicion",
                style      = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold,
                color      = MaterialTheme.colorScheme.primary
            )
            Text(
                text       = codigoBarras,
                style      = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium,
                maxLines   = 2
            )
            Text(
                text  = "${formatearDecimal(totalVendido)} ud vendidas",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// — Fila de movimiento individual —
@Composable
private fun FilaMovimiento(
    movimiento: MovimientoEntity,
    modifier: Modifier = Modifier
) {
    val esEntrada = movimiento.tipo == TipoMovimiento.ENTRADA ||
            movimiento.tipo == TipoMovimiento.AJUSTE_MANUAL

    val colorTipo = when (movimiento.tipo) {
        TipoMovimiento.VENTA         -> RojoAlerta
        TipoMovimiento.ENTRADA       -> VerdeStock
        TipoMovimiento.AJUSTE_MANUAL -> AzulPrincipal
        TipoMovimiento.CADUCIDAD     -> AmarilloAviso
    }

    val iconoTipo = when (movimiento.tipo) {
        TipoMovimiento.VENTA         -> Icons.Default.KeyboardArrowDown
        TipoMovimiento.ENTRADA       -> Icons.Default.KeyboardArrowUp
        TipoMovimiento.AJUSTE_MANUAL -> Icons.Default.Edit
        TipoMovimiento.CADUCIDAD     -> Icons.Default.Warning
    }

    val etiquetaTipo = when (movimiento.tipo) {
        TipoMovimiento.VENTA         -> "Venta"
        TipoMovimiento.ENTRADA       -> "Entrada"
        TipoMovimiento.AJUSTE_MANUAL -> "Ajuste"
        TipoMovimiento.CADUCIDAD     -> "Caducidad"
    }

    HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Icono tipo
        Surface(
            shape = MaterialTheme.shapes.small,
            color = colorTipo.copy(alpha = 0.12f),
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector        = iconoTipo,
                    contentDescription = etiquetaTipo,
                    tint               = colorTipo,
                    modifier           = Modifier.size(20.dp)
                )
            }
        }

        // Datos del movimiento
        Column(modifier = Modifier.weight(1f)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment     = Alignment.CenterVertically
            ) {
                Text(
                    text       = etiquetaTipo,
                    style      = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color      = colorTipo
                )
                if (movimiento.origenCsv) {
                    Surface(
                        shape = MaterialTheme.shapes.extraSmall,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text     = "CSV",
                            style    = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                            color    = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }
            Text(
                text  = movimiento.codigoBarras,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (movimiento.notas.isNotBlank()) {
                Text(
                    text  = movimiento.notas,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // Cantidad y fecha
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text       = "${if (esEntrada) "+" else "-"}${formatearDecimal(movimiento.cantidad)}",
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color      = colorTipo
            )
            Text(
                text  = formatearFecha(movimiento.fecha),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

// — Helpers —
private fun formatearFecha(epoch: Long): String =
    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(epoch))

private fun formatearDecimal(valor: Double): String =
    if (valor == valor.toLong().toDouble())
        valor.toLong().toString()
    else String.format("%.2f", valor)