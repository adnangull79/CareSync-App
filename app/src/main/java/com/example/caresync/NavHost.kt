package com.example.caresync

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.caresync.DoctorHome.AboutScreen
import com.example.caresync.DoctorHome.ContactScreen
import com.example.caresync.DoctorHome.DoctorCalculatorScreen
import com.example.caresync.DoctorHome.DoctorFAQScreen
import com.example.caresync.DoctorHome.DoctorNotificationsScreen
import com.example.caresync.DoctorHome.DoctorProfileScreen
import com.example.caresync.DoctorHome.DoctorSettingsScreen
import com.example.caresync.DoctorHome.EditDoctorProfileScreen
import com.example.caresync.DoctorHome.doctorhome
import com.example.caresync.Doctorsignup.doctorSignUpScreen
import com.example.caresync.Health_Article.HealthArticle
import com.example.caresync.Health_Article.HealthArticleDetailScreen
import com.example.caresync.Health_Article.HealthArticlesScreen
import com.example.caresync.PatientHome.AppointmentScreen
import com.example.caresync.PatientHome.CalculatorType
import com.example.caresync.PatientHome.DoctorDetailScreen
import com.example.caresync.PatientHome.DocumentScreen

import com.example.caresync.PatientHome.HealthCalculatorScreen
import com.example.caresync.PatientHome.HealthTipsScreen
import com.example.caresync.PatientHome.ImageViewScreen
import com.example.caresync.PatientHome.PatientAppointmentsScreen
import com.example.caresync.PatientHome.PatientFAQScreen
import com.example.caresync.PatientHome.ToolsScreen
import com.example.caresync.StartOfApp.OnboardingScreenthree
import com.example.caresync.StartOfApp.loginScreen
import com.example.caresync.StartOfApp.regScreen2
import com.example.caresync.StartOfApp.splashscrren
import com.example.caresync.PatientHome.PatientHomeScreen
import com.example.caresync.PatientHome.PatientProfileEditScreen
import com.example.caresync.PatientHome.PatientProfileScreen
import com.example.caresync.PatientHome.PatientSettingsScreen
import com.example.caresync.chatbot.ChatScreen
import com.example.caresync.notifications.NotificationScreen
import com.example.caresync.patientsignup.signUpScreen
import com.google.gson.Gson


