package org.khoyron.bilal

import androidx.compose.ui.window.ComposeUIViewController
import org.khoyron.bilal.di.appModule
import org.khoyron.bilal.ui.main.App
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin

fun MainViewController() = ComposeUIViewController {
    App()
}

fun initKoin() {
    stopKoin()
    startKoin {
        modules(appModule)
    }
}