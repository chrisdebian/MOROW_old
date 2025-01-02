package com.example.morow_compose

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import org.maplibre.android.geometry.LatLng

class LocationViewModel : ViewModel() {

    val latLonLiveData = MutableLiveData(LatLng(51.05, -0.72))

    fun setLocation(lat: Double, lon: Double) {
        latLonLiveData.value = LatLng(lat, lon)
    }
}