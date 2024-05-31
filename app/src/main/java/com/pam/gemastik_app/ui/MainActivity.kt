package com.pam.gemastik_app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityMainBinding
import com.pam.gemastik_app.ui.login.LoginActivity

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


    }

    override fun onResume() {
        super.onResume()
        binding.tvUser.text = updateData()
    }

    private fun updateData(): String{
        return "Selamat Datang, ${auth.currentUser?.email}"
    }
}