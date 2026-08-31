package org.khoyron.bilal.ui.mosquefinder

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilal.composeapp.generated.resources.*
import com.swmansion.kmpmaps.core.*
import dev.jordond.compass.Priority
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.LocationRequest
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.khoyron.bilal.domain.model.Mosque
import org.koin.compose.viewmodel.koinViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview

@Composable
fun MosqueFinderScreen(
    viewModel: MosqueFinderViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val scope = rememberCoroutineScope()
    val geolocator = remember { Geolocator() }

    LaunchedEffect(Unit) {
        var isInitialSearchDone = false
        // Fallback to periodic check if track has issues with types in this environment
        while(true) {
            val result = geolocator.current(LocationRequest(Priority.HighAccuracy))
            if (result is GeolocatorResult.Success) {
                val location = result.data
                viewModel.updateUserLocation(location.coordinates.latitude, location.coordinates.longitude)
                
                if (!isInitialSearchDone) {
                    viewModel.searchMosquesByLocation(location.coordinates.latitude, location.coordinates.longitude)
                    isInitialSearchDone = true
                }
            }
            delay(5000) // Update every 5 seconds
        }
    }
    
    MosqueFinderContent(
        uiState = uiState,
        onSearch = { viewModel.searchMosquesByName(it) },
        onMosqueSelected = { viewModel.selectMosque(it) },
        onMyLocation = {
            scope.launch {
                val result = geolocator.current(LocationRequest(Priority.HighAccuracy))
                if (result is GeolocatorResult.Success) {
                    val location = result.data
                    viewModel.updateUserLocation(location.coordinates.latitude, location.coordinates.longitude)
                    viewModel.searchMosquesByLocation(location.coordinates.latitude, location.coordinates.longitude)
                }
            }
        },
        onShare = { viewModel.shareMosque(it) },
        onNavigate = { viewModel.navigateToMosque(it) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MosqueFinderContent(
    uiState: MosqueFinderUiState,
    onSearch: (String) -> Unit,
    onMosqueSelected: (Mosque) -> Unit,
    onMyLocation: () -> Unit,
    onShare: (Mosque) -> Unit,
    onNavigate: (Mosque) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    
    // Camera position state initialized with current user location if available
    var cameraPosition by remember { 
        mutableStateOf(uiState.userLocation?.let { CameraPosition(it, 15f) }) 
    }
    var hasCenteredInitially by remember { mutableStateOf(uiState.userLocation != null) }
    var hasCenteredOnMosque by remember { mutableStateOf(false) }

    // Initial camera centering on user location
    LaunchedEffect(uiState.userLocation) {
        val userLoc = uiState.userLocation
        if (!hasCenteredInitially && userLoc != null) {
            cameraPosition = CameraPosition(userLoc, 15f)
            hasCenteredInitially = true
        }
    }

    // Focus on the first mosque ONLY when the very first batch of data is loaded
    LaunchedEffect(uiState.isLoading) {
        if (!uiState.isLoading && uiState.mosques.isNotEmpty() && !hasCenteredOnMosque) {
            uiState.selectedMosque?.let { mosque ->
                cameraPosition = CameraPosition(Coordinates(mosque.latitude, mosque.longitude), 15f)
                hasCenteredOnMosque = true
            }
        }
    }

    val customMarkers = remember(uiState.mosques, uiState.selectedMosque, uiState.userLocation) {
        val markers = mutableMapOf<String, @Composable (Marker) -> Unit>()
        uiState.mosques.forEach { mosque ->
            val isSelected = uiState.selectedMosque?.id == mosque.id
            // Gunakan ID unik yang mencakup status isSelected
            markers["mosque_${mosque.id}_$isSelected"] = { _ ->
                Icon(
                    painter = painterResource(
                        if (isSelected) Res.drawable.ic_marker_mosque_selected 
                        else Res.drawable.ic_marker_mosque
                    ),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(if (isSelected) 48.dp else 40.dp)
                )
            }
        }
        uiState.userLocation?.let {
            markers["user_location"] = { _ ->
                Icon(
                    painter = painterResource(Res.drawable.ic_user_location_marker),
                    contentDescription = null,
                    tint = Color.Unspecified,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        markers
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        if (uiState.userLocation == null && uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFF476B4E))
            }
        } else {
            // Map
            Map(
                modifier = Modifier.fillMaxSize()
                    .background(Color.White),
                cameraPosition = cameraPosition,
                onCameraMove = { cameraPosition = null },
                markers = remember(uiState.mosques, uiState.userLocation, uiState.selectedMosque) {
                    val list = uiState.mosques.map { mosque ->
                        val isSelected = uiState.selectedMosque?.id == mosque.id
                        Marker(
                            coordinates = Coordinates(mosque.latitude, mosque.longitude),
                            title = mosque.name,
                            contentId = "mosque_${mosque.id}_$isSelected"
                        )
                    }.toMutableList()
                    
                    uiState.userLocation?.let {
                        list.add(
                            Marker(
                                coordinates = it,
                                title = "My Location",
                                contentId = "user_location"
                            )
                        )
                    }
                    list
                },
                customMarkerContent = customMarkers,
                onMapClick = { /* Clear selection? */ },
                onMarkerClick = { marker ->
                    if (marker.contentId?.startsWith("mosque_") == true) {
                        // Ambil original ID (sebelum suffix _true/_false)
                        val mosqueId = marker.contentId?.split("_")?.get(1)
                        uiState.mosques.find { it.id == mosqueId }?.let {
                            onMosqueSelected(it)
                        }
                    }
                }
            )

            // Search Bar
            SearchBar(
                query = searchQuery,
                onQueryChange = { 
                    searchQuery = it
                    onSearch(it)
                },
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(16.dp)
            )


            // My Location Button
            FloatingActionButton(
                onClick = {
                    uiState.userLocation?.let {
                        cameraPosition = CameraPosition(it, 15f)
                    }
                    onMyLocation()
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(top = 90.dp, end = 16.dp),
                containerColor = Color.White,
                contentColor = Color(0xFF476B4E),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.ic_point_location),
                    contentDescription = "My Location",
                    modifier = Modifier.size(24.dp)
                )
            }

            // Bottom Detail Card
            uiState.selectedMosque?.let { mosque ->
                MosqueDetailCard(
                    mosque = mosque,
                    onShare = { onShare(mosque) },
                    onNavigate = { onNavigate(mosque) },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(Res.drawable.ic_search),
                contentDescription = null,
                tint = Color.Gray,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(12.dp))
            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text("Search for a mosque...", color = Color.Gray)
                }
                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    modifier = Modifier.fillMaxWidth()
                )
            }
            IconButton(onClick = { /* TODO: Filter */ }) {
                Icon(
                    imageVector = Icons.Default.Menu, // Closest to filter in default icons
                    contentDescription = "Filter",
                    tint = Color.Gray
                )
            }
        }
    }
}

