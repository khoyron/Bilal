package org.khoyron.bilal.navigation

import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController

// ── Top-level routes ──────────────────────────────────────────────────────────
sealed class Screen(val route: String) {
    data object Splash       : Screen("splash")
    data object Home         : Screen("home")
    data object SurahDetail  : Screen("surah_detail/{surahNumber}") {
        fun createRoute(surahNumber: Int) = "surah_detail/$surahNumber"
    }
    data object MapPicker    : Screen("map_picker")
}

// ── Bottom nav tabs ───────────────────────────────────────────────────────────
sealed class BottomNavTab(
    val route: String,
    val label: String,
    val iconRes: String
) {
    data object Azan         : BottomNavTab("azan",          "Prayer",  "ic_time")
    data object Quran        : BottomNavTab("quran",         "Quran",   "ic_alquran")
    data object Qiblah       : BottomNavTab("qiblah",        "Qibla",   "ic_qiblah")
    data object MosqueFinder : BottomNavTab("mosque_finder", "Mosque",  "ic_mosque_finder")
}

val bottomNavTabs = listOf(
    BottomNavTab.Azan,
    BottomNavTab.Quran,
    BottomNavTab.Qiblah,
    BottomNavTab.MosqueFinder,
)

fun NavHostController.navigateTab(route: String) {
    navigate(route) {
        popUpTo(graph.findStartDestination().id) { saveState = true }
        launchSingleTop = true
        restoreState    = true
    }
}