package com.example.latienditamanager.ui.screens.proveedores

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import com.example.latienditamanager.ui.theme.RojoAlerta
import com.example.latienditamanager.viewmodel.ProductoViewModel
import com.example.latienditamanager.viewmodel.ProveedorViewModel

@Composable
fun DialogoProveedor(
    proveedor: ProveedorEntity,
    esNuevo: Boolean,
    proveedorViewModel: ProveedorViewModel,
    productoViewModel: ProductoViewModel,
    onGuardar: (ProveedorEntity) -> Unit,
    onEliminar: () -> Unit,
    onDismiss: () -> Unit
) {
    var nombre   by remember { mutableStateOf(proveedor.nombre) }
    var telefono by remember { mutableStateOf(proveedor.telefono) }
    var email    by remember { mutableStateOf(proveedor.email) }
    var notas    by remember { mutableStateOf(proveedor.notas) }

    val productosRelacion by proveedorViewModel
        .proveedorViewModel_productosDeProveedor(proveedor.id)
        .collectAsState(initial = emptyList())

    val todosLosProductos by productoViewModel.productos.collectAsState()

    var mostrarConfirmEliminar  by remember { mutableStateOf(false) }
    var mostrarVincularProducto by remember { mutableStateOf(false) }

    if (mostrarConfirmEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmEliminar = false },
            title = { Text("Eliminar proveedor") },
            text  = { Text("¿Eliminar \"${proveedor.nombre}\"? Se eliminarán también sus vínculos con productos.") },
            confirmButton = {
                TextButton(
                    onClick = { onEliminar() },
                    colors  = ButtonDefaults.textButtonColors(contentColor = RojoAlerta)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmEliminar = false }) {
                    Text("Cancelar")
                }
            }
        )
        return
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text       = if (esNuevo) "Nuevo proveedor" else "Editar proveedor",
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value         = nombre,
                    onValueChange = { nombre = it },
                    label         = { Text("Nombre *") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = telefono,
                    onValueChange = { telefono = it },
                    label         = { Text("Teléfono") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = email,
                    onValueChange = { email = it },
                    label         = { Text("Email") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
                OutlinedTextField(
                    value         = notas,
                    onValueChange = { notas = it },
                    label         = { Text("Notas") },
                    modifier      = Modifier.fillMaxWidth(),
                    minLines      = 2
                )

                // — Productos vinculados — solo en edición
                if (!esNuevo && productosRelacion.isNotEmpty()) {
                    HorizontalDivider()
                    Text(
                        text       = "Productos vinculados",
                        style      = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                    productosRelacion.forEach { relacion ->
                        val producto = todosLosProductos
                            .find { it.codigoBarras == relacion.codigoBarras }
                        FilaProductoVinculado(
                            nombre        = producto?.nombre ?: relacion.codigoBarras,
                            precioCompra  = relacion.precioCompra,
                            precioVenta   = relacion.precioVenta,
                            onDesvincular = {
                                proveedorViewModel.desvincularProducto(relacion)
                            }
                        )
                    }
                }

                // — Botón vincular producto — solo en edición
                if (!esNuevo) {
                    OutlinedButton(
                        onClick  = { mostrarVincularProducto = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Vincular producto")
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier              = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (!esNuevo) {
                    IconButton(onClick = { mostrarConfirmEliminar = true }) {
                        Icon(
                            imageVector        = Icons.Default.Delete,
                            contentDescription = "Eliminar",
                            tint               = RojoAlerta
                        )
                    }
                } else {
                    Spacer(Modifier.width(48.dp))
                }
                Row {
                    TextButton(onClick = onDismiss) { Text("Cancelar") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            if (nombre.isBlank()) return@Button
                            onGuardar(
                                proveedor.copy(
                                    nombre   = nombre.trim(),
                                    telefono = telefono.trim(),
                                    email    = email.trim(),
                                    notas    = notas.trim()
                                )
                            )
                        }
                    ) { Text("Guardar") }
                }
            }
        }
    )

    // — Diálogo vincular producto —
    if (mostrarVincularProducto) {
        DialogoVincularProducto(
            todosLosProductos = todosLosProductos.map { it.codigoBarras to it.nombre },
            onVincular        = { codigoBarras, precioCompra, precioVenta ->
                proveedorViewModel.vincularProducto(
                    ProductoProveedorEntity(
                        codigoBarras = codigoBarras,
                        proveedorId  = proveedor.id,
                        precioCompra = precioCompra,
                        precioVenta  = precioVenta,
                        esPrincipal  = productosRelacion.isEmpty()
                    )
                )
                mostrarVincularProducto = false
            },
            onDismiss = { mostrarVincularProducto = false }
        )
    }
}

// — Fila producto vinculado — sin etiqueta Principal
@Composable
private fun FilaProductoVinculado(
    nombre: String,
    precioCompra: Double,
    precioVenta: Double,
    onDesvincular: () -> Unit
) {
    Row(
        modifier          = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text       = nombre,
                style      = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text  = "Compra: €${"%.2f".format(precioCompra)}  ·  Venta: €${"%.2f".format(precioVenta)}",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = onDesvincular) {
            Icon(
                imageVector        = Icons.Default.Delete,
                contentDescription = "Desvincular",
                tint               = RojoAlerta,
                modifier           = Modifier.size(18.dp)
            )
        }
    }
}

// — Diálogo vincular producto con búsqueda —
@Composable
private fun DialogoVincularProducto(
    todosLosProductos: List<Pair<String, String>>,
    onVincular: (String, Double, Double) -> Unit,
    onDismiss: () -> Unit
) {
    var productoSeleccionado by remember {
        mutableStateOf(todosLosProductos.firstOrNull())
    }
    var precioCompra  by remember { mutableStateOf("") }
    var precioVenta   by remember { mutableStateOf("") }
    var textoBusqueda by remember { mutableStateOf("") }

    val productosFiltrados = remember(textoBusqueda) {
        if (textoBusqueda.isBlank()) todosLosProductos
        else todosLosProductos.filter { (_, nombre) ->
            nombre.contains(textoBusqueda, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Vincular producto", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {

                // — Búsqueda —
                OutlinedTextField(
                    value         = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    label         = { Text("Buscar producto") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )

                // — Lista de productos filtrada —
                Column(
                    modifier = Modifier
                        .heightIn(max = 180.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (productosFiltrados.isEmpty()) {
                        Text(
                            text     = "Sin resultados",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        productosFiltrados.forEach { (codigo, nombre) ->
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = productoSeleccionado?.first == codigo,
                                    onClick  = { productoSeleccionado = codigo to nombre }
                                )
                                Text(
                                    text     = nombre,
                                    style    = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }

                HorizontalDivider()

                OutlinedTextField(
                    value         = precioCompra,
                    onValueChange = { precioCompra = it },
                    label         = { Text("Precio compra (€)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
                OutlinedTextField(
                    value         = precioVenta,
                    onValueChange = { precioVenta = it },
                    label         = { Text("Precio venta (€)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(
                        keyboardType = androidx.compose.ui.text.input.KeyboardType.Decimal
                    )
                )
            }
        },
        confirmButton = {
            Row {
                TextButton(onClick = onDismiss) { Text("Cancelar") }
                Spacer(Modifier.width(8.dp))
                Button(
                    onClick = {
                        val codigo = productoSeleccionado?.first ?: return@Button
                        onVincular(
                            codigo,
                            precioCompra.toDoubleOrNull() ?: 0.0,
                            precioVenta.toDoubleOrNull()  ?: 0.0
                        )
                    }
                ) { Text("Vincular") }
            }
        }
    )
}