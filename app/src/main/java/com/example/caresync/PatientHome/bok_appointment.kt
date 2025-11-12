// File: DoctorDetailScreen.kt
package com.example.caresync.PatientHome

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.AccessTime
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
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.caresync.R
// NOTE: your notification helper is used by fully-qualified name below,
// so this import isn't required. Keeping your other code "same as before".
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorDetailScreen(
    navController: NavController,
    doctorId: String
) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelGreen = colorResource(id = R.color.pastel_green)
    val pastelPink = colorResource(id = R.color.pastel_pink)
    val pastelOrange = colorResource(id = R.color.pastel_orange)
    val pastelBlue = colorResource(id = R.color.pastel_blue)

    val context = LocalContext.current
    val coroutine = rememberCoroutineScope()

    var doctorData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    // Booking-related states
    var showBookingDialog by remember { mutableStateOf(false) }
    var checkingCapacity by remember { mutableStateOf(false) }
    var showSuccessDialog by remember { mutableStateOf(false) }

    // Fetch doctor details from Firebase
    LaunchedEffect(doctorId) {
        val db = FirebaseFirestore.getInstance()
        try {
            val doc = db.collection("users").document(doctorId).get().await()
            doctorData = doc.data
        } catch (e: Exception) {
            Toast.makeText(context, "Error loading doctor details", Toast.LENGTH_SHORT).show()
        } finally {
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Doctor Details", color = white, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = white)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        },
        bottomBar = {
            if (!isLoading && doctorData != null) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .windowInsetsPadding(WindowInsets.navigationBars),
                    color = Color.White,
                    shadowElevation = 8.dp
                ) {
                    Button(
                        onClick = { showBookingDialog = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(16.dp),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text(
                            "Book Appointment",
                            color = white,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        )
                    }
                }
            }
        }
    ) { padding ->

        if (isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = primary)
            }
        } else {
            doctorData?.let { data ->
                val imageUrl = data["imageUrl"] as? String ?: data["profileImageUrl"] as? String
                val name = (data["fullName"] ?: data["name"] ?: "Unknown").toString()
                val specialization = (data["specialization"] ?: "Specialist").toString()
                val experience = (data["yearsExperience"] ?: data["experience"] ?: "N/A").toString()
                val qualification = (data["qualification"] ?: "Not specified").toString()
                val clinicName = (data["clinicName"] ?: "Not provided").toString()
                val address = (data["clinicAddress"] ?: "Not provided").toString()
                val workingHours = (data["workingHours"] ?: "N/A").toString()
                val patientCapacity = (data["patientCapacity"] ?: "N/A").toString()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .verticalScroll(rememberScrollState())
                        .background(
                            brush = Brush.verticalGradient(
                                colors = listOf(pastelGreen, pastelPink, pastelOrange, pastelBlue),
                                startY = 0f,
                                endY = 2000f
                            )
                        )
                ) {
                    // Header Section with Profile Image
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(primary)
                            .padding(vertical = 32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            // Profile Image with Loading State
                            ProfileImageWithLoader(imageUrl = imageUrl, primary = primary)

                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = "Dr. $name",
                                fontWeight = FontWeight.Bold,
                                color = white,
                                style = MaterialTheme.typography.headlineSmall,
                                fontSize = 24.sp
                            )
                            Text(
                                text = specialization,
                                color = Color.White.copy(alpha = 0.95f),
                                style = MaterialTheme.typography.bodyLarge,
                                fontSize = 16.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Stats Section - Professional Layout
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = white)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            StatItem("Experience", "$experience Yrs", primary)

                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = Color.LightGray
                            )

                            StatItem("Rating", "4.5 ★", primary)

                            Divider(
                                modifier = Modifier
                                    .height(40.dp)
                                    .width(1.dp),
                                color = Color.LightGray
                            )

                            StatItem("Patients", "$patientCapacity+", primary)
                        }
                    }

                    Spacer(Modifier.height(20.dp))

                    // About Section - Card View
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = white)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "About",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = primary,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = buildAboutText(name, specialization, experience, clinicName),
                                color = Color.DarkGray,
                                textAlign = TextAlign.Justify,
                                lineHeight = 22.sp,
                                fontSize = 15.sp
                            )
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Clinic Details Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = white)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Clinic Information",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = primary,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(16.dp))

                            // Clinic Name
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(bottom = 12.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Business,
                                    contentDescription = "Clinic",
                                    tint = primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Clinic Name",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = clinicName,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }

                            Divider(color = Color.LightGray.copy(alpha = 0.5f))
                            Spacer(Modifier.height(12.dp))

                            // Clinic Address
                            Row(
                                verticalAlignment = Alignment.Top,
                                modifier = Modifier.padding(bottom = 8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.LocationOn,
                                    contentDescription = "Address",
                                    tint = primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = "Address",
                                        fontSize = 12.sp,
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = address,
                                        fontSize = 16.sp,
                                        color = Color.Black,
                                        lineHeight = 20.sp
                                    )
                                }
                            }
                        }
                    }

                    Spacer(Modifier.height(16.dp))

                    // Additional Info Card
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = white)
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Text(
                                text = "Additional Details",
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.titleLarge,
                                color = primary,
                                fontSize = 20.sp
                            )
                            Spacer(Modifier.height(12.dp))

                            DetailRow("Qualification", qualification)
                            DetailRow("Working Hours", workingHours)
                        }
                    }

                    Spacer(Modifier.height(80.dp))
                }
            } ?: run {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Doctor not found", color = Color.Gray, fontSize = 18.sp)
                }
            }
        }

        // Beautiful Booking Dialog
        if (showBookingDialog) {
            BeautifulBookingDialog(
                doctorId = doctorId,
                doctorData = doctorData,
                onDismiss = { showBookingDialog = false },
                onBooked = { showSuccessDialog = true },
                context = context,
                coroutine = coroutine,
                checkingCapacity = { checkingCapacity = it },
                primary = primary,
                white = white
            )
        }

        // Loading Dialog
        if (checkingCapacity) {
            AlertDialog(
                onDismissRequest = {},
                title = { Text("Processing...", fontWeight = FontWeight.Bold, color = primary) },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = primary
                        )
                        Spacer(Modifier.width(16.dp))
                        Text("Checking capacity and booking your appointment...")
                    }
                },
                confirmButton = {}
            )
        }

        // Success Dialog
        if (showSuccessDialog) {
            AlertDialog(
                onDismissRequest = { showSuccessDialog = false },
                title = { Text("✓ Appointment Confirmed!", fontWeight = FontWeight.Bold, color = primary) },
                text = { Text("Your appointment has been successfully booked.") },
                confirmButton = {
                    Button(
                        onClick = { showSuccessDialog = false },
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text("OK")
                    }
                }
            )
        }
    }
}

