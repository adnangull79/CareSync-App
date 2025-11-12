package com.example.caresync.PatientHome

// File: HealthTipsScreen.kt

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R

data class HealthTipDetail(
    val icon: Int,
    val title: String,
    val description: String,
    val learnMoreUrl: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthTipsScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)

    val pastelColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val context = LocalContext.current
    var selectedTip by remember { mutableStateOf<HealthTipDetail?>(null) }
    var showTipDialog by remember { mutableStateOf(false) }

    // 🏃 Physical Health Tips (6 tips)
    val physicalHealthTips = listOf(
        HealthTipDetail(
            icon = R.drawable.ic_workout,
            title = "Exercise Daily",
            description = "Regular physical activity strengthens your heart, improves cardiovascular health, helps maintain a healthy weight, and boosts mental health. Exercise releases endorphins that reduce stress and anxiety, improves sleep quality, and increases energy levels throughout the day.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+daily+exercise"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_morningwalik,
            title = "Morning Walk",
            description = "Morning walks improve circulation, boost metabolism, enhance mood, and provide fresh air and sunlight for vitamin D. Walking in the morning helps regulate your sleep cycle, increases mental clarity, and sets a positive tone for the rest of your day.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+morning+walk"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Stay Hydrated",
            description = "Drinking adequate water throughout the day helps maintain body temperature, transport nutrients, remove waste, cushion joints, and protect sensitive tissues. Aim for 8-10 glasses daily for optimal health and energy levels.",
            learnMoreUrl = "https://www.google.com/search?q=importance+of+staying+hydrated"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_workout,
            title = "Strength Training",
            description = "Building muscle through resistance exercises increases metabolism, improves bone density, enhances posture, and reduces injury risk. Include strength training 2-3 times per week for comprehensive fitness.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+strength+training"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_morningwalik,
            title = "Get Enough Sleep",
            description = "Quality sleep of 7-9 hours per night is essential for physical recovery, immune function, hormone regulation, and cognitive performance. Establish a consistent sleep schedule for better health outcomes.",
            learnMoreUrl = "https://www.google.com/search?q=importance+of+sleep+for+health"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_workout,
            title = "Stretch Regularly",
            description = "Daily stretching improves flexibility, increases blood flow to muscles, reduces tension, prevents injuries, and enhances athletic performance. Spend 10-15 minutes stretching major muscle groups each day.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+daily+stretching"
        )
    )

    // 🧠 Mental Health Tips (6 tips)
    val mentalHealthTips = listOf(
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Practice Meditation",
            description = "Regular meditation reduces stress, improves focus, enhances emotional health, and promotes self-awareness. Start with just 5-10 minutes daily to experience significant mental clarity and calmness.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+meditation"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_workout,
            title = "Maintain Social Connections",
            description = "Strong social relationships reduce stress, boost happiness, improve self-esteem, and even increase longevity. Regularly connect with friends and family to support your mental well-being.",
            learnMoreUrl = "https://www.google.com/search?q=importance+of+social+connections"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_morningwalik,
            title = "Take Breaks",
            description = "Regular breaks during work or study improve productivity, reduce mental fatigue, enhance creativity, and prevent burnout. Follow the 20-20-20 rule: every 20 minutes, look 20 feet away for 20 seconds.",
            learnMoreUrl = "https://www.google.com/search?q=importance+of+taking+breaks"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Practice Gratitude",
            description = "Daily gratitude practice improves mood, increases resilience, enhances relationships, and promotes better sleep. Write down three things you're grateful for each day.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+gratitude+practice"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_workout,
            title = "Limit Screen Time",
            description = "Reducing screen time, especially before bed, improves sleep quality, reduces eye strain, enhances focus, and promotes better mental health. Set boundaries for device usage throughout the day.",
            learnMoreUrl = "https://www.google.com/search?q=effects+of+too+much+screen+time"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_morningwalik,
            title = "Seek Help When Needed",
            description = "Talking to a mental health professional is a sign of strength, not weakness. Therapy provides tools to manage stress, anxiety, depression, and improve overall emotional well-being.",
            learnMoreUrl = "https://www.google.com/search?q=importance+of+mental+health+therapy"
        )
    )

    // 🥗 Gut Health Tips (6 tips)
    val gutHealthTips = listOf(
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Eat More Fiber",
            description = "High-fiber foods support healthy digestion, feed beneficial gut bacteria, prevent constipation, and may reduce disease risk. Include whole grains, fruits, vegetables, and legumes in your diet.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+fiber+for+gut+health"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Include Probiotics",
            description = "Probiotic-rich foods like yogurt, kefir, sauerkraut, and kimchi introduce beneficial bacteria that support digestion, boost immunity, and improve nutrient absorption.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+probiotics"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_avoid_fst_food,
            title = "Avoid Processed Foods",
            description = "Processed foods high in sugar, unhealthy fats, and artificial additives can harm gut bacteria, cause inflammation, and disrupt digestion. Choose whole, natural foods instead.",
            learnMoreUrl = "https://www.google.com/search?q=effects+of+processed+food+on+gut"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Stay Hydrated",
            description = "Adequate water intake supports the mucosal lining of the intestines, aids nutrient absorption, and promotes regular bowel movements. Drink water consistently throughout the day.",
            learnMoreUrl = "https://www.google.com/search?q=water+and+digestive+health"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_workout,
            title = "Manage Stress",
            description = "Chronic stress negatively affects gut health by altering gut bacteria and increasing inflammation. Practice stress-reduction techniques like yoga, meditation, or deep breathing.",
            learnMoreUrl = "https://www.google.com/search?q=stress+effects+on+gut+health"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Eat Slowly",
            description = "Chewing food thoroughly and eating slowly aids digestion, improves nutrient absorption, reduces bloating, and helps you recognize fullness cues to prevent overeating.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+eating+slowly"
        )
    )

    // 🍎 Nutrition Tips (6 tips)
    val nutritionTips = listOf(
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Eat More Fruits",
            description = "Eating fruits daily provides essential vitamins, minerals, and fiber that boost your immune system, improve digestion, and reduce the risk of chronic diseases. Fruits are rich in antioxidants that protect your body from harmful free radicals.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+eating+fruits"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Include Vegetables",
            description = "Vegetables provide vital nutrients, fiber, and antioxidants that support overall health. Aim for a variety of colorful vegetables to ensure you get a wide range of nutrients.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+eating+vegetables"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_avoid_fst_food,
            title = "Avoid Fast Food",
            description = "Fast food is often high in calories, unhealthy fats, sodium, and sugar, which can lead to obesity, heart disease, diabetes, and other health problems. Choose home-cooked meals with fresh ingredients instead.",
            learnMoreUrl = "https://www.google.com/search?q=why+avoid+fast+food"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Balance Your Plate",
            description = "A balanced diet includes appropriate portions of proteins, carbohydrates, healthy fats, and plenty of fruits and vegetables. This ensures your body gets all essential nutrients for optimal function.",
            learnMoreUrl = "https://www.google.com/search?q=balanced+diet+importance"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Reduce Sugar Intake",
            description = "Excessive sugar consumption increases the risk of obesity, type 2 diabetes, heart disease, and tooth decay. Limit sugary drinks, desserts, and processed foods high in added sugars.",
            learnMoreUrl = "https://www.google.com/search?q=effects+of+too+much+sugar"
        ),
        HealthTipDetail(
            icon = R.drawable.ic_helthy_foood,
            title = "Choose Healthy Fats",
            description = "Include sources of healthy fats like avocados, nuts, seeds, olive oil, and fatty fish. These fats support brain health, reduce inflammation, and improve heart health.",
            learnMoreUrl = "https://www.google.com/search?q=benefits+of+healthy+fats"
        )
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Health Tips",
                        color = white,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = white
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = primary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(
                    brush = Brush.verticalGradient(
                        colors = pastelColors,
                        startY = 0f,
                        endY = 1800f
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {

            // 🏃 Physical Health Section
            TipSection(
                title = "🏃 Physical Health",
                tips = physicalHealthTips,
                primary = primary,
                onTipClick = { tip ->
                    selectedTip = tip
                    showTipDialog = true
                }
            )

            // 🧠 Mental Health Section
            TipSection(
                title = "🧠 Mental Health",
                tips = mentalHealthTips,
                primary = primary,
                onTipClick = { tip ->
                    selectedTip = tip
                    showTipDialog = true
                }
            )

            // 🥗 Gut Health Section
            TipSection(
                title = "🥗 Gut Health",
                tips = gutHealthTips,
                primary = primary,
                onTipClick = { tip ->
                    selectedTip = tip
                    showTipDialog = true
                }
            )

            // 🍎 Nutrition Section
            TipSection(
                title = "🍎 Nutrition",
                tips = nutritionTips,
                primary = primary,
                onTipClick = { tip ->
                    selectedTip = tip
                    showTipDialog = true
                }
            )

            Spacer(Modifier.height(16.dp))
        }
    }

    // Health Tip Dialog
    if (showTipDialog && selectedTip != null) {
        AlertDialog(
            onDismissRequest = { showTipDialog = false },
            title = {
                Text(
                    text = selectedTip!!.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp
                )
            },
            text = {
                Text(
                    text = selectedTip!!.description,
                    fontSize = 14.sp,
                    textAlign = TextAlign.Justify,
                    lineHeight = 20.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(selectedTip!!.learnMoreUrl))
                        context.startActivity(intent)
                    }
                ) {
                    Text("Learn More", color = primary, fontWeight = FontWeight.SemiBold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showTipDialog = false }) {
                    Text("Close", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun TipSection(
    title: String,
    tips: List<HealthTipDetail>,
    primary: Color,
    onTipClick: (HealthTipDetail) -> Unit
) {
    Column(
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Section Title
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp,
            color = primary
        )

        // 2-column Grid
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            tips.chunked(2).forEach { rowTips ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    rowTips.forEach { tip ->
                        TipCard(
                            tip = tip,
                            primary = primary,
                            modifier = Modifier.weight(1f),
                            onClick = { onTipClick(tip) }
                        )
                    }
                    // If odd number, add spacer for alignment
                    if (rowTips.size == 1) {
                        Spacer(Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
private fun TipCard(
    tip: HealthTipDetail,
    primary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, primary),
        modifier = modifier
            .height(140.dp)
            .clickable { onClick() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = tip.icon),
                contentDescription = tip.title,
                modifier = Modifier.size(60.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = tip.title,
                textAlign = TextAlign.Center,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 2,
                lineHeight = 16.sp
            )
        }
    }
}