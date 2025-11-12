package com.example.caresync.DoctorHome

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.caresync.R
import com.example.caresync.notification.NotificationModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

// ----------------------------------------------------------
// VIEWMODEL
// ----------------------------------------------------------
class DoctorNotificationsViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val _notifications = mutableStateListOf<NotificationModel>()
    val notifications: List<NotificationModel> get() = _notifications

    init {
        if (currentUserId != null) {
            firestore.collection("Notifications")
                .document(currentUserId)
                .collection("UserNotifications")
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
                .addSnapshotListener { snapshot, _ ->
                    if (snapshot != null) {
                        _notifications.clear()
                        _notifications.addAll(snapshot.toObjects(NotificationModel::class.java))
                    }
                }
        }
    }

    fun deleteNotifications(selected: List<NotificationModel>) {
        if (currentUserId == null) return
        val batch = firestore.batch()
        selected.forEach {
            val ref = firestore.collection("Notifications")
                .document(currentUserId)
                .collection("UserNotifications")
                .document(it.id)
            batch.delete(ref)
        }
        batch.commit()
    }
}

// ----------------------------------------------------------
// SCREEN
// ----------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorNotificationsScreen(
    navController: NavController,
    viewModel: DoctorNotificationsViewModel = viewModel()
) {
    val notifications = viewModel.notifications
    val selectedNotifications = remember { mutableStateListOf<NotificationModel>() }
    var isSelectionMode by remember { mutableStateOf(false) }
    val coroutine = rememberCoroutineScope()
    val context = LocalContext.current

    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelGreen = colorResource(id = R.color.pastel_green)
    val pastelPink = colorResource(id = R.color.pastel_pink)
    val pastelOrange = colorResource(id = R.color.pastel_orange)
    val pastelBlue = colorResource(id = R.color.pastel_blue)

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isSelectionMode) "${selectedNotifications.size} selected"
                        else "Notifications",
                        color = white
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isSelectionMode) {
                            selectedNotifications.clear()
                            isSelectionMode = false
                        } else navController.popBackStack()
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "", tint = white)
                    }
                },
                actions = {
                    if (isSelectionMode) {
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

                        IconButton(onClick = {
                            coroutine.launch {
                                viewModel.deleteNotifications(selectedNotifications.toList())
                                selectedNotifications.clear()
                                isSelectionMode = false
                            }
                        }) {
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
                .padding(padding)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        listOf(pastelGreen, pastelPink, pastelOrange, pastelBlue),
                        startY = 0f,
                        endY = 2000f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            if (notifications.isEmpty()) {
                Text("No Notifications Yet", color = Color.Gray, fontSize = 18.sp)
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(notifications) { notif ->
                        DoctorNotificationCard(
                            notification = notif,
                            isSelectionMode = isSelectionMode,
                            isSelected = selectedNotifications.contains(notif),
                            onLongPress = {
                                isSelectionMode = true
                                selectedNotifications.add(notif)
                            },
                            onSelectToggle = {
                                if (selectedNotifications.contains(notif)) selectedNotifications.remove(notif)
                                else selectedNotifications.add(notif)

                                if (selectedNotifications.isEmpty()) isSelectionMode = false
                            },
                            primary = primary,
                            white = white
                        )
                    }
                }
            }
        }
    }
}

// ----------------------------------------------------------
// CARD
// ----------------------------------------------------------
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DoctorNotificationCard(
    notification: NotificationModel,
    isSelectionMode: Boolean,
    isSelected: Boolean,
    onLongPress: () -> Unit,
    onSelectToggle: () -> Unit,
    primary: Color,
    white: Color
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = {
                    if (isSelectionMode) onSelectToggle()
                },
                onLongClick = onLongPress
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) primary.copy(alpha = 0.20f) else white
        ),
        elevation = CardDefaults.cardElevation(3.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {

            // SHOW CHECKBOX ONLY IN SELECTION MODE
            if (isSelectionMode) {
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = { onSelectToggle() }
                )
                Spacer(Modifier.width(12.dp))
            }

            Column(Modifier.weight(1f)) {
                Text(notification.title, fontWeight = FontWeight.Bold, color = Color.Black)
                Spacer(Modifier.height(4.dp))
                Text(notification.message, color = Color.DarkGray)
                Spacer(Modifier.height(6.dp))
                Text(formatTimestamp(notification.timestamp), fontSize = 12.sp, color = Color.Gray)
            }
        }
    }
}

fun formatTimestamp(timestamp: Any?): String {
    return try {
        val date = when (timestamp) {
            is com.google.firebase.Timestamp -> timestamp.toDate()
            is Long -> Date(timestamp)
            else -> return "Just now"
        }
        SimpleDateFormat("MMM dd, yyyy • hh:mm a", Locale.getDefault()).format(date)
    } catch (_: Exception) {
        "Just now"
    }
}
