package com.example.caresync.PatientHome

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import androidx.compose.ui.res.colorResource
import com.example.caresync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageViewScreen(
    imageUrl: String,
    imageTitle: String,
    onBackClick: () -> Unit
) {
    var scale by remember { mutableStateOf(1f) }

    // Fetch the primary color from your theme
    val primaryColor = colorResource(id = R.color.primary_button_color)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(imageTitle) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primaryColor,  // Use primary color here
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
                .padding(paddingValues)
                .pointerInput(Unit) {
                    detectTransformGestures { _, _, zoom, _ ->
                        scale = (scale * zoom).coerceIn(1f, 5f)
                    }
                },
            contentAlignment = Alignment.Center
        ) {
            SubcomposeAsyncImage(
                model = imageUrl,
                contentDescription = imageTitle,
                modifier = Modifier
                    .fillMaxSize()
                    .scale(scale),
                contentScale = ContentScale.Fit,
                loading = {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = primaryColor, // Use primary color for loading spinner
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                },
                error = {
                    Text(
                        text = "Failed to load image",
                        color = Color.White
                    )
                }
            )
        }
    }

    // Handle back press (Android back button)
    BackHandler {
        onBackClick()
    }
}
