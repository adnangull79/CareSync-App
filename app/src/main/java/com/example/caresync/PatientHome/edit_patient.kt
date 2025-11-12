package com.example.caresync.PatientHome

import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
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
import androidx.navigation.NavController
import coil.compose.rememberAsyncImagePainter
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PatientProfileEditScreen(navController: NavController) {

    val context = LocalContext.current
    val firestore = FirebaseFirestore.getInstance()
    val storage = FirebaseStorage.getInstance().reference
    val auth = FirebaseAuth.getInstance()
    val uid = auth.currentUser?.uid ?: return

    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val pastelColors = listOf(
        colorResource(id = R.color.pastel_green),
        colorResource(id = R.color.pastel_blue),
        colorResource(id = R.color.pastel_orange),
        colorResource(id = R.color.pastel_pink)
    )

    var patientName by remember { mutableStateOf(TextFieldValue("")) }
    var patientEmail by remember { mutableStateOf("example@email.com") }
    var patientImageUri by remember { mutableStateOf<Uri?>(null) }
    var existingImageUrl by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    // Fetch patient info
    LaunchedEffect(uid) {
        firestore.collection("users").document(uid).get()
            .addOnSuccessListener { snap ->
                val firstName = snap.getString("firstName") ?: ""
                val lastName = snap.getString("lastName") ?: ""
                patientName = TextFieldValue("$firstName $lastName")
                patientEmail = snap.getString("email") ?: "example@email.com"
                existingImageUrl = snap.getString("profileImageUrl") ?: ""
            }
    }

    // Image picker
    val imagePickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        patientImageUri = uri
    }

    // Update profile logic
    fun updateProfile() {
        if (isLoading) return
        isLoading = true

        val updatedData = hashMapOf(
            "firstName" to patientName.text.split(" ").first(),
            "lastName" to patientName.text.split(" ").last(),
            "email" to patientEmail
        )

        if (patientImageUri != null) {
            val imageRef = storage.child("patient_profile_images/$uid.jpg")
            imageRef.putFile(patientImageUri!!).addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    updatedData["profileImageUrl"] = uri.toString()
                    firestore.collection("users").document(uid).update(updatedData as Map<String, Any>)
                        .addOnSuccessListener {
                            isLoading = false
                            Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        }
                        .addOnFailureListener {
                            isLoading = false
                            Toast.makeText(context, "Profile update failed!", Toast.LENGTH_SHORT).show()
                        }
                }
            }
        } else {
            firestore.collection("users").document(uid).update(updatedData as Map<String, Any>)
                .addOnSuccessListener {
                    isLoading = false
                    Toast.makeText(context, "Profile Updated!", Toast.LENGTH_SHORT).show()
                    navController.popBackStack()
                }
                .addOnFailureListener {
                    isLoading = false
                    Toast.makeText(context, "Profile update failed!", Toast.LENGTH_SHORT).show()
                }
        }
    }

    // UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Edit Profile", color = white, fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
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
                        endY = 1600f
                    )
                )
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            // Profile Image
            Box(
                modifier = Modifier
                    .size(130.dp)
                    .clip(CircleShape)
                    .border(3.dp, primary, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                val painter = rememberAsyncImagePainter(patientImageUri ?: existingImageUrl)
                Image(
                    painter = painter,
                    contentDescription = "Profile Image",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Pencil icon BELOW the image
            Spacer(modifier = Modifier.height(10.dp))
            TextButton(
                onClick = { imagePickerLauncher.launch("image/*") },
                modifier = Modifier
                    .background(primary, shape = RoundedCornerShape(50))
                    .border(2.dp, white, RoundedCornerShape(50))
                    .height(36.dp)
                    .padding(horizontal = 10.dp)
            ) {
                Text(
                    text = "Update",
                    color = white,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }


            Spacer(modifier = Modifier.height(20.dp))

            // Section Header
            Text(
                "Personal Info",
                fontWeight = FontWeight.Bold,
                fontSize = 20.sp,
                color = primary
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Name Field
            CustomPatientTextField("Full Name", patientName) { patientName = it }

            // Email (non-editable)
            OutlinedTextField(
                value = patientEmail,
                onValueChange = {},
                enabled = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp),
                shape = RoundedCornerShape(12.dp),
                label = { Text("Email") },
                trailingIcon = {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Email",
                        tint = Color.Gray
                    )
                },
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = primary,
                    unfocusedBorderColor = Color.LightGray
                )
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = { updateProfile() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(55.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primary)
            ) {
                if (isLoading) {
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
fun CustomPatientTextField(label: String, value: TextFieldValue, onValueChange: (TextFieldValue) -> Unit) {
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
