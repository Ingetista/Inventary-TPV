package com.example.latienditamanager.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val EsquemaClaro = lightColorScheme(
    // — Primario: Azul —
    primary          = AzulPrincipal,
    onPrimary        = TextoBlanco,
    primaryContainer = AzulClaro,
    onPrimaryContainer = AzulOscuro,

    // — Secundario: Naranja —
    secondary        = NaranjaSecundario,
    onSecondary      = TextoBlanco,
    secondaryContainer = NaranjaClaro,
    onSecondaryContainer = NaranjaOscuro,

    // — Superficies —
    background       = SuperficieBlanca,
    onBackground     = TextoOscuro,
    surface          = SuperficieBlanca,
    onSurface        = TextoOscuro,
    surfaceVariant   = SuperficieGris,
    onSurfaceVariant = TextoMedio,
    outline          = BordeGris,

    // — Errores — usamos el rojo de alerta
    error            = RojoAlerta,
    onError          = TextoBlanco
)

@Composable
fun LaTienditaManagerTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = EsquemaClaro,
        typography  = Typography,
        content     = content
    )
}