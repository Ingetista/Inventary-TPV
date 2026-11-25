package com.example.latienditamanager.ui.screens.productos

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.data.local.entity.ProductoEntity
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import com.example.latienditamanager.ui.theme.RojoAlerta
import com.example.latienditamanager.ui.theme.VerdeStock
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun DialogoProducto(
    producto: ProductoEntity,
    esNuevo: Boolean,
    proveedores: List<ProveedorEntity>,
    proveedorActual: ProveedorEntity?,
    onGuardar: (ProductoEntity) -> Unit,
    onRegistrarEntrada: (codigoBarras: String, cantidad: Double) -> Unit,
    onAsignarProveedor: (ProductoProveedorEntity) -> Unit,
    onEliminar: () -> Unit,
    onDismiss: () -> Unit
) {
    var nombre       by remember { mutableStateOf(producto.nombre) }
    var codigo       by remember { mutableStateOf(producto.codigoBarras) }
    var stockActual  by remember { mutableStateOf(producto.stockActual.toString()) }
    var stockMinimo  by remember { mutableStateOf(producto.stockMinimo.toString()) }
    var unidad       by remember { mutableStateOf(producto.unidadMedida) }
    var fechaCad     by remember { mutableStateOf(
        producto.fechaCaducidad?.let {
            SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(it))
        } ?: ""
    )}

    var mostrarConfirmEliminar   by remember { mutableStateOf(false) }
    var mostrarDialogoEntrada    by remember { mutableStateOf(false) }
    var mostrarSelectorProveedor by remember { mutableStateOf(false) }
    var impuesto by remember { mutableStateOf(
        if (producto.impuesto == 0.0) "" else producto.impuesto.toString()
    ) }

    // — Confirmación eliminar —
    if (mostrarConfirmEliminar) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmEliminar = false },
            title = { Text("Eliminar producto") },
            text  = { Text("¿Eliminar \"${producto.nombre}\"? Esta acción no se puede deshacer.") },
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

