package com.example.caresync.StartOfApp

import com.example.caresync.Screen
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun loginScreen(navController: NavController) {
    val primaryBlue = colorResource(id = R.color.primary_button_color)
    val whiteColor = colorResource(id = R.color.white)
    val blueOutline = colorResource(id = R.color.border_background_color)

    var email by remember { mutableStateOf(TextFieldValue()) }
    var password by remember { mutableStateOf(TextFieldValue()) }
    var passwordVisible by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val auth = FirebaseAuth.getInstance()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    var showForgotPasswordDialog by remember { mutableStateOf(false) }

    Scaffold(snackbarHost = { SnackbarHost(hostState = snackbarHostState) }) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top section with back button, logo, tagline
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(0.3f)
                        .background(primaryBlue)
                        .padding(16.dp)
                ) {
                    // Back button
                    IconButton(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier.align(Alignment.TopStart)
                    ) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Back",
                            tint = whiteColor
                        )
                    }

                    // Centered logo & tagline
                    Column(
                        modifier = Modifier.align(Alignment.Center),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.whitelogo),
                            modifier = Modifier.size(100.dp),
                            contentDescription = "App Logo"
                        )
                        Text(
                            text = stringResource(id = R.string.Tagline),
                            color = whiteColor,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Form section
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(0.7f)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextFieldWithLabellogin("Email", email, { email = it }, primaryBlue, primaryBlue)

                    OutlinedTextFieldWithVisibilityLogin(
                        label = "Password",
                        value = password,
                        onValueChange = { password = it },
                        labelColor = primaryBlue,
                        borderColor = primaryBlue,
                        visible = passwordVisible,
                        onVisibilityChange = { passwordVisible = it }
                    )

                    // Forgot Password
                    Text(
                        text = "Forgot Password?",
                        color = primaryBlue,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clickable { showForgotPasswordDialog = true }
                    )

                    // Log In Button
                    Button(
                        onClick = {
                            isLoading = true
                            loginUser(auth, email.text, password.text, navController) {
                                isLoading = false
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .heightIn(min = 48.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = primaryBlue),
                        shape = RoundedCornerShape(8.dp),
                        enabled = !isLoading
                    ) {
                        if (isLoading) {
                            CircularProgressIndicator(
                                color = whiteColor,
                                strokeWidth = 2.dp,
                                modifier = Modifier.size(20.dp)
                            )
                        } else {
                            Text(text = "Log In", color = whiteColor)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Don't have an account? Sign Up",
                        style = TextStyle(color = primaryBlue, fontSize = 14.sp, fontWeight = FontWeight.SemiBold),
                        modifier = Modifier.clickable {
                            navController.navigate(Screen.RegisterScreen2.route)
                        }
                    )
                }
            }

            // Loading overlay
            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f)),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = primaryBlue)
                }
            }
        }
    }

    // Forgot Password Dialog
    if (showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = { showForgotPasswordDialog = false },
            title = { Text("Reset Password") },
            text = {
                Text("Click 'Send Email' to receive a password reset link. " +
                        "If you don't see it within a few minutes, please check your Spam/Junk folder.")
            },
            confirmButton = {
                TextButton(onClick = {
                    if (email.text.isBlank()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please enter your email first.")
                        }
                    } else {
                        auth.sendPasswordResetEmail(email.text)
                            .addOnCompleteListener { task ->
                                scope.launch {
                                    snackbarHostState.showSnackbar(
                                        if (task.isSuccessful)
                                            "Reset link sent to ${email.text}. If it doesn't arrive soon, check your Spam/Junk folder."
                                        else
                                            "Failed to send reset link. Please check your email and try again."
                                    )
                                }
                            }
                    }
                    showForgotPasswordDialog = false
                }) {
                    Text("Send Email", color = primaryBlue)
                }
            },
            dismissButton = {
                TextButton(onClick = { showForgotPasswordDialog = false }) {
                    Text("Cancel", color = primaryBlue)
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutlinedTextFieldWithLabellogin(
    label: String,
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    labelColor: Color,
    borderColor: Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(text = label, color = labelColor) },
        visualTransformation = VisualTransformation.None,
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
fun OutlinedTextFieldWithVisibilityLogin(
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

private fun loginUser(
    auth: FirebaseAuth,
    email: String,
    password: String,
    navController: NavController,
    onComplete: () -> Unit
) {
    if (email.isEmpty() || password.isEmpty()) {
        Toast.makeText(navController.context, "Please fill in all fields", Toast.LENGTH_LONG).show()
        onComplete()
        return
    }

    auth.signInWithEmailAndPassword(email, password)
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val db = FirebaseFirestore.getInstance()
                val uid = auth.currentUser?.uid ?: return@addOnCompleteListener

                db.collection("users").document(uid).get()
                    .addOnSuccessListener { document ->
                        val role = document.getString("role")

                        // Clear backstack and navigate to home
                        if (role == "doctor") {
                            navController.navigate(Screen.doctorhome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        } else {
                            navController.navigate(Screen.userhome.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                        onComplete()
                    }
                    .addOnFailureListener {
                        Toast.makeText(navController.context, "Failed to fetch user role", Toast.LENGTH_LONG).show()
                        onComplete()
                    }

            } else {
                Toast.makeText(navController.context, "Login failed: ${task.exception?.message}", Toast.LENGTH_LONG).show()
                onComplete()
            }
        }
}