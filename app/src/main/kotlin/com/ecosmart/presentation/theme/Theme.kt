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
 * Rediseño 2026-09-24 (RF-083): paleta muestreada píxel a píxel de la imagen
 * de referencia del usuario (mockup de 9 pantallas + logo).
 */
val VerdeBoton = Color(0xFF215D41)
val VerdeOscuro = Color(0xFF193F34)
val VerdeLogo = Color(0xFF2C6742)
val VerdeMedio = Color(0xFF49875B)
val VerdeClaroLogo = Color(0xFF7EAA77)
val VerdeTarjetaOscura = Color(0xFF286B4B)
val VerdeTarjeta = Color(0xFFDFEBD8)
val VerdeBarra = Color(0xFFBACFBA)
val FondoPagina = Color(0xFFE6EDDC)
val FondoPantalla = Color(0xFFF1F4E5)
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

val EcoSmartTypography = Typography(
    headlineMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 30.sp),
    titleLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.ExtraBold, fontSize = 26.sp),
    titleMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 18.sp),
    titleSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 15.sp),
    bodyLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 16.sp),
    bodyMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 14.sp),
    bodySmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Normal, fontSize = 12.sp),
    labelLarge = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.Bold, fontSize = 16.sp),
    labelMedium = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 13.sp),
    labelSmall = TextStyle(fontFamily = Nunito, fontWeight = FontWeight.SemiBold, fontSize = 11.sp),
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
