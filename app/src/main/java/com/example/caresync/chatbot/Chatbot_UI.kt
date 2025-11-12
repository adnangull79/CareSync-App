// File: ChatScreen.kt
package com.example.caresync.chatbot

import android.widget.Toast
import androidx.compose.animation.core.*
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.ThumbDown
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material.icons.outlined.ContentCopy
import androidx.compose.material.icons.outlined.ThumbDown
import androidx.compose.material.icons.outlined.ThumbUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.*
import androidx.compose.ui.platform.ClipboardManager
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.caresync.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(viewModel: ChatViewModel = viewModel(), navController: NavController) {
    val messages by viewModel.messages.collectAsState()
    var userInput by remember { mutableStateOf(TextFieldValue("")) }

    val primary = colorResource(id = R.color.primary_button_color)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    // ✅ Dismissible disclaimer state
    var showDisclaimer by remember { mutableStateOf(true) }

    // ✅ Health topic suggestions
    val healthTopics = listOf(
        // English (simple & common)
        "What are symptoms of diabetes?",
        "How to lower high blood pressure?",
        "Best foods for heart health",
        "How to improve sleep quality?",

        // Roman Urdu (very common in Pakistan)
        "Bukhar bar bar kyun hota hai?",
        "Seene me jalan ho to kya karein?",
        "Sir dard bohat hota hai, kya wajah ho sakti hai?",
        "Kamzori aur thakaan kaisay door ho?"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Health Assistant",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = primary)
            )
        },
        modifier = Modifier
            .imePadding()
            .navigationBarsPadding()
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
                .padding(padding)
        ) {
            // ✅ Medical Disclaimer Banner (Dismissible)
            if (showDisclaimer) {
                Surface(
                    color = Color(0xFFFFF3E0),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.dp, Color(0xFFFF9800))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                "⚕️ Medical Disclaimer",
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp,
                                color = Color(0xFFE65100)
                            )
                            Text(
                                "This AI provides general health information only. Not a substitute for professional medical advice. Consult a doctor for diagnosis and treatment.",
                                fontSize = 11.sp,
                                color = Color(0xFF6D4C41),
                                lineHeight = 14.sp
                            )
                        }

                        // ✅ Close button
                        IconButton(
                            onClick = { showDisclaimer = false },
                            modifier = Modifier.size(24.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close disclaimer",
                                tint = Color(0xFF6D4C41),
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Chat area
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
            ) {
                if (messages.isEmpty()) {
                    // ✅ Welcome message with health topics
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Spacer(Modifier.height(20.dp))

                        Text(
                            "👋 Welcome to CareSync Health AI",
                            fontWeight = FontWeight.Bold,
                            fontSize = 20.sp,
                            color = primary,
                            textAlign = TextAlign.Center
                        )

                        Text(
                            "Ask me about:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 16.sp,
                            color = Color.Black
                        )

                        // ✅ Health topic chips
                        Column(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            listOf(
                                "🤒 Symptoms & Common Problems",
                                "💊 Medicines & Side Effects",
                                "🥗 Diet & Healthy Eating",
                                "🏃 Exercise & Fitness Tips",
                                "😴 Sleep & Relaxation",
                                "🧠 Mental Health & Stress Care",
                                "🩹 Injury & First Aid Help"
                            ).forEach { topic ->
                                Surface(
                                    color = Color.White,
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, primary),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = topic,
                                        modifier = Modifier.padding(12.dp),
                                        fontSize = 14.sp,
                                        color = Color.Black
                                    )
                                }
                            }
                        }

                        Spacer(Modifier.height(8.dp))

                        Text(
                            "Try these questions:",
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp,
                            color = Color.Black
                        )

                        // ✅ Quick question suggestions
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(healthTopics.take(4)) { topic ->
                                AssistChip(
                                    onClick = {
                                        userInput = TextFieldValue(topic)
                                    },
                                    label = {
                                        Text(
                                            topic,
                                            fontSize = 12.sp,
                                            maxLines = 2
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = primary.copy(alpha = 0.1f),
                                        labelColor = primary
                                    ),
                                    border = BorderStroke(1.dp, primary),
                                    modifier = Modifier.width(180.dp)
                                )
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(vertical = 4.dp),
                        reverseLayout = true
                    ) {
                        items(messages.reversed()) { (text, isUser) ->
                            ChatBubble(message = text, isUser = isUser, primary = primary)
                        }
                    }
                }
            }

            // Loading indicator
            val isLoading by viewModel.isLoading.collectAsState()
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 10.dp),
                    contentAlignment = Alignment.Center
                ) {
                    LoadingDots(primary)
                }
            }

            // Input bar
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
            ) {
                OutlinedTextField(
                    value = userInput,
                    onValueChange = { value -> userInput = value },
                    placeholder = { Text("Ask health question...", color = Color.Gray) },
                    modifier = Modifier
                        .weight(1f)
                        .defaultMinSize(minHeight = 56.dp)
                        .heightIn(min = 56.dp, max = 160.dp),
                    shape = RoundedCornerShape(20.dp),
                    maxLines = 5,
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primary,
                        unfocusedBorderColor = primary,
                        cursorColor = primary,
                        containerColor = Color.White,
                        focusedTextColor = Color.Black,
                        unfocusedTextColor = Color.Black
                    ),
                    textStyle = LocalTextStyle.current.copy(fontSize = 15.sp)
                )

                Spacer(modifier = Modifier.width(6.dp))

                IconButton(
                    onClick = {
                        if (userInput.text.isNotBlank()) {
                            viewModel.sendMessageToGemini(userInput.text)
                            userInput = TextFieldValue("")
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .background(primary, shape = CircleShape)
                ) {
                    Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.White)
                }
            }
        }
    }
}

