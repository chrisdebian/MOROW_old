package com.example.morow_compose

import android.util.Log
import de.westnordost.osmapi.map.data.BoundingBox
import de.westnordost.osmapi.map.data.Node
import de.westnordost.osmapi.map.data.Relation
import de.westnordost.osmapi.map.data.Way
import de.westnordost.osmapi.map.handler.MapDataHandler

class MyMapDataHandler : MapDataHandler {
    override fun handle(w: Way) {
        Log.d("OAuth2", "Found way with id ${w.id}")
    }
    override fun handle(n: Node) {
        Log.d("OAuth2", "Found node with id ${n.id}")
    }

    override fun handle(b: BoundingBox) {

    }

    override fun handle(r: Relation) {

    }
}