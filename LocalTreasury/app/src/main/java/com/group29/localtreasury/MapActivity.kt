package com.group29.localtreasury

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import android.Manifest
import android.location.Address
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import com.google.android.libraries.places.api.Places
import java.io.IOException

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private var currentLocationMarker: Marker? = null
    private var itemLocationMarker: Marker? = null
    private var itemAddress: String? = ""
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.MAPS_API_KEY

        // Log an error if apiKey is not set.
        if (apiKey.isEmpty() || apiKey == "DEFAULT_API_KEY") {
            Log.e("Places test", "No api key")
            finish()
            return
        }

        // Initialize the SDK
        Places.initializeWithNewPlacesApiEnabled(applicationContext, apiKey)

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Retrieve item address from Intent
        itemAddress = intent.getStringExtra("ITEM_ADDRESS")

        if (isGoogleMapsInstalled()) {
            launchGoogleMapsNavigation()
        } else {
            requestLocationPermission()
        }
    }

    private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                displayLocations()
            } else {
                Toast.makeText(this, "Location permission is required for this app", Toast.LENGTH_LONG).show()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            displayLocations()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun launchGoogleMapsNavigation() {
        if (itemAddress.isNullOrEmpty()) {
            Toast.makeText(this, "Item address not available", Toast.LENGTH_SHORT).show()
            return
        }

        val uri = "google.navigation:q=${itemAddress!!.replace(" ", "+")}"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri)).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Unable to launch Google Maps", Toast.LENGTH_SHORT).show()
            requestLocationPermission()
        }
    }

    private fun displayLocations() {
        if (itemAddress.isNullOrEmpty()) {
            Toast.makeText(this, "Item address not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get user's current location
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)

                    // Add current location marker
                    currentLocationMarker = googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title("Your Location")
                    )

                    // Resolve the item address using Geocoder
                    resolveAddressUsingGeocoder(itemAddress!!) { itemLatLng ->
                        if (itemLatLng != null) {
                            // Add item location marker
                            itemLocationMarker = googleMap.addMarker(
                                MarkerOptions()
                                    .position(itemLatLng)
                                    .title("Item Location")
                            )

                            itemLocationMarker?.isVisible = true

                            // Adjust the camera to include both markers
                            val boundsBuilder = LatLngBounds.Builder()
                            boundsBuilder.include(userLatLng) // Include current location
                            boundsBuilder.include(itemLatLng) // Include item location

                            val bounds = boundsBuilder.build()

                            // Move camera with bounds
                            googleMap.setOnMapLoadedCallback {
                                googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds, 100)) // 100 is the padding in pixels
                            }
                        } else {
                            Toast.makeText(this, "Unable to determine item's location", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Unable to get your location", Toast.LENGTH_SHORT).show()
                }
            }

            startLocationUpdates()
        }
    }

    private fun resolveAddressUsingGeocoder(address: String, callback: (LatLng?) -> Unit) {
        try {
            val geocoder = Geocoder(this)
            val addressList = geocoder.getFromLocationName(address, 1)
            if (!addressList.isNullOrEmpty()) {
                val location = addressList[0]
                callback(LatLng(location.latitude, location.longitude))
            } else {
                callback(null)
            }
        } catch (e: IOException) {
            Log.e("Geocoder", "Error resolving address", e)
            callback(null)
        }
    }

    private fun isGoogleMapsInstalled(): Boolean {
        return try {
            packageManager.getPackageInfo("com.google.android.apps.maps", 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    private fun startLocationUpdates() {
        handler.postDelayed(object : Runnable {
            override fun run() {
                if (ActivityCompat.checkSelfPermission(this@MapActivity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                    fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                        if (location != null) {
                            val newLatLng = LatLng(location.latitude, location.longitude)
                            currentLocationMarker?.position = newLatLng
                            googleMap.moveCamera(CameraUpdateFactory.newLatLng(newLatLng))
                        }
                    }
                    handler.postDelayed(this, 60000) // Update every 60 seconds
                }
            }
        }, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }
}