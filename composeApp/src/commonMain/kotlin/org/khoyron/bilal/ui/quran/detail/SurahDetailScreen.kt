package org.khoyron.bilal.ui.quran.detail

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.ic_play
import bilal.composeapp.generated.resources.ic_play_audio
import bilal.composeapp.generated.resources.ic_save
import bilal.composeapp.generated.resources.ic_share
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel

// ── Warna tema ────────────────────────────────────────────────────────────────
private val Green        = Color(0xFF2D6A4F)
private val GreenDark    = Color(0xFF1B4332)
private val GreenMid     = Color(0xFF40916C)
private val GreenLight   = Color(0xFFD8F3DC)
private val GreenBadge   = Color(0xFFE8F5E9)
private val BgPage       = Color(0xFFF8F9F8)
private val White        = Color.White
private val TextDark     = Color(0xFF1A1A1A)
private val TextMid      = Color(0xFF6B7280)
private val IconGray     = Color(0xFF9CA3AF)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun SurahDetailScreen(
    surahNumber: Int = 1,
    onBack: () -> Unit = {},
    viewModel: SurahDetailViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPage)
        ) {
            // Top bar
            DetailTopBar(
                title = uiState.surah?.nameLatn ?: "Al-Quran",
                onBack = onBack,
                onSearch = {}
            )

            LazyColumn(modifier = Modifier.fillMaxSize()) {

                // ── Banner ────────────────────────────────────────────────
                item {
                    if (uiState.isLoading) {
                        ShimmerDetailBanner(brush = shimmerBrush())
                    } else {
                        uiState.surah?.let { surah ->
                            SurahDetailBanner(
                                nameLatn    = surah.nameLatn,
                                translation = surah.translation,
                                totalAyah   = surah.totalAyah,
                                isPlaying   = uiState.isPlaying,
                                onPlayAudio = { viewModel.togglePlayAudio() }
                            )
                        }
                    }
                    Spacer(Modifier.height(16.dp))
                }

                // ── Ayat list ─────────────────────────────────────────────
                if (uiState.isLoading) {
                    items(5) {
                        ShimmerAyahItem(brush = shimmerBrush())
                        Spacer(Modifier.height(10.dp))
                    }
                } else {
                    uiState.surah?.let { surah ->
                        items(surah.ayahs, key = { it.ayahNumber }) { ayah ->
                            AyahItem(
                                ayah       = ayah,
                                onPlay     = { viewModel.onPlayAyah(ayah) },
                                onBookmark = { viewModel.toggleBookmark(ayah.ayahNumber) },
                                onShare    = { viewModel.onShareAyah(ayah) }
                            )
                            Spacer(Modifier.height(10.dp))
                        }
                    }
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Shimmer brush ─────────────────────────────────────────────────────────────

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val x by transition.animateFloat(
        initialValue = 0f,
        targetValue  = 1200f,
        animationSpec = infiniteRepeatable(
            animation  = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_x"
    )
    return Brush.linearGradient(
        colors = listOf(Color(0xFFE2E8E4), Color(0xFFF0F4F1), Color(0xFFE2E8E4)),
        start  = Offset(x - 300f, 0f),
        end    = Offset(x, 0f)
    )
}

// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
fun DetailTopBar(
    title: String,
    onBack: () -> Unit,
    onSearch: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Tombol back
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onBack() },
            contentAlignment = Alignment.Center
        ) {
            Text(text = "←", fontSize = 20.sp, color = TextDark)
        }

        Text(
            text       = title,
            fontSize   = 18.sp,
            fontWeight = FontWeight.Bold,
            color      = TextDark
        )

        // Icon search
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable { onSearch() },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter           = painterResource(Res.drawable.ic_share),
                contentDescription = "Search",
                modifier          = Modifier.size(22.dp)
            )
        }
    }
}

// ── Banner ────────────────────────────────────────────────────────────────────

