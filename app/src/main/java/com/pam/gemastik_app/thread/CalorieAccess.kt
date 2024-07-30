package com.pam.gemastik_app.thread

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.model.UserCalorie
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalorieAccess() {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FB_DB_KEY)

    fun getData() {
        val databaseReference = database.getReference("calories").child(mAuth.uid!!)
        val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyData = databaseReference.child("daily_data").child(currDate)

        val dataListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dataUser: UserCalorie? = snapshot.getValue(UserCalorie::class.java)
                    if (dataUser != null) {
                        Log.d("Calorie Data", "Tinggi Badan: ${dataUser.tb}, Berat Badan: ${dataUser.bb}, Kalori: ${dataUser.calorie}")
                    } else {
                        Log.d("Calorie Data", "Data tidak ditemukan")
                    }
                } else {
                    Log.d("Calorie Data", "Snapshot tidak ada")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Calorie Data", "Gagal mendapatkan data: ${error.message}")
            }
        }

        dailyData.addListenerForSingleValueEvent(dataListener)
    }

    fun saveData(tb: String?, bb: String?, calorie: String?) {
        val databaseReference = database.getReference("calories").child(mAuth.uid!!)
        val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyData = databaseReference.child("daily_data").child(currDate)

        val dataUser = UserCalorie(tb, bb, calorie)

        // Log tambahan untuk memastikan nilai yang dikirim
        Log.d("status save", "tb: $tb, bb: $bb, calorie: $calorie")

        // Log tambahan untuk memastikan referensi benar
        Log.d("status save", "databaseReference: $databaseReference")
        Log.d("status save", "dailyData: $dailyData")

        dailyData.setValue(dataUser).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d("status save", "berhasil")
            } else {
                Log.e("status save", "gagal: ${task.exception?.message}")
            }
        }.addOnFailureListener { exception ->
            Log.e("status save", "gagal: ${exception.message}")
        }
    }


    fun updateCalorie(valueChange: Int) {
        val databaseReference = database.getReference("calories").child(mAuth.uid!!)
        val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyData = databaseReference.child("daily_data").child(currDate)
        dailyData.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    val dataUser: UserCalorie? = snapshot.getValue(UserCalorie::class.java)
                    if (dataUser != null) {
                        val currentCal: Int = dataUser.calorie?.toInt() ?: 0
                        val newBb = currentCal + valueChange
                        dataUser.bb = newBb.toString()
                        dailyData.setValue(dataUser).addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                Log.d("status update", "berhasil")
                            } else {
                                Log.e("status update", "gagal")
                            }
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("status update", "gagal")
            }
        })
    }
}
