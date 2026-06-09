package org.khoyron.bilal.ui.main

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Density
import org.khoyron.bilal.navigation.NavGraph

@Composable
fun App() {
    val density = LocalDensity.current
    val fixedDensity = Density(
        density = density.density,
        fontScale = 1f
    )

    CompositionLocalProvider(LocalDensity provides fixedDensity) {
        NavGraph()
    }
}

