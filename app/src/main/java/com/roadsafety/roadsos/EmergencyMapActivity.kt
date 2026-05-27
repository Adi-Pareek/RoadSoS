package com.roadsafety.roadsos


import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast

import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

import com.google.android.gms.location.LocationServices

import com.roadsafety.roadsos.service.HospitalService
import com.roadsafety.roadsos.service.PoliceService

import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import android.widget.Button
import android.widget.ImageButton

import android.location.Location
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority


class EmergencyMapActivity : AppCompatActivity() {

    private lateinit var map: MapView

    private lateinit var loadingText: TextView

    private lateinit var speedText: TextView
    private lateinit var toggleMapButton: Button
    private lateinit var currentLocationButton: ImageButton

    private var isSatellite = false

    private var currentLatitude = 0.0

    private var currentLongitude = 0.0
    private lateinit var fusedLocationClient:
            com.google.android.gms.location.FusedLocationProviderClient

    private lateinit var locationCallback:
            LocationCallback

    private lateinit var userMarker: Marker


    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)

        Configuration.getInstance().load(
            applicationContext,
            getSharedPreferences(
                "osm",
                MODE_PRIVATE
            )
        )

        Configuration.getInstance().userAgentValue =
            packageName

        setContentView(
            R.layout.activity_emergency_map
        )

        map =
            findViewById(R.id.map)

        loadingText =
            findViewById(R.id.loadingText)
        speedText =
            findViewById(R.id.speedText)
        currentLocationButton =
            findViewById(R.id.currentLocationButton)

        toggleMapButton =
            findViewById(R.id.toggleMapButton)

        map.setTileSource(
            TileSourceFactory.MAPNIK
        )
        map.setMultiTouchControls(true)
        map.zoomController.setVisibility(

            org.osmdroid.views
                .CustomZoomButtonsController
                .Visibility.NEVER
        )

        currentLocationButton.setOnClickListener {

            val userPoint =
                GeoPoint(
                    currentLatitude,
                    currentLongitude
                )

            map.controller.animateTo(
                userPoint
            )

            map.controller.setZoom(17.0)
        }
        toggleMapButton.setOnClickListener {

            if (!isSatellite) {

                map.setTileSource(
                    TileSourceFactory.USGS_SAT
                )

                toggleMapButton.text =
                    "Street"

                isSatellite = true

            } else {

                map.setTileSource(
                    TileSourceFactory.MAPNIK
                )

                toggleMapButton.text =
                    "Satellite"

                isSatellite = false
            }

            map.invalidate()
        }

        if (
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION
                ),
                1
            )

            return
        }

        fusedLocationClient =
            LocationServices
                .getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->

                if (location != null) {

                    currentLatitude =
                        location.latitude

                    currentLongitude =
                        location.longitude
                    map.overlays.clear()

                    val userPoint =
                        GeoPoint(
                            currentLatitude,
                            currentLongitude
                        )

                    map.controller.setZoom(17.0)

                    map.controller.animateTo(userPoint)

                    userMarker =
                        Marker(map)

                    userMarker.position =
                        userPoint

                    userMarker.title =
                        "You are here"

                    userMarker.setAnchor(
                        Marker.ANCHOR_CENTER,
                        Marker.ANCHOR_BOTTOM
                    )

                    val userIcon =
                        ContextCompat.getDrawable(
                            this,
                            org.osmdroid.library.R.drawable.person
                        )

                    userMarker.icon =
                        userIcon

                    map.overlays.add(userMarker)
                    map.invalidate()
                    locationCallback =
                        object : LocationCallback() {

                            override fun onLocationResult(
                                locationResult: LocationResult
                            )
                            {

                                val location =
                                    locationResult.lastLocation
                                        ?: return

                                currentLatitude =
                                    location.latitude

                                currentLongitude =
                                    location.longitude

                                val updatedPoint =
                                    GeoPoint(
                                        currentLatitude,
                                        currentLongitude
                                    )

                                userMarker.position =
                                    updatedPoint

                                map.invalidate()
                            }

                        }
                    currentLongitude =
                        location.longitude
                    val speedKmH =
                        (location.speed * 3.6)
                            .toInt()

                    speedText.text =
                        "$speedKmH km/h"


                    val locationRequest =
                        LocationRequest.Builder(

                            Priority.PRIORITY_HIGH_ACCURACY,

                            3000

                        ).build()

                    fusedLocationClient.requestLocationUpdates(

                        locationRequest,

                        locationCallback,

                        mainLooper
                    )

                    loadingText.visibility =
                        View.GONE

                    HospitalService
                        .loadNearbyHospitals(

                            this,

                            map,

                            currentLatitude,

                            currentLongitude
                        )

                    PoliceService
                        .loadNearbyPoliceStations(

                            this,

                            map,

                            currentLatitude,

                            currentLongitude
                        )

                } else {

                    Toast.makeText(
                        this,
                        "Location not found",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    override fun onResume() {

        super.onResume()

        map.onResume()
    }

    override fun onPause() {
        fusedLocationClient.removeLocationUpdates(
            locationCallback
        )

        super.onPause()

        map.onPause()
    }
}