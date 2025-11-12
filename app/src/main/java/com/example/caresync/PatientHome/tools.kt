// File: ToolsScreen.kt
package com.example.caresync.PatientHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.example.caresync.Screen

@Composable
fun ToolsScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)

    val pastelColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = pastelColors,
                    startY = 0f,
                    endY = 1200f
                )
            )
            .padding(start = 12.dp, end = 12.dp, top = 110.dp, bottom = 70.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Health Education Section
        Text(
            text = "Health Education",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "Health Tips",
                icon = Icons.Default.Info,
                bgColor = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.HealthTipsScreen.route) }
            )
            ToolCard(
                title = "Health Articles",
                icon = Icons.Default.Article,
                bgColor = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f),
                onClick = { navController.navigate(Screen.HealthArticlesScreen.route) }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Health Calculations Section
        Text(
            text = "Health Calculations",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        // First Row - BMI and BMR
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "BMI",
                icon = Icons.Default.FitnessCenter,
                bgColor = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("${Screen.Calculatorscreen.route}/BMI")
                }
            )
            ToolCard(
                title = "BMR",
                icon = Icons.Default.Person,
                bgColor = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("${Screen.Calculatorscreen.route}/BMR")
                }
            )
        }

        // Second Row - Water Intake and Body Fat
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ToolCard(
                title = "Water Intake",
                icon = Icons.Default.LocalDrink,
                bgColor = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("${Screen.Calculatorscreen.route}/WATER")
                }
            )
            ToolCard(
                title = "Body Fat",
                icon = Icons.Default.FitnessCenter,
                bgColor = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f),
                onClick = {
                    navController.navigate("${Screen.Calculatorscreen.route}/BODY_FAT")
                }
            )
        }

        Divider(modifier = Modifier.padding(vertical = 8.dp))

        // Health Reminder Section
        Text(
            text = "Health Reminder",
            fontWeight = FontWeight.SemiBold,
            fontSize = 18.sp,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        ToolCard(
            title = "Upcoming Reminders",
            icon = Icons.Default.Alarm,
            bgColor = Color.White,
            primary = primary,
            modifier = Modifier.fillMaxWidth(),
            onClick = { /* TODO */ }
        )
        Button(
            onClick = { /* Add New Reminder functionality */ },
            colors = ButtonDefaults.buttonColors(containerColor = primary),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("Add New Reminder", color = white)
        }

        // Empty space at bottom for scrolling
        Spacer(Modifier.height(100.dp))
    }
}

@Composable
fun ToolCard(
    title: String,
    icon: ImageVector,
    bgColor: Color,
    primary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bgColor,
        border = BorderStroke(1.dp, primary),
        modifier = modifier
            .height(120.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(icon, contentDescription = title, tint = primary, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(8.dp))
            Text(
                text = title,
                textAlign = TextAlign.Center,
                fontSize = 14.sp,
                fontWeight = FontWeight.Medium
            )
        }
    }
}