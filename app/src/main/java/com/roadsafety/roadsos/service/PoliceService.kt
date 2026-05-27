package com.roadsafety.roadsos.service

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.util.Log

import androidx.core.content.ContextCompat

import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley

import com.roadsafety.roadsos.R
import com.roadsafety.roadsos.utils.CustomInfoWindow

import org.json.JSONArray

import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

object PoliceService {

    private fun drawableToBitmap(
        drawable: Drawable
    ): Bitmap {

        if (drawable is BitmapDrawable) {
            return drawable.bitmap
        }

        val bitmap =
            Bitmap.createBitmap(
                drawable.intrinsicWidth.coerceAtLeast(1),
                drawable.intrinsicHeight.coerceAtLeast(1),
                Bitmap.Config.ARGB_8888
            )

        val canvas =
            Canvas(bitmap)

        drawable.setBounds(
            0,
            0,
            canvas.width,
            canvas.height
        )

        drawable.draw(canvas)

        return bitmap
    }

    fun loadNearbyPoliceStations(

        context: Context,

        map: MapView,

        latitude: Double,

        longitude: Double
    ) {

        val url =
            "https://overpass-api.de/api/interpreter?data=[out:json];node[amenity=police](around:3000,$latitude,$longitude);out;"

        val policeDrawable =
            ContextCompat.getDrawable(
                context,
                R.drawable.police_icon
            )

        val scaledBitmap =
            policeDrawable?.let {

                val bitmap =
                    drawableToBitmap(it)

                Bitmap.createScaledBitmap(
                    bitmap,
                    40,
                    40,
                    true
                )
            }

        val request =
            JsonObjectRequest(

                Request.Method.GET,

                url,

                null,

                { response ->

                    try {

                        val stations: JSONArray =
                            response.getJSONArray(
                                "elements"
                            )

                        for (i in 0 until stations.length()) {

                            val station =
                                stations.getJSONObject(i)

                            val lat =
                                station.getDouble("lat")

                            val lon =
                                station.getDouble("lon")

                            val point =
                                GeoPoint(
                                    lat,
                                    lon
                                )

                            val marker =
                                Marker(map)

                            marker.position =
                                point

                            marker.setAnchor(
                                Marker.ANCHOR_CENTER,
                                Marker.ANCHOR_BOTTOM
                            )

                            marker.setInfoWindowAnchor(
                                Marker.ANCHOR_CENTER,
                                -0.2f
                            )

                            marker.setFlat(false)

                            marker.setPanToView(true)

                            if (scaledBitmap != null) {

                                marker.icon =
                                    BitmapDrawable(
                                        context.resources,
                                        scaledBitmap
                                    )
                            }

                            val name =

                                if (
                                    station.has("tags")
                                ) {

                                    station
                                        .getJSONObject("tags")
                                        .optString(
                                            "name",
                                            "Police Station"
                                        )

                                } else {

                                    "Police Station"
                                }

                            marker.title =
                                name

                            marker.subDescription =
                                "Nearby Police Station"

                            marker.infoWindow =

                                CustomInfoWindow(

                                    map,

                                    context,

                                    lat,

                                    lon,

                                    latitude,

                                    longitude
                                )

                            marker.id =
                                "police_$i"

                            map.overlays.add(marker)
                        }

                        map.invalidate()

                    } catch (e: Exception) {

                        Log.e(
                            "PoliceService",
                            "Error: ${e.message}"
                        )
                    }
                },

                { error ->

                    Log.e(
                        "PoliceService",
                        "Volley Error: ${error.message}"
                    )
                }
            )

        Volley
            .newRequestQueue(context)
            .add(request)
    }
}