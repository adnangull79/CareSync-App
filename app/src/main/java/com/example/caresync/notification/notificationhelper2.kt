package com.example.caresync.notification

import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

object NotificationHelper {

    private val firestore = FirebaseFirestore.getInstance()

    // Generic Notification
    fun sendNotification(
        receiverId: String,
        title: String,
        message: String
    ) {
        val id = UUID.randomUUID().toString()
        val notification = NotificationModel(
            id = id,
            receiverId = receiverId,
            title = title,
            message = message,
            timestamp = System.currentTimeMillis(),
            read = false
        )

        firestore.collection("Notifications")
            .document(receiverId)
            .collection("UserNotifications")
            .document(id)
            .set(notification)
    }

    // Appointment Booked
    fun sendAppointmentBookedNotification(
        doctorId: String,
        patientName: String,
        date: String,
        time: String
    ) {
        sendNotification(
            receiverId = doctorId,
            title = "New Appointment Booked",
            message = "$patientName booked an appointment on $date at $time."
        )
    }

    // Appointment Cancelled
    fun sendAppointmentCancelledNotification(
        doctorId: String,
        patientName: String,
        date: String,
        time: String,
        reason: String
    ) {
        sendNotification(
            receiverId = doctorId,
            title = "Appointment Cancelled",
            message = "$patientName cancelled the appointment ($date • $time). Reason: $reason"
        )
    }

    // Appointment Checked / Completed
    fun sendAppointmentCheckedNotification(
        patientId: String,
        doctorName: String,
        date: String,
        time: String
    ) {
        sendNotification(
            receiverId = patientId,
            title = "Appointment Completed",
            message = "Dr. $doctorName marked your appointment on $date at $time as completed."
        )
    }

    // ✅ Appointment Missed (NEW)
    fun sendAppointmentMissedNotification(
        patientId: String,
        date: String,
        time: String
    ) {
        sendNotification(
            receiverId = patientId,
            title = "Appointment Missed",
            message = "Your appointment on $date at $time was marked as missed by the doctor."
        )
    }
}
