package com.example.caresync

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController

@Composable
fun OnboardingScreentwo(navController: NavController) {
    val primaryBlue = colorResource(id = R.color.primary_button_color)
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Skip
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Text(
                text = "Skip",
                fontSize = 16.sp,
                color =primaryBlue,
                modifier = Modifier
                    .padding(8.dp)
                    .clickable {
                        navController.navigate(Screen.RegisterScreen.route)
                    }
            )
        }

        // Image
        Image(
            painter = painterResource(id = R.drawable.ic_doc), // dummy drawable
            contentDescription = null,
            modifier = Modifier
                .size(220.dp).aspectRatio(1f)
                .padding(top = 20.dp)
        )

        // Texts
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 16.dp)
        ) {
            Text(
                text = "CareSync",
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold,
                color =primaryBlue
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Store and access your medical records anytime, anywhere. \n Stay organized with CareSync.",
                fontSize = 16.sp,
                color = Color.Black,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }

        // Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Button(
                onClick = { navController.navigate(Screen.onboardingScreenOne.route) }, // previous screen
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(50)
            ) {
                Text("Back", color = Color.White)
            }

            Button(
                onClick = { navController.navigate(Screen.onboardingScreenthree.route) }, // next screen
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(50)
            ) {
                Text("Next", color = Color.White)
            }
        }
    }
}
