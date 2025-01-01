package com.example.morow_compose

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.morow_compose.ui.theme.MorowcomposeTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.openid.appauth.AuthorizationException
import net.openid.appauth.AuthorizationRequest
import net.openid.appauth.AuthorizationResponse
import net.openid.appauth.AuthorizationService
import net.openid.appauth.AuthorizationServiceConfiguration
import net.openid.appauth.ClientSecretBasic
import net.openid.appauth.ResponseTypeValues
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style



class MainActivity : ComponentActivity() {
    lateinit var gpsListener: GpsListener

    val locationViewModel : LocationViewModel by viewModels()
    val userViewModel : UserViewModel by viewModels()

    lateinit var authService : AuthorizationService

    val launcher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        it.data?.apply {
            val resp = AuthorizationResponse.fromIntent(this)
            val ex = AuthorizationException.fromIntent(this)
            if (resp != null) {
                authService.performTokenRequest(
                    resp.createTokenExchangeRequest(),
                    ClientSecretBasic("secret") // replace with real secret
                ) { tresp, tex ->
                    if (tresp != null && tresp.accessToken != null) {
                        lifecycleScope.launch {
                            val osmHandler = OsmHandler(tresp.accessToken!!)
                            var displayName : String?
                            withContext(Dispatchers.IO) {
                                osmHandler.useOsmApi()
                                displayName = osmHandler.getCurrentUser().displayName
                            }
                            userViewModel.currentUser = displayName
                        }
                    } else {
                        Log.e("OAuth2", "Error getting access token: ${tex?.message}")
                    }
                }
            } else {
                Log.e("OAuth2", "Error with authorisation: ${ex?.message}")
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authService = AuthorizationService(this@MainActivity)


        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("https://master.apis.dev.openstreetmap.org/oauth2/authorize"),
            Uri.parse("https://master.apis.dev.openstreetmap.org/oauth2/token")
        )


        setContent {
            MorowcomposeTheme {
                var location  by remember { mutableStateOf(LatLng(51.05, -0.72))}
                var currentUser : String? by remember { mutableStateOf(null) }
                locationViewModel.latLonLiveData.observe(this) {
                    location = it
                }

                userViewModel.currentUserLive.observe(this) {
                    currentUser = it
                }

                Column {

                    if(currentUser != null) {
                        Text("Logged into OSM as $currentUser")
                    } else {
                        Button(onClick = {
                            val authRequestBuilder = AuthorizationRequest.Builder(
                                serviceConfig,
                                "Kw6Vi9fDjoA72eBE6pbyclJozK6_FEC31nfZcjC9WXU",
                                ResponseTypeValues.CODE,
                                Uri.parse("com.example.morow://android")
                            )
                            val authRequest = authRequestBuilder
                                .setScope("read_prefs write_api write_gpx write_notes")
                                .build()

                            val intent = authService.getAuthorizationRequestIntent(authRequest)

                            launcher.launch(intent)

                        }) {
                            Text("Login to OSM")
                        }
                    }

                    org.ramani.compose.MapLibre(
                        modifier = Modifier.fillMaxSize(),
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



