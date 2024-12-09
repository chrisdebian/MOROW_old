package com.example.morow_compose

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.example.morow_compose.ui.theme.MorowcomposeTheme
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style


class MainActivity : ComponentActivity() {
    lateinit var gpsListener: GpsListener

    val locationViewModel : LocationViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MorowcomposeTheme {
                var location  by remember { mutableStateOf(LatLng(51.05, -0.72))}

                locationViewModel.latLonLiveData.observe(this) {
                    location = it
                }

                org.ramani.compose.MapLibre(
                    modifier= Modifier.fillMaxSize(),
                    styleBuilder = Style.Builder().fromUri(
                        "https://tiles.openfreemap.org/styles/bright"
                    ),
                    cameraPosition = org.ramani.compose.CameraPosition(
                        target = location,
                        zoom = 14.0
                    )
                ) {

                }
            }
        }
        gpsListener = GpsListener {
            locationViewModel.setLocation(it.latitude, it.longitude)
        }

        checkPermissions()
    }

    fun checkPermissions() {
        val permission = Manifest.permission.ACCESS_FINE_LOCATION
        if(ContextCompat.checkSelfPermission(this, permission) !=
            PackageManager.PERMISSION_GRANTED) {
            val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){
                if(it) {
                    startGps()
                }
            }
            permissionsLauncher.launch(permission)
        } else {
            startGps()
        }
    }

    @SuppressLint("MissingPermission")
    fun startGps() {
        val mgr = getSystemService(LOCATION_SERVICE) as LocationManager
        mgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0f, gpsListener)
    }
}



