package com.example.latienditamanager

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.latienditamanager.data.local.database.AppDatabase
import com.example.latienditamanager.data.repository.ImportacionRepository
import com.example.latienditamanager.data.repository.MovimientoRepository
import com.example.latienditamanager.data.repository.ProductoRepository
import com.example.latienditamanager.data.repository.ProveedorRepository
import com.example.latienditamanager.navigation.AppNavigation
import com.example.latienditamanager.ui.theme.LaTienditaManagerTheme
import com.example.latienditamanager.viewmodel.MovimientoViewModel
import com.example.latienditamanager.viewmodel.ProductoViewModel
import com.example.latienditamanager.viewmodel.ProveedorViewModel
import com.example.latienditamanager.viewmodel.ImportacionViewModel
import com.example.latienditamanager.viewmodel.ViewModelFactory

class MainActivity : ComponentActivity() {

    private lateinit var productoViewModel: ProductoViewModel
    private lateinit var proveedorViewModel: ProveedorViewModel
    private lateinit var movimientoViewModel: MovimientoViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // 1. Base de datos
        val database = AppDatabase.getInstance(applicationContext)

        // 2. Repositories
        val productoRepository = ProductoRepository(database.productoDao())
        val proveedorRepository = ProveedorRepository(
            database.proveedorDao(),
            database.productoProveedorDao()
        )
        val movimientoRepository = MovimientoRepository(
            database.movimientoDao(),
            database.productoDao()
        )
        val importacionRepository = ImportacionRepository(
            database.productoDao(),
            database.movimientoDao()
        )
        // 3. Factory y ViewModels
        val factory = ViewModelFactory(
            productoRepository,
            proveedorRepository,
            movimientoRepository,
            importacionRepository
        )

        val importacionViewModel = ViewModelProvider(this, factory)[ImportacionViewModel::class.java]

        productoViewModel  = ViewModelProvider(this, factory)[ProductoViewModel::class.java]
        proveedorViewModel = ViewModelProvider(this, factory)[ProveedorViewModel::class.java]
        movimientoViewModel = ViewModelProvider(this, factory)[MovimientoViewModel::class.java]

        // 4. UI
        setContent {
            LaTienditaManagerTheme {
                AppNavigation(
                    productoViewModel  = productoViewModel,
                    proveedorViewModel = proveedorViewModel,
                    movimientoViewModel = movimientoViewModel,
                    importacionViewModel = importacionViewModel
                )
            }
        }
    }
}