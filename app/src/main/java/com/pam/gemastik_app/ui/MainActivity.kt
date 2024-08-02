package com.pam.gemastik_app.ui

import android.content.ContentResolver
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.databinding.ActivityMainBinding
import com.pam.gemastik_app.thread.CalorieAccess
import com.pam.gemastik_app.thread.ModelTask
import com.pam.gemastik_app.ui.login.LoginActivity
import com.pam.gemastik_app.ui.photoutil.CameraActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStream


class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val apiKey = BuildConfig.GEMINI_KEY

    companion object{
        lateinit var auth:FirebaseAuth
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val calorieAccess = CalorieAccess()
        calorieAccess.saveData("60", "167", "3200")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.pbModelRes.visibility = View.GONE

        binding.btSignOut.setOnClickListener(){
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btCamera.setOnClickListener(){
            startActivity(Intent(this, CameraActivity::class.java))
            finish()
        }

        binding.btRecc.setOnClickListener(){
            startActivity(Intent(this, RecommendationActivity::class.java))
            finish()
        }

        val imageUriString = intent.getStringExtra("imageUri")
        if (imageUriString != null) {
            val imageUri = Uri.parse(imageUriString)
            updateImage(imageUri)
            val bitmapImg: Bitmap? = uriToBitmap(contentResolver, imageUri)
            binding.pbModelRes.visibility = View.VISIBLE
            CoroutineScope(Dispatchers.Main).launch {
                val modelTask = ModelTask(apiKey)
                val responseText = bitmapImg?.let { modelTask.executeModelCall(it) }
                binding.tvGemini.text = responseText
                binding.pbModelRes.visibility = View.GONE // Hide loading indicator
            }
        }

    }

    override fun onResume() {
        super.onResume()
        binding.tvUser.text = updateData()
    }

    private fun updateData(): String{
        return "Selamat Datang, ${auth.currentUser?.email}"
    }

    private fun updateImage(image: Uri) {
        binding.ivImageFood.setImageURI(image)
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