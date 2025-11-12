// File: PatientAppointmentsScreen.kt
package com.example.caresync.PatientHome

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.caresync.R

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import com.example.caresync.notification.NotificationHelper


data class Appointment(
    val id: String = "",
    val doctorId: String = "",
    val doctorName: String = "",
    val doctorEmail: String = "",
    val doctorImage: String = "",
    val patientId: String = "",
    val patientName: String = "",
    val patientEmail: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientAppointmentsScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelGreen = colorResource(id = R.color.pastel_green)
    val pastelPink = colorResource(id = R.color.pastel_pink)
    val pastelOrange = colorResource(id = R.color.pastel_orange)
    val pastelBlue = colorResource(id = R.color.pastel_blue)

    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()
    val auth = FirebaseAuth.getInstance()
    val currentUserId = auth.currentUser?.uid ?: ""

    var appointments by remember { mutableStateOf<List<Appointment>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var selectedAppointment by remember { mutableStateOf<Appointment?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var showCancelConfirmation by remember { mutableStateOf(false) }
    var cancelReason by remember { mutableStateOf("") }

    // Function to fetch appointments (only Booked status)
    suspend fun fetchAppointments() {
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("Appointments")
                .whereEqualTo("patientId", currentUserId)
                .whereEqualTo("status", "Booked")  // Only fetch booked appointments
                .get()
                .await()

            appointments = snapshot.documents.mapNotNull { doc ->
                Appointment(
                    id = doc.id,
                    doctorId = doc.getString("doctorId") ?: "",
                    doctorName = doc.getString("doctorName") ?: "",
                    doctorEmail = doc.getString("doctorEmail") ?: "",
                    doctorImage = doc.getString("doctorImage") ?: "",
                    patientId = doc.getString("patientId") ?: "",
                    patientName = doc.getString("patientName") ?: "",
                    patientEmail = doc.getString("patientEmail") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: "",
                    status = doc.getString("status") ?: ""
                )
            }.sortedBy { it.date }

        } catch (e: Exception) {
            Toast.makeText(context, "Error loading appointments", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    // Fetch appointments on load
    LaunchedEffect(Unit) {
        fetchAppointments()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Appointments", color = white, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = white)
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
                    brush = Brush.verticalGradient(
                        colors = listOf(pastelGreen, pastelPink, pastelOrange, pastelBlue),
                        startY = 0f,
                        endY = 2000f
                    )
                )
        ) {
            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primary)
                }
            } else if (appointments.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "No Appointments Yet",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.Gray
                        )
                        Spacer(Modifier.height(8.dp))
                        Text(
                            text = "Book your first appointment with a doctor",
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(appointments) { appointment ->
                        AppointmentCard(
                            appointment = appointment,
                            primary = primary,
                            white = white,
                            onClick = {
                                selectedAppointment = appointment
                                showDetailDialog = true
                            }
                        )
                    }
                }
            }
        }

        // Detail Dialog
        if (showDetailDialog && selectedAppointment != null) {
            AppointmentDetailDialog(
                appointment = selectedAppointment!!,
                primary = primary,
                white = white,
                onDismiss = { showDetailDialog = false },
                onCancelClick = {
                    // Check 24 hour restriction
                    val canCancel = canCancelAppointment(selectedAppointment!!.date, selectedAppointment!!.time)
                    if (canCancel) {
                        showDetailDialog = false
                        showCancelConfirmation = true
                    } else {
                        Toast.makeText(
                            context,
                            "Cannot cancel! Appointments must be cancelled at least 24 hours before the scheduled time.",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            )
        }

        // Cancel Confirmation Dialog
        if (showCancelConfirmation && selectedAppointment != null) {
            CancelAppointmentDialog(
                appointment = selectedAppointment!!,
                primary = primary,
                white = white,
                cancelReason = cancelReason,
                onReasonChange = { cancelReason = it },
                onDismiss = {
                    showCancelConfirmation = false
                    cancelReason = ""
                },
                onConfirm = {
                    coroutine.launch {
                        try {
                            val db = FirebaseFirestore.getInstance()

                            // Send notification to doctor BEFORE deleting
                            com.example.caresync.notification.NotificationHelper.sendAppointmentCancelledNotification(
                                doctorId = selectedAppointment!!.doctorId,
                                patientName = selectedAppointment!!.patientName,
                                date = selectedAppointment!!.date,
                                time = selectedAppointment!!.time,
                                reason = cancelReason
                            )



                            // DELETE the appointment completely
                            db.collection("Appointments")
                                .document(selectedAppointment!!.id)
                                .delete()
                                .await()

                            // Refresh appointments list
                            fetchAppointments()

                            Toast.makeText(context, "Appointment cancelled and doctor notified", Toast.LENGTH_SHORT).show()
                            showCancelConfirmation = false
                            cancelReason = ""

                        } catch (e: Exception) {
                            Toast.makeText(context, "Failed to cancel: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            )
        }
    }
}

// Function to check if appointment can be cancelled (24 hour rule)
fun canCancelAppointment(dateStr: String, timeStr: String): Boolean {
    try {
        // Parse date format: "26/10/2025" -> day/month/year
        val dateParts = dateStr.split("/")
        if (dateParts.size != 3) return false

        val day = dateParts[0].toInt()
        val month = dateParts[1].toInt() - 1 // Calendar months are 0-indexed
        val year = dateParts[2].toInt()

        // Parse time format: "10:30 AM" or "02:45 PM"
        val timeParts = timeStr.trim().split(" ")
        if (timeParts.size != 2) return false

        val hourMinute = timeParts[0].split(":")
        if (hourMinute.size != 2) return false

        var hour = hourMinute[0].toInt()
        val minute = hourMinute[1].toInt()
        val amPm = timeParts[1].uppercase()

        // Convert to 24-hour format
        if (amPm == "PM" && hour != 12) {
            hour += 12
        } else if (amPm == "AM" && hour == 12) {
            hour = 0
        }

        // Create appointment calendar
        val appointmentCal = Calendar.getInstance().apply {
            set(Calendar.YEAR, year)
            set(Calendar.MONTH, month)
            set(Calendar.DAY_OF_MONTH, day)
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        // Current time
        val currentCal = Calendar.getInstance()

        // Calculate difference in milliseconds
        val diffInMillis = appointmentCal.timeInMillis - currentCal.timeInMillis
        val diffInHours = TimeUnit.MILLISECONDS.toHours(diffInMillis)

        // Must be at least 24 hours before appointment
        return diffInHours >= 24

    } catch (e: Exception) {
        e.printStackTrace()
        return false
    }
}

@Composable
fun AppointmentCard(
    appointment: Appointment,
    primary: Color,
    white: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = white)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Doctor Image
            Box(
                modifier = Modifier
                    .size(70.dp)
                    .clip(CircleShape)
                    .border(2.dp, primary, CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (appointment.doctorImage.isNotEmpty()) {
                    Image(
                        painter = rememberAsyncImagePainter(appointment.doctorImage),
                        contentDescription = "Doctor",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_doctor_placeholder),
                        contentDescription = "Doctor",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop
                    )
                }
            }

            Spacer(Modifier.width(16.dp))

            // Appointment Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Dr. ${appointment.doctorName}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                Spacer(Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "Date",
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = appointment.date,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = "Time",
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = appointment.time,
                        fontSize = 14.sp,
                        color = Color.DarkGray
                    )
                }

                Spacer(Modifier.height(8.dp))

                // Status Badge
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = primary.copy(alpha = 0.15f)
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        text = appointment.status,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = primary
                    )
                }
            }
        }
    }
}

