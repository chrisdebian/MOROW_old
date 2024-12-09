package com.example.morow_compose

import android.location.Location
import android.location.LocationListener

class GpsListener(val callback: (Location) -> Unit) : LocationListener {
    override fun onLocationChanged(location: Location) {
        callback(location)
    }
}