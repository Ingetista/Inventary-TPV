package com.example.latienditamanager.ui.screens.productos

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
import com.example.latienditamanager.data.local.entity.ProductoEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import com.example.latienditamanager.ui.theme.*
import com.example.latienditamanager.viewmodel.MovimientoViewModel
import com.example.latienditamanager.viewmodel.ProductoViewModel
import com.example.latienditamanager.viewmodel.ProveedorViewModel
import java.text.SimpleDateFormat
import java.util.*
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.ViewList

private enum class VistaProductos { LISTA, REJILLA }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductosScreen(
    productoViewModel: ProductoViewModel,
    proveedorViewModel: ProveedorViewModel,
    movimientoViewModel: MovimientoViewModel,
    onNavigateBack: () -> Unit
) {
    val productos     by productoViewModel.productos.collectAsState()
    val textoBusqueda by productoViewModel.textoBusqueda.collectAsState()
    val proveedores   by proveedorViewModel.proveedores.collectAsState()

    // — LOG DE DIAGNÓSTICO UI —
    android.util.Log.d("UI_SCREEN", ">>> ProductosScreen recibió collect. Total: ${productos.size}. Stock Adlerbrau: ${productos.find { it.codigoBarras.contains("ADLER") }?.stockActual}")

    var productoEditando    by remember { mutableStateOf<ProductoEntity?>(null) }
    var mostrarDialogoNuevo by remember { mutableStateOf(false) }
    var vistaActual by remember { mutableStateOf(VistaProductos.LISTA) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "Productos", fontWeight = FontWeight.Bold)
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Volver")
                    }
                },
                actions = {
                    // Toggle de vista
                    IconButton(onClick = {
                        vistaActual = if (vistaActual == VistaProductos.LISTA)
                            VistaProductos.REJILLA else VistaProductos.LISTA
                    }) {
                        Icon(
                            imageVector = if (vistaActual == VistaProductos.LISTA)
                                Icons.Default.GridView else Icons.Default.ViewList,
                            contentDescription = "Cambiar vista",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    IconButton(onClick = { mostrarDialogoNuevo = true }) {
                        Icon(Icons.Default.Add, contentDescription = "Añadir producto")
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // — Barra de búsqueda —
            OutlinedTextField(
                value         = textoBusqueda,
                onValueChange = { productoViewModel.onTextoBusquedaCambia(it) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                placeholder  = { Text("Buscar producto...") },
                leadingIcon  = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (textoBusqueda.isNotBlank()) {
                        IconButton(onClick = { productoViewModel.onTextoBusquedaCambia("") }) {
                            Icon(Icons.Default.Close, contentDescription = "Limpiar")
                        }
                    }
                },
                singleLine = true,
                shape      = MaterialTheme.shapes.medium
            )

            // — Lista de productos —
            if (productos.isEmpty()) {
                EstadoVacio(
                    mensaje = if (textoBusqueda.isBlank())
                        "No hay productos registrados.\nImporta un CSV o añade uno manualmente."
                    else
                        "No se encontró \"$textoBusqueda\"."
                )
            } else {
                when (vistaActual) {
                    VistaProductos.LISTA -> {
                        LazyColumn(
                            contentPadding      = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            items(items = productos, key = { it.codigoBarras }) { producto ->
                                CardProducto(
                                    producto = producto,
                                    onClick  = { productoEditando = producto }
                                )
                            }
                        }
                    }
                    VistaProductos.REJILLA -> {
                        // Recopilar relaciones de proveedor para todos los productos
                        val relacionesMap = productos.associate { producto ->
                            val relaciones by proveedorViewModel
                                .obtenerProveedorPrincipalDeProducto(producto.codigoBarras)
                                .collectAsState(initial = emptyList())
                            producto.codigoBarras to (relaciones.find { it.esPrincipal }
                                ?: relaciones.firstOrNull())
                        }

                        VistaRejilla(
                            productos           = productos,
                            proveedores         = proveedores,
                            relacionesProveedor = relacionesMap,
                            onProductoClick     = { productoEditando = it }
                        )
                    }
                }
            }
        }
    }

    // — Diálogo editar producto existente —
    productoEditando?.let { producto ->

        val proveedorDelProducto = remember { mutableStateOf<ProveedorEntity?>(null) }

        val relacionesDelProducto by proveedorViewModel
            .obtenerProveedorPrincipalDeProducto(producto.codigoBarras)
            .collectAsState(initial = emptyList())

        LaunchedEffect(relacionesDelProducto, proveedores) {
            val proveedorId = relacionesDelProducto
                .find { it.esPrincipal }?.proveedorId
                ?: relacionesDelProducto.firstOrNull()?.proveedorId
            proveedorDelProducto.value = proveedores.find { it.id == proveedorId }
        }

        DialogoProducto(
            producto           = producto,
            esNuevo            = false,
            proveedores        = proveedores,
            proveedorActual    = proveedorDelProducto.value,
            onGuardar          = { actualizado ->
                productoViewModel.actualizar(actualizado)
                productoEditando = null
            },
            onRegistrarEntrada = { codigo, cantidad ->
                productoViewModel.registrarEntrada(codigo, cantidad)
                productoEditando = null
            },
            onAsignarProveedor = { relacion ->
                proveedorViewModel.vincularProducto(relacion)
            },
            onEliminar         = {
                productoViewModel.eliminar(producto)
                productoEditando = null
            },
            onDismiss          = { productoEditando = null }
        )
    }

    // — Diálogo nuevo producto —
    if (mostrarDialogoNuevo) {
        DialogoProducto(
            producto           = ProductoEntity(codigoBarras = "", nombre = ""),
            esNuevo            = true,
            proveedores        = proveedores,
            proveedorActual    = null,
            onGuardar          = { nuevo ->
                productoViewModel.insertar(nuevo)
                mostrarDialogoNuevo = false
            },
            onRegistrarEntrada = { _, _ -> },
            onAsignarProveedor = {},
            onEliminar         = {},
            onDismiss          = { mostrarDialogoNuevo = false }
        )
    }
}

// — Card de producto —
@Composable
private fun CardProducto(
    producto: ProductoEntity,
    onClick: () -> Unit
) {
    val stockBajo        = producto.stockActual <= producto.stockMinimo
    val proximaCaducidad = producto.fechaCaducidad?.let { estaProximaACaducar(it) } == true
    val vencida          = producto.fechaCaducidad?.let { estaVencida(it) } == true

    val colorFondoCard = when {
        stockBajo || vencida -> RojoAlerta.copy(alpha = 0.05f)
        proximaCaducidad     -> AmarilloAviso.copy(alpha = 0.08f)
        else                 -> MaterialTheme.colorScheme.surface
    }

    val colorBorde = when {
        stockBajo || vencida -> RojoAlerta
        proximaCaducidad     -> AmarilloAviso
        else                 -> MaterialTheme.colorScheme.outline
    }

    Card(
        onClick  = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors   = CardDefaults.cardColors(containerColor = colorFondoCard),
        border   = androidx.compose.foundation.BorderStroke(
            width = if (stockBajo || proximaCaducidad || vencida) 1.5.dp else 0.5.dp,
            color = colorBorde
        )
    ) {
        Row(
            modifier          = Modifier.fillMaxWidth().padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    verticalAlignment      = Alignment.CenterVertically,
                    horizontalArrangement  = Arrangement.spacedBy(6.dp)
                ) {
                    if (stockBajo) {
                        Icon(
                            imageVector        = Icons.Default.Warning,
                            contentDescription = "Stock bajo",
                            tint               = RojoAlerta,
                            modifier           = Modifier.size(16.dp)
                        )
                    }
                    Text(
                        text       = producto.nombre,
                        style      = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color      = if (stockBajo) RojoAlerta
                        else MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    EtiquetaValor(
                        etiqueta   = "Stock",
                        valor      = "${formatearDecimal(producto.stockActual)} ${producto.unidadMedida}",
                        colorValor = if (stockBajo) RojoAlerta else VerdeStock
                    )
                    if (producto.stockMinimo > 0) {
                        EtiquetaValor(
                            etiqueta   = "Mín.",
                            valor      = "${formatearDecimal(producto.stockMinimo)} ${producto.unidadMedida}",
                            colorValor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    producto.fechaCaducidad?.let { fecha ->
                        EtiquetaValor(
                            etiqueta   = "Caduca",
                            valor      = formatearFecha(fecha),
                            colorValor = colorCaducidad(fecha)
                        )
                    }
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

// — Estado vacío —
@Composable
private fun EstadoVacio(mensaje: String) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector        = Icons.Default.List,
                contentDescription = null,
                modifier           = Modifier.size(48.dp),
                tint               = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text      = mensaje,
                style     = MaterialTheme.typography.bodyMedium,
                color     = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

// — Etiqueta + valor inline —
@Composable
private fun EtiquetaValor(
    etiqueta: String,
    valor: String,
    colorValor: androidx.compose.ui.graphics.Color
) {
    Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(
            text  = "$etiqueta:",
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text       = valor,
            style      = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            color      = colorValor
        )
    }
}

// — Helpers —
private fun formatearDecimal(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString()
    else String.format("%.2f", valor)

private fun formatearFecha(epoch: Long): String =
    SimpleDateFormat("dd/MM/yy", Locale.getDefault()).format(Date(epoch))

private fun estaProximaACaducar(epoch: Long): Boolean {
    val diasRestantes = (epoch - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
    return diasRestantes in 0..30
}

private fun estaVencida(epoch: Long): Boolean = epoch < System.currentTimeMillis()

private fun colorCaducidad(epoch: Long): androidx.compose.ui.graphics.Color {
    val diasRestantes = (epoch - System.currentTimeMillis()) / (1000 * 60 * 60 * 24)
    return when {
        diasRestantes < 0   -> RojoAlerta
        diasRestantes <= 7  -> RojoAlerta
        diasRestantes <= 30 -> AmarilloAviso
        else                -> VerdeStock
    }
}

