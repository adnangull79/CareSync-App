package com.example.caresync.DoctorHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorProfileScreen(navController: NavController) {

    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelColors = listOf(
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_pink),
        colorResource(id = R.color.pastel_orange),
        colorResource(id = R.color.pastel_blue)
    )

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val context = LocalContext.current

    var doctorData by remember { mutableStateOf<Map<String, String>>(emptyMap()) }
    var isLoading by remember { mutableStateOf(true) }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // ✅ Fetch doctor data and image from Firestore
    LaunchedEffect(Unit) {
        val uid = auth.currentUser?.uid ?: return@LaunchedEffect
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                if (doc.exists()) {
                    val data = doc.data?.mapValues { it.value.toString() } ?: emptyMap()
                    doctorData = data
                    profileImageUrl = data["profileImageUrl"]
                }
                isLoading = false
            }
            .addOnFailureListener {
                isLoading = false
            }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Doctor Profile",
                        color = white,
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = white)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        }
    ) { padding ->

        if (isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primary)
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = pastelColors,
                            startY = 0f,
                            endY = 1800f
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // ================== Profile Image ==================
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(3.dp, primary),
                    modifier = Modifier.size(130.dp)
                ) {
                    if (!profileImageUrl.isNullOrEmpty()) {
                        var isImageLoading by remember { mutableStateOf(true) }

                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Doctor Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .fillMaxSize(),
                                onState = {
                                    isImageLoading = it is AsyncImagePainter.State.Loading
                                }
                            )

                            if (isImageLoading) {
                                CircularProgressIndicator(
                                    color = primary,
                                    strokeWidth = 2.dp,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_doctor_placeholder),
                            contentDescription = "Doctor Placeholder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // ================== Name & Specialization ==================
                val fullName = doctorData["name"] ?: "Unknown"
                val firstName = fullName.split(" ").firstOrNull()?.uppercase() ?: "UNKNOWN"

                Text(
                    text = "Dr. $firstName",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = primary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = doctorData["specialization"] ?: "Specialization not available",
                    fontSize = 16.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ================== PERSONAL INFO ==================
                SectionHeader("Personal Info", primary)
                ProfileInfoCard(Icons.Default.Email, "Email", doctorData["email"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.Phone, "Phone", doctorData["phone"] ?: "Not available", primary)

                Spacer(modifier = Modifier.height(16.dp))

                // ================== PROFESSIONAL INFO ==================
                SectionHeader("Professional Info", primary)
                ProfileInfoCard(Icons.Default.Work, "Qualification", doctorData["qualification"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.LocalHospital, "Specialization", doctorData["specialization"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.Timer, "Years of Experience", doctorData["yearsExperience"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.Badge, "License Number", doctorData["licenseNumber"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.People, "Patient Capacity", doctorData["patientCapacity"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.Person, "Min Patients/Day", doctorData["minPatients"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.PersonOutline, "Max Patients/Day", doctorData["maxPatients"] ?: "Not available", primary)

                Spacer(modifier = Modifier.height(16.dp))

                // ================== CONTACT INFO ==================
                SectionHeader("Contact Info", primary)
                ProfileInfoCard(Icons.Default.Home, "Clinic Name", doctorData["clinicName"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.Place, "Clinic Address", doctorData["clinicAddress"] ?: "Not available", primary)
                ProfileInfoCard(Icons.Default.Schedule, "Working Hours", doctorData["workingHours"] ?: "Not available", primary)

                Spacer(modifier = Modifier.height(32.dp))

                // Edit Profile Button
                Button(
                    onClick = { navController.navigate(Screen.EditDoctorProfile.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        color = white,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, primary: Color) {
    Text(
        text = title,
        fontSize = 20.sp,
        color = primary,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun ProfileInfoCard(
    icon: ImageVector,
    title: String,
    value: String,
    primary: Color
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.95f),
        border = BorderStroke(1.dp, primary),
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = primary, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.SemiBold, color = Color.Black)
                Spacer(modifier = Modifier.height(4.dp))
                Text(value, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}
