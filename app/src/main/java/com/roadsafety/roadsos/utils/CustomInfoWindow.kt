package com.roadsafety.roadsos.utils

import android.content.Context
import android.content.Intent
import android.location.Location
import android.net.Uri
import android.widget.Button
import android.widget.TextView

import com.roadsafety.roadsos.R

import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.infowindow.InfoWindow

class CustomInfoWindow(

    mapView: MapView,

    private val context: Context,

    private val latitude: Double,

    private val longitude: Double,

    private val userLatitude: Double,

    private val userLongitude: Double

) : InfoWindow(

    R.layout.custom_info_window,

    mapView
) {

    override fun onOpen(item: Any?) {

        InfoWindow.closeAllInfoWindowsOn(
            mMapView
        )

        val marker =
            item as Marker

        val titleText =
            mView.findViewById<TextView>(
                R.id.titleText
            )

        val subText =
            mView.findViewById<TextView>(
                R.id.subText
            )

        val navigateButton =
            mView.findViewById<Button>(
                R.id.navigateButton
            )

        titleText.text =
            marker.title

        val results =
            FloatArray(1)

        Location.distanceBetween(

            userLatitude,
            userLongitude,

            latitude,
            longitude,

            results
        )

        val distanceKm =
            results[0] / 1000

        subText.text =
            "${String.format("%.1f", distanceKm)} km away"

        navigateButton.setOnClickListener {

            val uri =
                Uri.parse(
                    "google.navigation:q=$latitude,$longitude"
                )

            val intent =
                Intent(
                    Intent.ACTION_VIEW,
                    uri
                )

            intent.setPackage(
                "com.google.android.apps.maps"
            )

            context.startActivity(intent)
        }

        mView.setOnClickListener(null)
    }

    override fun onClose() {

    }
}