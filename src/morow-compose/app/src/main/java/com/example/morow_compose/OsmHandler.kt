package com.example.morow_compose

import android.util.Log
import de.westnordost.osmapi.OsmConnection
import de.westnordost.osmapi.map.MapDataApi
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.OsmLatLon
import de.westnordost.osmapi.notes.NotesApi
import de.westnordost.osmapi.user.UserApi
import de.westnordost.osmapi.user.UserDetails

class OsmHandler(accessToken: String) {

    val client = OsmConnection(
        "https://master.apis.dev.openstreetmap.org/api/0.6/",
        "MOROW-test",
        accessToken
    )

    fun useOsmApi() {
        val note = NotesApi(client).create(OsmLatLon(51.01, -1.01), "The Boar Inn")
        val mapApi = MapDataApi(client)
        mapApi.getMap(BoundingBox(51.02, -0.75, 51.07, -0.7), MyMapDataHandler())
        Log.d("OAuth2", "note id: ${note.id}")

    }

    fun getCurrentUser() : UserDetails {
        return UserApi(client).mine
    }
}