package org.khoyron.bilal.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedAlways
import platform.CoreLocation.kCLAuthorizationStatusAuthorizedWhenInUse
import platform.CoreLocation.kCLAuthorizationStatusDenied
import platform.CoreLocation.kCLAuthorizationStatusNotDetermined
import platform.CoreLocation.kCLAuthorizationStatusRestricted
import platform.darwin.NSObject

@Composable
actual fun RequestLocationPermission(
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    val delegate = remember {
        object : NSObject(), CLLocationManagerDelegateProtocol {
            override fun locationManager(manager: CLLocationManager, didChangeAuthorizationStatus: Int) {
                when (didChangeAuthorizationStatus) {
                    kCLAuthorizationStatusAuthorizedAlways,
                    kCLAuthorizationStatusAuthorizedWhenInUse -> onGranted()
                    kCLAuthorizationStatusDenied,
                    kCLAuthorizationStatusRestricted -> onDenied()
                    else -> {}
                }
            }
        }
    }
    
    val locationManager = remember { CLLocationManager() }

    LaunchedEffect(Unit) {
        locationManager.delegate = delegate
        val status = locationManager.authorizationStatus
        when (status) {
            kCLAuthorizationStatusNotDetermined -> {
                locationManager.requestWhenInUseAuthorization()
            }
            kCLAuthorizationStatusAuthorizedAlways,
            kCLAuthorizationStatusAuthorizedWhenInUse -> {
                onGranted()
            }
            else -> {
                onDenied()
            }
        }
    }
}
