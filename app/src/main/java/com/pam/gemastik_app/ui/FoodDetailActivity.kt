package com.pam.gemastik_app.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityFoodDetailsBinding
import com.pam.gemastik_app.databinding.ActivityReccomendationBinding
import com.pam.gemastik_app.ui.fragment.MenuFragment

class FoodDetailActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFoodDetailsBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFoodDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragment1: Fragment = MenuFragment()

        supportFragmentManager.beginTransaction().replace(R.id.flMenuFdetails, fragment1).commit()
    }
}