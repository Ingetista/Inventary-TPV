package com.example.latienditamanager.ui.screens.proveedores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import com.example.latienditamanager.ui.theme.RojoAlerta
import com.example.latienditamanager.viewmodel.ProductoViewModel
import com.example.latienditamanager.viewmodel.ProveedorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProveedoresScreen(
    proveedorViewModel: ProveedorViewModel,
    productoViewModel: ProductoViewModel,
    onNavigateBack: () -> Unit
) {
    val proveedores by proveedorViewModel.proveedores.collectAsState()

    var proveedorSeleccionado by remember { mutableStateOf<ProveedorEntity?>(null) }
    var mostrarDialogoNuevo   by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Proveedores",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    IconButton(onClick = { mostrarDialogoNuevo = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir proveedor")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor             = MaterialTheme.colorScheme.primary,
                    titleContentColor          = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor     = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        if (proveedores.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = null,
                        modifier           = Modifier.size(48.dp),
                        tint               = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text  = "No hay proveedores registrados.\nToca + para añadir uno.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(
                    items = proveedores,
                    key   = { it.id }
                ) { proveedor ->
                    CardProveedor(
                        proveedor           = proveedor,
                        proveedorViewModel  = proveedorViewModel,
                        onClick             = {
                            proveedorViewModel.seleccionarProveedor(proveedor)
                            proveedorSeleccionado = proveedor
                        }
                    )
                }
            }
        }
    }

    // — Diálogo detalle/edición de proveedor —
    proveedorSeleccionado?.let { proveedor ->
        DialogoProveedor(
            proveedor          = proveedor,
            esNuevo            = false,
            proveedorViewModel = proveedorViewModel,
            productoViewModel  = productoViewModel,
            onGuardar = { actualizado ->
                proveedorViewModel.actualizar(actualizado)
                proveedorSeleccionado = null
            },
            onEliminar = {
                proveedorViewModel.eliminar(proveedor)
                proveedorSeleccionado = null
            },
            onDismiss = { proveedorSeleccionado = null }
        )
    }

    // — Diálogo nuevo proveedor —
    if (mostrarDialogoNuevo) {
        DialogoProveedor(
            proveedor = ProveedorEntity(nombre = ""),
            esNuevo   = true,
            proveedorViewModel = proveedorViewModel,
            productoViewModel  = productoViewModel,
            onGuardar = { nuevo ->
                proveedorViewModel.insertar(nuevo)
                mostrarDialogoNuevo = false
            },
            onEliminar = {},
            onDismiss  = { mostrarDialogoNuevo = false }
        )
    }
}

// — Card de proveedor —
@Composable
private fun CardProveedor(
    proveedor: ProveedorEntity,
    proveedorViewModel: ProveedorViewModel,
    onClick: () -> Unit
) {
    val productosRelacion by proveedorViewModel
        .proveedorViewModel_productosDeProveedor(proveedor.id)
        .collectAsState(initial = emptyList())

    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        border = androidx.compose.foundation.BorderStroke(
            width = 0.5.dp,
            color = MaterialTheme.colorScheme.outline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Icono
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector        = Icons.Default.Person,
                        contentDescription = null,
                        tint               = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text       = proveedor.nombre,
                    style      = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold
                )
                val nProductos = productosRelacion.size
                Text(
                    text  = if (nProductos == 0) "Sin productos asociados"
                    else "$nProductos producto${if (nProductos == 1) "" else "s"} asociado${if (nProductos == 1) "" else "s"}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (proveedor.telefono.isNotBlank()) {
                    Text(
                        text  = proveedor.telefono,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Icon(
                imageVector        = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}