package org.khoyron.bilal

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import org.khoyron.bilal.di.appModule
import org.khoyron.bilal.ui.App
import org.koin.core.context.GlobalContext.startKoin

fun main() {
    startKoin {
        modules(appModule)
    }

    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Bilal",
        ) {
            App()
        }
    }
}