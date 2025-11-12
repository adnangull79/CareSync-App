// File: PatientHomeScreen.kt
package com.example.caresync.PatientHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import android.content.Intent
import android.net.Uri
import com.example.caresync.notifications.NotificationScreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientHomeScreen(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)

    val pastelColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    var firstName by remember { mutableStateOf("User") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uid) {
        uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                firstName = snap.getString("firstName") ?: "User"
                profileImageUrl = snap.getString("profileImageUrl")
            }
    }

    // ✅ FIXED: Use rememberSaveable to persist tab selection across navigation
    var selectedTab by rememberSaveable { mutableStateOf("home") }
    var isLoggingOut by remember { mutableStateOf(false) }

    // Health Calculations picker (bottom sheet)
    var showCalcSheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .wrapContentSize(Alignment.TopStart)
                ) {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .fillMaxHeight(0.6f),
                        drawerContainerColor = pastelColors[1]
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            color = primary
                        )

                        DrawerItemWithBullet("Profile", primary) {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.PatientProfileScreen.route)
                        }

                        DrawerItemWithBullet("Settings", primary) {
                            navController.navigate(Screen.PatientSettingsScreen.route)
                        }

                        DrawerItemWithBullet("FAQs", primary) {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.PatientFAQScreen.route)
                        }

                        DrawerItemWithBullet("About", primary) {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.AboutScreen.route)
                        }

                        DrawerItemWithBullet("Contact Us", primary) {
                            scope.launch { drawerState.close() }
                            navController.navigate(Screen.ContactScreen.route)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        // Logout Button
                        Button(
                            onClick = {
                                isLoggingOut = true
                                scope.launch {
                                    kotlinx.coroutines.delay(3000)
                                    auth.signOut()
                                    navController.navigate(Screen.onboardingScreenOne.route) {
                                        popUpTo(0) { inclusive = true }
                                    }
                                    isLoggingOut = false
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = primary),
                            modifier = Modifier
                                .padding(16.dp)
                                .fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            enabled = !isLoggingOut
                        ) {
                            if (isLoggingOut) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = white,
                                    strokeWidth = 2.dp
                                )
                                Spacer(Modifier.width(8.dp))
                                Text("Logging out...", color = white)
                            } else {
                                Text("Logout", color = white)
                            }
                        }
                    }
                }
            }
        ) {
            Scaffold(
                topBar = {
                    TopAppBar(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(90.dp),
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    when (selectedTab) {
                                        "home" -> {
                                            Image(
                                                painter = painterResource(id = R.drawable.stethoscope),
                                                contentDescription = "Logo",
                                                modifier = Modifier.size(40.dp)
                                            )
                                            Spacer(Modifier.width(8.dp))
                                            Text(
                                                text = "CareSync",
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis,
                                                color = white,
                                                style = MaterialTheme.typography.titleLarge,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 22.sp
                                            )
                                        }
                                        "appointments" -> Text("Appointments", color = white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        "docs" -> Text("My Documents", color = white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                        "tools" -> Text("Tools", color = white, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                                    }
                                }
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    IconButton(onClick = { navController.navigate(Screen.NotificationScreen.route) }) {
                                        Icon(
                                            imageVector = Icons.Default.Notifications,
                                            contentDescription = "Notifications",
                                            tint = white
                                        )
                                    }
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(
                                            imageVector = Icons.Default.Menu,
                                            contentDescription = "Open Menu",
                                            tint = white
                                        )
                                    }
                                }
                            }
                        },
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            containerColor = primary,
                            titleContentColor = white
                        )
                    )
                },
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { navController.navigate(Screen.ChatScreen.route) },
                        containerColor = primary
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_cahtbot),
                            contentDescription = "Chatbot",
                            modifier = Modifier.size(44.dp),
                            tint = white
                        )
                    }
                },
                bottomBar = {
                    NavigationBar(
                        containerColor = primary,
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars)
                            .height(60.dp)
                    ) {
                        BottomNavItem("Home", selectedTab == "home", { selectedTab = "home" }, Icons.Default.Home)
                        BottomNavItem("Appointment", selectedTab == "appointments", { selectedTab = "appointments" }, Icons.Default.DateRange)
                        BottomNavItem("My Docs", selectedTab == "docs", { selectedTab = "docs" }, Icons.Default.Description)
                        BottomNavItem("Tools", selectedTab == "tools", { selectedTab = "tools" }, Icons.Default.Build)
                    }
                }
            ) { padding ->
                when (selectedTab) {
                    "home" -> HomeContent(
                        padding = padding,
                        pastelColors = pastelColors,
                        primary = primary,
                        navController = navController,
                        onOpenCalcPicker = { showCalcSheet = true },
                        firstName = firstName,
                        profileImageUrl = profileImageUrl
                    )
                    "appointments" -> AppointmentScreen(navController)
                    "docs" -> DocumentScreen(navController)
                    "tools" -> ToolsScreen(navController)
                }
            }

            if (showCalcSheet) {
                ModalBottomSheet(
                    onDismissRequest = { showCalcSheet = false },
                    sheetState = sheetState
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "Choose a calculation",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        CalculationItem("BMI", Icons.Default.FitnessCenter, primary) {
                            showCalcSheet = false
                            navController.navigate("${Screen.Calculatorscreen.route}/BMI")
                        }
                        CalculationItem("BMR", Icons.Default.Person, primary) {
                            showCalcSheet = false
                            navController.navigate("${Screen.Calculatorscreen.route}/BMR")
                        }
                        CalculationItem("Water Intake", Icons.Default.LocalDrink, primary) {
                            showCalcSheet = false
                            navController.navigate("${Screen.Calculatorscreen.route}/WATER")
                        }
                        CalculationItem("Body Fat", Icons.Default.FitnessCenter, primary) {
                            showCalcSheet = false
                            navController.navigate("${Screen.Calculatorscreen.route}/BODY_FAT")
                        }
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun CalculationItem(
    title: String,
    icon: ImageVector,
    primary: Color,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(title) },
        leadingContent = { Icon(icon, contentDescription = title, tint = primary) },
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    )
    Divider()
}

