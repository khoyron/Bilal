package org.khoyron.bilal.ui.qiblah

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.ic_current_location
import bilal.composeapp.generated.resources.ic_get_current_position
import bilal.composeapp.generated.resources.the_pointer_qiblah
import org.jetbrains.compose.resources.painterResource
import androidx.compose.runtime.DisposableEffect
import org.khoyron.bilal.util.RequestLocationPermission
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

// ── Warna tema ────────────────────────────────────────────────────────────────
private val Green         = Color(0xFF2D6A4F)
private val GreenIconBg   = Color(0xFFE8F0E9)
private val BgPage        = Color(0xFFF2F3F5)
private val BgCard        = Color(0xFFFFFFFF)
private val BgInstruction = Color(0xFFE8EAF0)
private val TextDark      = Color(0xFF1A1A1A)
private val TextMid       = Color(0xFF6B7280)
private val White         = Color.White


@Preview
@Composable
fun QiblahPreview() {
    QiblahContent(
        uiState = QiblahUiState(
            locationName = "Makkah, Saudi Arabia",
            qiblahAngle = 100f,
            deviceBearing = 45f,
            directionDescription = "Your device is currently facing North-East towards the Holy Kaaba.",
            rotationInstruction = "Rotate the phone 55° to the left"
        ),
        onRefresh = {}
    )
}

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun QiblahScreen(
    viewModel: QiblahViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // ── Request permission & start sensor ─────────────────────────────────
    RequestLocationPermission(
        onGranted = { viewModel.startSensors() },
        onDenied = { /* Handle denied */ }
    )

    // ── Stop sensor saat screen keluar dari komposisi ─────────────────────
    DisposableEffect(Unit) {
        onDispose { viewModel.stopSensors() }
    }

    QiblahContent(
        uiState = uiState,
        onRefresh = { viewModel.refreshLocation() }
    )
}

@Composable
fun QiblahContent(
    uiState: QiblahUiState,
    onRefresh: () -> Unit
) {
    // Logic to prevent 360-degree jump when passing North
    var continuousBearing by remember { mutableFloatStateOf(uiState.deviceBearing) }
    var continuousQiblah by remember { mutableFloatStateOf(uiState.qiblahAngle) }

    // Revert to original remember(key) pattern as preferred by user
    remember(uiState.deviceBearing) {
        val diff = (uiState.deviceBearing - (continuousBearing % 360f) + 540f) % 360f - 180f
        continuousBearing += diff
    }

    remember(uiState.qiblahAngle) {
        val diff = (uiState.qiblahAngle - (continuousQiblah % 360f) + 540f) % 360f - 180f
        continuousQiblah += diff
    }

    // Animasi bearing device (untuk rotasi kompas N/S/E/W)
    val animatedBearing by animateFloatAsState(
        targetValue   = continuousBearing,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label         = "bearing_rotation"
    )

    // Animasi pointer qiblah
    val animatedQiblahAngle by animateFloatAsState(
        targetValue   = continuousQiblah,
        animationSpec = tween(durationMillis = 150, easing = FastOutSlowInEasing),
        label         = "qiblah_rotation"
    )

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            QiblahTopBar(onGetLocation = onRefresh)

            Spacer(Modifier.height(20.dp))

            LocationCard(
                locationName = uiState.locationName,
                modifier     = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(Modifier.height(32.dp))

            QiblahCompass(
                qiblahAngle   = animatedQiblahAngle,
                deviceBearing = animatedBearing
            )

            Spacer(Modifier.height(32.dp))

            Text(
                text       = "Device's angle to qibla",
                fontSize   = 22.sp,
                fontWeight = FontWeight.Bold,
                color      = TextDark,
                textAlign  = TextAlign.Center,
                modifier   = Modifier.padding(horizontal = 32.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text       = uiState.directionDescription,
                fontSize   = 14.sp,
                color      = TextMid,
                textAlign  = TextAlign.Center,
                lineHeight = 22.sp,
                modifier   = Modifier.padding(horizontal = 40.dp)
            )

            Spacer(Modifier.height(24.dp))

            InstructionBar(
                instruction = uiState.rotationInstruction,
                modifier    = Modifier.padding(horizontal = 16.dp)
            )
        }
    }
}

