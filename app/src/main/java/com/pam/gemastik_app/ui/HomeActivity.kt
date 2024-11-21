package com.pam.gemastik_app.ui

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.units.Mass
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.github.mikephil.charting.data.BarEntry
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityHomeBinding
import com.pam.gemastik_app.model.FoodModel
import com.pam.gemastik_app.model.Permission
import com.pam.gemastik_app.model.healthconnect.HealthConnectAppInfo
import com.pam.gemastik_app.model.healthconnect.HealthConnectManager
import com.pam.gemastik_app.model.healthconnect.WeightData
import com.pam.gemastik_app.model.healthconnect.dateTimeWithOffsetOrDefault
import com.pam.gemastik_app.thread.CalorieAccess
import com.pam.gemastik_app.thread.NotificationThreads
import com.pam.gemastik_app.ui.adapter.FoodAdapter
import com.pam.gemastik_app.ui.fragment.ChartFragment
import com.pam.gemastik_app.ui.fragment.MenuFragment
import kotlinx.coroutines.launch
import org.checkerframework.common.returnsreceiver.qual.This
import java.text.DecimalFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeAdapter: FoodAdapter
    private lateinit var firebaseDatabase: FirebaseDatabase
    private lateinit var databaseReference: DatabaseReference
    private val homeFood: MutableList<FoodModel> = ArrayList()
    private lateinit var notificationThreads: NotificationThreads
    private lateinit var requestPermissionLauncher: ActivityResultLauncher<String>
    private lateinit var healthConnectManager: HealthConnectManager
    private lateinit var healthConnectCompatibleApps:  Map<String, HealthConnectAppInfo>
    private var readingsList: MutableState<List<WeightData>> = mutableStateOf(listOf())

    private val calorieAccess: CalorieAccess = CalorieAccess()

    private val apiKey = BuildConfig.GEMINI_KEY

    companion object{
        lateinit var auth: FirebaseAuth
    }

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        healthConnectManager = HealthConnectManager(this)
        healthConnectCompatibleApps = healthConnectManager.healthConnectCompatibleApps

        requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                // Permission granted
            } else {
                // Permission denied
            }
        }

        notificationThreads = NotificationThreads(this, requestPermissionLauncher)

        lifecycleScope.launch{
            if (healthConnectManager.hasAllPermissions(permissions)) {
                Permission.exercisePermission = true
                Permission.caloriesBurnedPermission = true
                Permission.distancePermission = true
                Permission.stepsPermission = true
                Permission.heartRatePermission = true

                loadHealthData()
            }
        }

        FirebaseApp.initializeApp(this)

        firebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FB_DB_KEY)
        databaseReference = firebaseDatabase.getReference()

        val homeLM = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvHome.layoutManager = homeLM

        homeAdapter = FoodAdapter(this, homeFood)
        binding.rvHome.adapter = homeAdapter

        val fragment1: Fragment = MenuFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.fragment_menu_container, fragment1).commit()

        val barFragment: Fragment = ChartFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.chartMainContainer, barFragment).commit()

        binding.HelloUser.text = "Hello ${auth.currentUser?.displayName?: "User"}"

        loadBMI()
        val calendar: Calendar = Calendar.getInstance()
        val year: Int = calendar.get(Calendar.YEAR)
        val month: Int = calendar.get(Calendar.MONTH) + 1 // Bulan dimulai dari 0
        val day: Int = calendar.get(Calendar.DAY_OF_MONTH)
        binding.tvDate.text = "${day}-${month}-${year}"
        val hour: Int = calendar.get(Calendar.HOUR_OF_DAY)
        val partOfDay: String = when (hour) {
            in 6..11 -> "Morning"
            in 12..17 -> "Afternoon"
            else -> "Evening"
        }
        val pressedBtn = ContextCompat.getDrawable(this, R.drawable.bt_yellow)
        val unpressedBtn = ContextCompat.getDrawable(this, R.drawable.edit_text_background)

        val pressedText = ContextCompat.getColor(this, R.color.text)
        val unpressedText = ContextCompat.getColor(this, R.color.mid_grey)

        sendPartOfDayNotification(partOfDay)
        fetch(partOfDay, pressedBtn, unpressedBtn, unpressedText, pressedText)

        binding.tvBtnBreakfast.setOnClickListener {
            breakfast()
            binding.tvBtnBreakfast.background = pressedBtn
            binding.tvBtnBreakfast.setTextColor(pressedText)
            binding.tvBtnLunch.background = unpressedBtn
            binding.tvBtnLunch.setTextColor(unpressedText)
            binding.tvBtnDinner.background = unpressedBtn
            binding.tvBtnDinner.setTextColor(unpressedText)
        }

        binding.tvBtnLunch.setOnClickListener {
            lunch()
            binding.tvBtnBreakfast.background = unpressedBtn
            binding.tvBtnBreakfast.setTextColor(unpressedText)
            binding.tvBtnLunch.background = pressedBtn
            binding.tvBtnLunch.setTextColor(pressedText)
            binding.tvBtnDinner.background = unpressedBtn
            binding.tvBtnDinner.setTextColor(unpressedText)
        }

        binding.tvBtnDinner.setOnClickListener {
            dinner()
            binding.tvBtnBreakfast.background = unpressedBtn
            binding.tvBtnBreakfast.setTextColor(unpressedText)
            binding.tvBtnLunch.background = unpressedBtn
            binding.tvBtnLunch.setTextColor(unpressedText)
            binding.tvBtnDinner.background = pressedBtn
            binding.tvBtnDinner.setTextColor(pressedText)
        }

        binding.textView18.setOnClickListener {
            val intent = Intent(it.context, RecommendationActivity::class.java)
            startActivity(intent)
        }
    }

    private fun fetch(
        partOfDay: String,
        pressedBtn: Drawable?,
        unpressedBtn: Drawable?,
        unpressedText: Int,
        pressedText: Int
    ) {
        when (partOfDay) {
            "Morning" -> {
                breakfast()
                binding.tvBtnBreakfast.background = pressedBtn
                binding.tvBtnBreakfast.setTextColor(pressedText)
                binding.tvBtnLunch.background = unpressedBtn
                binding.tvBtnLunch.setTextColor(unpressedText)
                binding.tvBtnDinner.background = unpressedBtn
                binding.tvBtnDinner.setTextColor(unpressedText)
            }
            "Afternoon" -> {
                lunch()
                binding.tvBtnBreakfast.background = unpressedBtn
                binding.tvBtnBreakfast.setTextColor(unpressedText)
                binding.tvBtnLunch.background = pressedBtn
                binding.tvBtnLunch.setTextColor(pressedText)
                binding.tvBtnDinner.background = unpressedBtn
                binding.tvBtnDinner.setTextColor(unpressedText)
            }
            else -> {
                dinner()
                binding.tvBtnBreakfast.background = unpressedBtn
                binding.tvBtnBreakfast.setTextColor(unpressedText)
                binding.tvBtnLunch.background = unpressedBtn
                binding.tvBtnLunch.setTextColor(unpressedText)
                binding.tvBtnDinner.background = pressedBtn
                binding.tvBtnDinner.setTextColor(pressedText)
            }
        }
    }

    private fun dinner() {
        databaseReference.child("Foods").child("Dinner").addValueEventListener(object :
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                homeFood.clear()
                for (dataSnapshot in snapshot.children) {
                    val dinner = dataSnapshot.getValue(FoodModel::class.java)
                    if (dinner != null) {
                        homeFood.add(dinner)
                    }
                }
                homeAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database error", error.toString())
                Toast.makeText(this@HomeActivity, "Failed to load dinner data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun lunch() {
        databaseReference.child("Foods").child("Lunch").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                homeFood.clear()
                for (dataSnapshot in snapshot.children) {
                    val lunch = dataSnapshot.getValue(FoodModel::class.java)
                    if (lunch != null) {
                        homeFood.add(lunch)
                    }
                }
                homeAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Database error", error.toString())
                Toast.makeText(this@HomeActivity, "Failed to load lunch data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun breakfast() {
        databaseReference.child("Foods").child("Breakfast").addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                homeFood.clear()
                for (dataSnapshot in snapshot.children) {
                    val breaky = dataSnapshot.getValue(FoodModel::class.java)
                    if (breaky != null) {
                        homeFood.add(breaky)
                    }
                }
                homeAdapter.notifyDataSetChanged()
            }
            override fun onCancelled(error: DatabaseError) {
                Log.e("Database error", error.toString())
                Toast.makeText(this@HomeActivity, "Failed to load breakfast data", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun sendPartOfDayNotification(partOfDay: String) {
        val title = "Good $partOfDay"
        val message = when (partOfDay) {
            "Morning" -> "Start your day with a healthy breakfast and a positive mindset!"
            "Afternoon" -> "Keep pushing through your tasks. Stay hydrated!"
            "Evening" -> "Relax and unwind. You've earned it!"
            else -> "Hello there!"
        }

        // Send notification
        notificationThreads.sendNotificationInThread(message, title)
    }

    private fun loadBMI() {
        calorieAccess.getCalorieData { calorieList ->
            val userCalorie = calorieList.lastOrNull()
            if (userCalorie != null) {
                val bbValue = userCalorie.bb?.toIntOrNull() ?: 0
                val tbValue = userCalorie.tb?.toIntOrNull() ?: 0

                if (tbValue > 0) { // Avoid division by zero
                    val heightInMeters = tbValue / 100.0
                    val bmi = bbValue / (heightInMeters * heightInMeters)
                    val formattedBmi = DecimalFormat("#.##").format(bmi)

                    val bmiCategory = when {
                        bmi < 18.5 -> "Underweight"
                        bmi in 18.5..24.9 -> "Ideal"
                        bmi in 25.0..29.9 -> "Overweight"
                        else -> "Obesity"
                    }

                    binding.tvBMI.text = formattedBmi
                    binding.tvBMICategory.text = bmiCategory
                    binding.tvHeight.text = "${tbValue}Cm"
                    binding.tvWeight.text = "${bbValue}Kg"
                } else {
                    binding.tvBMI.text = "N/A"
                }
            } else {
                Log.d("Calorie Data", "No data available")
                binding.tvBMI.text = "N/A"
                binding.tvHeight.text = "N/A"
                binding.tvWeight.text = "N/A"
            }
        }
    }

    private fun loadHealthData() {
        lifecycleScope.launch {
            val remaining = calorieAccess.getRemainingCalories(healthConnectManager.readCaloriesBurnedToday())
            calorieAccess.updateCalories("-$remaining")
            calorieAccess.updateBurnedCalories(remaining)
        }
    }
}