// — Diálogo entrada de mercancía —
    if (mostrarDialogoEntrada) {
        var cantidadEntrada by remember { mutableStateOf("") }

        AlertDialog(
            onDismissRequest = { mostrarDialogoEntrada = false },
            title = { Text("Entrada de mercancía", fontWeight = FontWeight.Bold) },
            text  = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text  = producto.nombre,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    OutlinedTextField(
                        value         = cantidadEntrada,
                        onValueChange = { cantidadEntrada = it },
                        label         = { Text("Unidades que entran") },
                        modifier      = Modifier.fillMaxWidth(),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Decimal
                        )
                    )
                }
            },
            confirmButton = {
                Row {
                    TextButton(onClick = { mostrarDialogoEntrada = false }) {
                        Text("Cancelar")
                    }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val valor = cantidadEntrada.toDoubleOrNull()
                            if (valor != null && valor > 0) {
                                onRegistrarEntrada(producto.codigoBarras, valor)
                                mostrarDialogoEntrada = false
                            }
                        }
                    ) { Text("Confirmar") }
                }
            }
        )
        return
    }

    // — Diálogo principal —
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text       = if (esNuevo) "Nuevo producto" else "Editar producto",
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
                    value         = codigo,
                    onValueChange = { codigo = it },
                    label         = { Text("Código de barras") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value         = stockActual,
                        onValueChange = { stockActual = it },
                        label         = { Text("Stock actual") },
                        modifier      = Modifier.weight(1f),
                        singleLine    = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                    OutlinedTextField(
                        value         = unidad,
                        onValueChange = { unidad = it },
                        label         = { Text("Unidad") },
                        modifier      = Modifier.weight(0.6f),
                        singleLine    = true,
                        placeholder   = { Text("ud") }
                    )
                }
                OutlinedTextField(
                    value         = stockMinimo,
                    onValueChange = { stockMinimo = it },
                    label         = { Text("Stock mínimo (alerta)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )
                OutlinedTextField(
                    value         = fechaCad,
                    onValueChange = { fechaCad = it },
                    label         = { Text("Fecha caducidad (dd/MM/yyyy)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    placeholder   = { Text("Dejar vacío si no caduca") }
                )

                OutlinedTextField(
                    value         = impuesto,
                    onValueChange = { impuesto = it },
                    label         = { Text("Impuesto (%)") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true,
                    placeholder   = { Text("Ej: 21") },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Decimal
                    )
                )

                // — Proveedor y entrada — solo en edición
                if (!esNuevo) {
                    HorizontalDivider()
                    Row(
                        modifier              = Modifier.fillMaxWidth(),
                        verticalAlignment     = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text       = "Proveedor",
                                style      = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text  = proveedorActual?.nombre ?: "Sin asignar",
                                style = MaterialTheme.typography.bodyMedium,
                                color = if (proveedorActual != null)
                                    MaterialTheme.colorScheme.onSurface
                                else
                                    MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        TextButton(onClick = { mostrarSelectorProveedor = true }) {
                            Text(if (proveedorActual != null) "Cambiar" else "Asignar")
                        }
                    }

                    HorizontalDivider()

                    OutlinedButton(
                        onClick  = { mostrarDialogoEntrada = true },
                        modifier = Modifier.fillMaxWidth(),
                        colors   = ButtonDefaults.outlinedButtonColors(
                            contentColor = VerdeStock
                        ),
                        border = androidx.compose.foundation.BorderStroke(
                            width = 1.dp,
                            color = VerdeStock
                        )
                    ) {
                        Text(
                            text       = "+ Registrar entrada de mercancía",
                            fontWeight = FontWeight.SemiBold
                        )
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
                            val codigoFinal = codigo.ifBlank { nombre }
                            onGuardar(
                                producto.copy(
                                    codigoBarras   = codigoFinal,
                                    nombre         = nombre.trim(),
                                    stockActual    = stockActual.toDoubleOrNull() ?: 0.0,
                                    stockMinimo    = stockMinimo.toDoubleOrNull() ?: 0.0,
                                    unidadMedida   = unidad.ifBlank { "ud" },
                                    fechaCaducidad = parsearFechaDialogo(fechaCad),
                                    impuesto       = impuesto.toDoubleOrNull() ?: 0.0
                                )
                            )
                        }
                    ) { Text("Guardar") }
                }
            }
        }
    )

    // — Selector de proveedor —
    if (mostrarSelectorProveedor) {
        DialogoSelectorProveedor(
            proveedores     = proveedores,
            proveedorActual = proveedorActual,
            onSeleccionar   = { proveedor ->
                onAsignarProveedor(
                    ProductoProveedorEntity(
                        codigoBarras = producto.codigoBarras,
                        proveedorId  = proveedor.id,
                        precioCompra = 0.0,
                        precioVenta  = 0.0,
                        esPrincipal  = true
                    )
                )
                mostrarSelectorProveedor = false
            },
            onDismiss = { mostrarSelectorProveedor = false }
        )
    }
}

// — Selector de proveedor con búsqueda —
@Composable
private fun DialogoSelectorProveedor(
    proveedores: List<ProveedorEntity>,
    proveedorActual: ProveedorEntity?,
    onSeleccionar: (ProveedorEntity) -> Unit,
    onDismiss: () -> Unit
) {
    var textoBusqueda by remember { mutableStateOf("") }

    val proveedoresFiltrados = remember(textoBusqueda) {
        if (textoBusqueda.isBlank()) proveedores
        else proveedores.filter {
            it.nombre.contains(textoBusqueda, ignoreCase = true)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Seleccionar proveedor", fontWeight = FontWeight.Bold) },
        text  = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value         = textoBusqueda,
                    onValueChange = { textoBusqueda = it },
                    label         = { Text("Buscar proveedor") },
                    modifier      = Modifier.fillMaxWidth(),
                    singleLine    = true
                )
                Column(
                    modifier = Modifier
                        .heightIn(max = 240.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    if (proveedoresFiltrados.isEmpty()) {
                        Text(
                            text     = "Sin resultados",
                            style    = MaterialTheme.typography.bodySmall,
                            color    = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        proveedoresFiltrados.forEach { proveedor ->
                            Row(
                                modifier          = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = proveedorActual?.id == proveedor.id,
                                    onClick  = { onSeleccionar(proveedor) }
                                )
                                Text(
                                    text     = proveedor.nombre,
                                    style    = MaterialTheme.typography.bodyMedium,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) { Text("Cancelar") }
        }
    )
}

private fun parsearFechaDialogo(texto: String): Long? {
    if (texto.isBlank()) return null
    return try {
        SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(texto)?.time
    } catch (e: Exception) { null }
}