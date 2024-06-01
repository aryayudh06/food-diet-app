package com.pam.gemastik_app.ui

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.firebase.auth.FirebaseAuth
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityMainBinding
import com.pam.gemastik_app.ui.login.LoginActivity
import com.pam.gemastik_app.ui.photoutil.CameraActivity

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    companion object{
        lateinit var auth:FirebaseAuth
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btSignOut.setOnClickListener(){
            auth.signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }

        binding.btCamera.setOnClickListener(){
            startActivity(Intent(this, CameraActivity::class.java))
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        binding.tvUser.text = updateData()
    }

    private fun updateData(): String{
        return "Selamat Datang, ${auth.currentUser?.email}"
    }
}