package com.pam.gemastik_app.ui

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityFoodDetailsBinding
import com.pam.gemastik_app.databinding.ActivityFoodTrackingBinding
import com.pam.gemastik_app.thread.ModelTask
import com.pam.gemastik_app.ui.fragment.MenuFragment
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream

class FoodTrackingActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFoodTrackingBinding
    private val apiKey = BuildConfig.GEMINI_KEY
    private var mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance("https://gemastik-a8145-default-rtdb.asia-southeast1.firebasedatabase.app/")
    private var databaseReference: DatabaseReference = firebaseDatabase.reference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFoodTrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment1: Fragment = MenuFragment.newInstance(this::class.java.simpleName)

        supportFragmentManager.beginTransaction().replace(R.id.flMenuFdetails, fragment1).commit()

        foodRecog()

        binding.ibCancel.setOnClickListener{
            finish()
        }

        binding.ibConfirm.isEnabled = false

        binding.ibConfirm.setOnClickListener{
            saveData()
        }

    }

    private fun saveData() {
//        binding.tvResep1
//        databaseReference.child("user_food_tracking").child(mAuth.currentUser?.uid ?: return).addValueEventListener()
        Toast.makeText(this, "Data stored successfully", Toast.LENGTH_SHORT).show()
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
                    val split = responseText?.split(";")
                    val menu = split?.get(0)?.trim()
                    binding.tvRecordMenu.text = menu
                    binding.tvResep1.text = responseText
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
        var bitmap: Bitmap? = null
        try {
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return bitmap
    }
}