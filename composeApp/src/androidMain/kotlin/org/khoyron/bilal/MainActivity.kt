package org.khoyron.bilal

import android.app.Application
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.khoyron.bilal.di.appModule
import org.khoyron.bilal.ui.main.App
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin
class BilalApp : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            androidContext(this@BilalApp)
            modules(appModule)
        }
    }
}

// MainActivity jadi bersih
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