@Composable
fun ProfileImageWithLoader(imageUrl: String?, primary: Color) {
    Box(
        modifier = Modifier
            .size(130.dp)
            .clip(CircleShape)
            .border(4.dp, Color.White, CircleShape)
            .background(Color.White),
        contentAlignment = Alignment.Center
    ) {
        if (imageUrl.isNullOrEmpty()) {
            // Show dummy image directly if no URL exists
            Image(
                painter = painterResource(id = R.drawable.ic_doctor_placeholder),
                contentDescription = "Doctor Profile",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Show loading indicator only when fetching from Firebase
            val painter = rememberAsyncImagePainter(model = imageUrl)
            val painterState = painter.state

            if (painterState is AsyncImagePainter.State.Loading) {
                CircularProgressIndicator(
                    color = primary,
                    strokeWidth = 3.dp,
                    modifier = Modifier.size(40.dp)
                )
            } else {
                Image(
                    painter = painter,
                    contentDescription = "Doctor Profile",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }
        }
    }
}

@Composable
fun StatItem(label: String, value: String, primary: Color) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.padding(8.dp)
    ) {
        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            color = primary,
            fontSize = 20.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 14.sp
        )
    }
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Text(
            text = "$label: ",
            fontWeight = FontWeight.SemiBold,
            color = Color.Black.copy(alpha = 0.7f),
            fontSize = 15.sp
        )
        Text(
            text = value,
            color = Color.DarkGray,
            fontSize = 15.sp
        )
    }
}

fun buildAboutText(name: String, specialization: String, experience: String, clinicName: String): String {
    return "Dr. $name is a $specialization with $experience years of experience. ${name.split(" ").firstOrNull() ?: "The doctor"} is currently practicing at $clinicName clinic, providing comprehensive healthcare services with dedication and expertise."
}

