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
                        "Lalu pisahkan dengan ';', berapa kandungan Proteinnya. Lalu pisahkan dengan ';', Sebutkan kandungan mineral/vitaminnya." +
                        "jawab hanya dengan jawaban kata tersebut")
            }

            val response = generativeModel.generateContent(inputContent)
            Log.d("ModelTask", response.text.toString())
            response.text.toString()
        }
    }
}