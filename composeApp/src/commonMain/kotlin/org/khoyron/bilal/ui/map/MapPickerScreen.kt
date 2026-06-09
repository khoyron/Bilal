package org.khoyron.bilal.ui.map

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import bilal.composeapp.generated.resources.Res
import bilal.composeapp.generated.resources.ic_map_picker
import bilal.composeapp.generated.resources.ic_picker_location
import bilal.composeapp.generated.resources.ic_point_location
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import com.swmansion.kmpmaps.core.Map
import com.swmansion.kmpmaps.core.Coordinates
import com.swmansion.kmpmaps.core.CameraPosition
import com.swmansion.kmpmaps.core.Marker
import org.khoyron.bilal.data.local.SessionManager
import org.koin.compose.koinInject
import dev.jordond.compass.geocoder.Geocoder
import dev.jordond.compass.geocoder.placeOrNull
import dev.jordond.compass.geolocation.Geolocator
import dev.jordond.compass.geolocation.GeolocatorResult
import dev.jordond.compass.geolocation.LocationRequest
import dev.jordond.compass.Priority
import kotlinx.coroutines.launch

private const val PICKER_MARKER_KEY = "picker_marker"

@Composable
fun MapPickerScreen(
    onLocationSelected: (Double, Double) -> Unit,
    onBack: () -> Unit,
    sessionManager: SessionManager = koinInject()
) {
    val initialLat = sessionManager.getLat()
    val initialLon = sessionManager.getLon()

    val defaultCoords = if (initialLat != null && initialLon != null)
        Coordinates(initialLat, initialLon)
    else
        Coordinates(-7.2575, 112.7521)

    var selectedLocation by remember { mutableStateOf(defaultCoords) }
    var currentZoom by remember { mutableStateOf(15f) }
    var cameraPosition by remember { mutableStateOf<CameraPosition?>(CameraPosition(defaultCoords, currentZoom)) }

    val locationName by rememberLocationName(selectedLocation)
    val scope = rememberCoroutineScope()
    val geolocator = remember { Geolocator() }


    MapPickerContent(
        selectedLocation = selectedLocation,
        cameraPosition = cameraPosition,
        onCameraMove = { cameraPosition = null },
        onMapClick = { coordinates ->
            selectedLocation = coordinates
        },
        locationName = locationName,
        onSelectLocation = {
            onLocationSelected(selectedLocation.latitude, selectedLocation.longitude)
        },
        onBack = onBack,
        onZoomIn = {
            currentZoom = (currentZoom + 1f).coerceAtMost(21f)
            cameraPosition = CameraPosition(selectedLocation, currentZoom)
        },
        onZoomOut = {
            currentZoom = (currentZoom - 1f).coerceAtLeast(1f)
            cameraPosition = CameraPosition(selectedLocation, currentZoom)
        },
        onMyLocation = {
            scope.launch {
                val result = geolocator.current(LocationRequest(Priority.HighAccuracy))
                if (result is GeolocatorResult.Success) {
                    val newCoords = Coordinates(
                        result.data.coordinates.latitude,
                        result.data.coordinates.longitude
                    )
                    selectedLocation = newCoords
                    cameraPosition = CameraPosition(newCoords, currentZoom)
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapPickerContent(
    selectedLocation: Coordinates,
    cameraPosition: CameraPosition?,
    onCameraMove: () -> Unit,
    onMapClick: (Coordinates) -> Unit,
    locationName: String = "",
    onSelectLocation: () -> Unit,
    onBack: () -> Unit,
    onZoomIn: () -> Unit,
    onZoomOut: () -> Unit,
    onMyLocation: () -> Unit
) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Map picker",
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF476B4E)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.White
                )
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            Map(
                modifier = Modifier.fillMaxSize(),
                cameraPosition = cameraPosition,
                onCameraMove = {
                    onCameraMove()
                },
                onMapClick = { coordinates ->
                    onMapClick(coordinates)
                },
                markers = listOf(
                    Marker(
                        coordinates = selectedLocation,
                        title = "Selected Location",
                        contentId = PICKER_MARKER_KEY
                    )
                ),
                customMarkerContent = mapOf(
                    PICKER_MARKER_KEY to { _ ->
                        Image(
                            painter = painterResource(Res.drawable.ic_map_picker),
                            contentDescription = null,
                            modifier = Modifier.size(60.dp)
                        )
                    }
                )
            )

            // Floating info card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.TopCenter),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .background(Color(0xFFE8ECE7), RoundedCornerShape(12.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_picker_location),
                            contentDescription = null,
                            tint = Color(0xFF476B4E),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(
                            "CURRENT SELECTION",
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Text(
                            locationName.ifEmpty {
                                "Lat: ${selectedLocation.latitude.toString().take(7)}, Lon: ${selectedLocation.longitude.toString().take(7)}"
                            },   // "Surabaya, Indonesia"
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Black
                        )
                    }
                }
            }

            // Select Location Button (Floating)
            Column  (
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .align(Alignment.BottomCenter)
            ) {
                // Action Buttons (Right Side)
                Column(
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(end = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // My Location Button
                    FloatingActionButton(
                        onClick = onMyLocation,
                        containerColor = Color.White,
                        contentColor = Color(0xFF476B4E),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(48.dp),
                        elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.ic_point_location),
                            contentDescription = "My Location",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    // Zoom Buttons
                    Card(
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            IconButton(onClick = onZoomIn, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Add, contentDescription = "Zoom In", tint = Color.Black)
                            }
                            HorizontalDivider(
                                modifier = Modifier.width(32.dp),
                                thickness = 1.dp,
                                color = Color(0xFFEEEEEE)
                            )
                            IconButton(onClick = onZoomOut, modifier = Modifier.size(48.dp)) {
                                Icon(Icons.Default.Remove, contentDescription = "Zoom Out", tint = Color.Black)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(40.dp))

                Button(
                    onClick = onSelectLocation,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFF86A389),
                        disabledContainerColor = Color.LightGray
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Select Location", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}


@Composable
fun rememberLocationName(coordinates: Coordinates): State<String> {
    val locationName = remember { mutableStateOf("Loading...") }
    val geocoder = remember { Geocoder() }
    val scope = rememberCoroutineScope()

    LaunchedEffect(coordinates) {
        scope.launch {
            val place = geocoder.placeOrNull(
                latitude = coordinates.latitude,
                longitude = coordinates.longitude
            )
            locationName.value = if (place != null) {
                val city = place.locality ?: place.subAdministrativeArea ?: place.administrativeArea ?: ""
                val country = place.country ?: ""
                "$city, $country"
            } else {
                "${coordinates.latitude.toString().take(7)}, ${coordinates.longitude.toString().take(7)}"
            }
        }
    }

    return locationName
}

@Preview
@Composable
fun MapPickerPreview() {
    val dummyLocation = Coordinates(-7.2575, 112.7521)
    MapPickerContent(
        selectedLocation = dummyLocation,
        cameraPosition = CameraPosition(dummyLocation, 15f),
        onCameraMove = {},
        onMapClick = {},
        onSelectLocation = {},
        onBack = {},
        onZoomIn = {},
        onZoomOut = {},
        onMyLocation = {}
    )
}