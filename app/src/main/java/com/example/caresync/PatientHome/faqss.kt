package com.example.caresync.PatientHome


import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientFAQScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val faqList = listOf(
        "How can I book an appointment with a doctor?" to
                "From your dashboard, tap on 'Book Appointment'. Choose your preferred doctor, date, and time slot, then confirm your booking.",
        "Can I view my past appointments?" to
                "Yes, go to the 'Appointments' section in your dashboard. You can view details of upcoming and past appointments easily.",
        "Is my medical data secure?" to
                "Absolutely. CareSync uses Firebase with encrypted data storage to ensure your personal and health information remains safe and private.",
        "How can I reset my password?" to
                "Tap 'Forgot Password?' on the login screen or go to 'Settings' → 'Change Password'. Follow the steps to receive a reset link in your email. Check your spam folder if you don’t see it.",
        "How do I contact CareSync support?" to
                "Go to 'Settings' → 'Help & Support'. You’ll find both email and chat options to reach our support team.",
        "Can I update my personal details?" to
                "Yes. Open the 'Profile' tab and tap 'Edit Profile' to update your name, profile picture, or contact information.",
        "Can I cancel or reschedule an appointment?" to
                "Yes. In the 'Appointments' section, select your booking and choose 'Cancel' or 'Reschedule'. Please note: changes are allowed up to 24 hours before the appointment.",
        "Does CareSync send reminders for appointments?" to
                "Yes, automated reminders are sent to your registered email and app notifications before your scheduled appointments.",
        "Can I access CareSync from multiple devices?" to
                "Yes, just log in using your registered email and password on any device. Your account and data will stay in sync.",
        "What should I do if the app is not working properly?" to
                "First, check your internet connection. If the issue continues, try clearing app cache or reinstalling CareSync. For further help, contact our support team."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Patient FAQs",
                        fontWeight = FontWeight.Bold,
                        fontSize = 19.sp,
                        color = Color.White
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
    ) { paddingValues ->
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
                .padding(paddingValues)
                .padding(20.dp)
        ) {
            Text(
                text = "Find answers to the most common questions about using CareSync as a patient.",
                fontSize = 15.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            faqList.forEach { (question, answer) ->
                PatientFAQCard(question, answer, primary)
                Spacer(modifier = Modifier.height(12.dp))
            }

            Spacer(modifier = Modifier.height(30.dp))
            Text(
                text = "CareSync © 2025",
                fontSize = 13.sp,
                color = Color.Gray,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }
    }
}

@Composable
fun PatientFAQCard(question: String, answer: String, tint: Color) {
    var expanded by remember { mutableStateOf(false) }
    val rotation by animateFloatAsState(targetValue = if (expanded) 180f else 0f)

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { expanded = !expanded }
            .animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.94f),
        border = BorderStroke(1.dp, tint),
        shadowElevation = 6.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = question,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 16.sp,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = Icons.Default.ArrowDropDown,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.rotate(rotation)
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(8.dp))
                Divider(color = tint.copy(alpha = 0.3f), thickness = 1.dp)
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = answer,
                    fontSize = 14.sp,
                    color = Color.DarkGray,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
