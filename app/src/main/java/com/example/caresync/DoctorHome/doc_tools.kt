// File: DoctorNotesScreen.kt
package com.example.caresync.DoctorNotes

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class DoctorNote(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val timestamp: Long = 0L
)

@Composable
fun doc_notes(navController: NavController) {
    val primary = colorResource(id = R.color.primary_button_color)
    val pastelColors = listOf(
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_pink),
        colorResource(id = R.color.pastel_orange),
        colorResource(id = R.color.pastel_blue)
    )

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()
    val uid = auth.currentUser?.uid
    val scope = rememberCoroutineScope()

    var notes by remember { mutableStateOf<List<DoctorNote>>(emptyList()) }
    var showAddEditDialog by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDetailsDialog by remember { mutableStateOf(false) }
    var editingNote by remember { mutableStateOf<DoctorNote?>(null) }
    var deleteNote by remember { mutableStateOf<DoctorNote?>(null) }
    var selectedNoteColor by remember { mutableStateOf(pastelColors[3]) }

    suspend fun loadNotes() {
        if (uid == null) return
        val snapshot = db.collection("users").document(uid)
            .collection("notes")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .get()
            .await()
        notes = snapshot.documents.mapNotNull { doc ->
            doc.toObject(DoctorNote::class.java)?.copy(id = doc.id)
        }
    }

    LaunchedEffect(Unit) {
        loadNotes()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 12.dp, end = 12.dp, top = 100.dp, bottom = 80.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // ---------- Header ----------
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Notes", fontWeight = FontWeight.Bold, fontSize = 22.sp, color = Color.Black)
            Button(
                onClick = {
                    editingNote = null
                    showAddEditDialog = true
                },
                colors = ButtonDefaults.buttonColors(containerColor = primary),
                shape = RoundedCornerShape(10.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White)
                Spacer(Modifier.width(4.dp))
                Text("Add New", color = Color.White)
            }
        }

        // ---------- Notes List ----------
        if (notes.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(top = 100.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Info,
                        contentDescription = "No Notes",
                        tint = Color.Gray,
                        modifier = Modifier.size(60.dp)
                    )
                    Spacer(Modifier.height(8.dp))
                    Text("No notes found", color = Color.Gray, fontSize = 16.sp)
                }
            }
        } else {
            notes.forEachIndexed { index, note ->
                val bgColor = pastelColors[index % pastelColors.size]
                NoteCard(
                    note = note,
                    bg = Color.White,
                    primary = primary,
                    onEdit = {
                        editingNote = note
                        showAddEditDialog = true
                    },
                    onDelete = {
                        deleteNote = note
                        showDeleteDialog = true
                    },
                    onClick = {
                        selectedNoteColor = bgColor
                        editingNote = note
                        showDetailsDialog = true
                    }
                )
            }
        }

        Spacer(Modifier.height(60.dp))
    }

    // ---------- Add/Edit Dialog ----------
    if (showAddEditDialog) {
        var title by remember { mutableStateOf(editingNote?.title ?: "") }
        var desc by remember { mutableStateOf(editingNote?.description ?: "") }

        AlertDialog(
            onDismissRequest = { showAddEditDialog = false },
            containerColor = pastelColors[3],
            confirmButton = {
                Button(
                    onClick = {
                        if (uid == null) return@Button
                        val noteData = hashMapOf(
                            "title" to title,
                            "description" to desc,
                            "timestamp" to System.currentTimeMillis()
                        )
                        val notesRef = db.collection("users").document(uid).collection("notes")
                        scope.launch {
                            if (editingNote == null) {
                                notesRef.add(noteData)
                            } else {
                                notesRef.document(editingNote!!.id).set(noteData)
                            }
                            showAddEditDialog = false
                            editingNote = null
                            loadNotes()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Save", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showAddEditDialog = false
                        editingNote = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            title = { Text(if (editingNote == null) "Add Note" else "Edit Note") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("Title") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = desc,
                        onValueChange = { desc = it },
                        label = { Text("Description") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 100.dp)
                    )
                }
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ---------- Note Details Dialog ----------
    if (showDetailsDialog && editingNote != null) {
        AlertDialog(
            onDismissRequest = { showDetailsDialog = false },
            containerColor = selectedNoteColor,
            confirmButton = {
                Button(
                    onClick = {
                        showDetailsDialog = false
                        showAddEditDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = primary)
                ) {
                    Text("Edit", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = {
                        showDetailsDialog = false
                        deleteNote = editingNote
                        showDeleteDialog = true
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            title = { Text(editingNote!!.title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
            text = {
                Text(editingNote!!.description, fontSize = 16.sp, color = Color.Black)
            },
            shape = RoundedCornerShape(12.dp)
        )
    }

    // ---------- Delete Confirmation Dialog ----------
    if (showDeleteDialog && deleteNote != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            confirmButton = {
                Button(
                    onClick = {
                        if (uid != null && deleteNote != null) {
                            scope.launch {
                                db.collection("users").document(uid)
                                    .collection("notes").document(deleteNote!!.id)
                                    .delete()
                                showDeleteDialog = false
                                deleteNote = null
                                loadNotes()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                Button(
                    onClick = { showDeleteDialog = false },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Gray)
                ) {
                    Text("Cancel", color = Color.White)
                }
            },
            title = { Text("Delete Note?") },
            text = { Text("Are you sure you want to delete this note?") },
            shape = RoundedCornerShape(12.dp)
        )
    }
}

// ---------- Reusable Note Card ----------
@Composable
fun NoteCard(
    note: DoctorNote,
    bg: Color,
    primary: Color,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = bg,
        border = BorderStroke(1.dp, primary),
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 90.dp)
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    note.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                if (note.description.isNotBlank()) {
                    Text(
                        note.description,
                        fontSize = 15.sp,
                        color = Color.DarkGray,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                IconButton(
                    onClick = onEdit,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = primary)
                }
                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}
