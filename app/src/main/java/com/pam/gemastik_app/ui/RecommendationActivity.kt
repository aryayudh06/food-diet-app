package com.pam.gemastik_app.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.firebase.FirebaseApp
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityRecommendationBinding
import com.pam.gemastik_app.ml.FoodClassificationModel
import com.pam.gemastik_app.model.FoodModel
import com.pam.gemastik_app.thread.CalorieAccess
import com.pam.gemastik_app.thread.NotificationThreads
import com.pam.gemastik_app.ui.adapter.FoodAdapter
import com.pam.gemastik_app.ui.fragment.MenuFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import org.json.JSONObject
import org.tensorflow.lite.DataType
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.nio.ByteBuffer
import java.nio.ByteOrder

class RecommendationActivity : AppCompatActivity() {

    private val LOCATION_PERMISSION_REQUEST_CODE = 1
    private var api_id1 = BuildConfig.WEATHERBIT_KEY
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var binding: ActivityRecommendationBinding
    private lateinit var recommendationAdapter: FoodAdapter
    private val Recommendations: MutableList<FoodModel> = ArrayList()
    private val RecommendationsFood:ArrayList<String> = ArrayList()
    private lateinit var breakyAdapter: FoodAdapter
    private lateinit var lunchAdapter: FoodAdapter
    private lateinit var dinnerAdapter: FoodAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private lateinit var calorieAccess: CalorieAccess
    private lateinit var notificationThreads: NotificationThreads
    private val Breaky: MutableList<FoodModel> = ArrayList()
    private val Lunch: MutableList<FoodModel> = ArrayList()
    private val Dinner: MutableList<FoodModel> = ArrayList()
    private var reccmd = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecommendationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        FirebaseApp.initializeApp(this)

        firebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FB_DB_KEY)
        databaseReference = firebaseDatabase.getReference()

        val fragment1: MenuFragment = MenuFragment.newInstance(this::class.java.simpleName)

        supportFragmentManager.beginTransaction().replace(R.id.flMenu, fragment1).commit()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        reccmd = calculateRecommendation()
//        RecommendationsFood.add(reccmd)


        val recommendationLM = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val breakyLM = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val lunchLM = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        val dinnerLM = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        binding.rvReccomendation.layoutManager = recommendationLM
        binding.rvBreaky.layoutManager = breakyLM
        binding.rvLunch.layoutManager = lunchLM
        binding.rvDinner.layoutManager = dinnerLM

        recommendationAdapter = FoodAdapter(this, Recommendations)
        breakyAdapter = FoodAdapter(this, Breaky)
        lunchAdapter = FoodAdapter(this, Lunch)
        dinnerAdapter = FoodAdapter(this, Dinner)

        binding.rvReccomendation.adapter = recommendationAdapter
        binding.rvBreaky.adapter = breakyAdapter
        binding.rvLunch.adapter = lunchAdapter
        binding.rvDinner.adapter = dinnerAdapter

        fetchFood()

        binding.moreBreaky.setOnClickListener {
            val runnable = Runnable {
                databaseReference.child("Foods").child("Breakfast")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Breaky.clear()
                            for (dataSnapshot in snapshot.children) {
                                val breaky = dataSnapshot.getValue(FoodModel::class.java)
                                if (breaky != null) {
                                    Breaky.add(breaky)
                                }
                            }
                            breakyAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e("Database error", error.toString())
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Failed to load more breakfast data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            val moreBreaky = Thread(runnable)
            moreBreaky.start()
            binding.moreBreaky.visibility = View.GONE
        }

        binding.moreLunch.setOnClickListener {
            val runnable = Runnable {
                databaseReference.child("Foods").child("Lunch")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Lunch.clear()
                            for (dataSnapshot in snapshot.children) {
                                val lunch = dataSnapshot.getValue(FoodModel::class.java)
                                if (lunch != null) {
                                    Lunch.add(lunch)
                                }
                            }
                            lunchAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Failed to load more lunch data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            val moreLunch = Thread(runnable)
            moreLunch.start()
            binding.moreLunch.visibility = View.GONE
        }

        binding.moreDinner.setOnClickListener {
            val runnable = Runnable {
                databaseReference.child("Foods").child("Dinner")
                    .addValueEventListener(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Dinner.clear()
                            for (dataSnapshot in snapshot.children) {
                                val dinner = dataSnapshot.getValue(FoodModel::class.java)
                                if (dinner != null) {
                                    Dinner.add(dinner)
                                }
                            }
                            dinnerAdapter.notifyDataSetChanged()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(
                                this@RecommendationActivity,
                                "Failed to load more dinner data",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    })
            }
            val moreDinner = Thread(runnable)
            moreDinner.start()
            binding.moreDinner.visibility = View.GONE
        }
    }

    private fun fetchFood() {
        val runnable = Runnable {
            databaseReference.child("Foods").child("Breakfast")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Recommendations.clear()
                        for (dataSnapshot in snapshot.children) {
                            val food = dataSnapshot.getValue(FoodModel::class.java)
                            if (food != null) {
                                Log.d("food", "food list: ${food.namaMenu}")
                                Log.d("food classified", reccmd)
                            }
                            if (food != null && food.namaMenu.equals(reccmd, ignoreCase = true)) {
                                Log.d("food", "Found matching food: ${food.namaMenu}")
                                Recommendations.add(food)
                            }
                        }
                            recommendationAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Failed to load recommendation data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            databaseReference.child("Foods").child("Breakfast")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Breaky.clear()
                        val children = snapshot.children.toList()
                        for (i in 0 until 1) {
                            val dataSnapshot = children[i]
                            val breaky = dataSnapshot.getValue(FoodModel::class.java)
                            if (breaky != null) {
                                Breaky.add(breaky)
                            }
                        }
                        breakyAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Failed to load breakfast data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            databaseReference.child("Foods").child("Lunch")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Lunch.clear()
                        val children = snapshot.children.toList()
                        for (i in 0 until 1) {
                            val dataSnapshot = children[i]
                            val lunch = dataSnapshot.getValue(FoodModel::class.java)
                            if (lunch != null) {
                                Lunch.add(lunch)
                            }
                        }
                        lunchAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Failed to load lunch data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })

            databaseReference.child("Foods").child("Dinner")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        Dinner.clear()
                        val children = snapshot.children.toList()
                        for (i in 0 until 1) {
                            val dataSnapshot = children[i]
                            val dinner = dataSnapshot.getValue(FoodModel::class.java)
                            if (dinner != null) {
                                Dinner.add(dinner)
                            }
                        }
                        dinnerAdapter.notifyDataSetChanged()
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(
                            this@RecommendationActivity,
                            "Failed to load dinner data",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                })
        }
        val net = Thread(runnable)
        net.start()
    }

