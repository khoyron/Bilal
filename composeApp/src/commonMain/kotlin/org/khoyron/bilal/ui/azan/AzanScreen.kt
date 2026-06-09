package org.khoyron.bilal.ui.azan

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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.ic_bel
import bilal.composeapp.generated.resources.ic_checlist_green_bg
import bilal.composeapp.generated.resources.ic_half_moon
import bilal.composeapp.generated.resources.ic_picker_location
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.khoyron.bilal.model.PrayerTimeUi
import org.khoyron.bilal.navigation.Screen
import org.koin.compose.viewmodel.koinViewModel

// ── Warna tema ────────────────────────────────────────────────────────────────
private val Green        = Color(0xFF476B4E)
private val GreenDark    = Color(0xFF4B6D52)
private val GreenLight   = Color(0xFFECEEEB)
private val GreenBadge   = Color(0xFFE8ECE7)
private val TextDark     = Color(0xFF111417)
private val TextMid      = Color(0xFF4B4F4B)
private val TextGray     = Color.Gray
private val BorderGray   = Color(0xFFE1E3E4)
private val White        = Color.White

// ── Screen ────────────────────────────────────────────────────────────────────

@Preview
@Composable
fun AzanPreview() {
    AzanScreenContent(
        uiState = AzanUiState(
            isLoading = false,
            todayDate = "Tuesday, 25 June 2024",
            prayerList = listOf(
                PrayerTimeUi("FAJR", "04:32", isEnabled = true, isOnTime = false),
                PrayerTimeUi("DHUHR", "12:15", isEnabled = true, isOnTime = true),
                PrayerTimeUi("ASR", "15:42", isEnabled = false, isOnTime = false),
                PrayerTimeUi("MAGHRIB", "18:28", isEnabled = true, isOnTime = false),
                PrayerTimeUi("ISHA", "19:45", isEnabled = true, isOnTime = false)
            ),
            nextPrayer = PrayerTimeUi("ASR", "15:42"),
            countdown = "02:15:30",
            prayerProgress = 0.4f,
            alertEnabled = true,
            cityName = "Surabaya",
            countryName = "Indonesia"
        ),
        onTogglePrayer = { _, _ -> },
        onToggleAlert = { },
        onOpenMapPicker = { }
    )
}

@Preview
@Composable
fun AzanShimmerPreview() {
    AzanScreenContent(
        uiState = AzanUiState(isLoading = true),
        onTogglePrayer = { _, _ -> },
        onToggleAlert = { },
        onOpenMapPicker = { }
    )
}

@Composable
fun AzanScreen(
    mainNavController: NavController,
    viewModel: AzanViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    // Listen to results from MapPicker
    LaunchedEffect(Unit) {
        mainNavController.currentBackStackEntry
            ?.savedStateHandle
            ?.getStateFlow<String?>("location", null)
            ?.collect { locationStr ->
                locationStr?.let {
                    val parts = it.split(",")
                    if (parts.size == 2) {
                        val lat = parts[0].toDoubleOrNull()
                        val lon = parts[1].toDoubleOrNull()
                        if (lat != null && lon != null) {
                            viewModel.updateManualLocation(lat, lon)
                            // Clear the result after processing
                            mainNavController.currentBackStackEntry?.savedStateHandle?.remove<String>("location")
                        }
                    }
                }
            }
    }

    AzanScreenContent(
        uiState = uiState,
        onTogglePrayer = { name, enabled -> viewModel.togglePrayerSwitch(name, enabled) },
        onToggleAlert = { viewModel.toggleAlert(it) },
        onOpenMapPicker = {
            mainNavController.navigate(Screen.MapPicker.route)
        }
    )
}

