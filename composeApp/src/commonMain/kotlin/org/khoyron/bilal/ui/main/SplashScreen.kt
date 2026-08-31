package org.khoyron.bilal.ui.main

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.ic_mosque
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// Warna utama dari desain splash screen
private val SplashBackground = Color(0xFF6B9070)   // Sage green
private val SplashCircleBg   = Color(0xFF7FA082)   // Lingkaran lebih terang
private val White             = Color.White
private val WhiteSubtle       = White.copy(alpha = 0.75f)

/**
 * Splash screen untuk Bilal Azan – Spiritual Tranquility.
 */
@Composable
fun SplashScreen(
    viewModel: SplashViewModel = koinViewModel(),
    onSplashFinished: () -> Unit
) {
    val isReady by viewModel.isReady.collectAsState()
    val error by viewModel.error.collectAsState()

    SplashScreenContent(
        isReady = isReady,
        error = error,
        onOkClicked = { viewModel.onOkClicked() },
        onSplashFinished = onSplashFinished
    )
}

@Composable
fun SplashScreenContent(
    isReady: Boolean,
    error: SplashError?,
    onOkClicked: () -> Unit,
    onSplashFinished: () -> Unit
) {
    // ── Animatable values ──────────────────────────────────────────────────
    val logoAlpha  = remember { Animatable(0f) }
    val logoScale  = remember { Animatable(0.7f) }
    val textAlpha  = remember { Animatable(0f) }
    val textOffset = remember { Animatable(20f) }   // offset Y (dp) ke atas

    // Status animasi minimal selesai (3 detik)
    val isAnimationFinished = remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // Logo muncul + sedikit scale-up
        launch {
            logoAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }
        launch {
            logoScale.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 700, easing = FastOutSlowInEasing)
            )
        }

        // Teks muncul sedikit terlambat
        delay(400)
        launch {
            textAlpha.animateTo(
                targetValue = 1f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }
        launch {
            textOffset.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
            )
        }

        // Tunggu minimal 3 detik untuk animasi
        delay(3000)
        isAnimationFinished.value = true
    }

    // Navigasi dilakukan HANYA jika data Ready DAN animasi sudah 3 detik
    LaunchedEffect(isReady, isAnimationFinished.value) {
        if (isReady && isAnimationFinished.value) {
            onSplashFinished()
        }
    }

    // ── UI ────────────────────────────────────────────────────────────────
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(SplashBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {

            // ── Lingkaran dengan ikon masjid ──────────────────────────
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .alpha(logoAlpha.value)
                    .scale(logoScale.value)
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(SplashCircleBg)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_mosque),
                    contentDescription = "Masjid",
                    tint = White,
                    modifier = Modifier.size(72.dp)
                )
            }

            Spacer(modifier = Modifier.height(32.dp))

            // ── Nama aplikasi ─────────────────────────────────────────
            Text(
                text = "Bilal Azan",
                color = White,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    // translasi vertikal manual lewat padding atas
                    .padding(top = textOffset.value.dp)
            )

            Spacer(modifier = Modifier.height(6.dp))

            // ── Tagline ───────────────────────────────────────────────
            Text(
                text = "Towards a peaceful life",
                color = WhiteSubtle,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 3.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .alpha(textAlpha.value)
                    .padding(top = textOffset.value.dp)
            )
        }

        // ── Footer "Powered by Bilal" + indicator ─────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 48.dp)
                .alpha(textAlpha.value)
        ) {
            // Indicator bar (dua segmen seperti di desain)
            Box(
                modifier = Modifier
                    .width(56.dp)
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(White.copy(alpha = 0.5f))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Powered by Khoyron",
                color = WhiteSubtle,
                fontSize = 13.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )
        }

        // ── Error Dialog ──────────────────────────────────────────────
        if (error != null) {
            AlertDialog(
                onDismissRequest = { /* Prevent dismiss on outside click */ },
                title = { 
                    Text(text = if (error == SplashError.NoInternet) "No Connection" else "System Error") 
                },
                text = { 
                    Text(
                        text = if (error == SplashError.NoInternet) 
                            "Please check your internet connection and try again." 
                        else 
                            "We couldn't fetch some data, but you can still continue."
                    )
                },
                confirmButton = {
                    Button(onClick = onOkClicked) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Preview
@Composable
fun PreviewSplashScreen(){
    SplashScreenContent(
        isReady = true,
        error = null,
        onOkClicked = {},
        onSplashFinished = {}
    )
}
