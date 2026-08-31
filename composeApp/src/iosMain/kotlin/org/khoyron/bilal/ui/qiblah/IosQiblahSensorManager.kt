package org.khoyron.bilal.ui.qiblah

import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.CoreLocation.CLHeading
import platform.CoreLocation.CLLocation
import platform.CoreLocation.CLLocationManager
import platform.CoreLocation.CLLocationManagerDelegateProtocol
import platform.CoreLocation.kCLDistanceFilterNone
import platform.CoreLocation.kCLHeadingFilterNone
import platform.CoreLocation.kCLLocationAccuracyBest
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class)
class IosQiblahSensorManager : QiblahSensorManager {

    private val locationManager = CLLocationManager()
    
    private var onBearingChanged: ((Float) -> Unit)? = null
    private var onLocationChanged: ((Double, Double) -> Unit)? = null
    private var onError: ((String) -> Unit)? = null

    // Strong reference to delegate to prevent GC
    private val locationDelegate = object : NSObject(), CLLocationManagerDelegateProtocol {
        override fun locationManager(manager: CLLocationManager, didUpdateLocations: List<*>) {
            val location = didUpdateLocations.lastOrNull() as? CLLocation ?: return
            
            val (lat, lon) = location.coordinate.useContents { latitude to longitude }
            
            // Revert: Allow small values as user prefers them over 0
            if (lat == 0.0 && lon == 0.0) return
            
            onLocationChanged?.invoke(lat, lon)
        }

        override fun locationManager(manager: CLLocationManager, didUpdateHeading: CLHeading) {
            // trueHeading is relative to True North. magneticHeading is relative to Magnetic North.
            // If trueHeading is negative, it means it's invalid. Fallback to magneticHeading.
            val heading = if (didUpdateHeading.trueHeading >= 0) {
                didUpdateHeading.trueHeading
            } else {
                didUpdateHeading.magneticHeading
            }
            onBearingChanged?.invoke(heading.toFloat())
        }

        override fun locationManager(manager: CLLocationManager, didFailWithError: platform.Foundation.NSError) {
            onError?.invoke("Location update failed: ${didFailWithError.localizedDescription}")
        }
    }

    override fun start(
        onBearingChanged: (Float) -> Unit,
        onLocationChanged: (Double, Double) -> Unit,
        onError: (String) -> Unit
    ) {
        this.onBearingChanged = onBearingChanged
        this.onLocationChanged = onLocationChanged
        this.onError = onError

        locationManager.delegate = locationDelegate
        locationManager.desiredAccuracy = kCLLocationAccuracyBest
        locationManager.distanceFilter = kCLDistanceFilterNone
        locationManager.headingFilter = kCLHeadingFilterNone
        
        // Immediate check for last known location
        locationManager.location?.let { location ->
            val (lat, lon) = location.coordinate.useContents { latitude to longitude }
            if (lat != 0.0 || lon != 0.0) {
                onLocationChanged(lat, lon)
            }
        }
        
        // Try to request a fresh location immediately
        locationManager.requestLocation()
        
        // Start Compass (Heading) updates
        if (CLLocationManager.headingAvailable()) {
            locationManager.startUpdatingHeading()
        } else {
            onError("Heading (Compass) not available on this device")
        }

        // Start Location updates
        locationManager.startUpdatingLocation()
    }

    override fun stop() {
        locationManager.stopUpdatingHeading()
        locationManager.stopUpdatingLocation()
        locationManager.delegate = null
        onBearingChanged = null
        onLocationChanged = null
        onError = null
    }
}