@Composable
fun SurahDetailBanner(
    nameLatn: String,
    translation: String,
    totalAyah: Int,
    isPlaying: Boolean,
    onPlayAudio: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(Color(0xFF3A7D57), Color(0xFF2D6A4F), Color(0xFF1B4332)),
                    start  = Offset(0f, 0f),
                    end    = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative circles
        Box(
            modifier = Modifier
                .size(200.dp)
                .align(Alignment.TopEnd)
                .offset(x = 60.dp, y = (-40).dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.05f))
        )
        Box(
            modifier = Modifier
                .size(140.dp)
                .align(Alignment.BottomStart)
                .offset(x = (-40).dp, y = 40.dp)
                .clip(CircleShape)
                .background(White.copy(alpha = 0.04f))
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Nama surah
            Text(
                text       = nameLatn,
                fontSize   = 28.sp,
                fontWeight = FontWeight.Bold,
                color      = White
            )

            Spacer(Modifier.height(4.dp))

            // Terjemahan + jumlah ayat
            Text(
                text      = "${translation.uppercase()} • $totalAyah AYAHS",
                fontSize  = 12.sp,
                color     = White.copy(alpha = 0.75f),
                letterSpacing = 1.sp
            )

            Spacer(Modifier.height(20.dp))

            // Kotak putih placeholder kaligrafi
            Box(
                modifier = Modifier
                    .size(width = 120.dp, height = 80.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(White),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text      = "﷽",
                    fontSize  = 28.sp,
                    color     = GreenDark,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(20.dp))

            // Tombol Play Audio
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(50.dp))
                    .background(White.copy(alpha = 0.18f))
                    .clickable { onPlayAudio() }
                    .padding(horizontal = 20.dp, vertical = 10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Image(
                        painter           = painterResource(Res.drawable.ic_play_audio),
                        contentDescription = "Play",
                        modifier          = Modifier.size(16.dp)
                    )
                    Text(
                        text       = if (isPlaying) "Pause Audio" else "Play Audio",
                        fontSize   = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = White
                    )
                }
            }
        }
    }
}

// ── Ayah Item ─────────────────────────────────────────────────────────────────

@Composable
fun AyahItem(
    ayah: AyahUi,
    onPlay: () -> Unit,
    onBookmark: () -> Unit,
    onShare: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape  = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // ── Baris atas: badge nomor + action icons ────────────────
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Badge nomor ayat "1:1"
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(GreenBadge)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text       = "${ayah.surahNumber}:${ayah.ayahNumber}",
                        fontSize   = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color      = GreenMid
                    )
                }

                // Action icons
                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AyahActionIcon(
                        iconRes     = Res.drawable.ic_play,
                        description = "Play",
                        onClick     = onPlay
                    )
                    AyahActionIcon(
                        iconRes     = Res.drawable.ic_save,
                        description = "Bookmark",
                        tint        = if (ayah.isBookmarked) Green else IconGray,
                        onClick     = onBookmark
                    )
                    AyahActionIcon(
                        iconRes     = Res.drawable.ic_share,
                        description = "Share",
                        onClick     = onShare
                    )
                }
            }

            Spacer(Modifier.height(16.dp))

            // ── Teks Arab ─────────────────────────────────────────────
            Text(
                text      = ayah.arabic,
                fontSize  = 26.sp,
                color     = TextDark,
                textAlign = TextAlign.End,
                lineHeight = 44.sp,
                modifier  = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(12.dp))

            // ── Terjemahan ────────────────────────────────────────────
            Text(
                text       = ayah.translation,
                fontSize   = 14.sp,
                color      = TextMid,
                lineHeight = 22.sp
            )
        }
    }
}

@Composable
fun AyahActionIcon(
    iconRes: org.jetbrains.compose.resources.DrawableResource,
    description: String,
    tint: Color = IconGray,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter            = painterResource(iconRes),
            contentDescription = description,
            modifier           = Modifier.size(20.dp),
            colorFilter        = androidx.compose.ui.graphics.ColorFilter.tint(tint)
        )
    }
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

@Composable
fun ShimmerDetailBanner(brush: Brush) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(260.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
    )
}

@Composable
fun ShimmerAyahItem(brush: Brush) {
    Card(
        modifier  = Modifier.padding(horizontal = 16.dp).fillMaxWidth(),
        shape     = RoundedCornerShape(16.dp),
        colors    = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Badge + icons shimmer
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(48.dp).height(24.dp)
                        .clip(RoundedCornerShape(20.dp))
                        .background(brush)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(3) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(brush)
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Arab text shimmer (kanan ke kiri)
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .height(28.dp)
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            Spacer(Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.55f)
                    .height(24.dp)
                    .align(Alignment.End)
                    .clip(RoundedCornerShape(8.dp))
                    .background(brush)
            )

            Spacer(Modifier.height(14.dp))

            // Translation shimmer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )
            Spacer(Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(7.dp))
                    .background(brush)
            )
        }
    }
}

// Helper extension untuk offset di Box
private fun Modifier.offset(x: androidx.compose.ui.unit.Dp = 0.dp, y: androidx.compose.ui.unit.Dp = 0.dp): Modifier =
    this.padding(
        start = if (x > 0.dp) x else 0.dp,
        top   = if (y > 0.dp) y else 0.dp
    )