@Composable
fun AzanScreenContent(
    uiState: AzanUiState,
    onTogglePrayer: (String, Boolean) -> Unit,
    onToggleAlert: (Boolean) -> Unit,
    onOpenMapPicker: () -> Unit
) {
    MaterialTheme {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(White),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            // Header selalu tampil
            item {
                AzanHeaderCard(
                    dateText = uiState.todayDate.ifEmpty { "Loading..." },
                    locationText = "${uiState.cityName}, ${uiState.countryName}",
                    onOpenMapPicker = onOpenMapPicker
                )
                Spacer(Modifier.height(20.dp))
            }

            item {
                if (uiState.isLoading) {
                    // ── Shimmer state ─────────────────────────────────────────
                    AzanShimmer()
                } else {
                    // ── Content state ─────────────────────────────────────────
                    Row(
                        modifier = Modifier.padding(
                            start = 16.dp,
                            end = 16.dp,
                            top = 26.dp
                        ),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Kiri: daftar waktu sholat
                        AzanCard(
                            prayers = uiState.prayerList,
                            onTogglePrayer = onTogglePrayer,
                            modifier = Modifier.weight(1f)
                        )

                        // Kanan: next prayer + alert
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Spacer(Modifier.height(90.dp))

                            NextPrayerCard(
                                nextPrayerName = uiState.nextPrayer?.name ?: "-",
                                remainingTime = uiState.countdown,
                                progress = uiState.prayerProgress
                            )

                            uiState.nextPrayer?.let { next ->
                                PrayerAlertCard(
                                    item = next,
                                    isEnabled = uiState.alertEnabled,
                                    onToggle = onToggleAlert
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// ── Shimmer ───────────────────────────────────────────────────────────────────

@Composable
fun AzanShimmer() {
    val shimmerColors = listOf(
        Color(0xFFE0E0E0),
        Color(0xFFF5F5F5),
        Color(0xFFE0E0E0)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer_translate"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 200f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Row(
        modifier = Modifier.padding(start = 16.dp, end = 16.dp, top = 26.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Shimmer kartu kiri (prayer list)
        ShimmerCard(
            modifier = Modifier.weight(1f),
            brush = brush,
            itemCount = 5
        )

        // Shimmer kartu kanan
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(Modifier.height(90.dp))

            // Next Prayer Shimmer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(brush = brush, modifier = Modifier.size(35.dp), cornerRadius = 50)
                        ShimmerBox(brush = brush, modifier = Modifier.width(80.dp).height(20.dp), cornerRadius = 50)
                    }
                    Spacer(Modifier.height(40.dp))
                    ShimmerBox(brush = brush, modifier = Modifier.width(100.dp).height(12.dp))
                    Spacer(Modifier.height(10.dp))
                    ShimmerBox(brush = brush, modifier = Modifier.width(120.dp).height(26.dp))
                    Spacer(Modifier.height(12.dp))
                }
            }

            // Prayer Alert Shimmer Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = White),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ShimmerBox(brush = brush, modifier = Modifier.size(40.dp), cornerRadius = 50)
                        ShimmerBox(brush = brush, modifier = Modifier.width(40.dp).height(24.dp), cornerRadius = 50)
                    }
                    Spacer(Modifier.height(40.dp))
                    ShimmerBox(brush = brush, modifier = Modifier.width(100.dp).height(12.dp))
                    Spacer(Modifier.height(10.dp))
                    ShimmerBox(brush = brush, modifier = Modifier.width(80.dp).height(26.dp))
                }
            }
        }
    }
}

@Composable
fun ShimmerCard(
    brush: Brush,
    itemCount: Int,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header shimmer
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(12.dp))
                ShimmerBox(brush = brush, modifier = Modifier.size(24.dp))
                Spacer(Modifier.width(8.dp))
                ShimmerBox(brush = brush, modifier = Modifier.width(60.dp).height(18.dp))
            }
            Spacer(Modifier.height(15.dp))
            repeat(itemCount) {
                ShimmerBox(
                    brush = brush,
                    modifier = Modifier.fillMaxWidth().height(60.dp),
                    cornerRadius = 14
                )
                if (it < itemCount - 1) Spacer(Modifier.height(7.dp))
            }
        }
    }
}

@Composable
fun ShimmerBox(
    brush: Brush,
    modifier: Modifier = Modifier,
    height: androidx.compose.ui.unit.Dp = 0.dp,
    cornerRadius: Int = 12
) {
    val finalModifier = if (height > 0.dp) modifier.height(height) else modifier
    Box(
        modifier = finalModifier
            .clip(RoundedCornerShape(cornerRadius.dp))
            .background(brush)
    )
}

// ── Header ────────────────────────────────────────────────────────────────────

@Composable
fun AzanHeaderCard(
    dateText: String,
    locationText: String,
    onOpenMapPicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Spacer(Modifier.height(60.dp))
            Text(
                text = "Bilal Azan",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.Default,
                color = Color(0xFF48694F)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = dateText,
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                fontFamily = FontFamily.SansSerif,
                color = TextGray
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.ic_picker_location),
                    contentDescription = null,
                    modifier = Modifier
                        .size(14.dp)
                        .clickable{
                            onOpenMapPicker()
                        }
                )
                Spacer(Modifier.width(10.dp))
                Text(
                    text = locationText,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Normal,
                    fontFamily = FontFamily.SansSerif,
                    color = TextGray,
                    modifier = Modifier.clickable { onOpenMapPicker() }
                )
            }
        }
    }
}

