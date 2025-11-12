package com.example.caresync.patientsignup

import com.example.caresync.Screen
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.input.*
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import com.example.caresync.R

@Composable
fun signUpScreen(navController: NavController) {
    val primaryBlue = colorResource(id = R.color.primary_button_color)
    val whiteColor = colorResource(id = R.color.white)
    val blueOutline = colorResource(id = R.color.border_background_color)

    var firstName by remember { mutableStateOf(TextFieldValue()) }
    var lastName by remember { mutableStateOf(TextFieldValue()) }
    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var confirmPassword by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val db = FirebaseFirestore.getInstance()

    Column(modifier = Modifier.fillMaxSize()) {
        // Top logo section with back button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(0.25f)
                .background(primaryBlue)
        ) {
            Column(modifier = Modifier.padding(top = 1.dp)){
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
                        painter = painterResource(id = R.drawable.ic_patinet),
                        modifier = Modifier.size(150.dp),
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

        // Form section
        Column(
            modifier = Modifier
                .fillMaxSize()
                .weight(0.75f)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextFieldWithLabel("First Name", firstName, { firstName = it }, primaryBlue, primaryBlue)
            OutlinedTextFieldWithLabel("Last Name", lastName, { lastName = it }, primaryBlue, primaryBlue)
            OutlinedTextFieldWithLabel("Email", email, { email = it }, primaryBlue, primaryBlue)

            // Password with visibility toggle
            OutlinedTextFieldWithVisibility(
                label = "Password",
                value = password,
                onValueChange = { password = it },
                labelColor = primaryBlue,
                borderColor = primaryBlue,
                visible = passwordVisible,
                onVisibilityChange = { passwordVisible = it }
            )

            // Confirm Password with visibility toggle
            OutlinedTextFieldWithVisibility(
                label = "Confirm Password",
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                labelColor = primaryBlue,
                borderColor = primaryBlue,
                visible = confirmPasswordVisible,
                onVisibilityChange = { confirmPasswordVisible = it }
            )

            Button(
                onClick = {
                    signUpUser(auth, db, email.text, password.text, confirmPassword.text, firstName.text, lastName.text, navController)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp)
                    .heightIn(min = 48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Sign Up", color = whiteColor)
            }

            TextButton(
                onClick = { navController.navigate(Screen.loginScreen.route) },
                modifier = Modifier.padding(top = 16.dp)
            ) {
                Text("Already have an account? Login", color = primaryBlue)
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
    isPassword: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = labelColor) },
        visualTransformation = if (isPassword) PasswordVisualTransformation() else VisualTransformation.None,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(color = Color.White, shape = RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = labelColor,
            unfocusedBorderColor = borderColor,
            focusedLabelColor = labelColor,
            unfocusedLabelColor = labelColor,
        )
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextFieldWithVisibility(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    labelColor: Color,
    borderColor: Color,
    visible: Boolean,
    onVisibilityChange: (Boolean) -> Unit
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label, color = labelColor) },
        visualTransformation = if (visible) VisualTransformation.None else PasswordVisualTransformation(),
        trailingIcon = {
            val icon = if (visible) Icons.Default.Visibility else Icons.Default.VisibilityOff
            IconButton(onClick = { onVisibilityChange(!visible) }) {
                Icon(icon, contentDescription = "Toggle Password Visibility")
            }
        },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
            .background(Color.White, RoundedCornerShape(8.dp)),
        colors = TextFieldDefaults.outlinedTextFieldColors(
            focusedBorderColor = labelColor,
            unfocusedBorderColor = borderColor
        ),
        singleLine = true
    )
}

// Sign up function with Firestore storage
private fun signUpUser(
    auth: FirebaseAuth,
    db: FirebaseFirestore,
    email: String,
    password: String,
    confirmPassword: String,
    firstName: String,
    lastName: String,
    navController: NavController
) {
    if (email.isEmpty() || password.isEmpty()
        || confirmPassword.isEmpty()
        || firstName.isEmpty() || lastName.isEmpty()) {
        Toast.makeText(navController.context, "Please fill in all fields", Toast.LENGTH_LONG).show()
        return
    }

    if (password != confirmPassword) {
        Toast.makeText(navController.context, "Passwords do not match", Toast.LENGTH_LONG).show()
        return
    }

    auth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener
                val userData = hashMapOf(
                    "uid" to uid,
                    "firstName" to firstName,
                    "lastName" to lastName,
                    "email" to email,
                    "role" to "patient"
                )
                db.collection("users").document(uid).set(userData)
                    .addOnSuccessListener {
                        Toast.makeText(navController.context,
                            "Registration Successful", Toast.LENGTH_LONG).show()
                        navController.navigate(Screen.loginScreen.route)
                    }
                    .addOnFailureListener {
                        Toast.makeText(navController.context,
                            "Failed to save user data: ${it.message}",
                            Toast.LENGTH_LONG).show()
                    }
            } else {
                Toast.makeText(navController.context,
                    "Sign-up failed: ${task.exception?.message}",
                    Toast.LENGTH_LONG).show()
            }
        }
}
