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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontVariation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosmart.app.R

/*
 * Rediseño 2026-09-24 (RF-083) y Reconstrucción UI 2026-10 (especificacion-ui-app-eco.md §2.1):
 * Paleta de colores y tokens canónicos.
 */
// Colores canónicos de diseño (especificacion-ui-app-eco.md §2.1)
val ColorBgScreen = Color(0xFFFBF8F0)
val ColorSurfaceCard = Color(0xFFFFFFFF)
val ColorGreen700 = Color(0xFF1F5E34)
val ColorGreen900 = Color(0xFF0F4D2E)
val ColorGreen500 = Color(0xFF2E7D3E)
val ColorGreen100 = Color(0xFFE6F2DF)
val ColorLeaf = Color(0xFF4FA24A)
val ColorStateProgress = Color(0xFFE3F1EE)
val ColorStateProgressBorder = Color(0xFFCFE5E0)
val ColorStateIdle = Color(0xFFEAF0F2)
val ColorTextPrimary = Color(0xFF1B2B1F)
val ColorTextSecondary = Color(0xFF6B6F68)
val ColorTrack = Color(0xFFE3E5DF)
val ColorWater = Color(0xFF3E9BD6)

// Compatibilidad con paleta histórica y nombres ya usados en la app
val VerdeBoton = ColorGreen700
val VerdeOscuro = ColorGreen900
val VerdeLogo = Color(0xFF2C6742)
val VerdeMedio = Color(0xFF49875B)
val VerdeClaroLogo = Color(0xFF7EAA77)
val VerdeTarjetaOscura = Color(0xFF286B4B)
val VerdeTarjeta = ColorGreen100
val VerdeBarra = ColorTrack
val FondoPagina = ColorBgScreen
val FondoPantalla = ColorBgScreen
val CremaTexto = Color(0xFFF6F7EA)
val AmbarPasos = Color(0xFFF2B855)

private val EsquemaClaro = lightColorScheme(
    primary = VerdeBoton,
    onPrimary = CremaTexto,
    primaryContainer = VerdeTarjetaOscura,
    onPrimaryContainer = CremaTexto,
    secondary = VerdeMedio,
    onSecondary = CremaTexto,
    secondaryContainer = VerdeTarjeta,
    onSecondaryContainer = VerdeOscuro,
    tertiary = AmbarPasos,
    onTertiary = VerdeOscuro,
    tertiaryContainer = AmbarPasos,
    onTertiaryContainer = VerdeOscuro,
    background = FondoPantalla,
    onBackground = VerdeOscuro,
    surface = FondoPantalla,
    onSurface = VerdeOscuro,
    surfaceVariant = VerdeTarjeta,
    onSurfaceVariant = VerdeOscuro,
    outline = VerdeBarra,
    outlineVariant = VerdeBarra,
)

private val EsquemaOscuro = darkColorScheme(
    primary = VerdeClaroLogo,
    onPrimary = VerdeOscuro,
    primaryContainer = VerdeTarjetaOscura,
    onPrimaryContainer = CremaTexto,
    secondary = VerdeMedio,
    onSecondary = CremaTexto,
    secondaryContainer = Color(0xFF1F4A3B),
    onSecondaryContainer = CremaTexto,
    tertiary = AmbarPasos,
    onTertiary = VerdeOscuro,
    background = Color(0xFF12291F),
    onBackground = CremaTexto,
    surface = Color(0xFF12291F),
    onSurface = CremaTexto,
    surfaceVariant = Color(0xFF1F4A3B),
    onSurfaceVariant = CremaTexto,
)

/** Nunito (variable, res/font/nunito.ttf): la tipografía redondeada más cercana al logo y a los títulos del mockup. */
@OptIn(ExperimentalTextApi::class)
val Nunito = FontFamily(
    listOf(FontWeight.Normal, FontWeight.Medium, FontWeight.SemiBold, FontWeight.Bold, FontWeight.ExtraBold).map { peso ->
        Font(R.font.nunito, weight = peso, variationSettings = FontVariation.Settings(FontVariation.weight(peso.weight)))
    },
)

// Roles tipográficos explícitos de especificacion-ui-app-eco.md §2.2
val TypeDisplay = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 40.sp)
val TypePoints = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 32.sp)
val TypeTitle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 24.sp)
val TypeGreeting = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 20.sp)
val TypeSection = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
val TypeCardTitle = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
val TypeBody = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 12.sp)
val TypeMeta = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 11.sp)
val TypeChip = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 12.sp)
val TypeTab = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Medium, fontSize = 10.sp)
val TypeLink = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 12.sp)

val EcoSmartTypography = Typography(
    displayLarge = TypeDisplay,
    displayMedium = TypePoints,
    headlineMedium = TypeTitle,
    titleLarge = TypeGreeting,
    titleMedium = TypeSection,
    titleSmall = TypeCardTitle,
    bodyLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodyMedium = TypeBody,
    bodySmall = TypeMeta,
    labelLarge = TypeChip,
    labelMedium = TypeLink,
    labelSmall = TypeTab,
)

val EcoSmartShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp),
)
val FormaBotonPildora = RoundedCornerShape(percent = 50)

@Composable
fun EcoSmartTheme(useDarkTheme: Boolean = isSystemInDarkTheme(), content: @Composable () -> Unit) {
    val colorScheme = if (useDarkTheme) EsquemaOscuro else EsquemaClaro
    MaterialTheme(colorScheme = colorScheme, typography = EcoSmartTypography, shapes = EcoSmartShapes, content = content)
}
