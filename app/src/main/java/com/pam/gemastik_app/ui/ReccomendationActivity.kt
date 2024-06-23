package com.pam.gemastik_app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.pam.gemastik_app.databinding.ActivityReccomendationBinding
import org.json.JSONObject

class RecommendationActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var api_id1 = "abb8bc51aeed4ca6b5f9fc481a0ea6de"
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityReccomendationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityReccomendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        binding.btVar1.setOnClickListener {
            if (checkAndRequestPermissions()) {
                obtainLocation()
            }
        }
    }

    private fun checkAndRequestPermissions(): Boolean {
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, listPermissionsNeeded.toTypedArray(), LOCATION_PERMISSION_REQUEST_CODE)
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation() {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val weather_url1 = "https://api.weatherbit.io/v2.0/current?lat=${location.latitude}&lon=${location.longitude}&key=$api_id1"
                    getTemp(weather_url1)
                } else {
                    Log.e("Location Error", "Location is null")
                    binding.textView.text = "Unable to obtain location"
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location Error", e.message ?: "Unknown error")
                binding.textView.text = "Failed to get location"
            }
    }

    private fun getTemp(weather_url1: String) {
        val queue = Volley.newRequestQueue(this)

        val stringReq = StringRequest(Request.Method.GET, weather_url1,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val arr = obj.getJSONArray("data")
                    val obj2 = arr.getJSONObject(0)
                    val temperature = obj2.getString("temp")
                    val cityName = obj2.getString("city_name")
                    binding.textView.text = "$temperature °C in $cityName"
                } catch (e: Exception) {
                    Log.e("JSON Error", e.message ?: "Unknown error")
                    binding.textView.text = "Error parsing weather data"
                }
            },
            { error ->
                Log.e("Volley Error", error.message ?: "Unknown error")
                binding.textView.text = "That didn't work!"
            }
        )

        queue.add(stringReq)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, proceed with the action
                    obtainLocation()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
                return
            }
        }
    }
}
