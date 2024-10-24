package com.pam.gemastik_app.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityProfileBinding
import com.pam.gemastik_app.ui.MainActivity.Companion
import com.pam.gemastik_app.ui.fragment.ChartFragment
import com.pam.gemastik_app.ui.fragment.MenuFragment
import com.pam.gemastik_app.ui.login.LoginActivity

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment1: MenuFragment = MenuFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.flProfile, fragment1).commit()

        val barFragment: Fragment = ChartFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.chartProfile, barFragment).commit()

        binding.btnLogout.setOnClickListener {
            HomeActivity.auth .signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }
}