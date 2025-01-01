package com.example.morow_compose

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.LocationManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.WindowInsetsAnimation
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.morow_compose.ui.theme.MorowcomposeTheme
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.map.MapDataApi
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.handler.MapDataHandler
import de.westnordost.osmapi.notes.Note
import de.westnordost.osmapi.notes.NotesApi
import de.westnordost.osmapi.user.UserApi
import de.westnordost.osmapi.user.UserInfo
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
import net.openid.appauth.TokenResponse
import org.maplibre.android.geometry.LatLng
import org.maplibre.android.maps.Style


class MyMapDataHandler : MapDataHandler {
    override fun handle(w: Way) {
        Log.d("OAuth2", "Found way with id ${w.id}")
    }
    override fun handle(n: Node) {
        Log.d("OAuth2", "Found node with id ${n.id}")
    }

    override fun handle(b:BoundingBox) {

    }

    override fun handle(r: Relation) {

    }
}
class MainActivity : ComponentActivity() {
    lateinit var gpsListener: GpsListener

    val locationViewModel : LocationViewModel by viewModels()

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
                    if (tresp != null) {
                        lifecycleScope.launch {
                            val client = OsmConnection(
                                "https://master.apis.dev.openstreetmap.org/api/0.6/",
                                "MOROW-test",
                               tresp.accessToken
                            )

                            val note: Note?
                            withContext(Dispatchers.IO) {
                               note = NotesApi(client).create(OsmLatLon(51.0, -1.0), "The Geographer Inn")
                                val mapApi = MapDataApi(client)
                                mapApi.getMap(BoundingBox(51.02, -0.75, 51.07, -0.7), MyMapDataHandler())

                                Log.d("OAuth2", "note id: ${note.id}")
                            }
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

                locationViewModel.latLonLiveData.observe(this) {
                    location = it
                }

                Column {
                    Button (onClick = {
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



