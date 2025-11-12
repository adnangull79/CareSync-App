// File: DoctorAppointmentScreen.kt
package com.example.caresync.DoctorAppointment

import android.app.DatePickerDialog
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import coil.compose.SubcomposeAsyncImage
import com.example.caresync.R
import com.example.caresync.notification.NotificationHelper
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*

/* =========================================================
   Data + Filter
   ========================================================= */

data class AppointmentDetail(
    val id: String = "",
    val patientName: String = "",
    val patientImage: String = "",
    val date: String = "",
    val time: String = "",
    val status: String = "", // "Booked" | "Checked" | "Missed"
    val patientId: String = "",
    val timestamp: Date? = null
)

private enum class AppointmentFilter { ALL, TODAY, DATE }

/* =========================================================
   Screen
   ========================================================= */

@Composable
fun DoctorAppointmentScreen(navController: NavController) {

    val primary = colorResource(id = R.color.primary_button_color)
    val context = LocalContext.current

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val currentDoctorId = auth.currentUser?.uid ?: ""

    // ---------- State ----------
    var appointments by remember { mutableStateOf<List<AppointmentDetail>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    val scope = rememberCoroutineScope()

    var searchText by remember { mutableStateOf("") }
    var selectedAppointment by remember { mutableStateOf<AppointmentDetail?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    // Page-level menu (delete checked / reset all)
    var showPageMenu by remember { mutableStateOf(false) }

    // Confirmations
    var showCheckConfirmation by remember { mutableStateOf(false) }
    var showMissedConfirmation by remember { mutableStateOf(false) }
    var showDeleteCheckedConfirmation by remember { mutableStateOf(false) }
    var showResetAllConfirmation by remember { mutableStateOf(false) }
    var showDeleteSingleConfirmation by remember { mutableStateOf(false) }

    // Filter state
    val todayStr = remember {
        val cal = Calendar.getInstance()
        "${cal.get(Calendar.DAY_OF_MONTH)}/${cal.get(Calendar.MONTH) + 1}/${cal.get(Calendar.YEAR)}"
    }
    var selectedFilter by remember { mutableStateOf(AppointmentFilter.TODAY) } // default: Today (per your last instruction)
    var selectedDate by remember { mutableStateOf(todayStr) } // used when filter = DATE

    // Summary counters (based on current list)
    var totalCount by remember { mutableStateOf(0) }
    var checkedCount by remember { mutableStateOf(0) }
    var bookedCount by remember { mutableStateOf(0) }
    var missedCount by remember { mutableStateOf(0) }

    // ---------- Data Loading ----------
    suspend fun loadAppointmentsAll() {
        isLoading = true
        try {
            if (currentDoctorId.isEmpty()) {
                appointments = emptyList()
                return
            }
            val snap = db.collection("Appointments")
                .whereEqualTo("doctorId", currentDoctorId)
                .orderBy("timestamp", Query.Direction.DESCENDING) // latest first (requires composite index)
                .get()
                .await()

            appointments = snap.documents.mapNotNull { d ->
                val ts = d.getTimestamp("timestamp")?.toDate()
                AppointmentDetail(
                    id = d.id,
                    patientName = d.getString("patientName") ?: "",
                    patientImage = d.getString("patientImage") ?: "",
                    date = d.getString("date") ?: "",
                    time = d.getString("time") ?: "",
                    status = d.getString("status") ?: "Booked",
                    patientId = d.getString("patientId") ?: "",
                    timestamp = ts
                )
            }
        } finally {
            val list = appointments
            totalCount = list.size
            checkedCount = list.count { it.status == "Checked" }
            bookedCount = list.count { it.status == "Booked" }
            missedCount = list.count { it.status == "Missed" }
            isLoading = false
        }
    }

    suspend fun loadAppointmentsForDate(targetDate: String) {
        isLoading = true
        try {
            if (currentDoctorId.isEmpty()) {
                appointments = emptyList()
                return
            }
            val snap = db.collection("Appointments")
                .whereEqualTo("doctorId", currentDoctorId)
                .whereEqualTo("date", targetDate)
                .orderBy("timestamp", Query.Direction.DESCENDING) // latest first (may require index)
                .get()
                .await()

            appointments = snap.documents.mapNotNull { d ->
                val ts = d.getTimestamp("timestamp")?.toDate()
                AppointmentDetail(
                    id = d.id,
                    patientName = d.getString("patientName") ?: "",
                    patientImage = d.getString("patientImage") ?: "",
                    date = d.getString("date") ?: "",
                    time = d.getString("time") ?: "",
                    status = d.getString("status") ?: "Booked",
                    patientId = d.getString("patientId") ?: "",
                    timestamp = ts
                )
            }
        } finally {
            val list = appointments
            totalCount = list.size
            checkedCount = list.count { it.status == "Checked" }
            bookedCount = list.count { it.status == "Booked" }
            missedCount = list.count { it.status == "Missed" }
            isLoading = false
        }
    }

    fun filterLabel(): String = when (selectedFilter) {
        AppointmentFilter.ALL -> "All"
        AppointmentFilter.TODAY -> "Today"
        AppointmentFilter.DATE -> selectedDate
    }

    suspend fun reloadForCurrentFilter() {
        when (selectedFilter) {
            AppointmentFilter.ALL   -> loadAppointmentsAll()
            AppointmentFilter.TODAY -> loadAppointmentsForDate(todayStr)
            AppointmentFilter.DATE  -> loadAppointmentsForDate(selectedDate)
        }
    }

    // Initial load (Today by default)
    LaunchedEffect(Unit) { reloadForCurrentFilter() }

    // Client-side name search
    val filteredAppointments = remember(searchText, appointments) {
        if (searchText.isBlank()) appointments
        else appointments.filter { it.patientName.contains(searchText, ignoreCase = true) }
    }

    /* =========================================================
       UI
       ========================================================= */

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 8.dp, end = 8.dp, top = 100.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        // ---------- Summary header with primary filter pill on the right ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Summary", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            var showFilterMenu by remember { mutableStateOf(false) }
            Box {
                FilterPill(
                    text = filterLabel(),
                    onClick = { showFilterMenu = true },
                    primary = primary
                )
                DropdownMenu(
                    expanded = showFilterMenu,
                    onDismissRequest = { showFilterMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("All") },
                        onClick = {
                            showFilterMenu = false
                            selectedFilter = AppointmentFilter.ALL
                            scope.launch { reloadForCurrentFilter() }
                        },
                        leadingIcon = { Icon(Icons.Default.ViewList, contentDescription = null, tint = primary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Today") },
                        onClick = {
                            showFilterMenu = false
                            selectedFilter = AppointmentFilter.TODAY
                            selectedDate = todayStr
                            scope.launch { reloadForCurrentFilter() }
                        },
                        leadingIcon = { Icon(Icons.Default.Today, contentDescription = null, tint = primary) }
                    )
                    DropdownMenuItem(
                        text = { Text("Pick date") },
                        onClick = {
                            showFilterMenu = false
                            val cal = Calendar.getInstance()
                            DatePickerDialog(
                                context,
                                { _, y, m, d ->
                                    selectedDate = "$d/${m + 1}/$y"
                                    selectedFilter = AppointmentFilter.DATE
                                    scope.launch { reloadForCurrentFilter() }
                                },
                                cal.get(Calendar.YEAR),
                                cal.get(Calendar.MONTH),
                                cal.get(Calendar.DAY_OF_MONTH)
                            ).show()
                        },
                        leadingIcon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, tint = primary) }
                    )
                }
            }
        }

        // Summary cards
        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(100.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primary, strokeWidth = 2.dp)
            }
        } else {
            Surface(
                shape = RoundedCornerShape(16.dp),
                color = Color.White,
                border = BorderStroke(1.dp, primary),
                modifier = Modifier.fillMaxWidth().height(100.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = if (totalCount > 0) "$totalCount" else "--",
                            fontWeight = FontWeight.Bold, fontSize = 28.sp, color = Color.Black
                        )
                        Text(
                            when (selectedFilter) {
                                AppointmentFilter.ALL   -> "Total Appointments • All"
                                AppointmentFilter.TODAY -> "Total Appointments • Today ($todayStr)"
                                AppointmentFilter.DATE  -> "Total Appointments • $selectedDate"
                            },
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                AppointmentStatCard(
                    value = if (checkedCount > 0) "$checkedCount" else "--",
                    label = "Checked", bg = Color.White, primary = primary, modifier = Modifier.weight(1f)
                )
                AppointmentStatCard(
                    value = if (bookedCount > 0) "$bookedCount" else "--",
                    label = "Booked", bg = Color.White, primary = primary, modifier = Modifier.weight(1f)
                )
                AppointmentStatCard(
                    value = if (missedCount > 0) "$missedCount" else "--",
                    label = "Missed", bg = Color.White, primary = primary, modifier = Modifier.weight(1f)
                )
            }
        }

        Divider(thickness = 1.dp, color = Color.Gray.copy(alpha = 0.3f))

        // ---------- Title + page actions ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Booked Appointments", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)

            Box {
                IconButton(onClick = { showPageMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Options", tint = primary)
                }
                DropdownMenu(expanded = showPageMenu, onDismissRequest = { showPageMenu = false }) {
                    DropdownMenuItem(
                        text = { Text("Delete Checked") },
                        onClick = {
                            showPageMenu = false
                            showDeleteCheckedConfirmation = true
                        },
                        leadingIcon = { Icon(Icons.Default.DeleteSweep, contentDescription = null, tint = primary) }
                    )
                    DropdownMenuItem(
                        text = {
                            Text(
                                when (selectedFilter) {
                                    AppointmentFilter.ALL   -> "Reset All (All)"
                                    AppointmentFilter.TODAY -> "Reset All (Today)"
                                    AppointmentFilter.DATE  -> "Reset All (Selected Date)"
                                }
                            )
                        },
                        onClick = {
                            showPageMenu = false
                            showResetAllConfirmation = true
                        },
                        leadingIcon = { Icon(Icons.Default.RestartAlt, contentDescription = null, tint = Color.Red) }
                    )
                }
            }
        }

        // ---------- Search ----------
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color(0xFFE3F2FD),
            border = BorderStroke(1.dp, primary),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = primary)
                Spacer(Modifier.width(8.dp))
                BasicTextField(
                    value = searchText,
                    onValueChange = { searchText = it },
                    textStyle = TextStyle(color = Color.Black, fontSize = 16.sp),
                    decorationBox = { inner ->
                        if (searchText.isEmpty()) Text("Search patient...", color = Color.Gray)
                        inner()
                    },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // ---------- List ----------
        if (isLoading) {
            Box(Modifier.fillMaxWidth().height(200.dp), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = primary)
            }
        } else if (filteredAppointments.isEmpty()) {
            Box(Modifier.fillMaxWidth().padding(vertical = 32.dp), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(64.dp))
                    Spacer(Modifier.height(8.dp))
                    Text(
                        when (selectedFilter) {
                            AppointmentFilter.ALL   -> "No appointments yet"
                            AppointmentFilter.TODAY -> "No appointments for Today ($todayStr)"
                            AppointmentFilter.DATE  -> "No appointments for $selectedDate"
                        },
                        color = Color.Gray, fontSize = 16.sp
                    )
                }
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredAppointments.forEach { appt ->
                    AppointmentListCard(
                        appointment = appt,
                        primary = primary,
                        onCardClick = {
                            selectedAppointment = appt
                            showDetailDialog = true
                        },
                        onMarkChecked = {
                            selectedAppointment = appt
                            showCheckConfirmation = true
                        },
                        onMarkMissed = {
                            selectedAppointment = appt
                            showMissedConfirmation = true
                        },
                        onDelete = {
                            selectedAppointment = appt
                            showDeleteSingleConfirmation = true
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(100.dp))
    }

    /* =========================================================
       Dialogs
       ========================================================= */

    // Detail dialog
    if (showDetailDialog && selectedAppointment != null) {
        AppointmentDetailDialog(
            appointment = selectedAppointment!!,
            primary = primary,
            onDismiss = { showDetailDialog = false; selectedAppointment = null },
            onMarkChecked = { showDetailDialog = false; showCheckConfirmation = true }
        )
    }

    // Confirm: mark checked
    if (showCheckConfirmation && selectedAppointment != null) {
        AlertDialog(
            onDismissRequest = { showCheckConfirmation = false },
            title = { Text("Confirm Check", fontWeight = FontWeight.Bold) },
            text = { Text("Mark appointment with ${selectedAppointment!!.patientName} as checked?") },
            confirmButton = {
                Button(
                    onClick = {
                        val appt = selectedAppointment!!
                        db.collection("Appointments").document(appt.id)
                            .update("status", "Checked")
                            .addOnSuccessListener {
                                // notify patient
                                kotlinx.coroutines.GlobalScope.launch {
                                    NotificationHelper.sendAppointmentCheckedNotification(
                                        patientId = appt.patientId,
                                        doctorName = auth.currentUser?.displayName ?: "Doctor",
                                        date = appt.date,
                                        time = appt.time
                                    )
                                }
                                // update local list
                                appointments = appointments.map { if (it.id == appt.id) it.copy(status = "Checked") else it }
                                val list = appointments
                                totalCount = list.size
                                checkedCount = list.count { it.status == "Checked" }
                                bookedCount  = list.count { it.status == "Booked" }
                                missedCount  = list.count { it.status == "Missed" }
                                showCheckConfirmation = false
                                selectedAppointment = null
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) { Text("Yes, Check", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showCheckConfirmation = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    // Confirm: mark missed
    if (showMissedConfirmation && selectedAppointment != null) {
        AlertDialog(
            onDismissRequest = { showMissedConfirmation = false },
            title = { Text("Mark as Missed?", fontWeight = FontWeight.Bold) },
            text = { Text("Mark appointment with ${selectedAppointment!!.patientName} as missed?") },
            confirmButton = {
                Button(
                    onClick = {
                        val appt = selectedAppointment!!
                        db.collection("Appointments").document(appt.id)
                            .update("status", "Missed")
                            .addOnSuccessListener {
                                // notify patient
                                kotlinx.coroutines.GlobalScope.launch {
                                    NotificationHelper.sendAppointmentMissedNotification(
                                        patientId = appt.patientId, date = appt.date, time = appt.time
                                    )
                                }
                                appointments = appointments.map { if (it.id == appt.id) it.copy(status = "Missed") else it }
                                val list = appointments
                                totalCount = list.size
                                checkedCount = list.count { it.status == "Checked" }
                                bookedCount  = list.count { it.status == "Booked" }
                                missedCount  = list.count { it.status == "Missed" }
                                showMissedConfirmation = false
                                selectedAppointment = null
                            }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF7043))
                ) { Text("Yes, Mark Missed", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showMissedConfirmation = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    // Confirm: delete single appointment
    if (showDeleteSingleConfirmation && selectedAppointment != null) {
        AlertDialog(
            onDismissRequest = { showDeleteSingleConfirmation = false },
            title = { Text("Delete Appointment?", fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this appointment? This cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        val appt = selectedAppointment!!
                        scope.launch {
                            db.collection("Appointments").document(appt.id).delete().await()
                            appointments = appointments.filter { it.id != appt.id }
                            val list = appointments
                            totalCount = list.size
                            checkedCount = list.count { it.status == "Checked" }
                            bookedCount  = list.count { it.status == "Booked" }
                            missedCount  = list.count { it.status == "Missed" }
                            showDeleteSingleConfirmation = false
                            selectedAppointment = null
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteSingleConfirmation = false }) { Text("Cancel", color = Color.Gray) } }
        )
    }

    // Page menu: delete checked (respects filter)
    if (showDeleteCheckedConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteCheckedConfirmation = false },
            title = { Text("Delete Checked Appointments?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    when (selectedFilter) {
                        AppointmentFilter.ALL   -> "This will delete all checked appointments for ALL dates."
                        AppointmentFilter.TODAY -> "This will delete all checked appointments for Today ($todayStr)."
                        AppointmentFilter.DATE  -> "This will delete all checked appointments for $selectedDate."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            when (selectedFilter) {
                                AppointmentFilter.ALL -> {
                                    val snap = db.collection("Appointments")
                                        .whereEqualTo("doctorId", currentDoctorId)
                                        .whereEqualTo("status", "Checked")
                                        .get()
                                        .await()
                                    val batch = db.batch()
                                    snap.documents.forEach { d -> batch.delete(d.reference) }
                                    batch.commit().await()
                                }
                                AppointmentFilter.TODAY,
                                AppointmentFilter.DATE -> {
                                    val ids = appointments.filter { it.status == "Checked" }.map { it.id }
                                    val batch = db.batch()
                                    ids.forEach { id -> batch.delete(db.collection("Appointments").document(id)) }
                                    batch.commit().await()
                                }
                            }
                            reloadForCurrentFilter()
                            showDeleteCheckedConfirmation = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Delete", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showDeleteCheckedConfirmation = false }) { Text("Cancel") } }
        )
    }

    // Page menu: reset all (respects filter)
    if (showResetAllConfirmation) {
        AlertDialog(
            onDismissRequest = { showResetAllConfirmation = false },
            title = { Text("Reset All Appointments?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = {
                Text(
                    when (selectedFilter) {
                        AppointmentFilter.ALL   -> "This will permanently delete ALL appointments (checked, booked, missed) for ALL dates."
                        AppointmentFilter.TODAY -> "This will permanently delete ALL appointments for Today ($todayStr)."
                        AppointmentFilter.DATE  -> "This will permanently delete ALL appointments for $selectedDate."
                    }
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        scope.launch {
                            when (selectedFilter) {
                                AppointmentFilter.ALL -> {
                                    val snap = db.collection("Appointments")
                                        .whereEqualTo("doctorId", currentDoctorId)
                                        .get()
                                        .await()
                                    val batch = db.batch()
                                    snap.documents.forEach { d -> batch.delete(d.reference) }
                                    batch.commit().await()
                                }
                                AppointmentFilter.TODAY -> {
                                    val snap = db.collection("Appointments")
                                        .whereEqualTo("doctorId", currentDoctorId)
                                        .whereEqualTo("date", todayStr)
                                        .get()
                                        .await()
                                    val batch = db.batch()
                                    snap.documents.forEach { d -> batch.delete(d.reference) }
                                    batch.commit().await()
                                }
                                AppointmentFilter.DATE -> {
                                    val snap = db.collection("Appointments")
                                        .whereEqualTo("doctorId", currentDoctorId)
                                        .whereEqualTo("date", selectedDate)
                                        .get()
                                        .await()
                                    val batch = db.batch()
                                    snap.documents.forEach { d -> batch.delete(d.reference) }
                                    batch.commit().await()
                                }
                            }
                            reloadForCurrentFilter()
                            showResetAllConfirmation = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) { Text("Reset All", color = Color.White) }
            },
            dismissButton = { TextButton(onClick = { showResetAllConfirmation = false }) { Text("Cancel") } }
        )
    }
}

/* =========================================================
   Reusable UI
   ========================================================= */

@Composable
private fun FilterPill(text: String, onClick: () -> Unit, primary: Color) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = primary,
        tonalElevation = 2.dp,
        shadowElevation = 2.dp,
        border = BorderStroke(1.dp, primary)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Default.FilterList, contentDescription = "Filter", tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
            Text(text, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun AppointmentStatCard(
    value: String,
    label: String,
    bg: Color,
    primary: Color,
    modifier: Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, primary),
        modifier = modifier.height(90.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(value, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(label, color = Color.DarkGray)
        }
    }
}

/**
 * CARD WITH ITS OWN PER-ROW MENU.
 * The menu is **anchored to the 3-dots** inside this card, so it always opens
 * beside the tapped card (no floating at top).
 */
@Composable
fun AppointmentListCard(
    appointment: AppointmentDetail,
    primary: Color,
    onCardClick: () -> Unit,
    onMarkChecked: () -> Unit,
    onMarkMissed: () -> Unit,
    onDelete: () -> Unit
) {
    // Local state for this row's dropdown menu anchor
    var menuOpen by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, primary),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCardClick() }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(CircleShape)
                    .border(2.dp, primary, CircleShape)
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (appointment.patientImage.isNotEmpty()) {
                    SubcomposeAsyncImage(
                        model = appointment.patientImage,
                        contentDescription = "Patient",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                color = primary,
                                strokeWidth = 2.dp
                            )
                        },
                        error = {
                            Icon(Icons.Default.Person, contentDescription = "Default", modifier = Modifier.size(25.dp), tint = primary)
                        }
                    )
                } else {
                    Icon(Icons.Default.Person, contentDescription = "Default Patient", modifier = Modifier.size(25.dp), tint = primary)
                }
            }

            Spacer(Modifier.width(12.dp))

            // Info (Date and Time stacked)
            Column(modifier = Modifier.weight(1f)) {
                Text(appointment.patientName, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
                Spacer(Modifier.height(2.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Date", tint = primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(appointment.date, fontSize = 13.sp, color = Color.DarkGray)
                    }
                    Spacer(Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AccessTime, contentDescription = "Time", tint = primary, modifier = Modifier.size(14.dp))
                        Spacer(Modifier.width(4.dp))
                        Text(appointment.time, fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }

            // Status + 3-dots menu (anchored)
            Row(verticalAlignment = Alignment.CenterVertically) {

                when (appointment.status) {
                    "Checked" -> StatusBadge(bg = Color(0xFF4CAF50).copy(alpha = 0.15f), fg = Color(0xFF4CAF50), text = "Checked")
                    "Missed"  -> StatusBadge(bg = Color(0xFFF4511E).copy(alpha = 0.15f), fg = Color(0xFFF4511E), text = "Missed")
                    else      -> StatusBadge(bg = primary.copy(alpha = 0.15f), fg = primary, text = "Booked")
                }

                // Anchor box for the dropdown so it appears near THIS icon
                Box {
                    IconButton(onClick = { menuOpen = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "More", tint = primary)
                    }

                    DropdownMenu(
                        expanded = menuOpen,
                        onDismissRequest = { menuOpen = false },
                        // tiny offset so it feels beside the icon
                        offset = DpOffset(x = 0.dp, y = 0.dp)
                    ) {
                        DropdownMenuItem(
                            enabled = appointment.status != "Checked",
                            text = { Text("Mark as Checked") },
                            onClick = {
                                menuOpen = false
                                onMarkChecked()
                            },
                            leadingIcon = { Icon(Icons.Default.CheckCircle, contentDescription = null, tint = Color(0xFF4CAF50)) }
                        )
                        DropdownMenuItem(
                            enabled = appointment.status != "Missed",
                            text = { Text("Mark as Missed") },
                            onClick = {
                                menuOpen = false
                                onMarkMissed()
                            },
                            leadingIcon = { Icon(Icons.Default.EventBusy, contentDescription = null, tint = Color(0xFFF4511E)) }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Appointment") },
                            onClick = {
                                menuOpen = false
                                onDelete()
                            },
                            leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusBadge(bg: Color, fg: Color, text: String) {
    Card(colors = CardDefaults.cardColors(containerColor = bg), shape = RoundedCornerShape(8.dp)) {
        Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = when (text) {
                    "Checked" -> Icons.Default.CheckCircle
                    "Missed"  -> Icons.Default.EventBusy
                    else      -> Icons.Default.Schedule
                },
                contentDescription = text, tint = fg, modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(4.dp))
            Text(text = text, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = fg)
        }
    }
}

@Composable
fun AppointmentDetailDialog(
    appointment: AppointmentDetail,
    primary: Color,
    onDismiss: () -> Unit,
    onMarkChecked: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Appointment Details", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {

                Box(
                    modifier = Modifier.size(64.dp).align(Alignment.CenterHorizontally)
                        .clip(CircleShape).border(2.dp, primary, CircleShape).background(Color.White),
                    contentAlignment = Alignment.Center
                ) {
                    if (appointment.patientImage.isNotEmpty()) {
                        SubcomposeAsyncImage(
                            model = appointment.patientImage, contentDescription = "Patient",
                            modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Crop,
                            loading = { CircularProgressIndicator(modifier = Modifier.size(22.dp), color = primary, strokeWidth = 2.dp) },
                            error = { Icon(Icons.Default.Person, contentDescription = "Default", modifier = Modifier.size(34.dp), tint = primary) }
                        )
                    } else {
                        Icon(Icons.Default.Person, contentDescription = "Default Patient", modifier = Modifier.size(34.dp), tint = primary)
                    }
                }

                Divider()

                DetailRow("Patient Name:", appointment.patientName)
                DetailRow("Date:", appointment.date)
                DetailRow("Time:", appointment.time)
                DetailRow("Status:", appointment.status)
            }
        },
        confirmButton = {
            if (appointment.status != "Checked") {
                Button(onClick = onMarkChecked, colors = ButtonDefaults.buttonColors(containerColor = primary)) {
                    Text("Mark as Checked", color = Color.White)
                }
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Close", color = Color.Gray) } },
        containerColor = Color.White,
        shape = RoundedCornerShape(16.dp)
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontWeight = FontWeight.SemiBold, color = Color.Gray, fontSize = 14.sp)
        Text(value, fontWeight = FontWeight.Normal, color = Color.Black, fontSize = 14.sp, textAlign = TextAlign.End)
    }
}
