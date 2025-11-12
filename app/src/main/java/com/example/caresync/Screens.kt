package com.example.caresync

sealed class Screen(val route: String) {
    object SplashScreen : Screen("splashscrren")
    object onboardingScreenOne : Screen("OnboardingScreenOne")
    object onboardingScreentwo : Screen("OnboardingScreentwo")
    object onboardingScreenthree : Screen("OnboardingScreenthree")
    object RegisterScreen : Screen("regScreen")
    object signupScreen : Screen("signUpScreen")
    object loginScreen : Screen("loginScreen")
    object userhome : Screen("userhome")
    object RegisterScreen2 : Screen("regScreen2")
    object doctorsignup : Screen("doctorSignUpScreen")
    object doctorhome : Screen("doctorhome")
    object Apointment : Screen("AppointmentScreen")
    object documents : Screen("DocumentScreen")
    object Tools : Screen("ToolsScreen")
    object Calculatorscreen : Screen("HealthCalculatorScreen")

    object DoctorCalculatorScreen : Screen("DoctorCalculatorScreen/{type}") {
        fun passType(type: String) = "DoctorCalculatorScreen/$type"
    }
    object AboutScreen : Screen("AboutScreen")
    object DoctorSettings : Screen("doctor_settings_screen")
    object ContactScreen : Screen("contact_screen")
    object DoctorFAQScreen : Screen("doctor_faq_screen")
    object DoctorProfileScreen : Screen("doctor_profile_screen")
    object EditDoctorProfile : Screen("edit_doctor_profile")
    object PatientProfileScreen : Screen("patientProfileScreen")
    object PatientFAQScreen : Screen("patientFAQScreen")
    object EditPatientProfileScreen : Screen("edit_patient_profile_screen")
    object ChatScreen : Screen("chat_screen")
    object PatientSettingsScreen : Screen("patient_settings_screen")
    object PatientAppointments : Screen("patient_appointments")
    object HealthTipsScreen : Screen("health_tips_screen")
    object DoctorDetail : Screen("doctor_detail/{doctorId}") {
        fun createRoute(doctorId: String) = "doctor_detail/$doctorId"
    }

    object HealthArticlesScreen : Screen("health_articles_screen")
    object NotificationScreen : Screen("notifications")
    object HealthArticleDetailScreen : Screen("health_article_detail_screen/{articleJson}") {
        fun createRoute(articleJson: String) = "health_article_detail_screen/$articleJson"
    }






}
