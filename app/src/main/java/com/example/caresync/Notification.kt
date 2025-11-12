package com.example.caresync.notifications

import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.example.caresync.notification.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun NotificationScreen(navController: NavController) {

    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelGreen = colorResource(id = R.color.pastel_green)
    val pastelPink = colorResource(id = R.color.pastel_pink)
    val pastelOrange = colorResource(id = R.color.pastel_orange)
    val pastelBlue = colorResource(id = R.color.pastel_blue)

    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    var notifications by remember { mutableStateOf<List<NotificationModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // ✅ Selection Mode State
    val selectedNotifications = remember { mutableStateListOf<NotificationModel>() }
    var isSelectionMode by remember { mutableStateOf(false) }

    // ✅ Fetch Notifications
    suspend fun fetchNotifications() {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("Notifications")
                .document(currentUserId)
                .collection("UserNotifications")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            notifications = snapshot.toObjects(NotificationModel::class.java)

        } catch (e: Exception) {
            Toast.makeText(context, "Error loading notifications", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    LaunchedEffect(Unit) { fetchNotifications() }

    // ✅ Delete Selected
    fun deleteSelected() {
        val db = FirebaseFirestore.getInstance()
        val batch = db.batch()
        selectedNotifications.forEach { notif ->
            val ref = db.collection("Notifications")
                .document(currentUserId)
                .collection("UserNotifications")
                .document(notif.id)
            batch.delete(ref)
        }
        coroutine.launch {
            batch.commit().await()
            selectedNotifications.clear()
            isSelectionMode = false
            fetchNotifications()
        }
    }

    // ✅ UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedNotifications.size} selected"
                        else "Notifications",
                        color = white,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            selectedNotifications.clear()
                            isSelectionMode = false
                        } else navController.navigateUp()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "", tint = white)
                    }
                },
                actions = {
                    if (isSelectionMode) {
                        // Select All
                        IconButton(onClick = {
                            if (selectedNotifications.size == notifications.size) {
                                selectedNotifications.clear()
                            } else {
                                selectedNotifications.clear()
                                selectedNotifications.addAll(notifications)
                            }
                        }) {
                            Icon(Icons.Default.SelectAll, null, tint = white)
                        }

                        // Delete
                        IconButton(onClick = { deleteSelected() }) {
                            Icon(Icons.Default.Delete, null, tint = white)
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        }
    ) { padding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    Brush.verticalGradient(
                        listOf(pastelGreen, pastelPink, pastelOrange, pastelBlue),
                        0f, 2000f
                    )
                )
        ) {

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary)
                }
            } else if (notifications.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("No Notifications Yet", color = Color.Gray, fontSize = 18.sp)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notif ->

                        val isSelected = selectedNotifications.contains(notif)

                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .combinedClickable(
                                    onClick = {
                                        if (isSelectionMode) {
                                            if (isSelected) selectedNotifications.remove(notif)
                                            else selectedNotifications.add(notif)

                                            if (selectedNotifications.isEmpty()) isSelectionMode = false
                                        }
                                    },
                                    onLongClick = {
                                        isSelectionMode = true
                                        selectedNotifications.add(notif)
                                    }
                                ),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isSelected) primary.copy(alpha = 0.25f) else white
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                        ) {

                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {

                                // ✅ Show checkbox only in selection mode
                                if (isSelectionMode) {
                                    Checkbox(
                                        checked = isSelected,
                                        onCheckedChange = {
                                            if (isSelected) selectedNotifications.remove(notif)
                                            else selectedNotifications.add(notif)
                                        }
                                    )
                                    Spacer(Modifier.width(12.dp))
                                }

                                Column(Modifier.weight(1f)) {
                                    Text(notif.title, fontWeight = FontWeight.Bold, color = Color.Black, fontSize = 17.sp)
                                    Spacer(Modifier.height(6.dp))
                                    Text(notif.message, fontSize = 14.sp, color = Color.DarkGray)
                                    Spacer(Modifier.height(8.dp))
                                    Text(formatTimestamp(notif.timestamp), fontSize = 12.sp, color = Color.Gray)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

fun formatTimestamp(timestamp: Long?): String {
    if (timestamp == null) return "Just now"
    return SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(Date(timestamp))
}
