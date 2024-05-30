package com.example.rma

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.UiSettings

@Composable
fun MapScreen(apiKey: String) {
    val context = LocalContext.current

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted ->
            hasLocationPermission = granted
            (context as MainActivity).recreate()
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    AndroidView(
        factory = { context ->
            MapView(context).apply {
                onCreate(null)
                onResume()
                getMapAsync { googleMap ->
                    val osijekLocation = LatLng(45.5549, 18.6956) // Osijek Coordinates

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(osijekLocation)
                            .title("Marker in Osijek")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(osijekLocation, 12f))

                    // Enable location layer if permission granted
                    if (hasLocationPermission) {
                        googleMap.isMyLocationEnabled = true
                    }

                    // Apply custom map style to show only streets
                    googleMap.setMapStyle(
                        MapStyleOptions(
                            """
                            [
                                
                              {
                              "featureType": "poi",
                              "elementType": "all",
                              "stylers": [
                                { "visibility": "off" }
                              ]
                            },

                                {
                                    "featureType": "road",
                                    "elementType": "geometry",
                                    "stylers": [
                                        { "visibility": "simplified" }
                                    ]
                                }
                            ]
                            """.trimIndent()
                        )
                    )

                    // Adjust UI settings as needed
                    val uiSettings: UiSettings = googleMap.uiSettings
                    uiSettings.isZoomControlsEnabled = true
                }
            }
        },
        modifier = Modifier.fillMaxSize()

    )
}
