// File: DoctorProfileScreen.kt
package com.example.caresync.DoctorHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import com.example.caresync.PatientHome.CalculatorType
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun doc_profile(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val pastelColors = listOf(
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_pink),
        colorResource(id = R.color.pastel_orange),
        colorResource(id = R.color.pastel_blue)
    )

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    var doctorName by remember { mutableStateOf("Doctor Name") }
    var doctorCategory by remember { mutableStateOf("Specialist") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }
    var showCalculatorDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }

    // Fetch doctor info
    LaunchedEffect(uid) {
        uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                val name = snap.getString("name") ?: "Doctor Name"
                val firstWord = name.split(" ").firstOrNull()?.uppercase() ?: "DOCTOR"
                doctorName = "Dr. $firstWord"
                doctorCategory = snap.getString("specialization") ?: "Specialist"
                profileImageUrl = snap.getString("profileImageUrl")
            }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, end = 8.dp, top = 100.dp, bottom = 80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Profile Header ----------
        Surface(
            shape = CircleShape,
            border = BorderStroke(2.dp, primary),
            modifier = Modifier.size(110.dp)
        ) {
            if (!profileImageUrl.isNullOrEmpty()) {
                // Show loading indicator until image is loaded
                var isImageLoading by remember { mutableStateOf(true) }

                Box(contentAlignment = Alignment.Center) {
                    AsyncImage(
                        model = profileImageUrl,
                        contentDescription = "Doctor Image",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(),
                        onState = {
                            isImageLoading =
                                it is AsyncImagePainter.State.Loading || it is AsyncImagePainter.State.Empty
                        }
                    )

                    if (isImageLoading) {
                        CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
                    }
                }
        }
            //            else {
//                Image(
//                    painter = painterResource(id = R.drawable.ic_doctor_placeholder),
//                    contentDescription = "Doctor Image",
//                    contentScale = ContentScale.Crop,
//                    modifier = Modifier.clip(CircleShape)
//                )
//            }
        }

        // Doctor Name & Specialization
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = doctorName,
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = Color.Black
            )
            Text(
                text = doctorCategory,
                fontSize = 16.sp,
                color = Color.DarkGray
            )
        }

        // ---------- Edit & View Profile Buttons ----------
        Row(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Button(
                onClick = { navController.navigate(Screen.EditDoctorProfile.route) },
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(150.dp)
            ) {
                Text("Edit Profile", color = Color.White, fontSize = 16.sp)
            }

            OutlinedButton(
                onClick = { navController.navigate(Screen.DoctorProfileScreen.route) },
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color.White),
                border = BorderStroke(1.dp, primary),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.width(150.dp)
            ) {
                Text("View Profile", color = primary, fontSize = 16.sp)
            }
        }

        Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.3f))

        // ---------- Options Section ----------
        val options = listOf(
            Triple("Settings", Icons.Default.Settings, {
                navController.navigate(Screen.DoctorSettings.route)
            }),
            Triple("Calculator", Icons.Default.FitnessCenter, { showCalculatorDialog = true }),
            Triple("About", Icons.Default.Info, { navController.navigate(Screen.AboutScreen.route) }),
            Triple("Contact", Icons.Default.Email, { navController.navigate(Screen.ContactScreen.route) })
        )

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { (title, icon, action) ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = Color.White,
                    border = BorderStroke(1.dp, primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(65.dp)
                        .clickable { action() }
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(icon, contentDescription = title, tint = primary)
                            Spacer(Modifier.width(10.dp))
                            Text(
                                title,
                                fontSize = 17.sp,
                                fontWeight = FontWeight.Medium,
                                color = Color.Black
                            )
                        }
                        Icon(
                            Icons.Default.KeyboardArrowRight,
                            contentDescription = "Next",
                            tint = primary
                        )
                    }
                }
            }
        }

//        // ---------- Logout Button ----------
//        Button(
//            onClick = {
//                isLoggingOut = true
//                scope.launch {
//                    auth.signOut()
//                    delay(3000)
//                    navController.navigate(Screen.onboardingScreenOne.route) {
//                        popUpTo(0) { inclusive = true }
//                    }
//                    isLoggingOut = false
//                }
//            },
//            colors = ButtonDefaults.buttonColors(containerColor = primary),
//            shape = RoundedCornerShape(12.dp),
//            modifier = Modifier
//                .fillMaxWidth(0.8f)
//                .height(55.dp)
//        ) {
//            if (isLoggingOut) {
//                Row(verticalAlignment = Alignment.CenterVertically) {
//                    CircularProgressIndicator(
//                        modifier = Modifier.size(20.dp),
//                        color = Color.White,
//                        strokeWidth = 2.dp
//                    )
//                    Spacer(Modifier.width(8.dp))
//                    Text("Logging out...", color = Color.White, fontSize = 16.sp)
//                }
//            } else {
//                Text("Logout", color = Color.White, fontSize = 16.sp)
//            }
//        }

        // ---------- Calculator Dialog ----------
        if (showCalculatorDialog) {
            AlertDialog(
                onDismissRequest = { showCalculatorDialog = false },
                title = {
                    Text(
                        "Select Calculator",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    )
                },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                        CalculatorType.entries.forEach { type ->
                            Surface(
                                shape = RoundedCornerShape(12.dp),
                                color = pastelColors.random(),
                                border = BorderStroke(1.dp, primary),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(55.dp)
                                    .clickable {
                                        showCalculatorDialog = false
                                        navController.navigate(Screen.DoctorCalculatorScreen.passType(type.name))
                                    }
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.FitnessCenter,
                                        contentDescription = type.name,
                                        tint = primary
                                    )
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        type.name.replace("_", " "),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = Color.Black
                                    )
                                }
                            }
                        }
                    }
                },
                confirmButton = {
                    TextButton(onClick = { showCalculatorDialog = false }) {
                        Text("Close", color = primary)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(100.dp))
    }
}
