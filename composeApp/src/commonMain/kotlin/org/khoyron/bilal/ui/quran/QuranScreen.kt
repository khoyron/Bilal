package org.khoyron.bilal.ui.quran

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.amiri_quran
import bilal.composeapp.generated.resources.ic_alquran_white
import bilal.composeapp.generated.resources.ic_search
import bilal.composeapp.generated.resources.img_mosque
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import org.khoyron.bilal.navigation.SurahDetail
import org.khoyron.bilal.navigation.JuzDetail

// ── Warna tema ────────────────────────────────────────────────────────────────
private val Green         = Color(0xFF2D6A4F)
private val GreenDark     = Color(0xFF1B4332)
private val GreenMid      = Color(0xFF40916C)
private val GreenLight    = Color(0xFFD8F3DC)
private val GreenBanner1  = Color(0xFF2D6A4F)
private val GreenBanner2  = Color(0xFF1B4332)
private val BgPage        = Color(0xFFF8F9F8)
private val White         = Color.White
private val TextDark      = Color(0xFF1A1A1A)
private val TextMid       = Color(0xFF6B7280)
private val StarInactive  = Color(0xFFCBD5E0)
private val StarActive    = Color(0xFF2D6A4F)

@Composable
fun getAmiriFontFamily() = FontFamily(
    Font(Res.font.amiri_quran, FontWeight.Normal)
)

// ── Screen ────────────────────────────────────────────────────────────────────

@Composable
fun QuranScreen(
    navController: NavHostController,
    viewModel: QuranViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    QuranContent(
        uiState = uiState,
        onTabSelected = { viewModel.selectTab(it) },
        onFavoriteToggle = { viewModel.toggleFavorite(it) },
        onSurahClick = { surah ->
            navController.navigate(SurahDetail(surahNumber = surah.number))
        },
        onJuzClick = { juz ->
            navController.navigate(JuzDetail(juzNumber = juz.number))
        },
        onLastReadClick = { lastRead ->
            navController.navigate(SurahDetail(surahNumber = lastRead.surahNumber))
        }
    )
}

@Composable
fun QuranContent(
    uiState: QuranUiState,
    onTabSelected: (QuranTab) -> Unit,
    onFavoriteToggle: (Int) -> Unit,
    onSurahClick: (SurahUi) -> Unit,
    onJuzClick: (JuzUi) -> Unit,
    onLastReadClick: (LastReadUi) -> Unit
) {
    MaterialTheme {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(BgPage)
        ) {
            // ── Top Bar ───────────────────────────────────────────────────
            QuranTopBar()

            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                // ── Banner Last Read ──────────────────────────────────────
                item {
                    if (uiState.isLoading) {
                        ShimmerBanner(brush = shimmerBrush())
                    } else {
                        uiState.lastRead?.let { lastRead ->
                            LastReadBanner(
                                surahName  = lastRead.surahName,
                                ayahNumber = lastRead.ayahNumber,
                                onContinue = { onLastReadClick(lastRead) }
                            )
                        }
                    }
                    Spacer(Modifier.height(20.dp))
                }

                // ── Tab Selector ──────────────────────────────────────────
                item {
                    QuranTabRow(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = onTabSelected
                    )
                    Spacer(Modifier.height(12.dp))
                }

                // ── List ──────────────────────────────────────────────────
                if (uiState.isLoading) {
                    items(8) {
                        ShimmerSurahItem(brush = shimmerBrush())
                        Spacer(Modifier.height(8.dp))
                    }
                } else {
                    when (uiState.selectedTab) {
                        QuranTab.SURAH -> {
                            items(uiState.surahList, key = { it.number }) { surah ->
                                SurahItem(
                                    surah = surah,
                                    onFavoriteToggle = { onFavoriteToggle(surah.number) },
                                    onClick = { onSurahClick(surah) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                        QuranTab.JUZ -> {
                            items(uiState.juzList, key = { it.number }) { juz ->
                                JuzItem(
                                    juz = juz,
                                    onClick = { onJuzClick(juz) }
                                )
                                Spacer(Modifier.height(8.dp))
                            }
                        }
                    }
                    // Bottom padding
                    item { Spacer(Modifier.height(16.dp)) }
                }
            }
        }
    }
}

// ── Shimmer brush helper ──────────────────────────────────────────────────────

@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1200f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )
    return Brush.linearGradient(
        colors = listOf(Color(0xFFE2E8E4), Color(0xFFF0F4F1), Color(0xFFE2E8E4)),
        start = Offset(translateAnim - 300f, 0f),
        end = Offset(translateAnim, 0f)
    )
}


// ── Top Bar ───────────────────────────────────────────────────────────────────

@Composable
fun QuranTopBar() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Spacer(Modifier.size(40.dp)) // placeholder kiri biar judul center

        Text(
            text = "Al-Quran",
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        // Icon search
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .clickable {

                },
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }

    }
}

// ── Banner Last Read ──────────────────────────────────────────────────────────

