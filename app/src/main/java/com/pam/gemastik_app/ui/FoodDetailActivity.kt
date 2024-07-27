package com.pam.gemastik_app.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityFoodDetailsBinding
import com.pam.gemastik_app.thread.ModelTask
import com.pam.gemastik_app.ui.fragment.MenuFragment
import com.pam.gemastik_app.ui.photoutil.CameraActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FoodDetailActivity: AppCompatActivity() {
    private lateinit var binding: ActivityFoodDetailsBinding
    private val apiKey = BuildConfig.GEMINI_KEY

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityFoodDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val fragment1: Fragment = MenuFragment.newInstance(this::class.java.simpleName)

        supportFragmentManager.beginTransaction().replace(R.id.flMenuFdetails, fragment1).commit()

        binding.ibCancel.setOnClickListener {
            finish()
        }

        binding.ibConfirm.setOnClickListener {
            Toast.makeText(this, "Catatan berhasil ditambahkan", Toast.LENGTH_SHORT).show()
            finish()
        }

    }

    override fun onResume() {
        super.onResume()
        if (!intent.getStringExtra("bfoto").equals("")) {
            getRecipe()
            Glide.with(this)
                .load(intent.getStringExtra("bfoto"))
                .apply(RequestOptions()
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true))
                .into(binding.ivBackground)
            binding.tvRecordMenu.text = intent.getStringExtra("bmenu")
            binding.tvDescription.text = intent.getStringExtra("bdesc")
        } else {
            Toast.makeText(this, "Error loading food data", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getRecipe() {
        val runnable = Runnable {
            if (intent.getStringExtra("bmenu") != null) {
                CoroutineScope(Dispatchers.Main).launch {
                    val modelTask = ModelTask(apiKey)
                    val menuName = intent.getStringExtra("bmenu")
                    val responseText = menuName?.let { modelTask.recipeModel(it) }
                    binding.tvRecipeDetail.text = responseText
                }
            }
        }
        val recipe = Thread(runnable)
        recipe.start()
    }
}