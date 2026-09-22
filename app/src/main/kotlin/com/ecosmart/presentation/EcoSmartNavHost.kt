package com.ecosmart.presentation

import androidx.compose.runtime.Composable
import androidx.navigation.NamedNavArgument
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.ecosmart.domain.valueobject.CategoriaActividad
import com.ecosmart.presentation.activitydetail.ActividadDetalleScreen
import com.ecosmart.presentation.auth.RegistroScreen
import com.ecosmart.presentation.home.ContenidoEducativoScreen
import com.ecosmart.presentation.home.HomeScreen
import com.ecosmart.presentation.home.PuntosVerdesScreen
import com.ecosmart.presentation.permissions.SolicitudPermisosScreen
import com.ecosmart.presentation.profile.HistorialScreen
import com.ecosmart.presentation.profile.PerfilScreen
import com.ecosmart.presentation.verification.VerificacionCaminataScreen
import com.ecosmart.presentation.verification.VerificacionFotoScreen

/**
 * Rutas de navegación de EcoSmart, una por pantalla de entrada de cada
 * Módulo (ver tasks.md § Mapa de Módulos → Épicas → Historias de
 * Usuario). Los 5 módulos ya tienen sus pantallas reales (T031 ✅, T042 ✅,
 * T051/T053/T054/T062 ✅, T077/T079/T080 ✅, T088/T090 ✅). El cableado
 * "oficial" de las 5 pantallas de entrada con el flujo post-login →
 * permisos → home es T092 (Fase 8/Polish); acá ya se conectan todas
 * anticipadamente para poder probar el flujo real en Android Studio.
 */
sealed class EcoSmartRoute(val ruta: String) {
    data object Registro : EcoSmartRoute("registro")
    data object SolicitudPermisos : EcoSmartRoute("solicitud-permisos")
    data object Home : EcoSmartRoute("home")
    data object ActividadDetalle : EcoSmartRoute("actividad-detalle/{actividadId}") {
        val argumentos: List<NamedNavArgument> = listOf(navArgument("actividadId") { type = NavType.StringType })
        fun crearRuta(actividadId: String): String = "actividad-detalle/$actividadId"
    }
    data object VerificacionCaminata : EcoSmartRoute("verificacion-caminata/{actividadId}") {
        val argumentos: List<NamedNavArgument> = listOf(navArgument("actividadId") { type = NavType.StringType })
        fun crearRuta(actividadId: String): String = "verificacion-caminata/$actividadId"
    }
    data object VerificacionFoto : EcoSmartRoute("verificacion-foto/{actividadId}") {
        val argumentos: List<NamedNavArgument> = listOf(navArgument("actividadId") { type = NavType.StringType })
        fun crearRuta(actividadId: String): String = "verificacion-foto/$actividadId"
    }
    data object PuntosVerdes : EcoSmartRoute("puntos-verdes")
    data object ContenidoEducativo : EcoSmartRoute("contenido-educativo")
    data object Perfil : EcoSmartRoute("perfil")
    data object Historial : EcoSmartRoute("historial")
}

@Composable
fun EcoSmartNavHost(
    navController: NavHostController = rememberNavController(),
    startDestination: String = EcoSmartRoute.Registro.ruta,
) {
    NavHost(navController = navController, startDestination = startDestination) {
        composable(EcoSmartRoute.Registro.ruta) {
            RegistroScreen(
                onAutenticado = {
                    navController.navigate(EcoSmartRoute.SolicitudPermisos.ruta) {
                        popUpTo(EcoSmartRoute.Registro.ruta) { inclusive = true }
                    }
                },
            )
        }
        composable(EcoSmartRoute.SolicitudPermisos.ruta) {
            SolicitudPermisosScreen(
                onContinuar = {
                    navController.navigate(EcoSmartRoute.Home.ruta) {
                        popUpTo(EcoSmartRoute.SolicitudPermisos.ruta) { inclusive = true }
                    }
                },
            )
        }
        composable(EcoSmartRoute.Home.ruta) {
            HomeScreen(
                onSeleccionarActividad = { actividadId ->
                    navController.navigate(EcoSmartRoute.ActividadDetalle.crearRuta(actividadId.valor))
                },
                onVerPuntosVerdes = { navController.navigate(EcoSmartRoute.PuntosVerdes.ruta) },
                onVerContenidoEducativo = { navController.navigate(EcoSmartRoute.ContenidoEducativo.ruta) },
                onVerPerfil = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
            )
        }
        composable(EcoSmartRoute.ActividadDetalle.ruta, arguments = EcoSmartRoute.ActividadDetalle.argumentos) {
            ActividadDetalleScreen(
                onRealizarla = { actividadId, categoria ->
                    val destino = if (categoria == CategoriaActividad.CAMINAR) {
                        EcoSmartRoute.VerificacionCaminata.crearRuta(actividadId.valor)
                    } else {
                        EcoSmartRoute.VerificacionFoto.crearRuta(actividadId.valor)
                    }
                    navController.navigate(destino)
                },
            )
        }
        composable(EcoSmartRoute.PuntosVerdes.ruta) {
            PuntosVerdesScreen()
        }
        composable(EcoSmartRoute.ContenidoEducativo.ruta) {
            ContenidoEducativoScreen()
        }
        composable(EcoSmartRoute.VerificacionCaminata.ruta, arguments = EcoSmartRoute.VerificacionCaminata.argumentos) {
            VerificacionCaminataScreen(
                onVerProgreso = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                onVolver = { navController.popBackStack() },
            )
        }
        composable(EcoSmartRoute.VerificacionFoto.ruta, arguments = EcoSmartRoute.VerificacionFoto.argumentos) {
            VerificacionFotoScreen(
                onVerProgreso = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                onAtras = { navController.popBackStack() },
            )
        }
        composable(EcoSmartRoute.Perfil.ruta) {
            PerfilScreen(
                onVerHistorial = { navController.navigate(EcoSmartRoute.Historial.ruta) },
                onCerrarSesion = {
                    navController.navigate(EcoSmartRoute.Registro.ruta) {
                        popUpTo(0)
                    }
                },
            )
        }
        composable(EcoSmartRoute.Historial.ruta) {
            HistorialScreen()
        }
    }
}
