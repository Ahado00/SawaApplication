package com.example.sawaapplication.utils

import android.content.Context
import android.location.Geocoder
import com.google.firebase.firestore.GeoPoint
import java.util.Locale

fun Context.getCityNameFromGeoPoint(geoPoint: GeoPoint): String {
    return try {
        val geocoder = Geocoder(this, Locale.getDefault())
        val addresses = geocoder.getFromLocation(geoPoint.latitude, geoPoint.longitude, 1)
        val address = addresses?.getOrNull(0)

        if (address != null) {
            listOfNotNull(
                address.thoroughfare,
            ).joinToString(", ")
        } else {
            "No Location Set"
        }
    } catch (e: Exception) {
        e.printStackTrace()
        "Unknown Location"
    }
}