@Composable
fun MosqueDetailCard(
    mosque: Mosque,
    onShare: () -> Unit,
    onNavigate: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(Res.drawable.img_mosque),
                    contentDescription = null,
                    modifier = Modifier
                        .size(64.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentScale = ContentScale.Crop
                )
                Spacer(Modifier.width(12.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        mosque.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            color = Color(0xFFE8F5E9),
                            shape = RoundedCornerShape(4.dp)
                        ) {
                            Text(
                                if (mosque.isOpen) "OPEN NOW" else "CLOSED",
                                color = Color(0xFF4CAF50),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.LocationOn,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(12.dp)
                        )
                        Text(
                            mosque.distance ?: "",
                            color = Color.Gray,
                            fontSize = 12.sp
                        )
                    }
                }
                IconButton(onClick = { /* TODO: Favorite */ }) {
                    Icon(
                        imageVector = Icons.Outlined.FavoriteBorder,
                        contentDescription = null,
                        tint = Color.LightGray
                    )
                }
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                InfoChip(label = "NEXT PRAYER", value = mosque.nextPrayer ?: "-", modifier = Modifier.weight(1f))
                InfoChip(label = "DISTANCE", value = mosque.distance ?: "-", modifier = Modifier.weight(1f))
                InfoChip(label = "PARKING", value = if (mosque.hasParking) "✔" else "✘", modifier = Modifier.weight(1f))
            }
            
            Spacer(Modifier.height(16.dp))
            
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onNavigate() },
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF476B4E)),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_point_location),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Open in Maps", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(Modifier.width(12.dp))
                OutlinedButton(
                    onClick = { onShare() },
                    modifier = Modifier.size(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(0.dp),
                    border = BorderStroke(1.dp, Color.LightGray)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.ic_share),
                        contentDescription = null,
                        tint = Color(0xFF476B4E),
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun InfoChip(label: String, value: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color(0xFFF5F5F5),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 9.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(4.dp))
            Text(value, fontSize = 13.sp, color = Color.Black, fontWeight = FontWeight.Bold)
        }
    }
}

@Preview
@Composable
fun MosqueFinderPreview() {
    val dummyMosques = listOf(
        Mosque(
            id = "1",
            name = "Masjid Al-Ikhlas",
            latitude = -7.2575,
            longitude = 112.7521,
            address = "Jl. Kedung Cowek No. 31, Surabaya",
            isOpen = true,
            distance = "0.5 km",
            nextPrayer = "Dhuhr 12:45",
            capacity = "500+",
            hasParking = true
        )
    )
    val uiState = MosqueFinderUiState(
        mosques = dummyMosques,
        selectedMosque = dummyMosques[0],
        userLocation = Coordinates(-7.2575, 112.7521)
    )
    MosqueFinderContent(
        uiState = uiState,
        onSearch = {},
        onMosqueSelected = {},
        onMyLocation = {},
        onShare = {},
        onNavigate = {}
    )
}
