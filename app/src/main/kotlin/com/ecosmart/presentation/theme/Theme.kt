package com.ecosmart.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/*
 * Paleta y tipografía compartidas de EcoSmart (T020, rediseño post-QA
 * 2026-09-22 inspirado en las referencias visuales del usuario: verde
 * oscuro + crema + acentos ámbar, bordes muy redondeados). El tono sigue
 * siendo deliberadamente ameno y motivador, nunca punitivo (RNF-001,
 * constitution.md Principio IX): sin rojos de "error/alerta" para los
 * estados Rechazado/Indeterminado (esos se comunican con texto claro +
 * una acción, no con color de castigo).
 */
private val VerdeOscuro = Color(0xFF1B3B2F) // fondo "hero" / primary, como el mockup de referencia
private val VerdeMedio = Color(0xFF2E5D45)
private val VerdeSalvia = Color(0xFFAFCBB0) // tarjetas claras / superficies secundarias
private val CremaClaro = Color(0xFFF7F3E8) // fondo general de las pantallas
private val SuperficieOscura = Color(0xFF17301F)
private val AmbarVivo = Color(0xFFF0A93C) // acento: barras, badges, progreso, racha, nivel
private val TextoOscuro = Color(0xFF1C1C1C)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeOscuro,
    onPrimary = Color.White,
    secondary = VerdeMedio,
    onSecondary = Color.White,
    tertiary = AmbarVivo,
    onTertiary = TextoOscuro,
    background = CremaClaro,
    onBackground = TextoOscuro,
    surface = Color.White,
    onSurface = TextoOscuro,
    surfaceVariant = VerdeSalvia,
    onSurfaceVariant = VerdeOscuro,
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeSalvia,
    onPrimary = VerdeOscuro,
    secondary = VerdeMedio,
    onSecondary = Color.White,
    tertiary = AmbarVivo,
    onTertiary = TextoOscuro,
    background = VerdeOscuro,
    onBackground = Color.White,
    surface = SuperficieOscura,
    onSurface = Color.White,
    surfaceVariant = VerdeMedio,
    onSurfaceVariant = VerdeSalvia,
)

val EcoSmartTypography = Typography(
    titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = 26.sp),
    titleMedium = TextStyle(fontWeight = FontWeight.SemiBold, fontSize = 20.sp),
    bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = 16.sp),
    labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = 14.sp),
)

/** Bordes redondeados (corrección post-QA 2026-09-22): cascadea a Card/OutlinedTextField/etc. */
val EcoSmartShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)

/** Forma "píldora" para los botones principales (Login/Registro, etc.), igual que la referencia visual. */
val FormaBotonPildora = RoundedCornerShape(percent = 50)

@Composable
fun EcoSmartTheme(
    useDarkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (useDarkTheme) EsquemaOscuro else EsquemaClaro
    MaterialTheme(
        colorScheme = colorScheme,
        typography = EcoSmartTypography,
        shapes = EcoSmartShapes,
        content = content,
    )
}
