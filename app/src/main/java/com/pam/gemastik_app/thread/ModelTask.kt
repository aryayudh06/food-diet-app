package com.pam.gemastik_app.thread

import android.graphics.Bitmap
import android.util.Log
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ModelTask(private val apiKey: String) {

    suspend fun executeModelCall(img: Bitmap): String {
        return withContext(Dispatchers.IO) {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val inputContent = content {
                image(img);
                text(
                """
                Identifikasi makanan dari gambar yang diberikan. 
                Berikan respons dalam format berikut:
                - Nama makanan; jumlah kalori dalam ... kcal; jumlah protein dalam ... grams protein; kandungan mineral/vitamin.
        
                Contoh respons:
                "Nasi Goreng; 300 kcal; 10 grams protein; Vitamin A, Vitamin B12."
                
                Jawab **hanya** dengan format seperti contoh respons di atas **dan tidak menambahkan kata lain**.
                Jika tidak ada gambar makanan yang terdeteksi, jawab hanya dengan:
                "Tidak ada makanan terdeteksi."
                """
                )
            } 

            val response = generativeModel.generateContent(inputContent)
            Log.d("ModelTask", response.text.toString())
            response.text.toString()
        }
    }

    suspend fun recipeModel(food: String): String {
        return withContext(Dispatchers.IO) {
            val generativeModel = GenerativeModel(
                modelName = "gemini-1.5-flash",
                apiKey = apiKey
            )

            val inputContent = content {
                text("Berikan resep ${food} sehat secara lengkap. Sertakan langkah-langkah memasaknya")
            }

            val response = generativeModel.generateContent(inputContent)
            Log.d("Recipe AI Model", response.text.toString())
            response.text.toString()
        }
    }
}