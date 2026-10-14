package com.ecosmart.presentation.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Tokens de espaciado, radios y elevación derivados de especificacion-ui-app-eco.md §2.3.
 */

// --- Espaciado (space.*) ---
val SpaceScreenX: Dp = 16.dp
val SpaceGapCard: Dp = 12.dp
val SpaceSection: Dp = 24.dp

// --- Radios (radius.*) ---
val RadiusCard: Dp = 16.dp
val RadiusMedia: Dp = 20.dp
val RadiusPill: Dp = 999.dp
val RadiusIconCircle: Dp = 36.dp
val RadiusIconCircleLarge: Dp = 40.dp

// --- Formas asociadas a radios ---
val FormaTarjeta = RoundedCornerShape(16.dp)
val FormaMedia = RoundedCornerShape(20.dp)
val FormaPill = RoundedCornerShape(percent = 50)
val FormaIconoCirculo = CircleShape

// --- Elevación / Sombras (shadow.*) ---
val ElevacionTarjeta: Dp = 2.dp

/** Contenedor de tokens agrupados por categoría. */
object EcoSmartTokens {
    object Space {
        val screenX: Dp = SpaceScreenX
        val gapCard: Dp = SpaceGapCard
        val section: Dp = SpaceSection
    }

    object Radius {
        val card: Dp = RadiusCard
        val media: Dp = RadiusMedia
        val pill: Dp = RadiusPill
        val iconCircle: Dp = RadiusIconCircle
        val iconCircleLarge: Dp = RadiusIconCircleLarge
    }

    object Shape {
        val card = FormaTarjeta
        val media = FormaMedia
        val pill = FormaPill
        val iconCircle = FormaIconoCirculo
    }

    object Elevation {
        val card: Dp = ElevacionTarjeta
    }
}
