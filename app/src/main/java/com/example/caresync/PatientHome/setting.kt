package com.example.caresync.PatientHome

// File: PatientSettingsScreen.kt

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientSettingsScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showLogoutDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var isLoggingOut by remember { mutableStateOf(false) }
    var isDeleting by remember { mutableStateOf(false) }

    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var showReportProblemDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
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
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.logo),
                contentDescription = "CareSync Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(100.dp)
                    .clip(RoundedCornerShape(14.dp))
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Account & App Settings",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = primary
            )

            Spacer(modifier = Modifier.height(18.dp))

            SettingItem("Change Password", "Send password reset link", Icons.Default.Lock, primary) {
                showChangePasswordDialog = true
            }

            SettingItem("Report a Problem", "Send issue details via email", Icons.Default.BugReport, primary) {
                showReportProblemDialog = true
            }

            SettingItem("FAQs", "Frequently asked questions", Icons.Default.Help, primary) {
                navController.navigate(Screen.PatientFAQScreen.route)
            }

            SettingItem("Privacy Policy & Terms", "View our privacy policy and terms", Icons.Default.PrivacyTip, primary) {
                showPrivacyDialog = true
            }

            Spacer(modifier = Modifier.height(12.dp))

            SettingItem("Logout", "Sign out from this device", Icons.Default.Logout, primary) {
                showLogoutDialog = true
            }

            SettingItem(
                "Delete Account",
                "Permanently remove account and all data",
                Icons.Default.DeleteForever,
                colorResource(id = R.color.pastel_orange)
            ) {
                showDeleteDialog = true
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text("CareSync © 2025", fontSize = 13.sp, color = Color.Gray)
        }
    }

    // ---------------- Change Password Dialog ----------------
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = { showChangePasswordDialog = false },
            title = { Text("Change Password") },
            text = { Text("Click 'Send Email' to receive a password reset link." +
                    " If you don't see it, please check your Spam/Junk folder.") },
            confirmButton = {
                TextButton(onClick = {
                    val email = auth.currentUser?.email
                    if (email.isNullOrBlank()) {
                        scope.launch { snackbarHostState.showSnackbar("No email found.") }
                    } else {
                        auth.sendPasswordResetEmail(email)
                            .addOnCompleteListener { task ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (task.isSuccessful) "Reset link sent to $email. " +
                                                "If you don't see it, please check your Spam/Junk folder."
                                        else "Failed to send link."
                                    )
                                }
                            }
                    }
                    showChangePasswordDialog = false
                }) {
                    Text("Send Email", color = primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showChangePasswordDialog = false }) {
                    Text("Cancel", color = primary)
                }
            }
        )
    }

    // ---------------- Report Problem Dialog ----------------
    if (showReportProblemDialog) {
        AlertDialog(
            onDismissRequest = { showReportProblemDialog = false },
            title = { Text("Report a Problem") },
            text = { Text("Tap 'Send Email' to open your email app and report directly.") },
            confirmButton = {
                TextButton(onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:caresync.support@gmail.com")
                        putExtra(Intent.EXTRA_SUBJECT, "CareSync App - Issue Report")
                    }
                    context.startActivity(intent)
                    showReportProblemDialog = false
                }) {
                    Text("Send Email", color = primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showReportProblemDialog = false }) {
                    Text("Cancel", color = primary)
                }
            }
        )
    }

    // ---------------- Privacy Policy Dialog ----------------
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("Privacy Policy & Terms") },
            text = {
                Column {
                    Text(
                        "CareSync respects your privacy and securely stores your data in Firebase."
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "By using CareSync, you agree to our terms of service and privacy practices.",
                        fontWeight = FontWeight.SemiBold
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Close", color = primary)
                }
            }
        )
    }

    // ---------------- Logout Dialog ----------------
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { if (!isLoggingOut) showLogoutDialog = false },
            title = { Text("Logout") },
            text = { Text("Are you sure you want to log out?") },
            confirmButton = {
                TextButton(onClick = {
                    isLoggingOut = true
                    showLogoutDialog = false
                    scope.launch {
                        FirebaseAuth.getInstance().signOut()
                        delay(2000)
                        navController.navigate(Screen.onboardingScreenOne.route) {
                            popUpTo(0) { inclusive = true }
                        }
                        isLoggingOut = false
                    }
                }) {
                    if (isLoggingOut) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Red, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Logging out...", color = Color.Red)
                        }
                    } else Text("Logout", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isLoggingOut) showLogoutDialog = false }) {
                    Text("Cancel", color = primary)
                }
            }
        )
    }

    // ---------------- Delete Account Dialog ----------------
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { if (!isDeleting) showDeleteDialog = false },
            title = { Text("Delete Account") },
            text = { Text("This will permanently delete your account and all related data from Firestore.") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    isDeleting = true
                    scope.launch {
                        val user = FirebaseAuth.getInstance().currentUser
                        val uid = user?.uid
                        if (uid == null) {
                            snackbarHostState.showSnackbar("No authenticated user found.")
                            isDeleting = false
                            return@launch
                        }

                        // Delete all Firestore data for this patient
                        deletePatientData(db, uid)

                        kotlin.runCatching { user.delete().awaitTask() }

                        FirebaseAuth.getInstance().signOut()
                        snackbarHostState.showSnackbar("Account and data deleted successfully.")
                        navController.navigate(Screen.onboardingScreenOne.route) {
                            popUpTo(0) { inclusive = true }
                        }

                        isDeleting = false
                    }
                }) {
                    if (isDeleting) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CircularProgressIndicator(modifier = Modifier.size(18.dp), color = Color.Red, strokeWidth = 2.dp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Deleting...", color = Color.Red)
                        }
                    } else Text("Delete", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { if (!isDeleting) showDeleteDialog = false }) {
                    Text("Cancel", color = primary)
                }
            }
        )
    }
}

// ---- Delete patient data from Firestore ----
suspend fun deletePatientData(db: FirebaseFirestore, uid: String) {
    // delete patient calculators collection
    kotlin.runCatching {
        db.collection("users").document(uid).collection("calculators").get().awaitTask().documents.forEach {
            it.reference.delete()
        }
    }

    // delete patient documents collection
    kotlin.runCatching {
        db.collection("users").document(uid).collection("documents").get().awaitTask().documents.forEach {
            it.reference.delete()
        }
    }

    // delete patient appointments collection
    kotlin.runCatching {
        db.collection("users").document(uid).collection("appointments").get().awaitTask().documents.forEach {
            it.reference.delete()
        }
    }

    // delete from users collection
    kotlin.runCatching { db.collection("users").document(uid).delete().awaitTask() }
}

// Helper: Await Firebase Task
suspend fun <T> Task<T>.awaitTask(): T {
    return suspendCancellableCoroutine { cont ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) cont.resume(task.result)
            else cont.resumeWithException(task.exception ?: Exception("Unknown error"))
        }
    }
}

@Composable
fun SettingItem(
    title: String,
    subtitle: String = "",
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(14.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, tint),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = title, tint = tint, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = Color.Black)
                if (subtitle.isNotBlank()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(subtitle, fontSize = 13.sp, color = Color.DarkGray)
                }
            }
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Go", tint = tint)
        }
    }
}