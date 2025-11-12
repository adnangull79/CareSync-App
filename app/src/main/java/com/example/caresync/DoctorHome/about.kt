package com.example.caresync.DoctorHome


import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.caresync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "About CareSync",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
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
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // --- App Logo ---
            Image(
                painter = painterResource(id = R.drawable.logo), // replace with your app logo
                contentDescription = "CareSync Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            // --- App Name ---
            Text(
                text = "CareSync",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your Smart Healthcare Companion",
                fontSize = 16.sp,
                color = Color.DarkGray
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- About Section ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "About the App",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                    Text(
                        text = "CareSync is a modern healthcare app built to simplify the connection between patients, doctors, and labs. It enables users to book doctor appointments, request lab tests, chat with a health bot, and securely store health documents — all in one place.",
                        fontSize = 15.sp,
                        color = Color.Black.copy(alpha = 0.8f),
                        textAlign = TextAlign.Justify
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    Text(
                        text = "Core Features",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                    Text(
                        text = "• Doctor appointment booking\n" +
                                "• AI health chatbot for instant answers\n" +
                                "• Secure document storage\n" +
                                "• Health Calculators\n" +
                                "• Health education and reminders",
                        fontSize = 15.sp,
                        color = Color.Black.copy(alpha = 0.8f)
                    )

                    Divider(color = Color.Gray.copy(alpha = 0.3f))

                    Text(
                        text = "Developed By",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                    Text(
                        text = "\bAdnan Gull | Moin Awan | Muhammad Ali\b \nBSCS – University of Azad Jammu & Kashmir\nFinal Year Project 2025",
                        fontSize = 15.sp,
                        color = Color.Black.copy(alpha = 0.8f)
                    )

                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Version 1.0.0",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Thank You Section ---
            Text(
                text = "Thank you for using CareSync.\nEmpowering smarter healthcare decisions.",
                fontSize = 15.sp,
                color = Color.Black.copy(alpha = 0.7f),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 10.dp)
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }
}
