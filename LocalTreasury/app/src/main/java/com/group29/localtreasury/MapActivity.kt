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

class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var etAddress: EditText
    private lateinit var btnSearch: Button
    private var userAddress: String? = null
    private var destinationAddress: String? = null
    private var currentLocationMarker: Marker? = null
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_map)

        etAddress = findViewById(R.id.et_address)
        btnSearch = findViewById(R.id.btn_search)
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Request location permissions
        requestLocationPermission()

        btnSearch.setOnClickListener {
            val address = etAddress.text.toString()
            if (address.isNotEmpty()) {
                destinationAddress = address
                handleNavigationOrFallback(address)
            } else {
                Toast.makeText(this, "Please enter an address", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestLocationPermission() {
        val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                getUserAddress()
            } else {
                Toast.makeText(this, "Location permission is required for this app", Toast.LENGTH_LONG).show()
            }
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getUserAddress()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    private fun getUserAddress() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationProviderClient.lastLocation.addOnSuccessListener { location ->
                if (location != null) {
                    userAddress = "Your Current Location" // Placeholder for user address
                    val userLatLng = LatLng(location.latitude, location.longitude)
                    currentLocationMarker = googleMap.addMarker(
                        MarkerOptions().position(userLatLng).title(userAddress)
                    )
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                } else {
                    Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun handleNavigationOrFallback(address: String) {
        if (isGoogleMapsInstalled()) {
            startNavigationToAddress(address)
        } else {
            showAddressOnMap(address)
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

    private fun startNavigationToAddress(address: String) {
        val uri = "google.navigation:q=${address.replace(" ", "+")}"
        val intent = Intent(Intent.ACTION_VIEW, android.net.Uri.parse(uri)).apply {
            setPackage("com.google.android.apps.maps")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        } else {
            Toast.makeText(this, "Google Maps not installed", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showAddressOnMap(address: String) {
        googleMap.clear()

        if (userAddress != null) {
            // Add user's location marker
            currentLocationMarker?.let {
                googleMap.addMarker(
                    MarkerOptions()
                        .position(it.position)
                        .title(userAddress)
                )
            }
        }

        // Add destination marker as plain text
        val marker = MarkerOptions()
            .title(address)
            .snippet("Destination")
            .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED))
            .position(googleMap.cameraPosition.target) // Assuming it's centered at the map location
        googleMap.addMarker(marker)

        Toast.makeText(this, "Displaying markers for current location and destination.", Toast.LENGTH_SHORT).show()

        startLocationUpdates()
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