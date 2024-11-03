package com.pam.gemastik_app.ui

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityFoodTrackingBinding
import com.pam.gemastik_app.thread.ModelTask
import com.pam.gemastik_app.ui.fragment.MenuFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FoodTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodTrackingBinding
    private val apiKey = BuildConfig.GEMINI_KEY
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance("https://gemastik-a8145-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private val databaseReference: DatabaseReference = firebaseDatabase.reference

    // Variables to store parsed data
    private var menu: String = ""
    private var calorie: String = ""
    private var protein: String = ""
    private var minerals: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment1: Fragment = MenuFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.flMenuFdetails, fragment1).commit()

        foodRecog()

        binding.ibCancel.setOnClickListener {
            finish()
        }

        binding.ibConfirm.isEnabled = false

        binding.ibConfirm.setOnClickListener {
            saveData(calorie, menu, protein)
            Intent(this, HomeActivity::class.java)
        }
    }

    private fun saveData(calorie: String, menu: String, protein: String) {
        val userId = mAuth.uid ?: return
        val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyData = databaseReference.child("user_food_tracking").child(userId).child("daily_data").child(currDate)

        val dataUser = mapOf(
            "menu" to menu,
            "calorie" to calorie,
            "protein" to protein,
        )

        dailyData.setValue(dataUser).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Toast.makeText(this, "Data stored successfully", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Data saving failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }.addOnFailureListener { exception ->
            Toast.makeText(this, "Data saving failed: ${exception.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun foodRecog() {
        val runnable = Runnable {
            val imageUriString = intent.getStringExtra("imageUri")
            if (imageUriString != null) {
                val imageUri = Uri.parse(imageUriString)
                updateImage(imageUri)
                val bitmapImg: Bitmap? = uriToBitmap(contentResolver, imageUri)
                CoroutineScope(Dispatchers.Main).launch {
                    val modelTask = ModelTask(apiKey)
                    val responseText = bitmapImg?.let { modelTask.executeModelCall(it) }
                    val split = responseText?.split(";")?.map { it.trim() } ?: emptyList()

                    // Parse response data
                    menu = split.getOrNull(0) ?: "Unknown"
                    calorie = split.getOrNull(1) ?: "0 kcal"
                    protein = split.getOrNull(2) ?: "0 grams protein"
                    minerals = split.getOrNull(3) ?: ""

                    binding.tvRecordMenu.text = menu
                    binding.tvResep1.text = "- $calorie\n- $protein\n- $minerals"
                    binding.ibConfirm.isEnabled = true
                }
            }
        }
        val recog = Thread(runnable)
        recog.start()
    }

    private fun updateImage(image: Uri) {
        binding.ivBackground.setImageURI(image)
        binding.ibUploadFoto.setImageURI(image)
    }

    private fun uriToBitmap(contentResolver: ContentResolver, uri: Uri): Bitmap? {
        return try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            BitmapFactory.decodeStream(inputStream).also {
                inputStream?.close()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
