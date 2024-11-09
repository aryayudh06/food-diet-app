package com.pam.gemastik_app.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityProfileBinding
import com.pam.gemastik_app.model.RecentFoodsModel
import com.pam.gemastik_app.thread.CalorieAccess
import com.pam.gemastik_app.ui.adapter.RecentFoodsAdapter
import com.pam.gemastik_app.ui.fragment.ChartFragment
import com.pam.gemastik_app.ui.fragment.MenuFragment
import com.pam.gemastik_app.ui.login.LoginActivity
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FB_DB_KEY)
    private var databaseReference: DatabaseReference = firebaseDatabase.getReference()
    private val mAuth = HomeActivity.auth

    private lateinit var recentFoodsAdapter: RecentFoodsAdapter
    private val recentFoods: MutableList<RecentFoodsModel> = ArrayList()

    val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())

    private val calorieAccess: CalorieAccess = CalorieAccess()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment1: MenuFragment = MenuFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.flProfile, fragment1).commit()

        val barFragment: Fragment = ChartFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.chartProfile, barFragment).commit()

        val recentFoodsLM = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvRecentFoodsProfile.layoutManager = recentFoodsLM

        recentFoodsAdapter = RecentFoodsAdapter(this, recentFoods)
        binding.rvRecentFoodsProfile.adapter = recentFoodsAdapter

        loadBMI()
        fetchRecentFoods()

        binding.tvProfileName.text = mAuth.currentUser?.displayName ?: "User"
        binding.tvProfileEmail.text = mAuth.currentUser?.email

        binding.btnLogout.setOnClickListener {
            HomeActivity.auth .signOut()
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }
    }

    private fun fetchRecentFoods() {
        mAuth.currentUser?.uid?.let { databaseReference.child("user_food_tracking").child(it).child(currDate).limitToFirst(3).addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                recentFoods.clear()
                for (dataSnapshot in snapshot.children) {
                    val recent = dataSnapshot.getValue(RecentFoodsModel::class.java)
                    if (recent != null) {
                        recentFoods.add(recent)
                    }
                }
                if (recentFoods.isEmpty()) {
                    recentFoods.add(
                        RecentFoodsModel(
                            calorie = "N/A",
                            menu = "You haven't eaten anything today",
                            protein = null
                        )
                    )
                }
                recentFoodsAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@ProfileActivity, "Failed to load recent foods data", Toast.LENGTH_SHORT).show()
            }

        }) }
    }

    private fun loadBMI() {
        calorieAccess.getCalorieData { calorieList ->
            val userCalorie = calorieList.lastOrNull()
            if (userCalorie != null) {
                val bbValue = userCalorie.bb?.toIntOrNull() ?: 0
                val tbValue = userCalorie.tb?.toIntOrNull() ?: 0

                if (tbValue > 0) { // Avoid division by zero
                    val heightInMeters = tbValue / 100.0
                    val bmi = bbValue / (heightInMeters * heightInMeters)
                    val formattedBmi = DecimalFormat("#.##").format(bmi)

                    binding.tvBMINumber.text = formattedBmi
                    mAuth.uid?.let { uid ->
                        databaseReference.child("user_personalization").child(uid).child("gender")
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val gender = snapshot.getValue(String::class.java) ?: "N/A"
                                    binding.tvGenderText.text = gender
                                }

                                override fun onCancelled(error: DatabaseError) {
                                    Toast.makeText(this@ProfileActivity, "Failed to load gender data", Toast.LENGTH_SHORT).show()
                                }
                            })
                    }
                    binding.tvHeightText.text = "${tbValue} Cm"
                    binding.tvWeightText.text = "${bbValue} Kg"
                } else {
                    binding.tvBMINumber.text = "N/A"
                    binding.tvGenderText.text = "N/A"
                    binding.tvHeightText.text = "N/A"
                    binding.tvWeightText.text = "N/A"
                }
            } else {
                Log.d("Calorie Data", "No data available")
                binding.tvBMINumber.text = "N/A"
                binding.tvGenderText.text = "N/A"
                binding.tvHeightText.text = "N/A"
                binding.tvWeightText.text = "N/A"
            }
        }
    }

}