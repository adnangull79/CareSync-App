package com.example.caresync.DoctorHome



import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
fun ContactScreen(navController: NavController) {
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
                        "Contact Us",
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
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Header Logo ---
            Image(
                painter = painterResource(id = R.drawable.logo), // replace with your logo
                contentDescription = "CareSync Logo",
                contentScale = ContentScale.Fit,
                modifier = Modifier
                    .size(120.dp)
                    .clip(RoundedCornerShape(16.dp))
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "We’d love to hear from you!",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = primary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Whether you have questions, feedback, or partnership opportunities — we’re just a message away.",
                fontSize = 15.sp,
                color = Color.Black.copy(alpha = 0.8f),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Contact Options ---
            ContactCard(
                icon = Icons.Default.Email,
                title = "Email Us",
                info = "caresync.support@gmail.com",
                primary = primary
            )

            ContactCard(
                icon = Icons.Default.Phone,
                title = "Call Us",
                info = "+92 312 1234567",
                primary = primary
            )

            ContactCard(
                icon = Icons.Default.Language,
                title = "Visit Our Website",
                info = "www.caresyncapp.com",
                primary = primary
            )

            ContactCard(
                icon = Icons.Default.LocationOn,
                title = "Office Address",
                info = "Muzaffarabad, Azad Kashmir, Pakistan",
                primary = primary
            )

            Spacer(modifier = Modifier.height(24.dp))

            // --- Support Section ---
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = Color.White.copy(alpha = 0.9f),
                shadowElevation = 4.dp
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "Need Technical Support?",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = primary
                    )
                    Text(
                        text = "If you face any issues while using CareSync, please reach out to our support team. We’ll respond within 24 hours.",
                        fontSize = 15.sp,
                        color = Color.Black.copy(alpha = 0.8f),
                        textAlign = TextAlign.Justify
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            Text(
                text = "CareSync © 2025 — All rights reserved.",
                fontSize = 13.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(50.dp))
        }
    }
}

@Composable
fun ContactCard(icon: androidx.compose.ui.graphics.vector.ImageVector, title: String, info: String, primary: Color) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.9f),
        shadowElevation = 3.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = title,
                tint = primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = Color.Black
                )
                Text(
                    text = info,
                    fontSize = 14.sp,
                    color = Color.DarkGray
                )
            }
        }
    }
}
