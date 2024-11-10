package com.pam.gemastik_app.ui.login

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityUserDataBinding
import com.pam.gemastik_app.thread.CalorieAccess
import com.pam.gemastik_app.ui.HomeActivity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class UserDataActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUserDataBinding

    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance("https://gemastik-a8145-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var databaseReference: DatabaseReference = firebaseDatabase.reference

    private var selectedAct: Double? = null
    private var age: Int? = null

    private val calorieAccess: CalorieAccess = CalorieAccess()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUserDataBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            age = bundle.getInt("age")
        }

        val actSpinner = binding.actSpinner
        val actAdapter = ArrayAdapter.createFromResource(this, R.array.actIntensity, android.R.layout.simple_spinner_item)
        actAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        actSpinner.adapter = actAdapter
        actSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p0: AdapterView<*>, p1: View?, p2: Int, p3: Long) {
                selectedAct = when (p2) {
                    0 -> 1.2    // Sedentary
                    1 -> 1.375  // Lightly active
                    2 -> 1.55   // Moderately active
                    3 -> 1.725  // Very active
                    4 -> 1.9    // Super active
                    else -> 1.2 // Default to sedentary
                }
                Toast.makeText(p0.context, "Selected activity level factor: $selectedAct", Toast.LENGTH_LONG).show()
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
                TODO("Not yet implemented")
            }

        }

        binding.tvNextButton.setOnClickListener {
            val weightInputText = binding.etWeightInput.text.toString()
            val heightInputText = binding.etHeightInput.text.toString()

            if (weightInputText.isEmpty() || heightInputText.isEmpty()) {
                Toast.makeText(this, "Please enter both weight and height", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val weightInput = weightInputText.toIntOrNull()
            val heightInput = heightInputText.toIntOrNull()

            if (weightInput == null || heightInput == null) {
                Toast.makeText(this, "Please enter valid numeric values for weight and height", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val userGender = bundle?.getString("gender")?.lowercase() ?: "unknown"
            if (userGender == "unknown") {
                Toast.makeText(this, "Gender information is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (age != null) {
                val calorieIntake = selectedAct?.let { calculateDailyCalories(weightInput, heightInput, age!!, userGender, it) }
                if (calorieIntake != null) {
                    saveCalorieIntakeToFirebase(heightInput, weightInput, calorieIntake)
                    Toast.makeText(this, "Daily Calorie Intake: $calorieIntake", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(this, "Please enter valid weight, height, and age", Toast.LENGTH_SHORT).show()
            }

            startActivity(Intent(this, HomeActivity::class.java))
        }


    }

    private fun saveCalorieIntakeToFirebase(heightInput: Int, weightInput: Int, calorieIntake: Any?) {
        val userId = mAuth.currentUser?.uid

        if (userId == null) {
            Toast.makeText(this, "User not authenticated. Please sign in again.", Toast.LENGTH_SHORT).show()
            return
        }

        calorieAccess.saveData(heightInput.toString(), weightInput.toString(), calorieIntake.toString())

    }


    private fun calculateDailyCalories(weightInput: Int, heightInput: Int, age: Int, userGender: String, selectedAct: Double): Any {
        val bmr = if (userGender == "male") {
            (10 * weightInput) + (6.25 * heightInput) - (5 * age) + 5
        } else {
            (10 * weightInput) + (6.25 * heightInput) - (5 * age) - 161
        }
        return (bmr * selectedAct).toInt()
    }
}