// ── Compass ───────────────────────────────────────────────────────────────────

@Composable
fun QiblahCompass(
    qiblahAngle: Float,
    deviceBearing: Float,
    modifier: Modifier = Modifier
) {
    val compassSize = 300.dp
    val compassBg   = Color(0xFFF0F1F3)

    // Kompas rotate berlawanan dengan device → N selalu menunjuk Utara bumi
    val compassRotation = -deviceBearing

    // Pointer rotate: qiblahAngle dikurangi deviceBearing
    val pointerRotation = (qiblahAngle - deviceBearing)

    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier.size(compassSize)
    ) {
        // ── Layer 1: Background + N/S/E/W BERPUTAR mengikuti kompas ──────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(compassSize)
                .graphicsLayer { rotationZ = compassRotation }
        ) {
            // Background lingkaran abu
            Box(
                modifier = Modifier
                    .size(compassSize)
                    .clip(CircleShape)
                    .background(compassBg)
            )

            // Label N - atas
            Text(
                text       = "N",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Bold,
                color      = Color(0xFFE53935), // merah untuk N
                modifier   = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = 10.dp)
            )
            // S - bawah
            Text(
                text       = "S",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = TextMid,
                modifier   = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 10.dp)
            )
            // W - kiri
            Text(
                text       = "W",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = TextMid,
                modifier   = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
            )
            // E - kanan
            Text(
                text       = "E",
                fontSize   = 13.sp,
                fontWeight = FontWeight.Medium,
                color      = TextMid,
                modifier   = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 10.dp)
            )
        }

        // ── Layer 2: Ring putih tengah TIDAK berputar ─────────────────────
        Box(
            modifier = Modifier
                .size(compassSize * 0.5f)
                .clip(CircleShape)
                .background(White)
        )

        // ── Layer 3: Pointer LOCKED ke Qibla ─────────────────────────────
        Image(
            painter            = painterResource(Res.drawable.the_pointer_qiblah),
            contentDescription = "Qiblah Pointer",
            modifier           = Modifier
                .size(compassSize)
                .graphicsLayer { rotationZ = pointerRotation }
        )

        // ── Layer 4: Derajat di tengah TIDAK berputar ────────────────────
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(compassSize * 0.38f)
                .clip(CircleShape)
                .background(White)
        ) {
            // Calculate shortest diff between qiblah and device bearing
            var diff = qiblahAngle - deviceBearing
            while (diff < -180) diff += 360
            while (diff > 180) diff -= 360
            val displayAngle = diff.toInt().let { if (it < 0) -it else it }

            Text(
                text       = "$displayAngle°",
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = Green,
                textAlign  = TextAlign.Center
            )
        }
    }
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
fun QiblahTopBar(onGetLocation: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
        verticalAlignment     = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Spacer(Modifier.size(40.dp))

        Text(
            text       = "Qibla",
            fontSize   = 20.sp,
            fontWeight = FontWeight.Bold,
            color      = Green
        )

        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onGetLocation() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter            = painterResource(Res.drawable.ic_get_current_position),
                contentDescription = "Get Location",
                modifier           = Modifier.size(26.dp)
            )
        }
    }
}

// ── Location Card ─────────────────────────────────────────────────────────────

@Composable
fun LocationCard(
    locationName: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(BgPage)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(GreenIconBg),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter            = painterResource(Res.drawable.ic_current_location),
                    contentDescription = "Location",
                    modifier           = Modifier.size(22.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            Column {
                Text(
                    text       = "Current Location",
                    fontSize   = 12.sp,
                    color      = TextMid,
                    fontWeight = FontWeight.Normal
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text       = locationName,
                    fontSize   = 16.sp,
                    color      = TextDark,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Instruction Bar ───────────────────────────────────────────────────────────

@Composable
fun InstructionBar(
    instruction: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(50.dp))
            .background(BgInstruction)
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(
            verticalAlignment     = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier              = Modifier.fillMaxWidth()
        ) {
            Text(text = "🔄", fontSize = 18.sp)
            Spacer(Modifier.width(10.dp))
            Text(
                text       = instruction,
                fontSize   = 14.sp,
                fontWeight = FontWeight.Medium,
                color      = Green
            )
        }
    }
}