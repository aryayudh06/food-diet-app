package com.pam.gemastik_app.thread

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.model.UserCalorie
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

open class CalorieAccess : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    protected fun saveData(tb: String?, bb: String?, calorie:String?) {
        val mAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
        val databaseReference = database.getReference("calories").child(mAuth.uid!!)
        val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyData = databaseReference.child("daily_data").child(currDate)
        val dataUser = UserCalorie(tb, bb, calorie)
        dailyData.setValue(dataUser).addOnCompleteListener { task: Task<Void?> ->
            if (task.isSuccessful) {
                // Data berhasil disimpan
                Log.d("status", "berhasil")
            } else {
                // Terjadi kesalahan
                Log.e("status", "gagal")
            }
        }
    }

    private fun updateCalorie(valueChange: Int) {
        val mAuth = FirebaseAuth.getInstance()
        val database = FirebaseDatabase.getInstance()
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
                        dailyData.setValue(dataUser).addOnCompleteListener { task: Task<Void?> ->
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
