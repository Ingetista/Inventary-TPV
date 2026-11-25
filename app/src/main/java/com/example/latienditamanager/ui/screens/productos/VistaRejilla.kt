package com.example.latienditamanager.ui.screens.productos

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.latienditamanager.data.local.entity.ProductoEntity
import com.example.latienditamanager.data.local.entity.ProveedorEntity
import com.example.latienditamanager.data.local.entity.ProductoProveedorEntity
import com.example.latienditamanager.ui.theme.*

// Anchos de cada columna
private val anchoProducto    = 180.dp
private val anchoStock       = 90.dp
private val anchoProveedor   = 130.dp
private val anchoCompra      = 90.dp
private val anchoVenta       = 90.dp
private val anchoImpuesto    = 80.dp

@Composable
fun VistaRejilla(
    productos: List<ProductoEntity>,
    proveedores: List<ProveedorEntity>,
    relacionesProveedor: Map<String, ProductoProveedorEntity?>,
    onProductoClick: (ProductoEntity) -> Unit
) {
    val scrollHorizontal = rememberScrollState()
    val colorBorde = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val colorCabecera = MaterialTheme.colorScheme.primaryContainer

    Column(
        modifier = Modifier
            .fillMaxSize()
            .horizontalScroll(scrollHorizontal)
    ) {
        // — Cabecera fija —
        FilaCabecera(colorCabecera, colorBorde)

        // — Filas de datos —
        LazyColumn {
            items(
                items = productos,
                key   = { it.codigoBarras }
            ) { producto ->
                val relacion = relacionesProveedor[producto.codigoBarras]
                val proveedor = relacion?.let { r ->
                    proveedores.find { it.id == r.proveedorId }
                }

                FilaProducto(
                    producto   = producto,
                    proveedor  = proveedor,
                    relacion   = relacion,
                    colorBorde = colorBorde,
                    onClick    = { onProductoClick(producto) }
                )
            }
        }
    }
}

@Composable
private fun FilaCabecera(
    colorFondo: androidx.compose.ui.graphics.Color,
    colorBorde: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .background(colorFondo)
            .border(
                width = 0.5.dp,
                color = colorBorde
            )
    ) {
        CeldaCabecera("Producto",        anchoProducto)
        CeldaCabecera("Stock",           anchoStock)
        CeldaCabecera("Proveedor",       anchoProveedor)
        CeldaCabecera("P. Compra",       anchoCompra)
        CeldaCabecera("P. Venta",        anchoVenta)
        CeldaCabecera("Impuesto (%)",    anchoImpuesto)
    }
}

@Composable
private fun FilaProducto(
    producto: ProductoEntity,
    proveedor: ProveedorEntity?,
    relacion: ProductoProveedorEntity?,
    colorBorde: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit
) {
    val stockBajo   = producto.stockActual <= producto.stockMinimo
    val colorFondo  = when {
        stockBajo -> RojoAlerta.copy(alpha = 0.05f)
        else      -> MaterialTheme.colorScheme.surface
    }

    Surface(
        onClick = onClick,
        color   = colorFondo
    ) {
        Row(
            modifier = Modifier.border(width = 0.5.dp, color = colorBorde),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Producto
            CeldaDato(
                texto  = producto.nombre,
                ancho  = anchoProducto,
                color  = if (stockBajo) RojoAlerta
                else MaterialTheme.colorScheme.onSurface,
                negrita = true
            )
            // Stock
            CeldaDato(
                texto = "${formatearDecimal(producto.stockActual)} ${producto.unidadMedida}",
                ancho = anchoStock,
                color = if (stockBajo) RojoAlerta else VerdeStock,
                alineacion = TextAlign.End
            )
            // Proveedor
            CeldaDato(
                texto = proveedor?.nombre ?: "—",
                ancho = anchoProveedor,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // Precio compra
            CeldaDato(
                texto      = relacion?.let { "€${"%.2f".format(it.precioCompra)}" } ?: "—",
                ancho      = anchoCompra,
                alineacion = TextAlign.End,
                color      = MaterialTheme.colorScheme.onSurface
            )
            // Precio venta
            CeldaDato(
                texto      = relacion?.let { "€${"%.2f".format(it.precioVenta)}" } ?: "—",
                ancho      = anchoVenta,
                alineacion = TextAlign.End,
                color      = MaterialTheme.colorScheme.onSurface
            )
            // Impuesto
            CeldaDato(
                texto      = if (producto.impuesto > 0)
                    "${"%.0f".format(producto.impuesto)}%"
                else "—",
                ancho      = anchoImpuesto,
                alineacion = TextAlign.End,
                color      = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun CeldaCabecera(texto: String, ancho: Dp) {
    Text(
        text       = texto,
        modifier   = Modifier
            .width(ancho)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        style      = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color      = MaterialTheme.colorScheme.onPrimaryContainer,
        maxLines   = 1,
        overflow   = TextOverflow.Ellipsis
    )
}

@Composable
private fun CeldaDato(
    texto: String,
    ancho: Dp,
    color: androidx.compose.ui.graphics.Color,
    alineacion: TextAlign = TextAlign.Start,
    negrita: Boolean = false
) {
    Text(
        text      = texto,
        modifier  = Modifier
            .width(ancho)
            .padding(horizontal = 10.dp, vertical = 10.dp),
        style     = MaterialTheme.typography.bodySmall,
        fontWeight = if (negrita) FontWeight.SemiBold else FontWeight.Normal,
        color     = color,
        maxLines  = 2,
        overflow  = TextOverflow.Ellipsis,
        textAlign = alineacion
    )
}

private fun formatearDecimal(valor: Double): String =
    if (valor == valor.toLong().toDouble()) valor.toLong().toString()
    else String.format("%.2f", valor)