@Composable
fun navigation() {
    val navcontroller = rememberNavController()

    NavHost(navController = navcontroller, startDestination = "splashscrren") {

        composable(Screen.SplashScreen.route) {
            splashscrren(navController = navcontroller)
        }
        composable(Screen.onboardingScreenOne.route) {
            OnboardingScreenOne(navController = navcontroller)
        }
        composable(Screen.onboardingScreentwo.route) {
            OnboardingScreentwo(navController = navcontroller)
        }
        composable(Screen.onboardingScreenthree.route) {
            OnboardingScreenthree(navController = navcontroller)
        }
        composable(Screen.RegisterScreen.route) {
            regScreen(navController = navcontroller)
        }
        composable(Screen.RegisterScreen2.route) {
            regScreen2(navController = navcontroller)
        }
        composable(Screen.doctorsignup.route) {
            doctorSignUpScreen(navController = navcontroller)
        }
        composable("signupScreen") {
            signUpScreen(navController = navcontroller)
        }
        composable("loginScreen") {
            loginScreen(navController = navcontroller)
        }
        composable("userhome") {
            PatientHomeScreen(navController = navcontroller)
        }
        composable("doctorhome") {
            doctorhome(navController = navcontroller)
        }
        composable("appointmentScreen") {
            AppointmentScreen(navController = navcontroller)
        }
        composable("documentScreen") {
            DocumentScreen(navController = navcontroller)
        }
        composable("toolsScreen") {
            ToolsScreen(navController = navcontroller)
        }
        composable("doctor_notifications") {
            DoctorNotificationsScreen(navcontroller)
        }


        // ✅ HealthCalculator Screen
        composable("HealthCalculatorScreen") {
            HealthCalculatorScreen(navController = navcontroller, type = CalculatorType.BMI)
        }
        composable(
            route = "HealthCalculatorScreen/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeArg = backStackEntry.arguments?.getString("type") ?: "BMI"
            val type =
                runCatching { CalculatorType.valueOf(typeArg) }.getOrElse { CalculatorType.BMI }
            HealthCalculatorScreen(navController = navcontroller, type = type)
        }

        composable(
            route = "DoctorCalculatorScreen/{type}",
            arguments = listOf(navArgument("type") { type = NavType.StringType })
        ) { backStackEntry ->
            val typeArg = backStackEntry.arguments?.getString("type") ?: "BMI"
            val calcType =
                runCatching { CalculatorType.valueOf(typeArg) }.getOrElse { CalculatorType.BMI }
            DoctorCalculatorScreen(navController = navcontroller, type = calcType)
        }

        composable(Screen.AboutScreen.route) {
            AboutScreen(navController = navcontroller)
        }
        composable("contact_screen") {
            ContactScreen(navController = navcontroller)
        }
        composable(Screen.DoctorSettings.route) {
            DoctorSettingsScreen(navController = navcontroller)
        }
        composable(Screen.DoctorFAQScreen.route) {
            DoctorFAQScreen(navController = navcontroller)
        }
        composable(Screen.DoctorProfileScreen.route) {
            DoctorProfileScreen(navController = navcontroller)
        }
        composable(Screen.EditDoctorProfile.route) {
            EditDoctorProfileScreen(
                onBack = { navcontroller.popBackStack() }
            )
        }
        composable(Screen.PatientProfileScreen.route) {
            PatientProfileScreen(navController = navcontroller)
        }
        composable(Screen.PatientFAQScreen.route) {
            PatientFAQScreen(navController = navcontroller)
        }

        composable(Screen.EditPatientProfileScreen.route) {
            PatientProfileEditScreen(navController = navcontroller)
        }
        composable("chat_screen") {
            ChatScreen(navController = navcontroller)
        }

        //health article:
        composable(Screen.HealthArticlesScreen.route) {
            HealthArticlesScreen(navController =navcontroller)
        }
        composable(Screen.PatientSettingsScreen.route) {
            PatientSettingsScreen(navController = navcontroller)
        }
        composable(route = Screen.PatientAppointments.route) {
            PatientAppointmentsScreen(navController = navcontroller)
        }

        composable(Screen.HealthTipsScreen.route) {
            HealthTipsScreen(navController = navcontroller)
        }
        composable(route = Screen.NotificationScreen.route) {
            NotificationScreen(navController = navcontroller)
        }

        composable("healthArticleDetail/{url}") { backStackEntry ->
            val url = backStackEntry.arguments?.getString("url") ?: ""
            HealthArticleDetailScreen(navController = navcontroller, articleUrl = url)
        }

        composable(
            route = Screen.DoctorDetail.route,
            arguments = listOf(navArgument("doctorId") { type = NavType.StringType })
        ) { backStackEntry ->
            val doctorId = backStackEntry.arguments?.getString("doctorId") ?: ""
            DoctorDetailScreen(navController = navcontroller, doctorId = doctorId)
        }


        // ✅ New Full-Screen Zoomable Image Viewer
        composable(
            route = "imageView/{imageUrl}/{imageTitle}",
            arguments = listOf(
                navArgument("imageUrl") { type = NavType.StringType },
                navArgument("imageTitle") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val imageUrl = backStackEntry.arguments?.getString("imageUrl") ?: ""
            val imageTitle = backStackEntry.arguments?.getString("imageTitle") ?: ""

            ImageViewScreen(
                imageUrl = imageUrl,
                imageTitle = imageTitle,
                onBackClick = { navcontroller.popBackStack() } // ✅ fixed here
            )
        }
    }
}