// =======================
// Booking Dialog (UPDATED)
// =======================
@Composable
fun BeautifulBookingDialog(
    doctorId: String,
    doctorData: Map<String, Any>?,
    onDismiss: () -> Unit,
    onBooked: () -> Unit,
    context: android.content.Context,
    coroutine: kotlinx.coroutines.CoroutineScope,
    checkingCapacity: (Boolean) -> Unit,
    primary: Color,
    white: Color
) {
    var selectedDate by remember { mutableStateOf("") }
    var selectedTime by remember { mutableStateOf("") }
    var showConfirmation by remember { mutableStateOf(false) }

    // Helper: validate time window (9 AM inclusive to < 10 PM)
    fun isTimeWithinClinicHours(timeLabel: String): Boolean {
        // "hh:mm AM/PM"
        return try {
            val parts = timeLabel.trim().split(" ")
            if (parts.size != 2) return false
            val hm = parts[0].split(":")
            if (hm.size != 2) return false
            var hour = hm[0].toInt()
            val minute = hm[1].toInt()
            val ampm = parts[1].uppercase(Locale.getDefault())

            // Convert to 24h
            if (ampm == "PM" && hour != 12) hour += 12
            if (ampm == "AM" && hour == 12) hour = 0

            // Clinic hours: 09:00 to 21:59 (i.e., hour in [9..21])
            hour in 9..21 && minute in 0..59
        } catch (_: Exception) {
            false
        }
    }

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
                // Header
                Text(
                    text = "Book Appointment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 24.sp,
                    color = primary,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                Text(
                    text = "Select your preferred date and time",
                    color = Color.Gray,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Date Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "Date",
                            tint = primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Date",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (selectedDate.isEmpty()) "Select Date" else selectedDate,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedDate.isEmpty()) Color.Gray else Color.Black
                            )
                        }
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            val dp = DatePickerDialog(
                                context,
                                { _, year, month, day ->
                                    selectedDate = "$day/${month + 1}/$year"
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            )
                            // ✅ Disable past dates (min = today 00:00)
                            val todayStart = Calendar.getInstance().apply {
                                set(Calendar.HOUR_OF_DAY, 0)
                                set(Calendar.MINUTE, 0)
                                set(Calendar.SECOND, 0)
                                set(Calendar.MILLISECOND, 0)
                            }.timeInMillis
                            dp.datePicker.minDate = todayStart
                            dp.show()
                        }) {
                            Text("PICK", color = primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Time Selection Card
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.1f)),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.AccessTime,
                            contentDescription = "Time",
                            tint = primary,
                            modifier = Modifier.size(28.dp)
                        )
                        Spacer(Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Time",
                                fontSize = 12.sp,
                                color = Color.Gray,
                                fontWeight = FontWeight.Medium
                            )
                            Text(
                                text = if (selectedTime.isEmpty()) "Select Time" else selectedTime,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = if (selectedTime.isEmpty()) Color.Gray else Color.Black
                            )
                        }
                        TextButton(onClick = {
                            val cal = Calendar.getInstance()
                            TimePickerDialog(
                                context,
                                { _, hour, minute ->
                                    // Convert to AM/PM label
                                    val amPm = if (hour >= 12) "PM" else "AM"
                                    val formattedHour =
                                        if (hour > 12) hour - 12 else if (hour == 0) 12 else hour
                                    val label = "%02d:%02d %s".format(formattedHour, minute, amPm)

                                    // ✅ Business hour restriction: 9:00–21:59
                                    if (hour < 9 || hour >= 22) {
                                        Toast.makeText(
                                            context,
                                            "Clinic operates between 9:00 AM and 10:00 PM. Please select a valid time.",
                                            Toast.LENGTH_LONG
                                        ).show()
                                    } else {
                                        selectedTime = label
                                    }
                                },
                                cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE),
                                false
                            ).show()
                        }) {
                            Text("PICK", color = primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Buttons
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
                        Text("Cancel", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }

                    Button(
                        onClick = {
                            if (selectedDate.isEmpty() || selectedTime.isEmpty()) {
                                Toast.makeText(context, "Please select date and time", Toast.LENGTH_SHORT).show()
                                return@Button
                            }

                            // ✅ Validate time window again for safety
                            if (!isTimeWithinClinicHours(selectedTime)) {
                                Toast.makeText(
                                    context,
                                    "Please choose a time between 9:00 AM and 10:00 PM.",
                                    Toast.LENGTH_LONG
                                ).show()
                                return@Button
                            }

                            // ✅ Show confirmation dialog first
                            showConfirmation = true
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                    ) {
                        Text("Confirm", color = white, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    }
                }
            }
        }
    }

    // ✅ Confirmation Dialog before booking
    if (showConfirmation) {
        AlertDialog(
            onDismissRequest = { showConfirmation = false },
            title = { Text("Important Notice", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Appointment time may change depending on clinic flow.\n" +
                            "You will be served on a first-come-first-served basis.\n\n" +
                            "Do you want to continue?"
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    showConfirmation = false
                    // Proceed with the original booking flow
                    checkingCapacity(true)
                    onDismiss()

                    coroutine.launch {
                        try {
                            val db = FirebaseFirestore.getInstance()
                            val auth = FirebaseAuth.getInstance()
                            val patient = auth.currentUser

                            // Fetch patient data from Firestore
                            val patientDoc = patient?.uid?.let {
                                db.collection("users").document(it).get().await()
                            }

                            val patientName = patientDoc?.getString("firstName")?.let { first ->
                                val last = patientDoc.getString("lastName") ?: ""
                                "$first $last".trim()
                            } ?: patientDoc?.getString("fullName") ?: "Unknown Patient"

                            val patientImage = patientDoc?.getString("profileImageUrl") ?: ""

                            // Check capacity
                            val appointmentsRef = db.collection("Appointments")
                            val snapshot = appointmentsRef
                                .whereEqualTo("doctorId", doctorId)
                                .whereEqualTo("date", selectedDate)
                                .whereEqualTo("status", "Booked")
                                .get().await()

                            val currentCount = snapshot.size()

                            // Handle both String and Number types for capacity
                            val maxCap = when (val capacity = doctorData?.get("patientCapacity")) {
                                is String -> capacity.toIntOrNull() ?: 30
                                is Long -> capacity.toInt()
                                is Int -> capacity
                                else -> 30
                            }

                            if (currentCount >= maxCap) {
                                checkingCapacity(false)
                                Toast.makeText(
                                    context,
                                    "Doctor's capacity reached for that date ($currentCount/$maxCap). Please choose another day.",
                                    Toast.LENGTH_LONG
                                ).show()
                            } else {
                                val appointmentData = hashMapOf(
                                    "doctorId" to doctorId,
                                    "doctorName" to (doctorData?.get("fullName") ?: doctorData?.get("name") ?: ""),
                                    "doctorEmail" to (doctorData?.get("email") ?: ""),
                                    "doctorImage" to (doctorData?.get("imageUrl") ?: doctorData?.get("profileImageUrl") ?: ""),
                                    "patientId" to (patient?.uid ?: ""),
                                    "patientName" to patientName,
                                    "patientEmail" to (patient?.email ?: ""),
                                    "patientImage" to patientImage,
                                    "date" to selectedDate,
                                    "time" to selectedTime,
                                    "status" to "Booked",
                                    "cancelledBy" to "",
                                    "cancelReason" to "",
                                    "timestamp" to com.google.firebase.Timestamp.now()
                                )

                                db.collection("Appointments").add(appointmentData).await()

                                // Send Notification to doctor
                                com.example.caresync.notification.NotificationHelper.sendAppointmentBookedNotification(
                                    doctorId = doctorId,
                                    patientName = patientName,
                                    date = selectedDate,
                                    time = selectedTime
                                )


                                checkingCapacity(false)

                                Toast.makeText(
                                    context,
                                    "Appointment booked and doctor notified!",
                                    Toast.LENGTH_SHORT
                                ).show()

                                onBooked()
                            }
                        } catch (e: Exception) {
                            checkingCapacity(false)
                            Toast.makeText(context, "Booking failed: ${e.message}", Toast.LENGTH_LONG).show()
                            android.util.Log.e("Booking", "Error: ${e.message}", e)
                        }
                    }
                }) {
                    Text("Yes", color = primary, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmation = false }) {
                    Text("No")
                }
            }
        )
    }
}
