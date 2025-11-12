package com.example.caresync

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight

@Composable
fun regScreen(navController: NavController) {
    // Accessing colors from colors.xml
    val primaryBlue = colorResource(id = R.color.primary_button_color) // Blue color for main button
    val whiteColor = colorResource(id = R.color.white) // White color for backgrounds and secondary button

    // Main layout
    Column(modifier = Modifier.fillMaxSize()) {
        // First 60% with blue background
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(primaryBlue),
            contentAlignment = Alignment.Center
        ) {
            Column(modifier = Modifier,
                horizontalAlignment = Alignment.CenterHorizontally) {
                Image(
                    painter = painterResource(id = R.drawable.whitelogo),
                    modifier = Modifier.aspectRatio(1f).size(100.dp),
                    contentDescription = "App Logo"
                )
                Text("Your Ultimate Health Care Assistant", color =whiteColor,
                    fontWeight = FontWeight.Bold)
            }


        }

        // Below section (remaining 40%)
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.4f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Sign up button (secondary)

            Button(
                onClick = { navController.navigate(Screen.RegisterScreen2.route)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .border(2.dp, primaryBlue, RoundedCornerShape(8.dp))
                    .heightIn(min = 48.dp), // Add border here
                colors = ButtonDefaults.buttonColors(containerColor = whiteColor),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(text = "Sign Up", color = primaryBlue)
            }

            // Log in button (main, blue)
            Button(
                onClick = {navController.navigate(Screen.loginScreen.route)},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp)
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(corner = CornerSize(8.dp))
            ) {
                Text(text = "Log In", color = whiteColor)
            }
        }
    }
}