private enum class Metric { BMI, BODY_FAT, WATER }

data class HealthTip(
    val icon: Int,
    val title: String,
    val description: String,
    val learnMoreUrl: String
)

// ✅ Simple Appointment Data Class
data class SimpleAppointment(
    val id: String = "",
    val doctorName: String = "",
    val doctorImage: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeContent(
    padding: PaddingValues,
    pastelColors: List<Color>,
    primary: Color,
    navController: NavController,
    onOpenCalcPicker: () -> Unit,
    firstName: String,
    profileImageUrl: String?
) {
    val context = LocalContext.current
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    // Health metrics
    var bmiDisplay by remember { mutableStateOf("--") }
    var bodyFatDisplay by remember { mutableStateOf("--") }
    var waterDisplay by remember { mutableStateOf("--") }

    var bmiVal by remember { mutableStateOf<Double?>(null) }
    var bodyFatVal by remember { mutableStateOf<Double?>(null) }
    var waterVal by remember { mutableStateOf<Double?>(null) }
    var gender by remember { mutableStateOf<String?>(null) }

    // ✅ NEW: Upcoming appointment state
    var upcomingAppointment by remember { mutableStateOf<SimpleAppointment?>(null) }
    var isLoadingAppointment by remember { mutableStateOf(true) }

    // Advice sheet state
    var showMetricAdviceSheet by remember { mutableStateOf(false) }
    var selectedMetric by remember { mutableStateOf<Metric?>(null) }

    // Health tip dialog state
    var showTipDialog by remember { mutableStateOf(false) }
    var selectedTip by remember { mutableStateOf<HealthTip?>(null) }

    LaunchedEffect(uid) {
        if (uid == null) return@LaunchedEffect

        // Get user gender
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                gender = snap.getString("gender")
            }

        // Get health metrics
        db.collection("users").document(uid)
            .collection("calculators").document("BMI")
            .get()
            .addOnSuccessListener { snap ->
                snap.getDouble("result")?.let { v ->
                    bmiVal = v
                    bmiDisplay = String.format("%.1f", v)
                }
            }

        db.collection("users").document(uid)
            .collection("calculators").document("BODY_FAT")
            .get()
            .addOnSuccessListener { snap ->
                snap.getDouble("result")?.let { v ->
                    bodyFatVal = v
                    bodyFatDisplay = "${String.format("%.1f", v)}%"
                }
            }

        db.collection("users").document(uid)
            .collection("calculators").document("WATER")
            .get()
            .addOnSuccessListener { snap ->
                snap.getDouble("result")?.let { v ->
                    waterVal = v
                    waterDisplay = "${String.format("%.1f", v)} L"
                }
            }

        // ✅ NEW: Fetch upcoming appointment
        db.collection("Appointments")
            .whereEqualTo("patientId", uid)
            .whereEqualTo("status", "Booked")
            .limit(1)
            .get()
            .addOnSuccessListener { snapshot ->
                if (!snapshot.isEmpty) {
                    val doc = snapshot.documents[0]
                    upcomingAppointment = SimpleAppointment(
                        id = doc.id,
                        doctorName = doc.getString("doctorName") ?: "",
                        doctorImage = doc.getString("doctorImage") ?: "",
                        date = doc.getString("date") ?: "",
                        time = doc.getString("time") ?: "",
                        status = doc.getString("status") ?: ""
                    )
                }
                isLoadingAppointment = false
            }
            .addOnFailureListener {
                isLoadingAppointment = false
            }
    }

    val (bmiStatusText, _) = bmiStatus(bmiVal)
    val (bfStatusText, _) = bodyFatStatus(bodyFatVal, gender)
    val (waterStatusText, _) = waterStatusRecommended(waterVal)

    Column(
        modifier = Modifier
            .padding(padding)
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .background(
                brush = Brush.verticalGradient(
                    colors = pastelColors,
                    startY = 0f,
                    endY = 1200f
                )
            )
            .padding(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // User Profile Card
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(2.dp, primary),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
                .clickable { navController.navigate(Screen.PatientProfileScreen.route) }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // ✅ FIXED: Profile Image with loading inside
                    Box(
                        modifier = Modifier
                            .size(70.dp)
                            .clip(CircleShape)
                            .border(BorderStroke(2.dp, primary), CircleShape)
                            .background(Color.White),
                        contentAlignment = Alignment.Center
                    ) {
                        if (profileImageUrl != null) {
                            SubcomposeAsyncImage(
                                model = profileImageUrl,
                                contentDescription = "User Profile",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop,
                                loading = {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(30.dp),
                                        color = primary,
                                        strokeWidth = 3.dp
                                    )
                                },
                                error = {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Profile",
                                        modifier = Modifier.size(40.dp),
                                        tint = primary
                                    )
                                }
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = "Default Profile",
                                modifier = Modifier.size(40.dp),
                                tint = primary
                            )
                        }
                    }

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = firstName,
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = Color.Black
                        )
                        Text(
                            text = "Healthy",
                            fontWeight = FontWeight.Normal,
                            fontSize = 14.sp,
                            color = Color.Gray
                        )
                    }
                }
                TextButton(onClick = { navController.navigate(Screen.EditPatientProfileScreen.route) }) {
                    Text("Edit", color = primary, fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                }
            }
        }

        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .width(1.dp)
                .padding(horizontal = 8.dp)
        )

        Text("Health INFO", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(horizontal = 12.dp))

        // Health Metrics Row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MetricCard(
                labelBelow = "BMI",
                value = bmiDisplay,
                statusText = if (bmiVal == null) "Not set" else bmiStatusText,
                bg = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f)
            ) {
                selectedMetric = Metric.BMI
                showMetricAdviceSheet = true
            }
            MetricCard(
                labelBelow = "Body Fat",
                value = bodyFatDisplay,
                statusText = if (bodyFatVal == null) "Not set" else bfStatusText,
                bg = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f)
            ) {
                selectedMetric = Metric.BODY_FAT
                showMetricAdviceSheet = true
            }
            MetricCard(
                labelBelow = "Water Intake",
                value = waterDisplay,
                statusText = if (waterVal == null) "Not set" else waterStatusText,
                bg = Color.White,
                primary = primary,
                modifier = Modifier.weight(1f)
            ) {
                selectedMetric = Metric.WATER
                showMetricAdviceSheet = true
            }
        }



