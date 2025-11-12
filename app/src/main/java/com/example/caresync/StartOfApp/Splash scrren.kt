package com.example.caresync.StartOfApp

import com.example.caresync.Screen
import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay

@Composable
fun splashscrren(navController: NavController) {
    val scale = remember { androidx.compose.animation.core.Animatable(0f) }

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 0.7f,
            animationSpec = tween(
                durationMillis = 1200,        // ← increase speed by duration
                delayMillis = 100,
                easing = {
                    OvershootInterpolator(1.5f).getInterpolation(it) // slightly softer bounce
                }
            )
        )

        delay(100)
        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val db = FirebaseFirestore.getInstance()
            val uid = currentUser.uid
            db.collection("users").document(uid).get()
                .addOnSuccessListener { document ->
                    val role = document.getString("role")
                    if (role == "doctor") {
                        navController.navigate(Screen.doctorhome.route) {
                            popUpTo("splashscrren") { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.userhome.route) {
                            popUpTo("splashscrren") { inclusive = true }
                        }
                    }
                }
                .addOnFailureListener {
                    navController.navigate(Screen.userhome.route) {
                        popUpTo("splashscrren") { inclusive = true }
                    }
                }
        } else {
            navController.navigate(Screen.onboardingScreenOne.route) {
                popUpTo("splashscrren") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.LightGray),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(R.drawable.logo),
                contentDescription = null,
                modifier = Modifier.scale(scale.value)
            )

            Spacer(modifier = Modifier.height(6.dp))


            // Typewriter tagline
            TypeWriterText("The Ultimate Health Care APP")//chage it latter
        }
    }
}

@Composable
fun TypeWriterText(text: String, delayMillis: Long = 35L) {
    var displayText by remember { mutableStateOf("") }

    LaunchedEffect(text) {
        displayText = ""
        delay(100)
        for (i in text.indices) {
            displayText = text.substring(0, i + 1)
            delay(delayMillis)
        }
    }

    Text(
        text = displayText,
        fontSize = 18.sp,
        fontWeight = FontWeight.Medium,
        textAlign = TextAlign.Center,
        color = Color.Black
    )
}
