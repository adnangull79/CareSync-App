// File: AppointmentScreen.kt
package com.example.caresync.PatientHome

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

data class Doctor(
    val id: String = "",
    val name: String = "",
    val specialization: String = "",
    val rating: String = "⭐ 4.5",
    val imageUrl: String = ""
)

data class UpcomingAppointment(
    val id: String = "",
    val doctorName: String = "",
    val doctorImage: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = ""
)

@Composable
fun AppointmentScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)

    val pastelColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val categories = listOf(
        "All", "Cardiologist", "Dermatologist", "Neurologist", "Orthopedic",
        "Pediatrician", "Gynecologist", "Psychiatrist", "General Surgeon",
        "ENT Specialist", "Oncologist"
    )

    var selectedCategory by remember { mutableStateOf("All") }
    var expanded by remember { mutableStateOf(false) }
    var searchQuery by remember { mutableStateOf("") }

    var doctors by remember { mutableStateOf(listOf<Doctor>()) }
    var upcomingAppointments by remember { mutableStateOf(listOf<UpcomingAppointment>()) }
    var isLoadingDoctors by remember { mutableStateOf(true) }
    var isLoadingAppointments by remember { mutableStateOf(true) }

    val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

    // Fetch latest ONE upcoming appointment
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("Appointments")
                .whereEqualTo("patientId", currentUserId)
                .whereEqualTo("status", "Booked")
                .get()
                .await()

            upcomingAppointments = snapshot.documents.mapNotNull { doc ->
                UpcomingAppointment(
                    id = doc.id,
                    doctorName = doc.getString("doctorName") ?: "",
                    doctorImage = doc.getString("doctorImage") ?: "",
                    date = doc.getString("date") ?: "",
                    time = doc.getString("time") ?: "",
                    status = doc.getString("status") ?: ""
                )
            }.sortedByDescending { it.date }.take(1) // ✅ Show only latest one
        } catch (_: Exception) { }
        isLoadingAppointments = false
    }

    // Fetch doctors
    LaunchedEffect(Unit) {
        val db = FirebaseFirestore.getInstance()
        try {
            val snapshot = db.collection("users")
                .whereEqualTo("role", "doctor")
                .get().await()

            doctors = snapshot.documents.mapNotNull { doc ->
                Doctor(
                    id = doc.id,
                    name = doc.getString("name") ?: doc.getString("fullName") ?: return@mapNotNull null,
                    specialization = doc.getString("specialization") ?: "Unknown",
                    rating = doc.getString("rating") ?: "⭐ 4.5",
                    imageUrl = doc.getString("profileImageUrl") ?: ""
                )
            }
        } catch (_: Exception) { }
        isLoadingDoctors = false
    }

    val filteredDoctors = remember(searchQuery, selectedCategory, doctors) {
        val byCategory = if (selectedCategory == "All") doctors
        else doctors.filter { it.specialization.equals(selectedCategory, ignoreCase = true) }

        if (searchQuery.isEmpty()) byCategory else byCategory.filter {
            it.name.contains(searchQuery, ignoreCase = true)
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Brush.verticalGradient(colors = pastelColors, startY = 0f, endY = 1200f))
            .padding(horizontal = 12.dp, vertical = 12.dp)
    ) {

        item {
            Spacer(Modifier.height(90.dp))
            UpcomingAppointmentsSection(upcomingAppointments, isLoadingAppointments, primary, white, navController)
            Spacer(Modifier.height(20.dp))
            Text("Available Doctors", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primary)
            Spacer(Modifier.height(12.dp))
        }

        item {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search Doctor") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = white,
                        unfocusedContainerColor = white
                    )
                )
                Spacer(Modifier.width(10.dp))
                Box {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = primary,
                        modifier = Modifier.size(55.dp).clickable { expanded = true }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                            Icon(Icons.Default.FilterList, null, tint = white)
                        }
                    }
                    DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        categories.forEach {
                            DropdownMenuItem(text = { Text(it) }, onClick = {
                                selectedCategory = it
                                expanded = false
                            })
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }

        if (isLoadingDoctors) {
            item {
                Box(Modifier.fillMaxWidth().padding(60.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary)
                }
            }
        } else {
            itemsIndexed(filteredDoctors) { _, doctor ->
                DoctorCard(doctor, primary, white, navController)
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun DoctorCard(doctor: Doctor, primary: Color, white: Color, navController: NavController) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(white),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {

            Box(
                modifier = Modifier.size(60.dp).clip(CircleShape)
                    .border(2.dp, primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(doctor.imageUrl)
                if (painter.state is AsyncImagePainter.State.Loading)
                    CircularProgressIndicator(color = primary, strokeWidth = 2.dp)

                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(Modifier.weight(1f)) {
                Text("Dr. ${doctor.name}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(doctor.specialization, color = Color.Gray)
                Text(doctor.rating, fontSize = 12.sp)
            }

            Button(
                onClick = { navController.navigate(Screen.DoctorDetail.createRoute(doctor.id)) },
                colors = ButtonDefaults.buttonColors(primary),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Book", color = white, fontWeight = FontWeight.Bold) }
        }
    }
}

@Composable
fun UpcomingAppointmentsSection(
    upcomingAppointments: List<UpcomingAppointment>,
    isLoadingAppointments: Boolean,
    primary: Color,
    white: Color,
    navController: NavController
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = white),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {

            Text("Upcoming Appointments", fontWeight = FontWeight.Bold, fontSize = 18.sp, color = primary)

            when {
                isLoadingAppointments -> Box(
                    Modifier.fillMaxWidth().height(100.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
                }

                upcomingAppointments.isEmpty() -> Box(
                    Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("No Upcoming Appointments", fontSize = 16.sp, color = Color.Gray)
                        Text("Book your first appointment below", fontSize = 12.sp, color = Color.Gray)
                    }
                }

                else -> {
                    // ✅ Always show only the latest appointment
                    val latestAppointment = upcomingAppointments.first()
                    UpcomingAppointmentItem(latestAppointment, primary) {
                        navController.navigate(Screen.PatientAppointments.route)
                    }

                    // ✅ Show "See More" if at least ONE appointment exists
                    TextButton(
                        modifier = Modifier.align(Alignment.CenterHorizontally),
                        onClick = { navController.navigate(Screen.PatientAppointments.route) }
                    ) {
                        Text("See More", color = primary, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                        Icon(Icons.Default.ArrowForward, contentDescription = null, tint = primary)
                    }
                }
            }
        }
    }
}

@Composable
fun UpcomingAppointmentItem(app: UpcomingAppointment, primary: Color, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(primary.copy(alpha = 0.08f))
    ) {
        Row(
            Modifier.fillMaxWidth().padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                Modifier.size(60.dp).clip(CircleShape).border(2.dp, primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(app.doctorImage)
                if (painter.state is AsyncImagePainter.State.Loading)
                    CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
                Image(
                    painter = painter,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(16.dp))
            Column(Modifier.weight(1f)) {
                Text("Dr. ${app.doctorName}", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(app.date, color = Color.DarkGray)
                Text(app.time, color = Color.DarkGray)
            }

            Card(colors = CardDefaults.cardColors(primary.copy(alpha = 0.2f))) {
                Text(app.status, color = primary, modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp))
            }
        }
    }
}
