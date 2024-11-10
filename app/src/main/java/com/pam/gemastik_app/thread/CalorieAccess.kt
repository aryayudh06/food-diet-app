package com.pam.gemastik_app.thread

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener
import com.pam.gemastik_app.BuildConfig
import com.pam.gemastik_app.model.UserCalorie
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CalorieAccess {
    private val mAuth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance(BuildConfig.FB_DB_KEY)

    fun getCalorieData(callback: (List<UserCalorie>) -> Unit) {
        val userId = mAuth.uid ?: return
        val databaseReference = database.getReference("calories").child(userId).child("daily_data")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val calorieList = mutableListOf<UserCalorie>()
                if (snapshot.exists()) {
                    for (childSnapshot in snapshot.children) {
                        val dataUser: UserCalorie? = childSnapshot.getValue(UserCalorie::class.java)
                        if (dataUser != null) {
                            calorieList.add(dataUser)
                        }
                    }
                }
                callback(calorieList)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Calorie Data", "Failed to get data: ${error.message}")
                callback(emptyList()) // Return empty list on error
            }
        })
    }

    fun getPersonalization(onResult: (String?) -> Unit){
        val userId = mAuth.uid ?: return
        val databaseReference = database.getReference("user_personalization").child(userId).child("med")

        databaseReference.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                // Retrieve the value of "med"
                val medValue = snapshot.getValue(String::class.java)
                if (medValue != null) {
                    Log.d("Personalization", "Med value: $medValue")
                    onResult(medValue)
                } else {
                    Log.d("Personalization", "No med value found")
                    onResult(null)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Personalization", "Failed to retrieve med value", error.toException())
                onResult(null)
            }
        })
    }

    fun updateCalories(calorie: String?) {
        val userId = mAuth.uid ?: return
        val databaseReference = database.getReference("calories").child(userId)
        val currDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val dailyData = databaseReference.child("daily_data").child(currDate)

        // Ambil data yang sudah ada dari database
        dailyData.get().addOnSuccessListener { snapshot ->
            // Jika data sudah ada, ambil nilai tb dan bb yang sudah ada
            val existingTb = snapshot.child("tb").getValue(String::class.java)
            val existingBb = snapshot.child("bb").getValue(String::class.java)
            val existingCal = snapshot.child("calorie").getValue(String::class.java)?.toDoubleOrNull()
            val calDouble = calorie?.toDoubleOrNull()

            val cals = existingCal?.plus(calDouble!!)

            // Buat objek UserCalorie dengan nilai tb dan bb yang sudah ada atau yang baru
            val dataUser = UserCalorie(existingTb, existingBb, cals.toString())

            // Log tambahan untuk memastikan referensi dan data yang digunakan
            Log.d("status save", "existingTb: $existingTb, existingBb: $existingBb, calorie: ${cals.toString()}")

            // Perbarui hanya nilai calorie
            dailyData.setValue(dataUser).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d("status save", "berhasil")
                } else {
                    Log.e("status save", "gagal: ${task.exception?.message}")
                }
            }.addOnFailureListener { exception ->
                Log.e("status save", "gagal: ${exception.message}")
            }
        }.addOnFailureListener { exception ->
            Log.e("status save", "gagal ambil data sebelumnya: ${exception.message}")
        }
    }

    suspend fun getRemainingCalories(calBurned: Double): Double {
        var remainingCalories = 0.0
        remainingCalories = calBurned - getBurnedCalories()
        return remainingCalories
    }

    private suspend fun getBurnedCalories(): Double {
        val userId = mAuth.uid ?: return 0.0
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val databaseReference = database.getReference("daily_burned_calorie").child(userId).child("burned_calorie").child(currentDate)

        try {
            // Mengambil data burned_calories dari Firebase secara langsung
            val snapshot = databaseReference.get().await()
            return if (snapshot.exists()) {
                snapshot.getValue(Double::class.java) ?: 0.0 // Ambil nilai jika ada, jika tidak 0.0
            } else {
                0.0 // Jika snapshot tidak ada, return 0.0
            }
        } catch (e: Exception) {
            Log.e("Calorie Data", "Failed to retrieve burned calories: ${e.message}")
            return 0.0 // Return 0.0 jika terjadi error
        }
    }


    fun updateBurnedCalories(newCaloriesBurned: Double) {
        val userId = FirebaseAuth.getInstance().uid ?: return
        val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val databaseReference = database.getReference("daily_burned_calorie").child(userId).child("burned_calorie").child(currentDate)

        databaseReference.runTransaction(object : Transaction.Handler {
            override fun doTransaction(currentData: MutableData): Transaction.Result {
                // Membaca nilai saat ini atau set ke 0.0 jika tidak ada
                val currentBurnedCalories = currentData.getValue(Double::class.java) ?: 0.0
                currentData.value = currentBurnedCalories + newCaloriesBurned
                return Transaction.success(currentData)
            }

            override fun onComplete(error: DatabaseError?, committed: Boolean, snapshot: DataSnapshot?) {
                if (error != null) {
                    Log.e("Calorie Data", "Failed to update burned calories: ${error.message}")
                } else {
                    Log.d("Calorie Data", "Successfully updated burned calories.")
                }
            }
        })
    }

    fun saveData(tb: String?, bb: String?, calorie: String?) {
        val userId = mAuth.uid ?: return
        val databaseReference = database.getReference("calories").child(userId)
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

    fun updateBb(valueChange: Int) {
        val userId = mAuth.uid ?: return
        val databaseReference = database.getReference("calories").child(userId)
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
