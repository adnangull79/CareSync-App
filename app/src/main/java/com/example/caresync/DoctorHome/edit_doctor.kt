package com.example.caresync.DoctorHome

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.rememberAsyncImagePainter
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.tasks.await

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditDoctorProfileScreen(onBack: () -> Unit = {}) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelColors = listOf(
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_pink),
        colorResource(id = R.color.pastel_orange),
        colorResource(id = R.color.pastel_blue)
    )

    // State for all fields
    var name by remember { mutableStateOf(TextFieldValue("")) }
    var specialization by remember { mutableStateOf(TextFieldValue("")) }
    var experience by remember { mutableStateOf(TextFieldValue("")) }
    var qualification by remember { mutableStateOf(TextFieldValue("")) }
    var phone by remember { mutableStateOf(TextFieldValue("")) }
    var email by remember { mutableStateOf(TextFieldValue("")) }
    var clinicAddress by remember { mutableStateOf(TextFieldValue("")) }
    var patientCapacity by remember { mutableStateOf(TextFieldValue("")) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    // Launcher to pick image
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        imageUri = uri
    }

    // Fetch doctor data
    LaunchedEffect(Unit) {
        val doc = firestore.collection("users").document(uid).get().await()
        if (doc.exists()) {
            name = TextFieldValue(doc.getString("name") ?: "")
            specialization = TextFieldValue(doc.getString("specialization") ?: "")
            experience = TextFieldValue(doc.getString("experience") ?: "")
            qualification = TextFieldValue(doc.getString("qualification") ?: "")
            phone = TextFieldValue(doc.getString("phone") ?: "")
            email = TextFieldValue(doc.getString("email") ?: "")
            clinicAddress = TextFieldValue(doc.getString("clinicAddress") ?: "")
            patientCapacity = TextFieldValue(doc.getString("patientCapacity") ?: "")
            existingImageUrl = doc.getString("profileImageUrl") ?: ""
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = white, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { onBack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = white)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(containerColor = primary)
            )
        }
    ) { padding ->

        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(
                    brush = Brush.verticalGradient(
                        colors = pastelColors,
                        startY = 0f,
                        endY = 1800f
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Image
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
                    .border(3.dp, primary, CircleShape)
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(imageUri ?: existingImageUrl)
                Image(
                    painter = painter,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Section headers and fields
            Text("Personal Info", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primary)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField("Full Name", name) { name = it }
            CustomTextField("Qualification", qualification) { qualification = it }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Professional Info", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primary)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField("Specialization", specialization) { specialization = it }
            CustomTextField("Experience (Years)", experience) { experience = it }
            CustomTextField("Patient Capacity", patientCapacity) { patientCapacity = it }
            CustomTextField("Clinic Address", clinicAddress) { clinicAddress = it }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Contact Info", fontWeight = FontWeight.Bold, fontSize = 20.sp, color = primary)
            Spacer(modifier = Modifier.height(8.dp))
            CustomTextField("Phone", phone) { phone = it }
            CustomTextField("Email", email) { email = it }

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = {
                    loading = true

                    val doctorData = hashMapOf(
                        "name" to name.text.trim(),
                        "qualification" to qualification.text.trim(),
                        "specialization" to specialization.text.trim(),
                        "experience" to experience.text.trim(),
                        "patientCapacity" to patientCapacity.text.trim(),
                        "clinicAddress" to clinicAddress.text.trim(),
                        "phone" to phone.text.trim(),
                        "email" to email.text.trim()
                    )

                    if (imageUri != null) {
                        val imageRef = storage.child("doctor_profile_images/$uid.jpg")
                        imageRef.putFile(imageUri!!)
                            .addOnSuccessListener {
                                imageRef.downloadUrl.addOnSuccessListener { uri ->
                                    doctorData["profileImageUrl"] = uri.toString()
                                    firestore.collection("users").document(uid)
                                        .update(doctorData as Map<String, Any>)
                                        .addOnSuccessListener {
                                            loading = false
                                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                            onBack()
                                        }
                                }
                            }
                            .addOnFailureListener {
                                loading = false
                                Toast.makeText(context, "Image upload failed!", Toast.LENGTH_SHORT).show()
                            }
                    } else {
                        firestore.collection("users").document(uid)
                            .update(doctorData as Map<String, Any>)
                            .addOnSuccessListener {
                                loading = false
                                Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                                onBack()
                            }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                if (loading) {
                    CircularProgressIndicator(color = white, strokeWidth = 2.dp)
                } else {
                    Text("Save Changes", color = white, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
fun CustomTextField(label: String, value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
    val primary = colorResource(id = R.color.primary_button_color)
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primary,
            unfocusedBorderColor = Color.LightGray
        )
    )
}
