package com.example.latienditamanager.ui.screens.dashboard

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.navigation.Rutas
import com.example.latienditamanager.ui.theme.*
import com.example.latienditamanager.viewmodel.MovimientoViewModel
import com.example.latienditamanager.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    productoViewModel: ProductoViewModel,
    movimientoViewModel: MovimientoViewModel,
    onNavigate: (String) -> Unit
) {
    val stockBajo by productoViewModel.stockBajo.collectAsState()
    val proximosACaducar by productoViewModel.proximosACaducar.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = "La Tienditamanager",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Panel de inventario",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {

            // — Sección de alertas —
            Text(
                text = "Estado del inventario",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                TarjetaAlerta(
                    modifier    = Modifier.weight(1f),
                    icono       = Icons.Default.Warning,
                    cantidad    = stockBajo.size,
                    etiqueta    = "Stock bajo",
                    colorFondo  = RojoAlerta.copy(alpha = 0.1f),
                    colorIcono  = RojoAlerta,
                    colorTexto  = RojoAlerta
                )
                TarjetaAlerta(
                    modifier    = Modifier.weight(1f),
                    icono       = Icons.Default.DateRange,
                    cantidad    = proximosACaducar.size,
                    etiqueta    = "Próx. caducidad",
                    colorFondo  = AmarilloAviso.copy(alpha = 0.1f),
                    colorIcono  = AmarilloAviso,
                    colorTexto  = AmarilloAviso
                )
            }

            HorizontalDivider(color = MaterialTheme.colorScheme.outline)

            // — Sección de navegación —
            Text(
                text = "Acceso rápido",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotonNavegacion(
                        modifier  = Modifier.weight(1f),
                        icono     = Icons.Default.List,
                        etiqueta  = "Productos",
                        onClick   = { onNavigate(Rutas.PRODUCTOS) }
                    )
                    BotonNavegacion(
                        modifier  = Modifier.weight(1f),
                        icono     = Icons.Default.Person,
                        etiqueta  = "Proveedores",
                        onClick   = { onNavigate(Rutas.PROVEEDORES) }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    BotonNavegacion(
                        modifier  = Modifier.weight(1f),
                        icono     = Icons.Default.Menu,
                        etiqueta  = "Movimientos",
                        onClick   = { onNavigate(Rutas.MOVIMIENTOS) }
                    )
                    BotonNavegacion(
                        modifier  = Modifier.weight(1f),
                        icono     = Icons.Default.Add,
                        etiqueta  = "Importar CSV",
                        esAccion  = true,
                        onClick   = { onNavigate(Rutas.IMPORTACION) }
                    )
                }
            }
        }
    }
}

// — Componentes internos —

@Composable
private fun TarjetaAlerta(
    modifier: Modifier = Modifier,
    icono: ImageVector,
    cantidad: Int,
    etiqueta: String,
    colorFondo: androidx.compose.ui.graphics.Color,
    colorIcono: androidx.compose.ui.graphics.Color,
    colorTexto: androidx.compose.ui.graphics.Color
) {
    Card(
        modifier = modifier,
        colors   = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = icono,
                contentDescription = etiqueta,
                tint = colorIcono,
                modifier = Modifier.size(28.dp)
            )
            Text(
                text  = cantidad.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = colorTexto
            )
            Text(
                text  = etiqueta,
                style = MaterialTheme.typography.labelMedium,
                color = colorTexto
            )
        }
    }
}

@Composable
private fun BotonNavegacion(
    modifier: Modifier = Modifier,
    icono: ImageVector,
    etiqueta: String,
    esAccion: Boolean = false,
    onClick: () -> Unit
) {
    val colorFondo = if (esAccion)
        MaterialTheme.colorScheme.secondary
    else
        MaterialTheme.colorScheme.primaryContainer

    val colorContenido = if (esAccion)
        MaterialTheme.colorScheme.onSecondary
    else
        MaterialTheme.colorScheme.onPrimaryContainer

    Card(
        modifier = modifier,
        onClick  = onClick,
        colors   = CardDefaults.cardColors(containerColor = colorFondo)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = icono,
                contentDescription = etiqueta,
                tint               = colorContenido,
                modifier           = Modifier.size(32.dp)
            )
            Text(
                text  = etiqueta,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                color = colorContenido
            )
        }
    }
}