//    private fun fetchFood() {
//        val runnable = Runnable {
//
//            // Mendapatkan hasil rekomendasi dari `calculateRecommendation()`
//            val reccomd = calculateRecommendation()
//
//            // Mendapatkan referensi ke kategori makanan
//            val categories = listOf("Breakfast", "Lunch", "Dinner")
//
//            for (category in categories) {
//                databaseReference.child("Foods").child(category)
//                    .addListenerForSingleValueEvent(object : ValueEventListener {
//                        override fun onDataChange(snapshot: DataSnapshot) {
//                            val foodList = when (category) {
//                                "Breakfast" -> Breaky
//                                "Lunch" -> Lunch
//                                "Dinner" -> Dinner
//                                else -> return
//                            }
//                            foodList.clear()
//                            for (dataSnapshot in snapshot.children) {
//                                val food = dataSnapshot.getValue(FoodModel::class.java)
//                                if (food != null) {
//                                    foodList.add(food)
//                                    Log.d("food", food.namaMenu)
//                                    // Cek apakah makanan ini adalah rekomendasi
//                                    if (food.namaMenu == reccomd) {
//                                        Recommendations.clear()
//                                        Recommendations.add(food)
//                                        Log.d("food", food.namaMenu)
//                                        recommendationAdapter.notifyDataSetChanged()
//                                    }
//                                }
//                            }
//
//                            when (category) {
//                                "Breakfast" -> breakyAdapter.notifyDataSetChanged()
//                                "Lunch" -> lunchAdapter.notifyDataSetChanged()
//                                "Dinner" -> dinnerAdapter.notifyDataSetChanged()
//                            }
//                        }
//
//                        override fun onCancelled(error: DatabaseError) {
//                            Toast.makeText(
//                                this@RecommendationActivity,
//                                "Failed to load $category data",
//                                Toast.LENGTH_SHORT
//                            ).show()
//                        }
//                    })
//            }
//        }
//
//        val net = Thread(runnable)
//        net.start()
//    }

    private fun checkAndRequestPermissions(): Boolean {
        val fineLocationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationPermission =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)

        val listPermissionsNeeded = ArrayList<String>()

        if (fineLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        if (coarseLocationPermission != PackageManager.PERMISSION_GRANTED) {
            listPermissionsNeeded.add(Manifest.permission.ACCESS_COARSE_LOCATION)
        }

        if (listPermissionsNeeded.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                listPermissionsNeeded.toTypedArray(),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return false
        }
        return true
    }

    @SuppressLint("MissingPermission")
    private fun obtainLocation(onResult: (String?) -> Unit) {
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val weather_url1 =
                        "https://api.weatherbit.io/v2.0/current?lat=${location.latitude}&lon=${location.longitude}&key=$api_id1"
                    getTemp({ temperature ->
                        if (temperature != "0") {
                            // Successfully received temperature data
                            onResult(temperature)
                        } else {
                            // Handle the error case
                            println("Failed to retrieve temperature data")
                        }
                    }, weather_url1)
                } else {
                    Log.e("Location Error", "Location is null")
                    Toast.makeText(this, "Unable to obtain location", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Log.e("Location Error", e.message ?: "Unknown error")
                Toast.makeText(this, "An error appeared while finding locaiton", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun getTemp(onResult: (String?) -> Unit, weather_url1: String) {
        val queue = Volley.newRequestQueue(this)

        val stringReq = StringRequest(Request.Method.GET, weather_url1,
            { response ->
                try {
                    val obj = JSONObject(response)
                    val arr = obj.getJSONArray("data")
                    val obj2 = arr.getJSONObject(0)
                    val temperature = obj2.getString("temp")
                    val cityName = obj2.getString("city_name")
                    Log.d("weather", "$cityName $temperature")
                    Toast.makeText(this, "City $cityName, Temp $temperature", Toast.LENGTH_SHORT)
                        .show()
                    // Check if it's warm and send a hydration reminder
                    if (temperature.toDouble() > 30.0) { // Set your desired threshold here
                        notificationThreads.sendNotificationInThread(
                            "It's quite warm outside! Remember to stay hydrated and drink extra water.",
                            "Hydration Reminder"
                        )
                    }
                    onResult(temperature)
                } catch (e: Exception) {
                    Log.e("JSON Error", e.message ?: "Unknown error")
                    Toast.makeText(this, "Error parsing weather data", Toast.LENGTH_SHORT).show()
                    onResult("27")
                }
            },
            { error ->
                Log.e("Volley Error", error.message ?: "Unknown error")
                Toast.makeText(this, "Failed to obtain weather data", Toast.LENGTH_SHORT).show()
                onResult("27")
            }
        )

        queue.add(stringReq)
    }

    private fun calculateRecommendation(): String {
        val location = if (checkAndRequestPermissions()) {
            calorieAccess = CalorieAccess()
            var persona = ""
            var temp = ""
            calorieAccess.getPersonalization { data ->
                if (data != null) {
                    persona = data
                }
            }
            obtainLocation { temperature ->
                if (temperature != null) {
                    temp = temperature
                }
            }
            var classified = classifyData(persona, temp)
            Log.d("classified", classified)
            return classified
        } else {
            return "Unknown"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    // Permission was granted, proceed with the action
                    calculateRecommendation()
                } else {
                    // Permission denied, show a message to the user
                    Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun classifyData(condition: String, temp: String?): String {
        // Load the model
        val model = FoodClassificationModel.newInstance(this)

        // Retrieve the condition array from resources
        val medsArray = resources.getStringArray(R.array.meds)

        // Get the index of the condition in medsArray
        var conditionIndex =
            medsArray.indexOf(condition).toFloat() // Convert to float for the model input
        if (conditionIndex == -1f) {
            // If condition is not found, return "Unknown"
            conditionIndex = 0F
        }

        // Parse temp to a float, or use a default value if null
        val temperature =
            temp?.toFloatOrNull() ?: 25.0f // Default to 25°C if temp is null or invalid

        // Prepare input tensor
        val inputFeature0 = TensorBuffer.createFixedSize(intArrayOf(1, 2), DataType.FLOAT32)

        // Convert input data into a ByteBuffer
        val byteBuffer = ByteBuffer.allocateDirect(2 * 4) // 2 inputs, each 4 bytes (float)
        byteBuffer.order(ByteOrder.nativeOrder())
        byteBuffer.putFloat(conditionIndex)
        byteBuffer.putFloat(temperature)

        inputFeature0.loadBuffer(byteBuffer)

        // Run model inference and get results
        val outputs = model.process(inputFeature0)
        val outputFeature0 = outputs.outputFeature0AsTensorBuffer

        // Post-process the output (assume it’s a probability array)
        val confidences = outputFeature0.floatArray
        val foodLabels = listOf("Ayam bakar", "Oatmeal", "Nasi goreng")
        val maxIndex = confidences.indices.maxByOrNull { confidences[it] } ?: -1

        // Close the model to release resources
        model.close()

        // Return the label with the highest confidence
        return if (maxIndex != -1) foodLabels[maxIndex] else "Unknown"
    }
}
