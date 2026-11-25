package com.example.latienditamanager.ui.screens.importacion

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.ui.theme.*
import com.example.latienditamanager.viewmodel.EstadoImportacion
import com.example.latienditamanager.viewmodel.ImportacionViewModel
import com.example.latienditamanager.viewmodel.MovimientoViewModel
import com.example.latienditamanager.viewmodel.ProductoViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportacionScreen(
    productoViewModel: ProductoViewModel,
    movimientoViewModel: MovimientoViewModel,
    importacionViewModel: ImportacionViewModel,
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val estado  by importacionViewModel.estado.collectAsState()

    // Launcher del selector de archivos del sistema
    val selectorArchivo = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            importacionViewModel.importar(context, it)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text("Importar CSV", fontWeight = FontWeight.Bold)
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(32.dp)
        ) {

            // — Instrucciones —
            Card(
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text       = "¿Cómo funciona?",
                        style      = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold
                    )
                    PasoInstruccion(
                        numero = "1",
                        texto  = "Abre la app Hiopos y exporta el reporte de ventas del día en formato CSV"
                    )
                    PasoInstruccion(
                        numero = "2",
                        texto  = "Guarda el archivo en el dispositivo"
                    )
                    PasoInstruccion(
                        numero = "3",
                        texto  = "Toca el botón de abajo, selecciona el archivo y la app actualizará el stock automáticamente"
                    )
                }
            }

            // — Botón selector de archivo —
            Button(
                onClick  = {
                    importacionViewModel.resetearEstado()
                    selectorArchivo.launch("text/*")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp),
                enabled  = estado !is EstadoImportacion.Cargando,
                colors   = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector        = Icons.Default.Add,
                    contentDescription = null,
                    modifier           = Modifier.size(22.dp)
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text  = "Seleccionar archivo CSV",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold
                )
            }

            // — Panel de estado —
            AnimatedVisibility(
                visible = estado !is EstadoImportacion.Inactivo,
                enter   = fadeIn() + slideInVertically(),
                exit    = fadeOut()
            ) {
                when (val s = estado) {
                    is EstadoImportacion.Cargando -> PanelCargando()
                    is EstadoImportacion.Exito    -> PanelExito(
                        productosNuevos        = s.productosNuevos,
                        movimientosRegistrados = s.movimientosRegistrados,
                        onImportarOtro         = {
                            importacionViewModel.resetearEstado()
                            selectorArchivo.launch("text/*")
                        }
                    )
                    is EstadoImportacion.Error -> PanelError(
                        mensaje    = s.mensaje,
                        onReintentar = {
                            importacionViewModel.resetearEstado()
                            selectorArchivo.launch("text/*")
                        }
                    )
                    else -> {}
                }
            }
        }
    }
}

// — Panel: procesando —
@Composable
private fun PanelCargando() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = AzulPrincipal.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(28.dp),
                color    = AzulPrincipal,
                strokeWidth = 3.dp
            )
            Column {
                Text(
                    text       = "Procesando archivo...",
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color      = AzulPrincipal
                )
                Text(
                    text  = "Esto solo tardará un momento",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

// — Panel: importación exitosa —
@Composable
private fun PanelExito(
    productosNuevos: Int,
    movimientosRegistrados: Int,
    onImportarOtro: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = VerdeStock.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = VerdeStock.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment      = Alignment.CenterVertically,
                horizontalArrangement  = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint               = VerdeStock,
                    modifier           = Modifier.size(28.dp)
                )
                Text(
                    text       = "Importación completada",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = VerdeStock
                )
            }

            HorizontalDivider(color = VerdeStock.copy(alpha = 0.2f))

            FilaResultado(
                icono    = Icons.Default.List,
                texto    = "$movimientosRegistrados ventas procesadas",
                color    = VerdeStock
            )

            if (productosNuevos > 0) {
                FilaResultado(
                    icono  = Icons.Default.Add,
                    texto  = "$productosNuevos productos nuevos añadidos al inventario",
                    color  = AzulPrincipal
                )
            } else {
                FilaResultado(
                    icono  = Icons.Default.CheckCircle,
                    texto  = "Todos los productos ya estaban registrados",
                    color  = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(4.dp))

            OutlinedButton(
                onClick  = onImportarOtro,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Importar otro archivo")
            }
        }
    }
}

// — Panel: error —
@Composable
private fun PanelError(
    mensaje: String,
    onReintentar: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = RojoAlerta.copy(alpha = 0.08f)
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 1.dp,
            color = RojoAlerta.copy(alpha = 0.4f)
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                verticalAlignment     = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector        = Icons.Default.Warning,
                    contentDescription = null,
                    tint               = RojoAlerta,
                    modifier           = Modifier.size(28.dp)
                )
                Text(
                    text       = "Error en la importación",
                    style      = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color      = RojoAlerta
                )
            }

            Text(
                text  = mensaje,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            OutlinedButton(
                onClick  = onReintentar,
                modifier = Modifier.fillMaxWidth(),
                colors   = ButtonDefaults.outlinedButtonColors(
                    contentColor = RojoAlerta
                )
            ) {
                Text("Reintentar")
            }
        }
    }
}

// — Componentes auxiliares —
@Composable
private fun PasoInstruccion(numero: String, texto: String) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment     = Alignment.Top
    ) {
        Surface(
            shape = MaterialTheme.shapes.small,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(20.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text  = numero,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Text(
            text  = texto,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun FilaResultado(
    icono: androidx.compose.ui.graphics.vector.ImageVector,
    texto: String,
    color: androidx.compose.ui.graphics.Color
) {
    Row(
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector        = icono,
            contentDescription = null,
            tint               = color,
            modifier           = Modifier.size(18.dp)
        )
        Text(
            text  = texto,
            style = MaterialTheme.typography.bodyMedium,
            color = color
        )
    }
}