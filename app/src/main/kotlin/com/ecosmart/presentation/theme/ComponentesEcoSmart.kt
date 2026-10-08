package com.ecosmart.presentation.theme

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ecosmart.app.R

/**
 * Encabezado de todas las pantallas (RF-083): isotipo del logo + "ecosmart" a la
 * izquierda, avatar circular a la derecha. `onVolver` agrega una flecha atrás antes del
 * logo (toda pantalla que no sea una pestaña de la barra inferior la necesita); se omite
 * en Home/Historial/Perfil, que son raíces de navegación sin "pantalla anterior" real.
 */
@Composable
fun EncabezadoEcoSmart(
    inicialUsuario: String = "U",
    modifier: Modifier = Modifier,
    saludo: String? = null,
    onAvatar: (() -> Unit)? = null,
    onVolver: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        onVolver?.let {
            IconButton(onClick = it, modifier = Modifier.size(32.dp)) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
            }
            Box(modifier = Modifier.padding(start = 4.dp))
        }
        Image(
            painter = painterResource(R.drawable.logo_ecosmart_icono),
            contentDescription = "EcoSmart",
            modifier = Modifier.size(40.dp),
        )
        Text(
            text = "ecosmart",
            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.ExtraBold, fontSize = 28.sp),
            color = VerdeLogo,
            modifier = Modifier.padding(start = 8.dp),
        )
        Box(modifier = Modifier.weight(1f))
        saludo?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(end = 8.dp),
            )
        }
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(VerdeOscuro)
                .then(if (onAvatar != null) Modifier.clickable(onClick = onAvatar) else Modifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = inicialUsuario.take(1).uppercase(),
                style = MaterialTheme.typography.titleSmall,
                color = CremaTexto,
            )
        }
    }
}

enum class PestanaInferior { INICIO, MISIONES, PERFIL }

@Composable
private fun iconoHoja(): ImageVector = ImageVector.vectorResource(R.drawable.ic_hoja)

/** Barra inferior Inicio / Misiones / Perfil del mockup (RF-083). */
@Composable
fun BarraInferiorEcoSmart(
    seleccion: PestanaInferior,
    onInicio: () -> Unit,
    onMisiones: () -> Unit,
    onPerfil: () -> Unit,
) {
    NavigationBar(containerColor = FondoPagina, tonalElevation = 0.dp) {
        val colores = NavigationBarItemDefaults.colors(
            selectedIconColor = VerdeBoton,
            selectedTextColor = VerdeBoton,
            unselectedIconColor = VerdeMedio,
            unselectedTextColor = VerdeMedio,
            indicatorColor = Color.Transparent,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.INICIO,
            onClick = onInicio,
            icon = { Icon(Icons.Filled.Home, contentDescription = null) },
            label = { Text("Inicio", fontWeight = if (seleccion == PestanaInferior.INICIO) FontWeight.ExtraBold else FontWeight.Medium) },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.MISIONES,
            onClick = onMisiones,
            icon = { Icon(iconoHoja(), contentDescription = null) },
            label = { Text("Misiones", fontWeight = if (seleccion == PestanaInferior.MISIONES) FontWeight.ExtraBold else FontWeight.Medium) },
            colors = colores,
        )
        NavigationBarItem(
            selected = seleccion == PestanaInferior.PERFIL,
            onClick = onPerfil,
            icon = { Icon(Icons.Filled.Person, contentDescription = null) },
            label = { Text("Perfil", fontWeight = if (seleccion == PestanaInferior.PERFIL) FontWeight.ExtraBold else FontWeight.Medium) },
            colors = colores,
        )
    }
}

/** Tarjeta verde claro de esquinas muy redondeadas usada en todo el mockup. */
@Composable
fun TarjetaEcoSmart(
    modifier: Modifier = Modifier,
    color: Color = VerdeTarjeta,
    contenido: @Composable () -> Unit,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = color),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) { contenido() }
    }
}
