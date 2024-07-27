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
                image(img)
                text("Apa nama makanan tersebut, jawab hanya dengan nama makanan saja. Lalu pisahkan dengan ';', berapa kandungan Kalorinya." +
                        " Lalu pisahkan dengan ';', berapa kandungan Proteinnya. Lalu pisahkan dengan ';', Sebutkan kandungan mineral/vitaminnya." +
                        " Jawab hanya dengan jawaban kata tersebut." + " Jika tidak ada gambar makanan terdeteksi, jawab dengan 'Tidak ada makanan terdeteksi.'")
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