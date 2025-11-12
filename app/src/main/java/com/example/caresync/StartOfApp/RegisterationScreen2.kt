package com.example.caresync.StartOfApp


import com.example.caresync.Screen
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.example.caresync.R

@Composable
fun regScreen2(navController: NavController) {
    val primaryBlue = colorResource(id = R.color.primary_button_color)
    val whiteColor = colorResource(id = R.color.white)
    val blueOutline = colorResource(id = R.color.border_background_color)

    Column(modifier = Modifier.fillMaxSize()) {
        // Logo (same as regScreen)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.6f)
                .background(primaryBlue)
        ) {
            Column(modifier = Modifier.padding(top = 10.dp)){
                // Back Button
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier // top-right corner
                        .padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack, // your back icon drawable
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                // Center Column
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.whitelogo),
                        modifier = Modifier.aspectRatio(1f).size(100.dp),
                        contentDescription = "App Logo"
                    )

                    Text(
                        text = stringResource(id = R.string.Tagline),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }


    }}
        // Buttons Section
        Column(
            modifier = Modifier
                .weight(0.4f)
                .fillMaxWidth()
                .padding(15.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Health Seeker Button (Blue)
            Button(
                onClick = { navController.navigate(Screen.signupScreen.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .heightIn(min = 55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(10.dp)
            ) {
                Text("Sign Up as Health Seeker", fontSize = 18.sp, color = whiteColor, fontWeight = FontWeight.Bold)
            }

            // Doctor Button (White with Blue Border)
            OutlinedButton(
                onClick = { navController.navigate(Screen.doctorsignup.route) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 12.dp)
                    .heightIn(min = 55.dp),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = whiteColor),
                shape = RoundedCornerShape(10.dp),
                border = ButtonDefaults.outlinedButtonBorder.copy(width = 2.dp, brush = androidx.compose.ui.graphics.SolidColor(primaryBlue))
            ) {
                Text("Sign Up as Doctor", fontSize = 18.sp, color = primaryBlue, fontWeight = FontWeight.Bold)
            }

            // Login Button (Optional, Small Text Button)
            TextButton(
                onClick = { navController.navigate(Screen.loginScreen.route) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Already have an account? Login", color = primaryBlue)
            }
        }
    }
}
