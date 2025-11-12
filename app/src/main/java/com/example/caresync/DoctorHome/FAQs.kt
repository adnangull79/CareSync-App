// File: DoctorFAQScreen.kt
package com.example.caresync.DoctorHome

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
fun DoctorFAQScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val faqList = listOf(
        "How can I view my patient appointments?" to "Go to the 'Appointments' section from your dashboard. You'll find a detailed list of all upcoming and past appointments along with patient details.",
        "Can I update my availability schedule?" to "Yes, you can manage your available time slots in the 'Settings' section under 'Doctor Availability'.",
        "Is patient data stored securely?" to "Absolutely. CareSync uses Firebase with encrypted data storage to ensure all patient and doctor data remains confidential and secure.",
        "How can I reset my password?" to "In the 'Settings' section, tap on 'Change Password' and follow the steps to securely update your credentials.",
        "How do I contact CareSync support?" to "You can contact our support team by going to 'Settings' → 'Report a Problem'. You’ll find email support options there.",
        "Can I add personal notes for each patient?" to
                "Yes, in the 'Notes' section, you can add, edit, or delete notes linked to each patient for better record keeping.",
        "Does CareSync send appointment reminders to patients?" to
                "Yes, automated reminders are sent to patients before their scheduled appointments to minimize no-shows.",
        "How can I update my profile information?" to
                "Open the 'Profile' tab and tap on 'Edit Profile' to update your name, specialization, or profile picture.",
        "Can I access CareSync on multiple devices?" to
                "Yes, simply log in using the same account credentials on any device to access your synced data securely.",
        "What should I do if the app isn’t loading properly?" to
                "Try checking your internet connection first. If the issue persists, clear the app cache or contact CareSync support from the 'Report a Problem' section."
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Frequently Asked Questions",
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
                text = "Find answers to the most common questions about using CareSync as a doctor.",
                fontSize = 15.sp,
                color = Color.DarkGray,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            faqList.forEach { (question, answer) ->
                FAQCard(question, answer, primary)
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
fun FAQCard(question: String, answer: String, tint: Color) {
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
