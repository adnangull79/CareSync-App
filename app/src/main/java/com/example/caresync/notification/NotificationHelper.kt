
package com.example.caresync.notifications

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

object NotificationHelper {

    // Send notification when appointment is booked
    suspend fun sendAppointmentBookedNotification(
        doctorId: String,
        patientName: String,
        date: String,
        time: String
    ) {
        try {
            // Save notification to Firestore - Doctor will receive it
            val notification = hashMapOf(
                "userId" to doctorId,
                "title" to "New Appointment Booked",
                "message" to "$patientName has booked an appointment for $date at $time",
                "type" to "appointment_booked",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "read" to false
            )

            FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notification)
                .await()

            Log.d("NotificationHelper", "Booking notification saved to Firestore")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error sending booking notification: ${e.message}")
        }
    }

    // Send notification when appointment is cancelled
    suspend fun sendAppointmentCancelledNotification(
        doctorId: String,
        patientName: String,
        date: String,
        time: String,
        reason: String
    ) {
        try {
            val notification = hashMapOf(
                "userId" to doctorId,
                "title" to "Appointment Cancelled",
                "message" to "$patientName cancelled the appointment for $date at $time. Reason: $reason",
                "type" to "appointment_cancelled",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "read" to false
            )

            FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notification)
                .await()

            Log.d("NotificationHelper", "Cancellation notification saved to Firestore")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error sending cancellation notification: ${e.message}")
        }
    }

    // Send reminder notification to patient (1 hour before)
    suspend fun sendOneHourReminderNotification(
        patientId: String,
        doctorName: String,
        time: String,
        date: String
    ) {
        try {
            val notification = hashMapOf(
                "userId" to patientId,
                "title" to "Appointment Reminder",
                "message" to "Your appointment with Dr. $doctorName is in 1 hour at $time. Don't forget!",
                "type" to "reminder_1hour",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "read" to false
            )

            FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notification)
                .await()

            Log.d("NotificationHelper", "1-hour reminder saved to Firestore")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error sending 1-hour reminder: ${e.message}")
        }
    }

    // Send notification at exact appointment time
    suspend fun sendAppointmentTimeNotification(
        patientId: String,
        doctorName: String,
        clinicAddress: String
    ) {
        try {
            val notification = hashMapOf(
                "userId" to patientId,
                "title" to "Appointment Time!",
                "message" to "Your appointment with Dr. $doctorName is now! Location: $clinicAddress",
                "type" to "appointment_time",
                "timestamp" to com.google.firebase.Timestamp.now(),
                "read" to false
            )

            FirebaseFirestore.getInstance()
                .collection("notifications")
                .add(notification)
                .await()

            Log.d("NotificationHelper", "Appointment time notification saved to Firestore")
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error sending appointment time notification: ${e.message}")
        }
    }

    // Initialize FCM token for current user (call on login)
    fun initializeFCMToken(userId: String) {
        try {
            FirebaseMessaging.getInstance().token.addOnSuccessListener { token ->
                // Save token to user's document
                FirebaseFirestore.getInstance()
                    .collection("users")
                    .document(userId)
                    .update("fcmToken", token)
                    .addOnSuccessListener {
                        Log.d("NotificationHelper", "FCM token saved successfully")
                    }
                    .addOnFailureListener { e ->
                        Log.e("NotificationHelper", "Failed to save FCM token: ${e.message}")
                    }
            }
        } catch (e: Exception) {
            Log.e("NotificationHelper", "Error initializing FCM token: ${e.message}")
        }
    }

    // Listen for notifications for current user (call in MainActivity or HomeScreen)
    fun listenForNotifications(userId: String, onNotificationReceived: (String, String) -> Unit) {
        FirebaseFirestore.getInstance()
            .collection("notifications")
            .whereEqualTo("userId", userId)
            .whereEqualTo("read", false)
            .addSnapshotListener { snapshots, error ->
                if (error != null) {
                    Log.e("NotificationHelper", "Listen failed: ${error.message}")
                    return@addSnapshotListener
                }

                snapshots?.documentChanges?.forEach { change ->
                    if (change.type == com.google.firebase.firestore.DocumentChange.Type.ADDED) {
                        val data = change.document.data
                        val title = data["title"] as? String ?: "Notification"
                        val message = data["message"] as? String ?: ""

                        // Show notification
                        onNotificationReceived(title, message)

                        // Mark as read
                        change.document.reference.update("read", true)
                    }
                }
            }
    }
}