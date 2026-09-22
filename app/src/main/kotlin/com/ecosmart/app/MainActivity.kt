package com.ecosmart.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.ecosmart.infrastructure.session.SesionUsuario
import com.ecosmart.presentation.EcoSmartNavHost
import com.ecosmart.presentation.EcoSmartRoute
import com.ecosmart.presentation.theme.EcoSmartTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Punto de entrada único de la app: todas las pantallas son Compose,
 * enrutadas por [EcoSmartNavHost]. `@AndroidEntryPoint` habilita la
 * inyección de ViewModels vía `hiltViewModel()` en cada pantalla.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var sesionUsuario: SesionUsuario

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Corrección post-QA (RF-010): si ya hay una sesión persistida, se
        // salta Registro/Login y se entra directo a Home.
        val yaAutenticado = sesionUsuario.usuarioActualId.value != null
        setContent {
            EcoSmartTheme {
                EcoSmartNavHost(
                    startDestination = if (yaAutenticado) EcoSmartRoute.Home.ruta else EcoSmartRoute.Registro.ruta,
                )
            }
        }
    }
}
