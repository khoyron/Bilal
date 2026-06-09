package org.khoyron.bilal.ui.home

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.ic_time
import bilal.composeapp.generated.resources.ic_alquran
import bilal.composeapp.generated.resources.ic_qiblah
import bilal.composeapp.generated.resources.ic_mosque_finder
import org.jetbrains.compose.resources.painterResource
import org.khoyron.bilal.navigation.BottomNavTab
import org.khoyron.bilal.navigation.bottomNavTabs
import org.khoyron.bilal.navigation.navigateTab
import org.khoyron.bilal.ui.azan.AzanScreen
import org.khoyron.bilal.ui.quran.QuranScreen
import org.khoyron.bilal.ui.qiblah.QiblahScreen
import org.khoyron.bilal.ui.mosquefinder.MosqueFinderScreen

private val ColorPrimary  = Color(0xFF2D6A4F)
private val ColorInactive = Color(0xFF9E9E9E)
private val ColorNavBg    = Color(0xFFFFFFFF)

@Composable
fun HomeScreen(
    // navController dari NavGraph utama — untuk navigasi ke SurahDetail, dll
    navController: NavHostController
) {
    // navController khusus untuk bottom nav tabs
    val bottomNavController = rememberNavController()
    val navBackStackEntry   by bottomNavController.currentBackStackEntryAsState()
    val currentDestination  = navBackStackEntry?.destination

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = ColorNavBg,
                tonalElevation = androidx.compose.ui.unit.Dp(4f)
            ) {
                bottomNavTabs.forEach { tab ->
                    val selected = currentDestination
                        ?.hierarchy
                        ?.any { it.route == tab.route } == true

                    NavigationBarItem(
                        selected = selected,
                        onClick  = { bottomNavController.navigateTab(tab.route) },
                        icon = {
                            Icon(
                                painter = painterResource(
                                    when (tab) {
                                        BottomNavTab.Azan         -> Res.drawable.ic_time
                                        BottomNavTab.Quran        -> Res.drawable.ic_alquran
                                        BottomNavTab.Qiblah       -> Res.drawable.ic_qiblah
                                        BottomNavTab.MosqueFinder -> Res.drawable.ic_mosque_finder
                                    }
                                ),
                                contentDescription = tab.label
                            )
                        },
                        label = {
                            Text(
                                text       = tab.label,
                                fontSize   = 11.sp,
                                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor   = ColorPrimary,
                            selectedTextColor   = ColorPrimary,
                            unselectedIconColor = ColorInactive,
                            unselectedTextColor = ColorInactive,
                            indicatorColor      = ColorPrimary.copy(alpha = 0.12f)
                        )
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController    = bottomNavController,
            startDestination = BottomNavTab.Azan.route,
            modifier         = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            composable(BottomNavTab.Azan.route) {
                AzanScreen(mainNavController = navController)
            }
            composable(BottomNavTab.Quran.route) {
                // Teruskan navController utama ke QuranScreen
                // agar bisa navigate ke SurahDetail
                QuranScreen(navController = navController)
            }
            composable(BottomNavTab.Qiblah.route) {
                QiblahScreen()
            }
            composable(BottomNavTab.MosqueFinder.route) {
                MosqueFinderScreen()
            }
        }
    }
}