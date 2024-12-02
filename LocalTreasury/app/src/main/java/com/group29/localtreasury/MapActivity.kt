package com.group29.localtreasury

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.EditText
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
import android.net.Uri
import android.util.Log
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.api.net.FindCurrentPlaceRequest

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var etAddress: EditText
    private lateinit var btnSearch: Button
    private var currentLocationMarker: Marker? = null
    private var itemLocationMarker: Marker? = null
    private var itemAddress: String? = ""
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        // Define a variable to hold the Places API key.
        val apiKey = BuildConfig.PLACES_API_KEY

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
        displayLocations()
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
        }
    }

    private fun displayLocations() {
        val localItemAddress: String = itemAddress ?: ""
        if (itemAddress.isNullOrEmpty()) {
            Toast.makeText(this, "Item address not available", Toast.LENGTH_SHORT).show()
            return
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            // Get user's current location
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    currentLocationMarker = googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title("Your Location")
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))
                }
            }

            // Use Places API to find the item's location
            val placesClient = Places.createClient(this)
            val request = FindCurrentPlaceRequest.newInstance(listOf(Place.Field.ID, Place.Field.LAT_LNG, Place.Field.NAME))

            placesClient.findCurrentPlace(request).addOnSuccessListener { response ->
                val foundPlace = response.placeLikelihoods.firstOrNull { it.place.name?.contains(localItemAddress, ignoreCase = true) == true }

                if (foundPlace != null) {
                    val itemLatLng = foundPlace.place.latLng
                    if (itemLatLng != null) {
                        itemLocationMarker = googleMap.addMarker(
                            MarkerOptions()
                                .position(itemLatLng)
                                .title("Item Location")
                                .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
                        )
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(itemLatLng, 12f))
                    } else {
                        Toast.makeText(this, "Unable to determine item's location", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(this, "Item address not found using Places API", Toast.LENGTH_SHORT).show()
                }
            }.addOnFailureListener {
                Toast.makeText(this, "Places API failed to retrieve location", Toast.LENGTH_SHORT).show()
            }

            startLocationUpdates()
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