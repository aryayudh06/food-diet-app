package com.pam.gemastik_app.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.R
import com.pam.gemastik_app.databinding.ActivityDailyRecentFoodsBinding
import com.pam.gemastik_app.model.DailyRecentFoodsModel
import com.pam.gemastik_app.model.RecentFoodsModel
import com.pam.gemastik_app.ui.adapter.DailyRecentFoodsAdapter
import com.pam.gemastik_app.ui.fragment.MenuFragment

class DailyRecentFoodsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDailyRecentFoodsBinding

    private var firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FB_DB_KEY)
    private var databaseReference: DatabaseReference = firebaseDatabase.getReference()
    private val mAuth = HomeActivity.auth

    private lateinit var dailyFoodsAdapter: DailyRecentFoodsAdapter

    private val dailyFoodsList = mutableListOf<DailyRecentFoodsModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDailyRecentFoodsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val fragment1: MenuFragment = MenuFragment.newInstance(this::class.java.simpleName)
        supportFragmentManager.beginTransaction().replace(R.id.flDaily, fragment1).commit()

        val lm = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.rvDailyFoods.layoutManager = lm

        dailyFoodsAdapter = DailyRecentFoodsAdapter(this, dailyFoodsList)
        binding.rvDailyFoods.adapter = dailyFoodsAdapter

        fetchDailyFoods()
    }

    private fun fetchDailyFoods() {
        mAuth.currentUser?.uid?.let { userId ->
            val dailyDataRef = databaseReference.child("user_food_tracking").child(userId).child("daily_data")

            dailyDataRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val dailyFoodsList = mutableListOf<DailyRecentFoodsModel>()

                    for (dateSnapshot in snapshot.children) {
                        val date = dateSnapshot.key ?: continue
                        val foodsList = mutableListOf<RecentFoodsModel>()

                        for (foodSnapshot in dateSnapshot.children) {
                            val food = foodSnapshot.getValue(RecentFoodsModel::class.java)
                            if (food != null) {
                                foodsList.add(food)
                            }
                        }

                        dailyFoodsList.add(DailyRecentFoodsModel(date, foodsList))
                    }

                    dailyFoodsList.sortByDescending { it.date }

                    dailyFoodsAdapter.updateData(dailyFoodsList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DailyRecentFoodsActivity, "Failed to load daily food data", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }


}