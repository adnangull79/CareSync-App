package com.example.caresync.notification

// File: FCMService.kt


import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.caresync.MainActivity
import com.example.caresync.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class FCMService : FirebaseMessagingService() {

    // Called when a new notification arrives
    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Get notification data
        val title = message.notification?.title ?: "CareSync"
        val body = message.notification?.body ?: "You have a new notification"

        // Show notification to user
        showNotification(title, body)
    }

    // Called when FCM token is created or refreshed
    override fun onNewToken(token: String) {
        super.onNewToken(token)

        // Save token to Firestore for current user
        saveTokenToFirestore(token)
    }

    // Save FCM token to user's document in Firestore
    private fun saveTokenToFirestore(token: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val db = FirebaseFirestore.getInstance()

        db.collection("users").document(userId)
            .update("fcmToken", token)
            .addOnSuccessListener {
                // Token saved successfully
            }
            .addOnFailureListener {
                // Failed to save token (user might not exist yet)
            }
    }

    // Display notification to user
    private fun showNotification(title: String, message: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "caresync_notifications"
        val channelName = "CareSync Notifications"

        // Create notification channel (required for Android 8.0+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                channelName,
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Appointment notifications and reminders"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open app when notification is clicked
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Build notification
        val notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setAutoCancel(true) // Dismiss when clicked
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setVibrate(longArrayOf(0, 500, 200, 500)) // Vibration pattern
            .build()

        // Show notification (use current timestamp as unique ID)
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}