//  Upcoming Appointment Card
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = BorderStroke(1.dp, primary),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Upcoming Appointment",
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    color = primary
                )

                if (isLoadingAppointment) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(80.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
                    }
                } else if (upcomingAppointment == null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "No upcoming appointments",
                                fontSize = 14.sp,
                                color = Color.Gray
                            )
                        }
                    }
                } else {
                    // ✅ IMPROVED: Professional Appointment Card Layout
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { navController.navigate(Screen.PatientAppointments.route) },
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = primary.copy(alpha = 0.08f)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Doctor Image
                            Box(
                                modifier = Modifier
                                    .size(60.dp)
                                    .clip(CircleShape)
                                    .border(2.dp, primary, CircleShape)
                                    .background(Color.White),
                                contentAlignment = Alignment.Center
                            ) {
                                if (upcomingAppointment!!.doctorImage.isNotEmpty()) {
                                    SubcomposeAsyncImage(
                                        model = upcomingAppointment!!.doctorImage,
                                        contentDescription = "Doctor",
                                        modifier = Modifier.fillMaxSize(),
                                        contentScale = ContentScale.Crop,
                                        loading = {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(25.dp),
                                                color = primary,
                                                strokeWidth = 2.dp
                                            )
                                        },
                                        error = {
                                            Icon(
                                                imageVector = Icons.Default.Person,
                                                contentDescription = "Default",
                                                modifier = Modifier.size(30.dp),
                                                tint = primary
                                            )
                                        }
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.Person,
                                        contentDescription = "Default Doctor",
                                        modifier = Modifier.size(30.dp),
                                        tint = primary
                                    )
                                }
                            }

                            // Doctor Info & Appointment Details
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                // Doctor Name
                                Text(
                                    text = "Dr. ${upcomingAppointment!!.doctorName}",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp,
                                    color = Color.Black
                                )

                                // Date Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CalendarToday,
                                        contentDescription = "Date",
                                        tint = primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = upcomingAppointment!!.date,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }

                                // Time Row
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.AccessTime,
                                        contentDescription = "Time",
                                        tint = primary,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Text(
                                        text = upcomingAppointment!!.time,
                                        fontSize = 14.sp,
                                        color = Color.DarkGray,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }

                            // Status Badge
                            Card(
                                colors = CardDefaults.cardColors(
                                    containerColor = primary.copy(alpha = 0.2f)
                                ),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    text = upcomingAppointment!!.status,
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Health Tips Section
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("Health Tips", fontWeight = FontWeight.SemiBold)
            Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Scroll Arrow")
        }

        val tips = listOf(
            HealthTip(
                icon = R.drawable.ic_helthy_foood,
                title = "Eat more fruits",
                description = "Eating fruits daily provides essential vitamins, minerals, and fiber that boost your immune system, improve digestion, and reduce the risk of chronic diseases.",
                learnMoreUrl = "https://www.google.com/search?q=benefits+of+eating+fruits"
            ),
            HealthTip(
                icon = R.drawable.ic_workout,
                title = "Exercise daily",
                description = "Regular physical activity strengthens your heart, improves cardiovascular health, helps maintain a healthy weight, and boosts mental health.",
                learnMoreUrl = "https://www.google.com/search?q=benefits+of+daily+exercise"
            ),
            HealthTip(
                icon = R.drawable.ic_morningwalik,
                title = "Do Morning Walk",
                description = "Morning walks improve circulation, boost metabolism, enhance mood, and provide fresh air and sunlight for vitamin D.",
                learnMoreUrl = "https://www.google.com/search?q=benefits+of+morning+walk"
            ),
            HealthTip(
                icon = R.drawable.ic_clean_enviroment,
                title = "Keep Environment Clean",
                description = "A clean environment reduces the spread of diseases, improves air quality, and promotes mental well-being.",
                learnMoreUrl = "https://www.google.com/search?q=importance+of+clean+environment+for+health"
            ),
            HealthTip(
                icon = R.drawable.ic_avoid_fst_food,
                title = "Avoid Fast Food",
                description = "Fast food is often high in calories, unhealthy fats, sodium, and sugar, which can lead to obesity, heart disease, diabetes, and other health problems.",
                learnMoreUrl = "https://www.google.com/search?q=why+avoid+fast+food"
            )
        )

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(horizontal = 2.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            items(tips.size) { index ->
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.clickable {
                        selectedTip = tips[index]
                        showTipDialog = true
                    }
                ) {
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = Color.White,
                        border = BorderStroke(1.dp, primary),
                        modifier = Modifier.size(120.dp)
                    ) {
                        Image(
                            painter = painterResource(id = tips[index].icon),
                            contentDescription = "Tip image",
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(tips[index].title, textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }

            item {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.width(80.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = primary,
                        modifier = Modifier
                            .size(60.dp)
                            .clickable { navController.navigate(Screen.HealthTipsScreen.route) }
                    ) {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = "See More",
                                tint = Color.White,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text("See More", textAlign = TextAlign.Center, fontSize = 12.sp)
                }
            }

            item {
                Spacer(Modifier.width(12.dp))
            }
        }

        Spacer(Modifier.height(80.dp))
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
                    Text("Cancel", color = Color.Gray, fontWeight = FontWeight.Medium)
                }
            },
            containerColor = Color.White,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Metric Advice Bottom Sheet
    if (showMetricAdviceSheet && selectedMetric != null) {
        val metric = selectedMetric!!
        val danger = Color(0xFFD32F2F)

        val title = when (metric) {
            Metric.BMI -> "BMI Calculator"
            Metric.BODY_FAT -> "Body Fat Calculator"
            Metric.WATER -> "Water Intake Calculator"
        }
        val valueText = when (metric) {
            Metric.BMI -> bmiDisplay
            Metric.BODY_FAT -> bodyFatDisplay
            Metric.WATER -> waterDisplay
        }
        val statusText = when (metric) {
            Metric.BMI -> if (bmiVal == null) "Not set" else bmiStatusText
            Metric.BODY_FAT -> if (bodyFatVal == null) "Not set" else bfStatusText
            Metric.WATER -> if (waterVal == null) "Not set" else "Recommended"
        }

        val centerColor: Color = when (metric) {
            Metric.BMI -> if (bmiVal == null) Color.Black else if (bmiHealthy(bmiVal!!)) primary else danger
            Metric.BODY_FAT -> if (bodyFatVal == null) Color.Black else if (bodyFatHealthy(bodyFatVal!!, gender)) primary else danger
            Metric.WATER -> Color.Black
        }

        val adviceText = when (metric) {
            Metric.BMI -> {
                if (bmiVal == null) "No BMI yet. Calculate to get personalized tips."
                else when {
                    bmiVal!! < 18.5 -> "You're underweight. Focus on calorie-dense, nutritious foods and strength training."
                    bmiVal!! < 25.0 -> "You're in a healthy BMI range. Maintain balanced meals and regular activity."
                    bmiVal!! < 30.0 -> "BMI indicates overweight. Increase daily movement and review portion sizes."
                    else -> "BMI is in the obese range. Aim for gradual weight loss and consider professional guidance."
                }
            }
            Metric.BODY_FAT -> {
                if (bodyFatVal == null) "No Body Fat value yet. Calculate to get advice."
                else {
                    when (bodyFatCategory(bodyFatVal!!, gender)) {
                        "Low" -> "Body fat is low. Ensure adequate calories and healthy fats; watch energy levels."
                        "Healthy" -> "Body fat is in a healthy range. Keep up your current routine."
                        else -> "Body fat is high. Prioritize consistent exercise and protein-rich, calorie-aware meals."
                    }
                }
            }
            Metric.WATER -> {
                if (waterVal == null) "No Water Intake value yet. Calculate to get your daily target."
                else "Your recommended daily water intake is $valueText. Drink at least this much to stay hydrated and healthy."
            }
        }

        ModalBottomSheet(
            onDismissRequest = {
                showMetricAdviceSheet = false
                selectedMetric = null
            },
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ) {
            Surface(
                color = Color.White,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, color = Color.Black)
                    Text("Current value", color = Color.Black, fontSize = 12.sp)
                    Text(valueText, fontWeight = FontWeight.Bold, fontSize = 22.sp, color = centerColor)
                    Text(text = statusText, color = centerColor, fontWeight = FontWeight.SemiBold)
                    Text(text = adviceText, textAlign = TextAlign.Center, color = Color.Black)

                    val route = when (metric) {
                        Metric.BMI -> "${Screen.Calculatorscreen.route}/BMI"
                        Metric.BODY_FAT -> "${Screen.Calculatorscreen.route}/BODY_FAT"
                        Metric.WATER -> "${Screen.Calculatorscreen.route}/WATER"
                    }
                    Button(
                        onClick = {
                            showMetricAdviceSheet = false
                            selectedMetric = null
                            navController.navigate(route)
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = primary),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Update value", color = Color.White)
                    }

                    Spacer(Modifier.height(6.dp))
                }
            }
        }
    }
}

