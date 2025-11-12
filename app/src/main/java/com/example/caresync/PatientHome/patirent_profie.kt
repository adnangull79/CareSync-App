package com.example.caresync.PatientHome


import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var patientName by remember { mutableStateOf("Patient Name") }
    var patientEmail by remember { mutableStateOf("example@email.com") }
    var patientImage by remember { mutableStateOf<String?>(null) }

    var isLoading by remember { mutableStateOf(true) }

    // Fetch patient info
    LaunchedEffect(uid) {
        uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                val firstName = snap.getString("firstName") ?: ""
                val lastName = snap.getString("lastName") ?: ""
                patientName = "$firstName $lastName"
                patientEmail = snap.getString("email") ?: "example@email.com"
                patientImage = snap.getString("profileImageUrl")
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
                        "My Profile",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
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
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        brush = Brush.verticalGradient(
                            colors = gradientColors,
                            startY = 0f,
                            endY = 1200f
                        )
                    )
                    .verticalScroll(rememberScrollState())
                    .padding(padding)
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Profile Image
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(3.dp, primary),
                    modifier = Modifier.size(130.dp)
                ) {
                    if (!patientImage.isNullOrEmpty()) {
                        var isImageLoading by remember { mutableStateOf(true) }

                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = patientImage,
                                contentDescription = "Profile Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape),
                                onState = {
                                    isImageLoading =
                                        it is AsyncImagePainter.State.Loading || it is AsyncImagePainter.State.Empty
                                }
                            )
                            if (isImageLoading) {
                                CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
                            }
                        }
                    } else {
                        Image(
                            painter = painterResource(id = R.drawable.ic_patinet),
                            contentDescription = "Patient Placeholder",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.clip(CircleShape)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Patient Info
                Text(
                    text = patientName,
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = primary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = patientEmail,
                    fontSize = 15.sp,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Edit Profile Button
                Button(
                    onClick = { navController.navigate(Screen.EditPatientProfileScreen.route) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primary),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = "Edit Profile",
                        color = Color.White,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Additional Info Section
                SectionHeader("Personal Information", primary)
                InfoCard("Email", patientEmail, Icons.Default.Email, primary)
                InfoCard("Full Name", patientName, Icons.Default.Person, primary)
                InfoCard("Account Type", "Patient", Icons.Default.Badge, primary)

                Spacer(modifier = Modifier.height(20.dp))

                // Quick Links
                SectionHeader("Quick Actions", primary)
                QuickActionCard("My Appointments", Icons.Default.DateRange, primary) {
                    navController.navigate(Screen.PatientAppointments.route)
                }

                QuickActionCard("FAQs", Icons.Default.Help, primary) {
                    navController.navigate(Screen.PatientFAQScreen.route)
                }
                QuickActionCard("About App", Icons.Default.Info, primary) {
                    navController.navigate(Screen.AboutScreen.route)
                }

                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SectionHeader(title: String, color: Color) {
    Text(
        text = title,
        fontSize = 20.sp,
        fontWeight = FontWeight.SemiBold,
        color = color,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    )
}

@Composable
fun InfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color),
        shadowElevation = 4.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = label, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(label, fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.Black)
                Text(value, fontSize = 14.sp, color = Color.DarkGray)
            }
        }
    }
}

@Composable
fun QuickActionCard(title: String, icon: androidx.compose.ui.graphics.vector.ImageVector, color: Color, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        border = BorderStroke(1.dp, color),
        shadowElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = color, modifier = Modifier.size(26.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }
    }
}