@Composable
fun AppointmentDetailDialog(
    appointment: Appointment,
    primary: Color,
    white: Color,
    onDismiss: () -> Unit,
    onCancelClick: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = white)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header with close button
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Appointment Details",
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = primary
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(20.dp))

                // Doctor Image & Name
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(80.dp)
                            .clip(CircleShape)
                            .border(3.dp, primary, CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (appointment.doctorImage.isNotEmpty()) {
                            Image(
                                painter = rememberAsyncImagePainter(appointment.doctorImage),
                                contentDescription = "Doctor",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_doctor_placeholder),
                                contentDescription = "Doctor",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }

                    Spacer(Modifier.width(16.dp))

                    Column {
                        Text(
                            text = "Dr. ${appointment.doctorName}",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = Color.Black
                        )
                        Text(
                            text = appointment.doctorEmail,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))
                Divider(color = Color.LightGray)
                Spacer(Modifier.height(16.dp))

                // Appointment Info
                DetailInfoRow(
                    icon = Icons.Default.CalendarToday,
                    label = "Date",
                    value = appointment.date,
                    primary = primary
                )
                Spacer(Modifier.height(12.dp))

                DetailInfoRow(
                    icon = Icons.Default.AccessTime,
                    label = "Time",
                    value = appointment.time,
                    primary = primary
                )
                Spacer(Modifier.height(12.dp))

                DetailInfoRow(
                    icon = Icons.Default.Person,
                    label = "Patient",
                    value = appointment.patientName,
                    primary = primary
                )
                Spacer(Modifier.height(12.dp))

                DetailInfoRow(
                    icon = Icons.Default.Email,
                    label = "Email",
                    value = appointment.patientEmail,
                    primary = primary
                )

                Spacer(Modifier.height(16.dp))

                // Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = primary.copy(alpha = 0.15f)
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            text = "Status: ${appointment.status}",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = primary
                        )
                    }
                }

                Spacer(Modifier.height(24.dp))

                // Cancel Button
                Button(
                    onClick = onCancelClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Text(
                        "Cancel Appointment",
                        color = white,
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp
                    )
                }
            }
        }
    }
}

@Composable
fun DetailInfoRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    primary: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = primary,
            modifier = Modifier.size(20.dp)
        )
        Spacer(Modifier.width(12.dp))
        Column {
            Text(
                text = label,
                fontSize = 12.sp,
                color = Color.Gray,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = value,
                fontSize = 16.sp,
                color = Color.Black,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CancelAppointmentDialog(
    appointment: Appointment,
    primary: Color,
    white: Color,
    cancelReason: String,
    onReasonChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(24.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
            colors = CardDefaults.cardColors(containerColor = white)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Cancel Appointment?",
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = Color.Red
                )

                Spacer(Modifier.height(16.dp))

                Text(
                    text = "Please provide a reason for cancellation:",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = cancelReason,
                    onValueChange = onReasonChange,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    placeholder = { Text("Enter cancellation reason...") },
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = Color.LightGray
                    ),
                    maxLines = 4
                )

                Spacer(Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            contentColor = primary
                        ),
                        border = androidx.compose.foundation.BorderStroke(2.dp, primary)
                    ) {
                        Text("Keep", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            if (cancelReason.trim().isNotEmpty()) {
                                onConfirm()
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                        enabled = cancelReason.trim().isNotEmpty()
                    ) {
                        Text("Cancel", color = white, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }
}