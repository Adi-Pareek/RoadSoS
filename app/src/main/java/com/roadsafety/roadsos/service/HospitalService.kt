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

object HospitalService {

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

    fun loadNearbyHospitals(

        context: Context,

        map: MapView,

        latitude: Double,

        longitude: Double
    ) {

        val url =
            "https://overpass-api.de/api/interpreter?data=[out:json];node[amenity=hospital](around:3000,$latitude,$longitude);out;"

        val hospitalDrawable =
            ContextCompat.getDrawable(
                context,
                R.drawable.hospital_icon
            )

        val scaledBitmap =
            hospitalDrawable?.let {

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

                        val hospitals: JSONArray =
                            response.getJSONArray(
                                "elements"
                            )

                        for (i in 0 until hospitals.length()) {

                            val hospital =
                                hospitals.getJSONObject(i)

                            val lat =
                                hospital.getDouble("lat")

                            val lon =
                                hospital.getDouble("lon")

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
                                    hospital.has("tags")
                                ) {

                                    hospital
                                        .getJSONObject("tags")
                                        .optString(
                                            "name",
                                            "Hospital"
                                        )

                                } else {

                                    "Hospital"
                                }

                            marker.title =
                                name

                            marker.subDescription =
                                "Nearby Hospital"

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
                                "hospital_$i"

                            map.overlays.add(marker)
                        }

                        map.invalidate()

                    } catch (e: Exception) {

                        Log.e(
                            "HospitalService",
                            "Error: ${e.message}"
                        )
                    }
                },

                { error ->

                    Log.e(
                        "HospitalService",
                        "Volley Error: ${error.message}"
                    )
                }
            )

        Volley
            .newRequestQueue(context)
            .add(request)
    }
}