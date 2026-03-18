package com.arcentales.alumnosapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.arcentales.alumnosapp.ui.navigation.AppNavigation
import com.arcentales.alumnosapp.ui.theme.AlumnosAppTheme

/**
 * Punto de entrada de la aplicación.
 * Configura el tema y lanza la navegación principal.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AlumnosAppTheme {
                AppNavigation()
            }
        }
    }
}
