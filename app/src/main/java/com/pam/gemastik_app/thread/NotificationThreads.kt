package com.pam.gemastik_app.thread

import android.Manifest
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pam.gemastik_app.R

class NotificationThreads(
    private val context: Context,
    private val requestPermissionLauncher: ActivityResultLauncher<String>? = null
) {

    private val channelId = "example_channel_id"

    init {
        createNotificationChannel()
    }

    // Function to create a notification channel (for Android 8.0 and higher)
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Example Channel"
            val descriptionText = "This is an example notification channel"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    // Function to build and display a notification with a custom message
    fun sendNotificationInThread(message: String, title: String) {
        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.logo)  // Replace with your own icon
            .setContentTitle(title)
            .setContentText(message)  // Set custom message passed as parameter
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setSound(Uri.EMPTY)
            .setDefaults(Notification.DEFAULT_VIBRATE)

        val notificationManager = NotificationManagerCompat.from(context)

        // Check for POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permission if launcher is provided
            requestPermissionLauncher?.launch(Manifest.permission.POST_NOTIFICATIONS)
            return
        }

        // Start a new thread for the background task
        Thread {
            // Simulate some background work (e.g., waiting for a specific event)
            Thread.sleep(5000)  // Replace this with your actual background task

            // Show the notification once the task is done
            notificationManager.notify(1, builder.build())
        }.start()
    }
}
 