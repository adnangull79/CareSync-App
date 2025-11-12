package com.example.caresync.DoctorHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.draw.shadow
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
import coil.compose.AsyncImage
import coil.compose.AsyncImagePainter
import coil.request.ImageRequest
import com.example.caresync.DoctorAppointment.DoctorAppointmentScreen
import com.example.caresync.DoctorNotes.doc_notes
import com.example.caresync.PatientHome.CalculatorType
import com.example.caresync.R
import com.example.caresync.Screen
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun doctorhome(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val context = LocalContext.current
    val uid = auth.currentUser?.uid

    var doctorName by remember { mutableStateOf("Doctor") }
    var doctorCategory by remember { mutableStateOf("Specialist") }
    var profileImageUrl by remember { mutableStateOf<String?>(null) }

    // ✅ Firestore fetching
    LaunchedEffect(uid) {
        uid ?: return@LaunchedEffect
        db.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                val name = snap.getString("name") ?: "Doctor"
                val firstWord = name.split(" ").firstOrNull()?.uppercase() ?: "DOCTOR"
                doctorName = "Dr. $firstWord"
                doctorCategory = snap.getString("specialization") ?: "Specialist"
                profileImageUrl = snap.getString("profileImageUrl")
            }
    }

    // ✅ FIXED: Use rememberSaveable to persist tab state
    var selectedTab by rememberSaveable { mutableStateOf("home") }
    var isLoggingOut by remember { mutableStateOf(false) }

    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                Box(modifier = Modifier.fillMaxSize()) {
                    ModalDrawerSheet(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .fillMaxHeight(0.7f),
                        drawerContainerColor = gradientColors[1]
                    ) {
                        Spacer(Modifier.height(16.dp))
                        Text(
                            text = "Menu",
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(16.dp),
                            color = primary
                        )

                        DrawerItemWithBullet("View Profile", primary) {
                            navController.navigate(Screen.DoctorProfileScreen.route)
                        }
                        DrawerItemWithBullet("Edit Profile", primary) {
                            navController.navigate(Screen.EditDoctorProfile.route)
                        }
                        DrawerItemWithBullet("Settings", primary) {
                            navController.navigate(Screen.DoctorSettings.route)
                        }
                        DrawerItemWithBullet("Report a Problem", primary) {
                            navController.navigate(Screen.ContactScreen.route)
                        }
                        DrawerItemWithBullet("FAQs", primary) {
                            navController.navigate(Screen.DoctorFAQScreen.route)
                        }
                        DrawerItemWithBullet("About", primary) {
                            navController.navigate(Screen.AboutScreen.route)
                        }

                        Spacer(modifier = Modifier.weight(1f))

                        Button(
                            onClick = {
                                isLoggingOut = true
                                scope.launch {
                                    auth.signOut()
                                    delay(3000)
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
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(20.dp),
                                        color = white,
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text("Logging out...", color = white)
                                }
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
                    val topBarTitle = when (selectedTab) {
                        "home" -> "Welcome $doctorName"
                        "appointments" -> "Appointments"
                        "notes" -> "Notes"
                        "profile" -> "Profile"
                        else -> "CareSync"
                    }

                    TopAppBar(
                        title = {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = topBarTitle,
                                    color = white,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis,
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 20.sp
                                )
                                Row {
                                    IconButton(onClick = { navController.navigate("doctor_notifications")
                                    }) {
                                        Icon(Icons.Default.Notifications, contentDescription = "Notifications", tint = white)
                                    }
                                    IconButton(onClick = { scope.launch { drawerState.open() } }) {
                                        Icon(Icons.Default.Menu, contentDescription = "Menu", tint = white)
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
                    FloatingActionButton(onClick = { navController.navigate(Screen.ChatScreen.route) }, containerColor = primary) {
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
                        BottomNavItem("Appointments", selectedTab == "appointments", { selectedTab = "appointments" }, Icons.Default.DateRange)
                        BottomNavItem("Notes", selectedTab == "notes", { selectedTab = "notes" }, Icons.Default.StickyNote2)
                        BottomNavItem("Profile", selectedTab == "profile", { selectedTab = "profile" }, Icons.Default.Person)
                    }
                }
            ) { padding ->
                Box(
                    modifier = Modifier
                        .background(
                            brush = Brush.verticalGradient(
                                colors = gradientColors,
                                startY = 0f,
                                endY = 1200f
                            )
                        )
                        .fillMaxSize()
                ) {
                    when (selectedTab) {
                        "home" -> DoctorHomeContent(
                            padding, primary, doctorName, doctorCategory,
                            profileImageUrl, navController, uid
                        )
                        "appointments" -> DoctorAppointmentScreen(navController)
                        "profile" -> doc_profile(navController)
                        "notes" -> doc_notes(navController)
                    }
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
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clickable { onClick() }
    ) {
        Text("•", fontSize = 20.sp, color = textColor, modifier = Modifier.padding(end = 8.dp))
        Text(title, style = MaterialTheme.typography.bodyMedium, color = textColor)
    }
}

@Composable
private fun DoctorHomeContent(
    padding: PaddingValues,
    primary: Color,
    doctorName: String,
    doctorCategory: String,
    profileImageUrl: String?,
    navController: NavController,
    uid: String?
) {
    val context = LocalContext.current
    val db = remember { FirebaseFirestore.getInstance() }

    var totalBooked by remember { mutableStateOf(0) }
    var checkedCount by remember { mutableStateOf(0) }
    var remainingCount by remember { mutableStateOf(0) }
    var isLoadingAppointments by remember { mutableStateOf(true) }

    LaunchedEffect(uid) {
        if (uid == null) {
            isLoadingAppointments = false
            return@LaunchedEffect
        }

        try {
            db.collection("Appointments")
                .whereEqualTo("doctorId", uid)
                .get()
                .addOnSuccessListener { snapshot ->
                    val appointments = snapshot.documents

                    totalBooked = appointments.size
                    checkedCount = appointments.count { it.getString("status") == "Checked" }
                    remainingCount = appointments.count { it.getString("status") == "Booked" }

                    isLoadingAppointments = false
                }
                .addOnFailureListener {
                    isLoadingAppointments = false
                }
        } catch (e: Exception) {
            isLoadingAppointments = false
        }
    }

    Column(
        modifier = Modifier
            .padding(padding)
            .verticalScroll(rememberScrollState())
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("Profile", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, primary),
            modifier = Modifier
                .fillMaxWidth()
                .height(130.dp)
                .clickable { navController.navigate(Screen.DoctorProfileScreen.route) }
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = CircleShape,
                    border = BorderStroke(2.dp, primary),
                    modifier = Modifier.size(90.dp)
                ) {
                    if (!profileImageUrl.isNullOrEmpty()) {
                        var isImageLoading by remember { mutableStateOf(true) }

                        Box(contentAlignment = Alignment.Center) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(profileImageUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Doctor Image",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.clip(CircleShape),
                                onState = {
                                    isImageLoading = it is AsyncImagePainter.State.Loading
                                }
                            )
                            if (isImageLoading) {
                                CircularProgressIndicator(color = primary, strokeWidth = 2.dp, modifier = Modifier.size(24.dp))
                            }
                        }
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Doctor Placeholder", tint = primary, modifier = Modifier.padding(16.dp))
                    }
                }

                Spacer(Modifier.width(12.dp))
                Column {
                    Text(doctorName, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
                    Text(doctorCategory, fontSize = 16.sp, color = Color.DarkGray)
                    Text("Rating: 4.5", fontSize = 14.sp, color = Color.Gray)
                    Spacer(Modifier.height(6.dp))
                    Text(
                        "Edit",
                        color = primary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.EditDoctorProfile.route)
                        }
                    )
                }
            }
        }

        Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.3f))
        Text("Calculators", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            QuickAccessCard("BMI", Icons.Default.FitnessCenter, primary, Modifier.weight(1f)) {
                navController.navigate(Screen.DoctorCalculatorScreen.passType(CalculatorType.BMI.name))
            }
            QuickAccessCard("BMR", Icons.Default.Person, primary, Modifier.weight(1f)) {
                navController.navigate(Screen.DoctorCalculatorScreen.passType(CalculatorType.BMR.name))
            }
            QuickAccessCard("Water", Icons.Default.LocalDrink, primary, Modifier.weight(1f)) {
                navController.navigate(Screen.DoctorCalculatorScreen.passType(CalculatorType.WATER.name))
            }
            QuickAccessCard("Body Fat", Icons.Default.Accessibility, primary, Modifier.weight(1f)) {
                navController.navigate(Screen.DoctorCalculatorScreen.passType(CalculatorType.BODY_FAT.name))
            }
        }

        Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.3f))
        Text("Appointments", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        if (isLoadingAppointments) {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
                }
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalBooked > 0) "$totalBooked" else "--",
                            fontWeight = FontWeight.Bold,
                            fontSize = 28.sp,
                            color = primary
                        )
                        Text("Total Booked Appointments", color = primary)
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            AppointmentCard(
                value = if (isLoadingAppointments) "--" else if (checkedCount > 0) "$checkedCount" else "--",
                label = "Checked",
                primary = primary,
                modifier = Modifier.weight(1f)
            )
            AppointmentCard(
                value = if (isLoadingAppointments) "--" else if (remainingCount > 0) "$remainingCount" else "--",
                label = "Remaining",
                primary = primary,
                modifier = Modifier.weight(1f)
            )
        }

        // ✅ NEW: Health Articles Card
        Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.3f))
        Text("More", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

        Surface(
            shape = RoundedCornerShape(18.dp),
            border = BorderStroke(1.dp, primary),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .height(110.dp)
                .clickable { navController.navigate(Screen.HealthArticlesScreen.route) }
                .shadow(4.dp, RoundedCornerShape(18.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(60.dp)
                        .background(primary.copy(alpha = 0.1f), shape = CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Article,
                        contentDescription = "Health Articles",
                        tint = primary,
                        modifier = Modifier.size(34.dp)
                    )
                }
                Spacer(Modifier.width(14.dp))
                Column {
                    Text(
                        text = "Health Articles",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = Color.Black
                    )
                    Text(
                        text = "Explore the latest Health Articles",
                        fontSize = 14.sp,
                        color = Color.Gray,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(120.dp))
    }
}

@Composable
private fun QuickAccessCard(title: String, icon: ImageVector, primary: Color, modifier: Modifier = Modifier, onClick: () -> Unit = {}) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = BorderStroke(1.dp, primary),
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(1f)
                .clickable { onClick() }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Icon(icon, contentDescription = title, tint = primary, modifier = Modifier.size(35.dp))
            }
        }
        Spacer(Modifier.height(4.dp))
        Text(title, textAlign = TextAlign.Center, fontSize = 14.sp, color = Color.Black)
    }
}

@Composable
private fun AppointmentCard(value: String, label: String, primary: Color, modifier: Modifier) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, primary),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primary)
            Spacer(Modifier.height(4.dp))
            Text(label, color = primary)
        }
    }
}

@Composable
private fun RowScope.BottomNavItem(title: String, selected: Boolean, onClick: () -> Unit, icon: ImageVector) {
    NavigationBarItem(
        selected = selected,
        onClick = onClick,
        icon = {
            Icon(
                icon,
                contentDescription = title,
                tint = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                modifier = Modifier.size(24.dp)
            )
        },
        label = {
            Text(
                text = title,
                color = if (selected) Color.White else Color.White.copy(alpha = 0.6f),
                fontSize = 10.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        alwaysShowLabel = true,
        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
    )
}
