package org.khoyron.bilal.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import org.khoyron.bilal.ui.main.SplashScreen
import org.khoyron.bilal.ui.home.HomeScreen
import org.khoyron.bilal.ui.quran.detail.SurahDetailScreen
import org.khoyron.bilal.ui.map.MapPickerScreen
import kotlinx.serialization.Serializable

@Composable
fun NavGraph(
    navController: NavHostController = rememberNavController()
) {
    NavHost(
        navController    = navController,
        startDestination = Screen.Splash.route
    ) {

        // ── Splash ────────────────────────────────────────────────────────
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        // ── Home ──────────────────────────────────────────────────────────
        composable(Screen.Home.route) {
            HomeScreen(navController = navController)
        }

        // ── Map Picker ────────────────────────────────────────────────────
        composable(Screen.MapPicker.route) {
            MapPickerScreen(
                onLocationSelected = { lat, lon ->
                    // We can save result to savedStateHandle or call a ViewModel method
                    navController.previousBackStackEntry
                        ?.savedStateHandle
                        ?.set("location", "$lat,$lon")
                    navController.popBackStack()
                },
                onBack = { navController.popBackStack() }
            )
        }

        // ── Surah Detail ──────────────────────────────────────────────────
        composable<SurahDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<SurahDetail>()
            SurahDetailScreen(
                surahNumber = route.surahNumber,
                onBack      = { navController.popBackStack() }
            )
        }

        // ── Juz Detail ────────────────────────────────────────────────────
        composable<JuzDetail> { backStackEntry ->
            val route = backStackEntry.toRoute<JuzDetail>()
            SurahDetailScreen(
                juzNumber = route.juzNumber,
                onBack    = { navController.popBackStack() }
            )
        }
    }
}


@Serializable
data class SurahDetail(val surahNumber: Int)

@Serializable
data class JuzDetail(val juzNumber: Int)