package com.pam.gemastik_app.ui.healthconnect

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.pam.gemastik_app.R
import com.pam.gemastik_app.model.healthconnect.HealthConnectManager
import kotlinx.coroutines.launch
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZonedDateTime
import java.time.temporal.ChronoUnit
import kotlin.random.Random

class HealthConnectTest : AppCompatActivity() {
    private lateinit var requestPermissionsLauncher: ActivityResultLauncher<Set<String>>
    private lateinit var healthConnectManager: HealthConnectManager
    private var permissionsGranted: Boolean = false

    val permissions = setOf(
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(ExerciseSessionRecord::class),
        HealthPermission.getWritePermission(HeartRateRecord::class),
        HealthPermission.getWritePermission(StepsRecord::class),
        HealthPermission.getWritePermission(DistanceRecord::class),
        HealthPermission.getWritePermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class)
//        HealthPermission.getReadPermission()
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_health_connect_test)
        healthConnectManager = HealthConnectManager(this)
        Log.d("hc test", healthConnectManager.toString())
        requestPermissionsLauncher = registerForActivityResult(
            healthConnectManager.requestPermissionsActivityContract()
        ) { grantedPermissions ->
            if (grantedPermissions.containsAll(permissions)) {
                Log.d("hc test", "All permissions granted")
                loadHealthData()
            } else {
                Log.d("hc test", "Permissions not granted in launcher callback")
                Toast.makeText(
                    this,
                    "Permissions required to access health data",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

// Call launch to request permissions
        requestPermissionsLauncher.launch(permissions)

        var btn = findViewById<Button>(R.id.btGen)
        btn.setOnClickListener(){
            insertExerciseSession()
        }
//        lifecycleScope.launch {
//            Log.d("hc test 2", "scope on")
//            tryWithPermissionsCheck {
//                Log.d("hc test 3", "scope in")
//                loadHealthData()
//            }
//        }
    }

    private fun insertExerciseSession() {
        lifecycleScope.launch {
                Log.d("insert 1", "scope in")
                val startOfDay = ZonedDateTime.now().truncatedTo(ChronoUnit.DAYS)
                val latestStartOfSession = ZonedDateTime.now().minusMinutes(30)
                val offset = Random.nextDouble()

                // Generate random start time between the start of the day and (now - 30mins).
                val startOfSession = startOfDay.plusSeconds(
                    (Duration.between(startOfDay, latestStartOfSession).seconds * offset).toLong()
                )
                val endOfSession = startOfSession.plusMinutes(30)
                Log.d("insert 2", "scope in")
                healthConnectManager.writeExerciseSession(startOfSession, endOfSession)
                Log.d("insert 3", "finish")
                loadHealthData()
            }
        }


    private fun loadHealthData() {
        // Mengambil waktu awal hari ini dalam zona waktu lokal tanpa mengonversi ke UTC
        val startOfDay = LocalDateTime.now().with(LocalTime.MIN)

        // Menggunakan akhir hari ini dalam zona waktu lokal
        val endOfDay = LocalDateTime.now()

        // Jika perlu konversi ke Instant, lakukan di sini
//        val startOfDayInstant = startOfDay.toInstant()
//        val endOfDayInstant = endOfDay.toInstant()
        lifecycleScope.launch {
            val caloriesBurned = healthConnectManager.readCaloriesBurnedToday()
            findViewById<TextView>(R.id.tvCalBurned).text =
                "Calories Burned Today: ${caloriesBurned?: "No data"} kcal"
            Log.d("records", caloriesBurned.toString())

            Log.d("date", "$startOfDay $endOfDay")
        }
    }

}
