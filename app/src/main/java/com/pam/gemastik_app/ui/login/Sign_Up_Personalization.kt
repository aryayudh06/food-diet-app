package com.pam.gemastik_app.ui.login

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pam.gemastik_app.R
import com.pam.gemastik_app.model.UserData
import com.pam.gemastik_app.databinding.ActivitySignUpPersonalizationBinding
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class Sign_Up_Personalization : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpPersonalizationBinding
    private var selectedGender: String? = null
    private var selectedMed: String? = null
    private var selectedDateOfBirth: String? = null
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance("https://gemastik-a8145-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var databaseReference: DatabaseReference = firebaseDatabase.reference
    private var currUser: FirebaseUser? = mAuth.currentUser
    private var email: String = ""
    private var passwd: String = ""
    private var uname: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPersonalizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            email = bundle.getString("email").toString()
            passwd = bundle.getString("passwd").toString()
            uname = bundle.getString("uname").toString()
        }

        setupGenderSpinner()
        setupMedsSpinner()
        setupDatePicker()

        binding.nextButton.setOnClickListener {
            if (selectedDateOfBirth != null) {
                lifecycleScope.launch {
                    createUserAndSaveData(email, passwd)
                }
            } else {
                Toast.makeText(this, "Please select your date of birth", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupGenderSpinner() {
        val genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.genderSpinner.adapter = genderAdapter
        binding.genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedGender = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupMedsSpinner() {
        val medsAdapter = ArrayAdapter.createFromResource(this, R.array.meds, android.R.layout.simple_spinner_item)
        medsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.medsSpinner.adapter = medsAdapter
        binding.medsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMed = parent.getItemAtPosition(position).toString()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupDatePicker() {
        binding.selectDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this, { _, selectedYear, selectedMonth, dayOfMonth ->
                selectedDateOfBirth = "$dayOfMonth/${selectedMonth + 1}/$selectedYear"
                binding.selectDateOfBirth.text = selectedDateOfBirth
            }, year, month, day)
            //set limit
            val currentDateInMillis = calendar.timeInMillis
            datePickerDialog.datePicker.maxDate = currentDateInMillis

            datePickerDialog.show()
        }
    }

    private fun calculateAge(selectedDateOfBirth: String?): Int {
        selectedDateOfBirth?.let {
            val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val dateOfBirth = sdf.parse(it)
            dateOfBirth?.let {
                val dobCalendar = Calendar.getInstance()
                dobCalendar.time = dateOfBirth

                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - dobCalendar.get(Calendar.YEAR)

                if (today.get(Calendar.DAY_OF_YEAR) < dobCalendar.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }
                return age
            }
        }
        return 0
    }

    private suspend fun createUserAndSaveData(email: String, password: String) {
        try {
            // Create user using Firebase Auth
            val authResult = mAuth.createUserWithEmailAndPassword(email, password).await()
            val user = authResult.user

            // Update user profile with username
            user?.updateProfile(UserProfileChangeRequest.Builder().setDisplayName(uname).build())?.await()

            // Save user data to Firebase Database
            saveDataToFirebase(user)
        } catch (e: Exception) {
            Toast.makeText(this, "Error: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private suspend fun saveDataToFirebase(user: FirebaseUser?) {
        val age = calculateAge(selectedDateOfBirth)

        val sendBundle = Bundle()
        sendBundle.putString("gender", selectedGender)
        sendBundle.putInt("age", age)

        user?.let {
            val userId = it.uid

            val userData = UserData(selectedGender, selectedMed, selectedDateOfBirth)
            databaseReference.child("user_personalization").child(userId).setValue(userData).await()

            // Send data to next activity
            val intent = Intent(this, UserDataActivity::class.java)

            Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_LONG).show()
            startActivity(intent)
            finish()
        }
    }
    }