@Composable
fun ChatBubble(message: String, isUser: Boolean, primary: Color) {
    val clipboard: ClipboardManager = LocalClipboardManager.current
    val context = LocalContext.current

    val backgroundColor = if (isUser) primary else Color.White
    val borderColor = if (isUser) Color.White else primary
    val textColor = if (isUser) Color.White else Color.Black
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    val formattedMessage = formatMessage(message)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = alignment
    ) {

        Column(
            modifier = Modifier.widthIn(max = 330.dp)
        ) {

            // Bubble (Text)
            Surface(
                color = backgroundColor,
                shape = RoundedCornerShape(18.dp),
                border = BorderStroke(1.dp, borderColor),
                tonalElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                FormattedText(
                    text = formattedMessage,
                    color = textColor,
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                    fontSize = 15.sp
                )
            }

            Spacer(modifier = Modifier.height(2.dp))

            // Copy button (small & right aligned)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(end = 4.dp),
                horizontalArrangement = Arrangement.End
            ) {
                IconButton(
                    onClick = {
                        clipboard.setText(AnnotatedString(message))
                        Toast.makeText(context, "Copied to clipboard", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.size(20.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.ContentCopy,
                        contentDescription = "Copy",
                        tint = Color.Gray,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}


// ✅ Helper function to format message
fun formatMessage(message: String): String {
    return message
        .replace("**", "") // Remove markdown bold markers
        .replace("*", "•")  // Convert asterisks to bullets
        .replace("###", "")  // Remove heading markers
        .replace("##", "")
        .replace("#", "")
        .trim()
}

// ✅ Composable for formatted text with bold support
@Composable
fun FormattedText(
    text: String,
    color: Color,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 15.sp
) {
    // Parse text for bold formatting (between ** markers in original)
    val parts = text.split("•")

    Column(modifier = modifier) {
        parts.forEachIndexed { index, part ->
            if (part.isNotBlank()) {
                // Check if this part should be bold (contains keywords)
                val isBold = part.trim().length < 50 &&
                        (part.contains(":") ||
                                part.trim().startsWith("⚠️") ||
                                part.trim().startsWith("🚨") ||
                                index == 0)

                Row(
                    modifier = Modifier.padding(vertical = 2.dp),
                    verticalAlignment = Alignment.Top
                ) {
                    if (index > 0) {
                        Text(
                            text = "• ",
                            color = color,
                            fontSize = fontSize,
                            fontWeight = FontWeight.Normal
                        )
                    }
                    Text(
                        text = part.trim(),
                        color = color,
                        fontSize = fontSize,
                        fontWeight = if (isBold) FontWeight.Bold else FontWeight.Normal,
                        lineHeight = (fontSize.value * 1.4).sp
                    )
                }
            }
        }
    }
}

@Composable
fun LoadingDots(color: Color) {
    val transition = rememberInfiniteTransition()
    val scale1 by transition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(animation = tween(400), repeatMode = RepeatMode.Reverse)
    )
    val scale2 by transition.animateFloat(
        initialValue = 1.0f, targetValue = 0.8f,
        animationSpec = infiniteRepeatable(animation = tween(400), repeatMode = RepeatMode.Reverse)
    )
    val scale3 by transition.animateFloat(
        initialValue = 0.8f, targetValue = 1.3f,
        animationSpec = infiniteRepeatable(animation = tween(400), repeatMode = RepeatMode.Reverse)
    )

    Row(
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .wrapContentWidth()
            .padding(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(9.dp)
                .scale(scale1)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(9.dp)
                .scale(scale2)
                .background(color, shape = CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Box(
            modifier = Modifier
                .size(9.dp)
                .scale(scale3)
                .background(color, shape = CircleShape)
        )
    }
}