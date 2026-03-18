package com.arcentales.alumnosapp.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.arcentales.alumnosapp.ui.screens.AddEditAlumnoScreen
import com.arcentales.alumnosapp.ui.screens.AlumnoListScreen
import com.arcentales.alumnosapp.ui.screens.FavoritosScreen
import com.arcentales.alumnosapp.ui.viewmodel.AlumnoViewModel

/**
 * Define las rutas de navegación de la app.
 * El ViewModel se comparte entre todas las pantallas.
 */
sealed class Screen(val route: String) {
    object AlumnoList  : Screen("alumnos")
    object AddAlumno   : Screen("add_alumno")
    object EditAlumno  : Screen("edit_alumno/{alumnoId}") {
        fun createRoute(alumnoId: Int) = "edit_alumno/$alumnoId"
    }
    object Favoritos   : Screen("favoritos")
}

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val viewModel: AlumnoViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Screen.AlumnoList.route
    ) {

        // ── Lista principal ──────────────────────────────────────────────
        composable(Screen.AlumnoList.route) {
            AlumnoListScreen(
                viewModel = viewModel,
                onAddClick = { navController.navigate(Screen.AddAlumno.route) },
                onEditClick = { alumnoId ->
                    navController.navigate(Screen.EditAlumno.createRoute(alumnoId))
                },
                onFavoritosClick = { navController.navigate(Screen.Favoritos.route) }
            )
        }

        // ── Agregar alumno ───────────────────────────────────────────────
        composable(Screen.AddAlumno.route) {
            AddEditAlumnoScreen(
                viewModel = viewModel,
                alumnoId = null,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Editar alumno (recibe ID como argumento) ─────────────────────
        composable(
            route = Screen.EditAlumno.route,
            arguments = listOf(navArgument("alumnoId") { type = NavType.IntType })
        ) { backStackEntry ->
            val alumnoId = backStackEntry.arguments?.getInt("alumnoId")
            AddEditAlumnoScreen(
                viewModel = viewModel,
                alumnoId = alumnoId,
                onBack = { navController.popBackStack() }
            )
        }

        // ── Favoritos ────────────────────────────────────────────────────
        composable(Screen.Favoritos.route) {
            FavoritosScreen(
                viewModel = viewModel,
                onBack = { navController.popBackStack() },
                onEditClick = { alumnoId ->
                    navController.navigate(Screen.EditAlumno.createRoute(alumnoId))
                }
            )
        }
    }
}
