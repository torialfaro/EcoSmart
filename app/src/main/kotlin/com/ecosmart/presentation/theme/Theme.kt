package com.ecosmart.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/*
 * Paleta y tipografía compartidas de EcoSmart (T020). El tono es
 * deliberadamente ameno y motivador, nunca punitivo (RNF-001,
 * constitution.md Principio IX): sin rojos de "error/alerta" para los
 * estados Rechazado/Indeterminado (esos se comunican con texto claro +
 * una acción, no con color de castigo).
 */
private val VerdePlanta = Color(0xFF2E5D32)
private val VerdeBrote = Color(0xFF4C8C4A)
private val VerdeClaro = Color(0xFFB7E4C7)
private val FondoClaro = Color(0xFFF7FBF5)
private val FondoOscuro = Color(0xFF142015)
private val AmbarSuave = Color(0xFFE8A33D) // acentos motivadores: racha, nivel, progreso

private val EsquemaClaro = lightColorScheme(
    primary = VerdePlanta,
    onPrimary = Color.White,
    secondary = VerdeBrote,
    tertiary = AmbarSuave,
    background = FondoClaro,
    surface = FondoClaro,
    surfaceVariant = VerdeClaro,
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaro,
    onPrimary = VerdePlanta,
    secondary = VerdeBrote,
    tertiary = AmbarSuave,
    background = FondoOscuro,
    surface = FondoOscuro,
)

val EcoSmartTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 22.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

@Composable
fun EcoSmartTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) EsquemaOscuro else EsquemaClaro
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EcoSmartTypography,
        content = content,
    )
}