// ── Prayer List Card ──────────────────────────────────────────────────────────

@Composable
fun AzanCard(
    prayers: List<PrayerTimeUi>,
    onTogglePrayer: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(vertical = 16.dp, horizontal = 12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Spacer(Modifier.width(12.dp))
                Image(
                    painter = painterResource(Res.drawable.ic_bel),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "Azan",
                    fontSize = 20.sp,
                    fontFamily = FontFamily.Default,
                    color = Color(0xFF222222)
                )
            }
            Spacer(Modifier.height(15.dp))
            Column {
                prayers.forEachIndexed { index, item ->
                    PrayerItem(
                        item = item,
                        onToggle = { onTogglePrayer(item.name, it) }
                    )
                    if (index < prayers.lastIndex) {
                        Spacer(Modifier.height(7.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun PrayerItem(
    item: PrayerTimeUi,
    onToggle: (Boolean) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(
                if (item.isOnTime) Color(0xBCECEEEB) else Color.Transparent
            )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (item.isOnTime) {
                Box(
                    modifier = Modifier
                        .width(5.dp)
                        .fillMaxHeight()
                        .background(Green)
                )
            } else {
                Spacer(Modifier.width(4.dp))
            }

            Spacer(Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = item.name,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (item.isOnTime) Green else Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (item.isOnTime) {
                        Spacer(Modifier.width(4.dp))
                        Image(
                            painter = painterResource(Res.drawable.ic_checlist_green_bg),
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    text = item.time,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color(0xFF222222)
                )
            }

            Switch(
                modifier = Modifier.scale(0.7f),
                checked = item.isEnabled,
                onCheckedChange = onToggle,
                colors = SwitchDefaults.colors(
                    uncheckedBorderColor = BorderGray,
                    checkedBorderColor = Green,
                    checkedThumbColor = White,
                    checkedTrackColor = Green,
                    uncheckedThumbColor = White,
                    uncheckedTrackColor = BorderGray
                )
            )
        }
    }
}

// ── Next Prayer Card ──────────────────────────────────────────────────────────

@Composable
fun NextPrayerCard(
    nextPrayerName: String,
    remainingTime: String,
    progress: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                PrayerCountdownIcon(progress = progress)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(50.dp))
                        .background(GreenBadge)
                        .padding(horizontal = 9.dp, vertical = 5.dp)
                ) {
                    Text(
                        text = if (nextPrayerName.length>7) nextPrayerName else "NEXT: $nextPrayerName",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Green,
                        maxLines = 1,
                        softWrap = false
                    )
                }
            }

            Spacer(Modifier.height(35.dp))

            Text(
                text = "NEXT PRAYER",
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                color = TextMid
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = remainingTime,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(Modifier.height(12.dp))
        }
    }
}

@Composable
fun PrayerCountdownIcon(progress: Float) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.size(35.dp)
    ) {
        CircularProgressIndicator(
            progress = { 1f },
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFE6E6E6),
            strokeWidth = 3.dp
        )
        CircularProgressIndicator(
            progress = { 1f - progress },
            modifier = Modifier.fillMaxSize(),
            color = GreenDark,
            strokeWidth = 3.dp
        )
        Image(
            painter = painterResource(Res.drawable.ic_bel),
            contentDescription = null,
            modifier = Modifier.size(12.dp)
        )
    }
}

// ── Alert Card ────────────────────────────────────────────────────────────────

@Composable
fun PrayerAlertCard(
    item: PrayerTimeUi,
    isEnabled: Boolean,
    onToggle: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(28.dp),
        colors = CardDefaults.cardColors(containerColor = White),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.ic_half_moon),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
                Switch(
                    modifier = Modifier.scale(0.8f),
                    checked = isEnabled,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        uncheckedBorderColor = BorderGray,
                        checkedBorderColor = Green,
                        checkedTrackColor = Green,
                        uncheckedTrackColor = BorderGray,
                        checkedThumbColor = White,
                        uncheckedThumbColor = White
                    )
                )
            }

            Spacer(Modifier.height(36.dp))

            Text(
                text = "${item.name} ALERT",
                fontSize = 12.sp,
                letterSpacing = 2.sp,
                fontWeight = FontWeight.Medium,
                color = TextMid
            )

            Spacer(Modifier.height(7.dp))

            Text(
                text = item.time,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
        }
    }
}