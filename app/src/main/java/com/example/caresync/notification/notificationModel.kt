package com.example.caresync.notification



data class NotificationModel(
    val id: String = "",
    val senderId: String = "",
    val receiverId: String = "",
    val title: String = "",
    val message: String = "",
    val timestamp: Long = System.currentTimeMillis(),
    val read: Boolean = false
)
