package com.example.caresync.Doctorsignup

import com.example.caresync.Screen
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun doctorSignUpScreen(navController: NavController) {
    val primaryBlue = colorResource(id = R.color.primary_button_color)
    val whiteColor = colorResource(id = R.color.white)
    val blueOutline = colorResource(id = R.color.primary_button_color)

    val auth = FirebaseAuth.getInstance()
    val firestore = FirebaseFirestore.getInstance()

    // States
    var fullName by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var phone by remember { mutableStateOf(TextFieldValue()) }
    var qualification by remember { mutableStateOf(TextFieldValue()) }
    var specialization by remember { mutableStateOf("") }
    var yearsExperience by remember { mutableStateOf(TextFieldValue()) }
    var licenseNumber by remember { mutableStateOf(TextFieldValue()) }
    var clinicName by remember { mutableStateOf(TextFieldValue()) }
    var clinicAddress by remember { mutableStateOf(TextFieldValue()) }
    var workingHours by remember { mutableStateOf(TextFieldValue()) }
    var patientCapacity by remember { mutableStateOf(TextFieldValue()) }
    var minPatients by remember { mutableStateOf(TextFieldValue()) }
    var maxPatients by remember { mutableStateOf(TextFieldValue()) }

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var specializationDropdownExpanded by remember { mutableStateOf(false) }

    val doctorCategories = listOf(
        "Cardiologist",
        "Dermatologist",
        "Neurologist",
        "Orthopedic",
        "Pediatrician",
        "Gynecologist",
        "Psychiatrist",
        "General Surgeon",
        "ENT Specialist",
        "Oncologist"
    )

    Column(modifier = Modifier.fillMaxSize()) {
        // Logo
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .background(primaryBlue)
        ) {
            Column(modifier = Modifier.padding(top = 1.dp)) {
                IconButton(
                    onClick = { navController.navigateUp() },
                    modifier = Modifier.padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = Color.White
                    )
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_doctor),
                        modifier = Modifier.size(100.dp),
                        contentDescription = "App Logo"
                    )
                    Text(
                        text = stringResource(id = R.string.Tagline),
                        color = Color.White,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Fields
        LazyColumn(
            modifier = Modifier
                .weight(0.75f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Full Name",
                    fullName,
                    { fullName = it },
                    primaryBlue,
                    blueOutline
                )
            }
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Email",
                    email,
                    { email = it },
                    primaryBlue,
                    blueOutline
                )
            }

            // Password field with toggle
            item {
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text("Password", color = primaryBlue) },
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle Password")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryBlue,
                        unfocusedBorderColor = primaryBlue
                    )
                )
            }

            // Confirm Password field with toggle
            item {
                OutlinedTextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it },
                    label = { Text("Confirm Password", color = primaryBlue) },
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        val image = if (confirmPasswordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(imageVector = image, contentDescription = "Toggle Confirm Password")
                        }
                    },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                        .background(Color.White, RoundedCornerShape(8.dp)),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = primaryBlue,
                        unfocusedBorderColor = primaryBlue
                    )
                )
            }

            item { OutlinedTextFieldWithLabel("Phone Number", phone, { phone = it }, primaryBlue, blueOutline, keyboardType = KeyboardType.Phone) }
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Qualification",
                    qualification,
                    { qualification = it },
                    primaryBlue,
                    blueOutline
                )
            }

            // Specialization as OutlinedTextField with trailing dropdown
            item {
                Box {
                    OutlinedTextField(
                        value = specialization,
                        onValueChange = { specialization = it },
                        label = { Text("Specialization", color = primaryBlue) },
                        trailingIcon = {
                            IconButton(onClick = { specializationDropdownExpanded = !specializationDropdownExpanded }) {
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = "Select Specialization")
                            }
                        },
                        readOnly = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 16.dp)
                            .background(Color.White, RoundedCornerShape(8.dp)),
                        colors = TextFieldDefaults.outlinedTextFieldColors(
                            focusedBorderColor = primaryBlue,
                            unfocusedBorderColor = primaryBlue
                        )
                    )
                    DropdownMenu(
                        expanded = specializationDropdownExpanded,
                        onDismissRequest = { specializationDropdownExpanded = false },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        doctorCategories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    specialization = category
                                    specializationDropdownExpanded = false
                                }
                            )
                        }
                    }
                }
            }

            item { OutlinedTextFieldWithLabel("Years of Experience", yearsExperience, { yearsExperience = it }, primaryBlue, primaryBlue, keyboardType = KeyboardType.Number) }
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Medical Reg. Number (Optional)",
                    licenseNumber,
                    { licenseNumber = it },
                    primaryBlue,
                    primaryBlue
                )
            }
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Clinic Name",
                    clinicName,
                    { clinicName = it },
                    primaryBlue,
                    primaryBlue
                )
            }
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Clinic Address",
                    clinicAddress,
                    { clinicAddress = it },
                    primaryBlue,
                    primaryBlue
                )
            }
            item {
                com.example.caresync.patientsignup.OutlinedTextFieldWithLabel(
                    "Working Hours / Availability",
                    workingHours,
                    { workingHours = it },
                    primaryBlue,
                    primaryBlue
                )
            }
            item { OutlinedTextFieldWithLabel("Patient Capacity", patientCapacity, { patientCapacity = it }, primaryBlue, primaryBlue, keyboardType = KeyboardType.Number) }
            item { OutlinedTextFieldWithLabel("Minimum Patients Per Day", minPatients, { minPatients = it }, primaryBlue, primaryBlue, keyboardType = KeyboardType.Number) }
            item { OutlinedTextFieldWithLabel("Maximum Patients Per Day", maxPatients, { maxPatients = it }, primaryBlue, primaryBlue, keyboardType = KeyboardType.Number) }

            // Sign Up Button
            item {
                Button(
                    onClick = {
                        if(password.text != confirmPassword.text){
                            Toast.makeText(navController.context,"Passwords do not match", Toast.LENGTH_LONG).show()
                        } else {
                            doctorSignUpUser(
                                auth,
                                firestore,
                                fullName.text,
                                email.text,
                                password.text,
                                phone.text,
                                qualification.text,
                                specialization,
                                yearsExperience.text,
                                licenseNumber.text,
                                clinicName.text,
                                clinicAddress.text,
                                workingHours.text,
                                patientCapacity.text,
                                minPatients.text,
                                maxPatients.text,
                                navController
                            )
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                        .heightIn(min = 48.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Sign Up", color = whiteColor, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextFieldWithLabel(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    labelColor: Color,
    borderColor: Color,
    isPassword: Boolean = false,
    keyboardType: KeyboardType = KeyboardType.Text
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = labelColor) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        keyboardOptions = KeyboardOptions(keyboardType = keyboardType),
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = labelColor,
            unfocusedBorderColor = borderColor
        )
    )
}

private fun doctorSignUpUser(
    auth: FirebaseAuth,
    firestore: FirebaseFirestore,
    fullName: String,
    email: String,
    password: String,
    phone: String,
    qualification: String,
    specialization: String,
    yearsExperience: String,
    licenseNumber: String,
    clinicName: String,
    clinicAddress: String,
    workingHours: String,
    patientCapacity: String,
    minPatients: String,
    maxPatients: String,
    navController: NavController
) {
    if (fullName.isEmpty() || email.isEmpty() || password.isEmpty() || phone.isEmpty() ||
        qualification.isEmpty() || specialization.isEmpty() || yearsExperience.isEmpty() ||
        clinicName.isEmpty() || clinicAddress.isEmpty() || workingHours.isEmpty() ||
        patientCapacity.isEmpty() || minPatients.isEmpty() || maxPatients.isEmpty()
    ) {
        Toast.makeText(navController.context, "Please fill all required fields", Toast.LENGTH_LONG).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener { task ->
        if (task.isSuccessful) {
            val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
            val doctorData = hashMapOf(
                "uid" to uid,
                "fullName" to fullName,
                "email" to email,
                "phone" to phone,
                "qualification" to qualification,
                "specialization" to specialization,
                "yearsExperience" to yearsExperience,
                "licenseNumber" to licenseNumber,
                "clinicName" to clinicName,
                "clinicAddress" to clinicAddress,
                "workingHours" to workingHours,
                "patientCapacity" to patientCapacity,
                "minPatients" to minPatients,
                "maxPatients" to maxPatients,
                "role" to "doctor"
            )
            firestore.collection("users").document(uid).set(doctorData).
            addOnSuccessListener {
                Toast.makeText(navController.context,
                    "Doctor registered successfully!", Toast.LENGTH_LONG).show()
                navController.navigate(Screen.loginScreen.route)
            }.addOnFailureListener {
                Toast.makeText(navController.context, "Failed to save data", Toast.LENGTH_LONG).show()
            }
        } else {
            Toast.makeText(navController.context, "Sign-up failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
        }
    }
}