@Composable
fun LastReadBanner(
    surahName: String,
    ayahNumber: Int,
    onContinue: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(GreenBanner1, GreenBanner2),
                    start = Offset(0f, 0f),
                    end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
                )
            )
    ) {
        // Decorative circle background kanan (pengganti gambar masjid)
        Box(
            modifier = Modifier
                .size(160.dp)
                .align(Alignment.CenterEnd)
        ){
            Image(
                painter = painterResource(Res.drawable.img_mosque),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }


        Column(
            modifier = Modifier
                .padding(start = 20.dp, top = 16.dp, bottom = 16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Label "LAST READ"
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.ic_alquran_white),
                    contentDescription = null,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "LAST READ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    letterSpacing = 1.5.sp,
                    color = White.copy(alpha = 0.8f)
                )
            }

            // Nama surah + ayah
            Column {
                Text(
                    text = surahName,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = White
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "Ayah No: $ayahNumber",
                    fontSize = 14.sp,
                    color = White.copy(alpha = 0.85f)
                )
            }

            // Tombol continue
            Button(
                onClick = onContinue,
                colors = ButtonDefaults.buttonColors(
                    containerColor = White,
                    contentColor = GreenDark
                ),
                shape = RoundedCornerShape(50.dp),
                modifier = Modifier.height(36.dp)
            ) {
                Text(
                    text = "Continue Reading",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

// ── Tab Row ───────────────────────────────────────────────────────────────────

@Composable
fun QuranTabRow(
    selectedTab: QuranTab,
    onTabSelected: (QuranTab) -> Unit
) {
    Row(
        modifier = Modifier.padding(horizontal = 20.dp)
    ) {
        QuranTab.entries.forEach { tab ->
            val isSelected = tab == selectedTab
            val label = when (tab) {
                QuranTab.SURAH -> "Surah"
                QuranTab.JUZ   -> "Juz"
            }
            Column(
                modifier = Modifier
                    .clickable { onTabSelected(tab) }
                    .padding(end = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = label,
                    fontSize = 15.sp,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Green else TextMid
                )
                Spacer(Modifier.height(4.dp))
                // Underline
                Box(
                    modifier = Modifier
                        .width(if (isSelected) 32.dp else 0.dp)
                        .height(2.dp)
                        .clip(RoundedCornerShape(1.dp))
                        .background(if (isSelected) Green else Color.Transparent)
                )
            }
        }
    }
}

// ── Surah Item ────────────────────────────────────────────────────────────────

@Composable
fun SurahItem(
    surah: SurahUi,
    onFavoriteToggle: () -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenLight)
            ) {
                Text(
                    text = surah.number.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
            }

            Spacer(Modifier.width(14.dp))

            // Nama & info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = surah.nameLatn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${surah.translation} • ${surah.totalAyah} VERSES",
                    fontSize = 11.sp,
                    color = TextMid,
                    letterSpacing = 0.3.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Nama Arab
            Text(
                text = surah.nameArabic,
                fontSize = 20.sp,
                fontFamily = getAmiriFontFamily(),
                color = GreenMid,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.End
            )
        }
    }
}

@Composable
fun SurahNumberBadge(
    number: Int,
    isFavorite: Boolean
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(40.dp)
    ) {
        // Star shape menggunakan Box berlayer
        Text(
            text = "★",
            fontSize = 36.sp,
            color = if (isFavorite) StarActive else StarInactive,
            lineHeight = 36.sp
        )
        Text(
            text = number.toString(),
            fontSize = if (number < 10) 11.sp else 9.sp,
            fontWeight = FontWeight.Bold,
            color = if (isFavorite) White else TextMid,
            textAlign = TextAlign.Center
        )
    }
}

// ── Juz Item ──────────────────────────────────────────────────────────────────

@Composable
fun JuzItem(
    juz: JuzUi,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Nomor juz
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(GreenLight)
            ) {
                Text(
                    text = juz.number.toString(),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Green
                )
            }

            Spacer(Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = juz.nameLatn,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = TextDark
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = "${juz.startSurah} — ${juz.endSurah}",
                    fontSize = 11.sp,
                    color = TextMid,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Text(
                text = "${juz.totalAyah} ayah",
                fontSize = 12.sp,
                color = GreenMid,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

@Composable
fun ShimmerBanner(brush: Brush) {
    Box(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(brush)
    )
}

@Composable
fun ShimmerSurahItem(brush: Brush) {
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Shimmer star badge
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(brush)
            )
            Spacer(Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.45f)
                        .height(14.dp)
                        .clip(RoundedCornerShape(7.dp))
                        .background(brush)
                )
                Spacer(Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.65f)
                        .height(10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(brush)
                )
            }
            Box(
                modifier = Modifier
                    .width(48.dp)
                    .height(18.dp)
                    .clip(RoundedCornerShape(9.dp))
                    .background(brush)
            )
        }
    }
}

@Preview
@Composable
fun QuranPreview() {
    val dummySurahList = listOf(
        SurahUi(1, "Al-Fatihah", "الفاتحة", "THE OPENING", 7),
        SurahUi(2, "Al-Baqarah", "البقرة", "THE COW", 286),
        SurahUi(3, "Ali 'Imran", "آل عمران", "FAMILY OF IMRAN", 200)
    )
    val dummyJuzList = listOf(
        JuzUi(1, "Juz' 1", "Al-Fatihah 1", "Al-Baqarah 141", 148),
        JuzUi(2, "Juz' 2", "Al-Baqarah 142", "Al-Baqarah 252", 111)
    )
    QuranContent(
        uiState = QuranUiState(
            isLoading = false,
            surahList = dummySurahList,
            juzList = dummyJuzList,
            lastRead = LastReadUi("Al-Baqarah", 2, 142)
        ),
        onTabSelected = {},
        onFavoriteToggle = {},
        onSurahClick = {},
        onJuzClick = {},
        onLastReadClick = {}
    )
}
