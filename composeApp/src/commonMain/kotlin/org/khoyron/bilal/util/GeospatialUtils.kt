package org.khoyron.bilal.util

import kotlin.math.max
import kotlin.math.min

object GeospatialUtils {

    /**
     * Checks if a point (lat, lng) is inside a polygon using Ray Casting algorithm.
     * @param polygon List of rings, where each ring is a list of [lng, lat] coordinates.
     */
    fun isPointInPolygon(lat: Double, lng: Double, polygon: List<List<List<Double>>>): Boolean {
        var isInside = false
        // First ring is usually the exterior ring
        val exteriorRing = polygon.firstOrNull() ?: return false
        
        // Ray casting for exterior ring
        if (checkRing(lat, lng, exteriorRing)) {
            isInside = true
            // Check interior rings (holes) - if point is in a hole, it's outside
            for (i in 1 until polygon.size) {
                if (checkRing(lat, lng, polygon[i])) {
                    return false
                }
            }
        }
        
        return isInside
    }

    private fun checkRing(lat: Double, lng: Double, ring: List<List<Double>>): Boolean {
        var intersects = false
        var j = ring.size - 1
        for (i in ring.indices) {
            val xi = ring[i][0] // lng
            val yi = ring[i][1] // lat
            val xj = ring[j][0]
            val yj = ring[j][1]

            val intersect = ((yi > lat) != (yj > lat)) &&
                    (lng < (xj - xi) * (lat - yi) / (yj - yi) + xi)
            if (intersect) intersects = !intersects
            j = i
        }
        return intersects
    }

    /**
     * Basic Bounding Box check for optimization.
     */
    fun isPointInBoundingBox(lat: Double, lng: Double, bbox: DoubleArray): Boolean {
        return lat >= bbox[1] && lat <= bbox[3] && lng >= bbox[0] && lng <= bbox[2]
    }

    /**
     * Calculates a simple Bounding Box [minLng, minLat, maxLng, maxLat] for a MultiPolygon.
     */
    fun calculateBoundingBox(coordinates: List<List<List<List<Double>>>>): DoubleArray {
        var minLng = Double.MAX_VALUE
        var minLat = Double.MAX_VALUE
        var maxLng = -Double.MAX_VALUE
        var maxLat = -Double.MAX_VALUE

        for (polygon in coordinates) {
            for (ring in polygon) {
                for (point in ring) {
                    minLng = min(minLng, point[0])
                    minLat = min(minLat, point[1])
                    maxLng = max(maxLng, point[0])
                    maxLat = max(maxLat, point[1])
                }
            }
        }
        return doubleArrayOf(minLng, minLat, maxLng, maxLat)
    }
}
