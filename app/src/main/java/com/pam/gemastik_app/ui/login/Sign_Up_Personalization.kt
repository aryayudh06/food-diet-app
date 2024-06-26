package com.pam.gemastik_app.ui.login

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivitySignUpPersonalizationBinding
import com.pam.gemastik_app.ui.MainActivity
import java.util.Calendar

class Sign_Up_Personalization : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpPersonalizationBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpPersonalizationBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val genderSpinner = binding.genderSpinner
        val genderAdapter = ArrayAdapter.createFromResource(this, R.array.gender, android.R.layout.simple_spinner_item)

        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        genderSpinner.adapter = genderAdapter
        genderSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                Toast.makeText(parent.context, "Selected: ${parent.getItemAtPosition(position)}", Toast.LENGTH_LONG).show()
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
                Toast.makeText(parent.context, "Selected: ${parent.getItemAtPosition(position)}", Toast.LENGTH_LONG).show()
            }
            override fun onNothingSelected(parent: AdapterView<*>) {
                // Do nothing
            }
        }

        binding.selectDateOfBirth.setOnClickListener {

            val calendar = Calendar.getInstance()
            var year = calendar.get(Calendar.YEAR)
            var month = calendar.get(Calendar.MONTH)
            var day = calendar.get(Calendar.DAY_OF_MONTH)

            val datePickerDialog = DatePickerDialog(this@Sign_Up_Personalization,
                DatePickerDialog.OnDateSetListener { view, year, month, dayOfMonth ->
                    val selectedDate = "$dayOfMonth/${month + 1}/$year"
                    binding.selectDateOfBirth.text = selectedDate
                }, year, month, day)
            datePickerDialog.show()
        }

        binding.nextButton.setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }
}