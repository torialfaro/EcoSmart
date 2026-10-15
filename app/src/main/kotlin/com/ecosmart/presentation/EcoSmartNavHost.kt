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
import com.ecosmart.presentation.auth.PerfilEdicionScreen
import com.ecosmart.presentation.auth.RegistroScreen
import com.ecosmart.presentation.home.ActividadesScreen
import com.ecosmart.presentation.home.ContenidoEducativoScreen
import com.ecosmart.presentation.home.ContenidoEducativoDetalleScreen
import com.ecosmart.presentation.home.HomeScreen
import com.ecosmart.presentation.home.PuntosVerdesScreen
import com.ecosmart.presentation.permissions.SolicitudPermisosScreen
import com.ecosmart.presentation.profile.HistorialScreen
import com.ecosmart.presentation.profile.PerfilScreen
import com.ecosmart.presentation.rewards.RecompensasScreen
import com.ecosmart.presentation.theme.BarraInferiorEcoSmart
import com.ecosmart.presentation.theme.PestanaInferior
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
    data object Actividades : EcoSmartRoute("actividades")
    data object Recompensas : EcoSmartRoute("recompensas")
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
    data object ContenidoEducativoDetalle : EcoSmartRoute("contenido-educativo/{id}") {
        val argumentos: List<NamedNavArgument> = listOf(navArgument("id") { type = NavType.StringType })
        fun crearRuta(id: String): String = "contenido-educativo/$id"
    }
    data object Perfil : EcoSmartRoute("perfil")
    data object PerfilEdicion : EcoSmartRoute("perfil-edicion")
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
                onVerTodasLasActividades = {
                    navController.navigate(EcoSmartRoute.Actividades.ruta)
                },
                onVerContenidoEducativo = { navController.navigate(EcoSmartRoute.ContenidoEducativo.ruta) },
                onVerPerfil = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                barraInferior = { BarraInferior(navController, PestanaInferior.INICIO) },
            )
        }
        composable(EcoSmartRoute.Actividades.ruta) {
            ActividadesScreen(
                onSeleccionarActividad = { actividadId ->
                    navController.navigate(EcoSmartRoute.ActividadDetalle.crearRuta(actividadId))
                },
                onVerPerfil = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                barraInferior = { BarraInferior(navController, PestanaInferior.ACTIVIDADES) },
            )
        }
        composable(EcoSmartRoute.Recompensas.ruta) {
            RecompensasScreen(
                onVerPerfil = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                barraInferior = { BarraInferior(navController, PestanaInferior.RECOMPENSAS) },
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
                onVolver = { navController.popBackStack() },
                barraInferior = { BarraInferior(navController, PestanaInferior.ACTIVIDADES) },
            )
        }
        composable(EcoSmartRoute.PuntosVerdes.ruta) {
            PuntosVerdesScreen(
                onVolver = { navController.popBackStack() },
                barraInferior = { BarraInferior(navController, PestanaInferior.PUNTOS_VERDES) },
            )
        }
        composable(EcoSmartRoute.ContenidoEducativo.ruta) {
            ContenidoEducativoScreen(
                onSeleccionarContenido = { id -> navController.navigate(EcoSmartRoute.ContenidoEducativoDetalle.crearRuta(id)) },
                onVerPerfil = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                onVolver = { navController.popBackStack() },
                barraInferior = { BarraInferior(navController, PestanaInferior.EDUCACION) },
            )
        }
        composable(EcoSmartRoute.ContenidoEducativoDetalle.ruta, arguments = EcoSmartRoute.ContenidoEducativoDetalle.argumentos) { entrada ->
            ContenidoEducativoDetalleScreen(
                id = checkNotNull(entrada.arguments?.getString("id")),
                onVolver = { navController.popBackStack() },
            )
        }
        composable(EcoSmartRoute.VerificacionCaminata.ruta, arguments = EcoSmartRoute.VerificacionCaminata.argumentos) {
            VerificacionCaminataScreen(
                onVerProgreso = { navController.navigate(EcoSmartRoute.Perfil.ruta) },
                onVolver = { navController.popBackStack() },
                barraInferior = { BarraInferior(navController, PestanaInferior.ACTIVIDADES) },
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
                onEditarPerfil = { navController.navigate(EcoSmartRoute.PerfilEdicion.ruta) },
                onCerrarSesion = {
                    navController.navigate(EcoSmartRoute.Registro.ruta) {
                        popUpTo(0)
                    }
                },
                barraInferior = { BarraInferior(navController, PestanaInferior.PERFIL) },
            )
        }
        composable(EcoSmartRoute.PerfilEdicion.ruta) {
            PerfilEdicionScreen(
                onVolver = { navController.popBackStack() },
                onCuentaEliminada = {
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

/**
 * Barra inferior del mockup (RF-UI-002b, RF-UI-002c): Inicio → Home, Actividades →
 * Actividades, Recompensas → Recompensas, Educación → ContenidoEducativo,
 * Puntos Verdes → PuntosVerdes, Perfil → Perfil.
 */
@Composable
private fun BarraInferior(navController: NavHostController, seleccion: PestanaInferior) {
    fun ir(ruta: String) = navController.navigate(ruta) {
        popUpTo(EcoSmartRoute.Home.ruta)
        launchSingleTop = true
    }
    BarraInferiorEcoSmart(
        seleccion = seleccion,
        onInicio = { ir(EcoSmartRoute.Home.ruta) },
        onActividades = { ir(EcoSmartRoute.Actividades.ruta) },
        onRecompensas = { ir(EcoSmartRoute.Recompensas.ruta) },
        onEducacion = { ir(EcoSmartRoute.ContenidoEducativo.ruta) },
        onPuntosVerdes = { ir(EcoSmartRoute.PuntosVerdes.ruta) },
        onPerfil = { ir(EcoSmartRoute.Perfil.ruta) },
    )
}
