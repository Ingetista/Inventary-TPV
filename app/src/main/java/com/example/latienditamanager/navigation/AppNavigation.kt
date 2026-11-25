package com.example.latienditamanager.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.latienditamanager.ui.screens.dashboard.DashboardScreen
import com.example.latienditamanager.ui.screens.importacion.ImportacionScreen
import com.example.latienditamanager.ui.screens.movimientos.MovimientosScreen
import com.example.latienditamanager.ui.screens.productos.ProductosScreen
import com.example.latienditamanager.ui.screens.proveedores.ProveedoresScreen
import com.example.latienditamanager.viewmodel.ImportacionViewModel
import com.example.latienditamanager.viewmodel.MovimientoViewModel
import com.example.latienditamanager.viewmodel.ProductoViewModel
import com.example.latienditamanager.viewmodel.ProveedorViewModel

// Objeto que centraliza todas las rutas — evita strings sueltos por el código
object Rutas {
    const val DASHBOARD     = "dashboard"
    const val PRODUCTOS     = "productos"
    const val PROVEEDORES   = "proveedores"
    const val MOVIMIENTOS   = "movimientos"
    const val IMPORTACION   = "importacion"
}

@Composable
fun AppNavigation(
    productoViewModel: ProductoViewModel,
    proveedorViewModel: ProveedorViewModel,
    movimientoViewModel: MovimientoViewModel,
    importacionViewModel: ImportacionViewModel,
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController = navController,
        startDestination = Rutas.DASHBOARD
    ) {
        composable(Rutas.DASHBOARD) {
            DashboardScreen(
                productoViewModel  = productoViewModel,
                movimientoViewModel = movimientoViewModel,
                onNavigate = { ruta -> navController.navigate(ruta) }
            )
        }
        composable(Rutas.PRODUCTOS) {
            ProductosScreen(
                productoViewModel  = productoViewModel,
                proveedorViewModel = proveedorViewModel,
                movimientoViewModel = movimientoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.PROVEEDORES) {
            ProveedoresScreen(
                proveedorViewModel = proveedorViewModel,
                productoViewModel  = productoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.MOVIMIENTOS) {
            MovimientosScreen(
                movimientoViewModel = movimientoViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
        composable(Rutas.IMPORTACION) {
            ImportacionScreen(
                productoViewModel   = productoViewModel,
                movimientoViewModel = movimientoViewModel,
                importacionViewModel = importacionViewModel,
                onNavigateBack = { navController.popBackStack() }
            )
        }
    }
}