@Composable
fun DrawerItemWithBullet(title: String, textColor: Color, onClick: () -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text("•", fontSize = 20.sp, color = textColor, modifier = Modifier.padding(end = 8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@Composable
private fun MetricCard(
    labelBelow: String,
    value: String,
    statusText: String,
    bg: Color,
    primary: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = bg,
            border = BorderStroke(1.dp, primary),
            modifier = Modifier
                .height(86.dp)
                .fillMaxWidth()
                .clickable { onClick() }
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Value (make this smaller too if you like)
                Text(
                    value,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    fontSize = 16.sp   // was default; shrink if needed
                )
                Spacer(Modifier.height(4.dp))

                // Status line: Recommended / Overweight / Healthy / Not set
                val statusColor = if (statusText == "Not set") Color(0xFF9E9E9E) else Color.Black
                Text(
                    statusText,
                    color = statusColor,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
        }
        Spacer(Modifier.height(6.dp))
        Text(
            labelBelow,
            color = primary,
            fontWeight = FontWeight.SemiBold,
            fontSize = 12.sp     // optional: smaller label under the card
        )
    }
}


// Helper Functions
private fun bmiStatus(bmi: Double?): Pair<String, Boolean> {
    if (bmi == null) return "Not set" to false
    return when {
        bmi < 18.5 -> "Underweight" to false
        bmi < 25.0 -> "Healthy" to true
        bmi < 30.0 -> "Overweight" to false
        else -> "Obese" to false
    }
}

private fun bmiHealthy(bmi: Double) = bmi in 18.5..24.9

private fun bodyFatCategory(value: Double, gender: String?): String {
    val g = gender?.lowercase()
    return if (g == "male") {
        when {
            value < 6 -> "Low"
            value <= 24 -> "Healthy"
            else -> "High"
        }
    } else if (g == "female") {
        when {
            value < 14 -> "Low"
            value <= 31 -> "Healthy"
            else -> "High"
        }
    } else {
        when {
            value < 10 -> "Low"
            value <= 31 -> "Healthy"
            else -> "High"
        }
    }
}

private fun bodyFatStatus(value: Double?, gender: String?): Pair<String, Boolean> {
    if (value == null) return "Not set" to false
    return when (bodyFatCategory(value, gender)) {
        "Healthy" -> "Healthy" to true
        "Low" -> "Low" to false
        "High" -> "High" to false
        else -> "—" to false
    }
}

private fun bodyFatHealthy(value: Double, gender: String?) = bodyFatCategory(value, gender) == "Healthy"

private fun waterStatusRecommended(value: Double?): Pair<String, Boolean> {
    if (value == null) return "Not set" to false
    return "Recommended" to true
}

@Composable
private fun RowScope.BottomNavItem(
    title: String,
    selected: Boolean,
    onClick: () -> Unit,
    icon: ImageVector
) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f)
            )
        },
        label = {
            Text(
                text = title,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(
            indicatorColor = Color.Transparent
        )
    )
}