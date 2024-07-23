package com.pam.gemastik_app.ui.login

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pam.gemastik_app.R
import com.pam.gemastik_app.model.UserData
import com.pam.gemastik_app.databinding.ActivitySignUpPersonalizationBinding
import com.pam.gemastik_app.ui.MainActivity
import java.util.Calendar

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPersonalizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.extras
        if (bundle != null) {
            email = bundle.getString("email").toString()
            passwd = bundle.getString("passwd").toString()
        }

        val genderSpinner = binding.genderSpinner
        val genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item)
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        genderSpinner.adapter = genderAdapter
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedGender = parent.getItemAtPosition(position).toString()
                Toast.makeText(parent.context, "Selected: $selectedGender", Toast.LENGTH_LONG).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        val medsSpinner = binding.medsSpinner
        val medsAdapter = ArrayAdapter.createFromResource(this, R.array.meds, android.R.layout.simple_spinner_item)
        medsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        medsSpinner.adapter = medsAdapter
        medsSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                selectedMed = parent.getItemAtPosition(position).toString()
                Toast.makeText(parent.context, "Selected: $selectedMed", Toast.LENGTH_LONG).show()
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.selectDateOfBirth.setOnClickListener {
            val calendar = Calendar.getInstance()
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this@Sign_Up_Personalization, DatePickerDialog.OnDateSetListener { _, selectedYear, selectedMonth, dayOfMonth ->
                selectedDateOfBirth = "$dayOfMonth/${selectedMonth + 1}/$selectedYear"
                binding.selectDateOfBirth.text = selectedDateOfBirth
            }, year, month, day)
            datePickerDialog.show()
        }

        binding.nextButton.setOnClickListener {
            createUserAndSaveData(email, passwd)
        }
    }

    private fun createUserAndSaveData(email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener { signInTask ->
                        if (signInTask.isSuccessful) {
                            saveDataToFirebase()
                        } else {
                            Toast.makeText(this, signInTask.exception?.localizedMessage, Toast.LENGTH_LONG).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Authentication failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun saveDataToFirebase() {
        val userId = mAuth.currentUser?.uid ?: return
        val userData = UserData(selectedGender, selectedMed, selectedDateOfBirth)
        databaseReference.child("user_personalization").child(userId).setValue(userData)
            .addOnSuccessListener {
                Toast.makeText(this, "Data saved successfully!", Toast.LENGTH_LONG).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save data: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
