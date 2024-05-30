package com.example.rma

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
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
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.NavController
@Composable
fun MapScreen(navController: NavController) {
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
        }
    )

    LaunchedEffect(key1 = true) {
        if (!hasLocationPermission) {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    if (hasLocationPermission) {
        AndroidView(
            factory = { context ->
                MapView(context).apply {
                    onCreate(null)
                    onResume()
                    getMapAsync { googleMap ->
                        val osijekLocation = LatLng(45.5549, 18.6956) // Osijek Coordinates

                        // Move the camera to Osijek
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(osijekLocation, 12f))

                        // Enable location layer if permission granted
                        googleMap.isMyLocationEnabled = true

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

                        // Fetch locations from Firestore
                        FirebaseFirestore.getInstance().collection("locations")
                            .get()
                            .addOnSuccessListener { documents ->
                                for (document in documents) {

                                    val latitude = document.getDouble("latitude")
                                    val longitude = document.getDouble("longitude")

                                    if (latitude != null && longitude != null) {
                                        val location = LatLng(latitude, longitude)
                                        googleMap.addMarker(
                                            MarkerOptions()
                                                .position(location)

                                        )?.tag = document.id // Set the document ID as the tag
                                    }
                                }

                                googleMap.setOnMarkerClickListener { marker ->
                                    val documentId = marker.tag as? String
                                    if (documentId != null) {
                                        navController.navigate("locationDetail/$documentId")
                                    }
                                    true
                                }
                            }
                    }
                }
            },
            modifier = Modifier.fillMaxSize()
        )
    } else {
        // Handle the case when permission is not granted
        Text(
            text = "Permission not granted for accessing location.",
            modifier = Modifier.fillMaxSize(),
            textAlign = TextAlign.Center
        )
    }
}

