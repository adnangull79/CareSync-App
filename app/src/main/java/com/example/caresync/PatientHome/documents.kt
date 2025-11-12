// File: DocumentScreen.kt
package com.example.caresync.PatientHome

import android.app.DatePickerDialog
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.*

data class Document(
    val id: String = "",
    val name: String = "",
    val type: String = "",
    val date: String = "",
    val imageUrl: String = ""
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DocumentScreen(navController: NavController) {
    val context = LocalContext.current
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelBlue = colorResource(id = R.color.pastel_blue)

    val gradientColors = listOf(
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_orange)
    )

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }
    var documents by remember { mutableStateOf<List<Document>>(emptyList()) }
    var isUploading by remember { mutableStateOf(false) }
    var selectedDocument by remember { mutableStateOf<Document?>(null) }

    val storage = FirebaseStorage.getInstance()
    val firestore = FirebaseFirestore.getInstance()
    val auth = FirebaseAuth.getInstance()
    val currentUser = auth.currentUser
    val userId = currentUser?.uid
    val coroutineScope = rememberCoroutineScope()

    // Fetch documents
    suspend fun fetchDocuments() {
        userId?.let {
            val snapshot = firestore.collection("users").document(it).collection("documents").get().await()
            documents = snapshot.documents.mapNotNull { doc ->
                Document(
                    id = doc.id,
                    name = doc.getString("name") ?: "",
                    type = doc.getString("type") ?: "",
                    date = doc.getString("date") ?: "",
                    imageUrl = doc.getString("imageUrl") ?: ""
                )
            }.sortedByDescending {
                try {
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(it.date)
                } catch (e: Exception) {
                    Date(0)
                }
            }
        }
    }

    LaunchedEffect(Unit) {
        fetchDocuments()
    }

    val filteredDocs = documents.filter {
        it.name.contains(searchQuery, ignoreCase = true)
    }

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
            .padding(horizontal = 16.dp, vertical = 20.dp)
            .padding(top = 90.dp, bottom = 70.dp)
    ) {
        // Top Title + Add Button
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Documents", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Document", tint = white)
                Spacer(modifier = Modifier.width(6.dp))
                Text("Add Document", color = white)
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search Document") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = "Search") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = white,
                unfocusedContainerColor = white
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Document List
        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            items(filteredDocs) { document ->
                DocumentCard(
                    document = document,
                    primary = primary,
                    white = white,
                    pastelBlue = pastelBlue,
                    navController = navController,
                    onEdit = {
                        selectedDocument = document
                        showEditDialog = true
                    },
                    onDelete = {
                        selectedDocument = document
                        showDeleteDialog = true
                    }
                )
            }
        }
    }

    // Add, Edit & Delete dialogs (same as before)
    if (showAddDialog) {
        AddDocumentDialog(
            primary = primary,
            white = white,
            isUploading = isUploading,
            selectedImageUri = selectedImageUri,
            onImageSelected = { selectedImageUri = it },
            onDismiss = { showAddDialog = false },
            onSave = { title, category, date ->
                if (title.isNotEmpty() && selectedImageUri != null && date.isNotEmpty() && !isUploading) {
                    isUploading = true
                    val fileName = "${UUID.randomUUID()}.jpg"
                    val ref = storage.reference.child("documents/$userId/$fileName")

                    ref.putFile(selectedImageUri!!).addOnSuccessListener {
                        ref.downloadUrl.addOnSuccessListener { uri ->
                            val docData = hashMapOf(
                                "name" to title,
                                "type" to category,
                                "date" to date,
                                "imageUrl" to uri.toString()
                            )
                            firestore.collection("users").document(userId!!).collection("documents")
                                .add(docData)
                                .addOnSuccessListener {
                                    coroutineScope.launch {
                                        delay(1000)
                                        fetchDocuments()
                                        isUploading = false
                                        showAddDialog = false
                                        selectedImageUri = null
                                        Toast.makeText(context, "Document added successfully", Toast.LENGTH_SHORT).show()
                                    }
                                }
                        }
                    }.addOnFailureListener {
                        isUploading = false
                        Toast.makeText(context, "Upload Failed", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    if (showEditDialog && selectedDocument != null) {
        EditDocumentDialog(
            document = selectedDocument!!,
            primary = primary,
            onDismiss = { showEditDialog = false },
            onSave = { newName, newType, newDate ->
                coroutineScope.launch {
                    try {
                        firestore.collection("users").document(userId!!)
                            .collection("documents").document(selectedDocument!!.id)
                            .update(
                                mapOf(
                                    "name" to newName,
                                    "type" to newType,
                                    "date" to newDate
                                )
                            ).await()
                        fetchDocuments()
                        showEditDialog = false
                        Toast.makeText(context, "Document updated successfully", Toast.LENGTH_SHORT).show()
                    } catch (e: Exception) {
                        Toast.makeText(context, "Failed to update: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }

    if (showDeleteDialog && selectedDocument != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Document?", fontWeight = FontWeight.Bold, color = Color.Red) },
            text = { Text("Are you sure you want to delete '${selectedDocument!!.name}'? This action cannot be undone.") },
            confirmButton = {
                Button(
                    onClick = {
                        coroutineScope.launch {
                            try {
                                firestore.collection("users").document(userId!!)
                                    .collection("documents").document(selectedDocument!!.id)
                                    .delete().await()
                                try {
                                    val imageRef = storage.getReferenceFromUrl(selectedDocument!!.imageUrl)
                                    imageRef.delete().await()
                                } catch (_: Exception) { }
                                fetchDocuments()
                                showDeleteDialog = false
                                Toast.makeText(context, "Document deleted successfully", Toast.LENGTH_SHORT).show()
                            } catch (e: Exception) {
                                Toast.makeText(context, "Failed to delete: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Delete", color = white)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}

// ✅ FIXED CARD: ensures Type + Date always show
@Composable
fun DocumentCard(
    document: Document,
    primary: Color,
    white: Color,
    pastelBlue: Color,
    navController: NavController,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = white,
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            var isImageLoading by remember { mutableStateOf(true) }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clickable {
                        navController.navigate(
                            "imageView/${Uri.encode(document.imageUrl)}/${Uri.encode(document.name)}"
                        )
                    }
            ) {
                val painter = rememberAsyncImagePainter(
                    model = document.imageUrl,
                    onLoading = { isImageLoading = true },
                    onSuccess = { isImageLoading = false },
                    onError = { isImageLoading = false }
                )

                if (isImageLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center).size(24.dp),
                        color = primary,
                        strokeWidth = 2.dp
                    )
                }

                Image(
                    painter = painter,
                    contentDescription = "Document Image",
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(RoundedCornerShape(8.dp))
                        .border(BorderStroke(2.dp, primary), RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = document.name,
                    fontWeight = FontWeight.Bold,
                    fontSize = MaterialTheme.typography.bodyLarge.fontSize
                )
                if (document.type.isNotBlank()) {
                    Text(
                        text = document.type,
                        style = MaterialTheme.typography.bodyMedium,
                        color = Color(0xFF4A4A4A)
                    )
                }
                if (document.date.isNotBlank()) {
                    Text(
                        text = "Date: ${document.date}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.Gray
                    )
                }
            }

            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Default.MoreVert,
                        contentDescription = "Options",
                        tint = primary
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("View") },
                        onClick = {
                            showMenu = false
                            navController.navigate(
                                "imageView/${Uri.encode(document.imageUrl)}/${Uri.encode(document.name)}"
                            )
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete", color = Color.Red) },
                        onClick = {
                            showMenu = false
                            onDelete()
                        }
                    )
                }
            }
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddDocumentDialog(
    primary: Color,
    white: Color,
    isUploading: Boolean,
    selectedImageUri: Uri?,
    onImageSelected: (Uri?) -> Unit,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Lab Report") }
    var date by remember { mutableStateOf("") }
    val categories = listOf("Lab Report", "Prescription", "X-ray", "Other")

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) {
        onImageSelected(it)
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> date = String.format("%04d-%02d-%02d", year, month + 1, day) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Document", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Date Picker
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Select Date") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Pick Date",
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Upload Image Button
                Button(
                    onClick = { launcher.launch("image/*") },
                    enabled = !isUploading,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (selectedImageUri != null) primary else primary.copy(alpha = 0.7f)
                    )
                ) {
                    Text(
                        text = when {
                            isUploading -> "Uploading..."
                            selectedImageUri != null -> "Image Selected ✓"
                            else -> "Upload Image"
                        },
                        color = white
                    )
                }

                if (isUploading) {
                    LinearProgressIndicator(
                        color = primary,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, date) },
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(12.dp),
                enabled = !isUploading
            ) {
                if (isUploading) {
                    CircularProgressIndicator(
                        color = white,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                }
                Text(if (isUploading) "Saving..." else "Save", color = white)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                enabled = !isUploading
            ) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDocumentDialog(
    document: Document,
    primary: Color,
    onDismiss: () -> Unit,
    onSave: (String, String, String) -> Unit
) {
    val context = LocalContext.current
    var title by remember { mutableStateOf(document.name) }
    var category by remember { mutableStateOf(document.type) }
    var date by remember { mutableStateOf(document.date) }
    val categories = listOf("Lab Report", "Prescription", "X-ray", "Other")

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day -> date = String.format("%04d-%02d-%02d", year, month + 1, day) },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Document", fontWeight = FontWeight.Bold) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Document Title") },
                    modifier = Modifier.fillMaxWidth()
                )

                // Category Dropdown
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    OutlinedTextField(
                        value = category,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        categories.forEach { item ->
                            DropdownMenuItem(
                                text = { Text(item) },
                                onClick = {
                                    category = item
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                // Date Picker
                OutlinedTextField(
                    value = date,
                    onValueChange = {},
                    label = { Text("Select Date") },
                    trailingIcon = {
                        Icon(
                            Icons.Default.CalendarToday,
                            contentDescription = "Pick Date",
                            modifier = Modifier.clickable { datePickerDialog.show() }
                        )
                    },
                    readOnly = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(title, category, date) },
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Update", color = Color.White)
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}