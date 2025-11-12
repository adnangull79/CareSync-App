// File: HealthCalculatorScreen.kt
package com.example.caresync.PatientHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
// 1) Imports (top of HealthCalculatorScreen.kt)
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.caresync.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlin.math.log10
import kotlin.math.max

enum class CalculatorType { BMI, BMR, WATER, BODY_FAT }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HealthCalculatorScreen(
    navController: NavController,
    type: CalculatorType
) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)

    val auth = remember { FirebaseAuth.getInstance() }
    val db = remember { FirebaseFirestore.getInstance() }
    val uid = auth.currentUser?.uid

    // ---- Inputs ----
    var gender by remember { mutableStateOf("Male") } // BMR & Body Fat
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var neck by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }

    // Unit state (store abbreviation for calc, show full names in dropdown)
    var heightUnit by remember { mutableStateOf("cm") } // cm | m | ft
    var weightUnit by remember { mutableStateOf("kg") } // kg | g | lb
    var neckUnit by remember { mutableStateOf("cm") }   // cm | in
    var waistUnit by remember { mutableStateOf("cm") }  // cm | in
    var hipUnit by remember { mutableStateOf("cm") }    // cm | in

    var lastValue by remember { mutableStateOf<Double?>(null) }
    var lastUnit by remember { mutableStateOf<String?>(null) }
    var resultText by remember { mutableStateOf<String?>(null) }
    var adviceText by remember { mutableStateOf<String?>(null) }
    var isSaving by remember { mutableStateOf(false) }
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(titleFor(type), color = white) },
                navigationIcon = {
                    IconButton(onClick = { navController.navigateUp() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = white)
                    }
                },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = primary,
                    titleContentColor = white
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = fullFormFor(type),
                color = primary,
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center
            )

            Surface(
                shape = MaterialTheme.shapes.large,
                color = pastelFor(type),
                border = BorderStroke(1.dp, primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = descriptionFor(type),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodyMedium
                    )

                    if (type == CalculatorType.BMR || type == CalculatorType.BODY_FAT) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            SelectChip(text = "Male", selected = gender == "Male", primary = primary) { gender = "Male" }
                            SelectChip(text = "Female", selected = gender == "Female", primary = primary) { gender = "Female" }
                        }
                    }

                    if (type != CalculatorType.WATER) {
                        UnitNumberField(
                            label = "Height",
                            value = height,
                            onValueChange = { height = numbersOnly(it) },
                            unitAbbr = heightUnit,
                            onUnitChange = { heightUnit = it },
                            unitOptions = listOf(
                                "Centimeters" to "cm",
                                "Meters" to "m",
                                "Feet" to "ft"
                            ),
                            primary = primary
                        )
                    }

                    if (type == CalculatorType.BMR || type == CalculatorType.BMI || type == CalculatorType.WATER) {
                        UnitNumberField(
                            label = "Weight",
                            value = weight,
                            onValueChange = { weight = numbersOnly(it) },
                            unitAbbr = weightUnit,
                            onUnitChange = { weightUnit = it },
                            unitOptions = listOf(
                                "Kilograms" to "kg",
                                "Grams" to "g",
                                "Pounds" to "lb"
                            ),
                            primary = primary
                        )
                    }

                    if (type == CalculatorType.BMR) {
                        LabeledNumberField(
                            label = "Age (years)",
                            value = age,
                            onValueChange = { age = digitsOnly(it) },
                            primary = primary
                        )
                    }

                    if (type == CalculatorType.BODY_FAT) {
                        UnitNumberField(
                            label = "Neck circumference",
                            value = neck,
                            onValueChange = { neck = numbersOnly(it) },
                            unitAbbr = neckUnit,
                            onUnitChange = { neckUnit = it },
                            unitOptions = listOf("Centimeters" to "cm", "Inches" to "in"),
                            primary = primary
                        )
                        UnitNumberField(
                            label = "Waist circumference",
                            value = waist,
                            onValueChange = { waist = numbersOnly(it) },
                            unitAbbr = waistUnit,
                            onUnitChange = { waistUnit = it },
                            unitOptions = listOf("Centimeters" to "cm", "Inches" to "in"),
                            primary = primary
                        )
                        if (gender == "Female") {
                            UnitNumberField(
                                label = "Hip circumference",
                                value = hip,
                                onValueChange = { hip = numbersOnly(it) },
                                unitAbbr = hipUnit,
                                onUnitChange = { hipUnit = it },
                                unitOptions = listOf("Centimeters" to "cm", "Inches" to "in"),
                                primary = primary
                            )
                        }
                    }

                    val canCalculate = when (type) {
                        CalculatorType.BMI -> height.isNotBlank() && weight.isNotBlank()
                        CalculatorType.BMR -> height.isNotBlank() && weight.isNotBlank() && age.isNotBlank()
                        CalculatorType.WATER -> weight.isNotBlank()
                        CalculatorType.BODY_FAT -> {
                            val base = height.isNotBlank() && neck.isNotBlank() && waist.isNotBlank()
                            if (gender == "Female") base && hip.isNotBlank() else base
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val heightCm = if (type != CalculatorType.WATER)
                                    toCm(height.toDoubleOrNull() ?: 0.0, heightUnit) else 0.0
                                val weightKg = if (type == CalculatorType.BMR || type == CalculatorType.BMI || type == CalculatorType.WATER)
                                    toKg(weight.toDoubleOrNull() ?: 0.0, weightUnit) else 0.0
                                val neckCm = if (type == CalculatorType.BODY_FAT)
                                    toCm(neck.toDoubleOrNull() ?: 0.0, neckUnit) else 0.0
                                val waistCm = if (type == CalculatorType.BODY_FAT)
                                    toCm(waist.toDoubleOrNull() ?: 0.0, waistUnit) else 0.0
                                val hipCm = if (type == CalculatorType.BODY_FAT && gender == "Female")
                                    toCm(hip.toDoubleOrNull() ?: 0.0, hipUnit) else null

                                val (v, unit) = when (type) {
                                    CalculatorType.BMI -> computeBmi(heightCm, weightKg) to "kg/m²"
                                    CalculatorType.BMR -> computeBmr(heightCm, weightKg, age.toIntOrNull() ?: 0, gender) to "kcal/day"
                                    CalculatorType.WATER -> computeWaterLiters(weightKg) to "L/day"
                                    CalculatorType.BODY_FAT -> computeBodyFatPercent(heightCm, neckCm, waistCm, hipCm, gender) to "%"
                                }

                                lastValue = v
                                lastUnit = unit
                                resultText = centeredResultText(v, unit, type)
                                adviceText = adviceFor(type, v, gender)
                            },
                            enabled = canCalculate,
                            colors = ButtonDefaults.buttonColors(containerColor = primary)
                        ) {
                            Text("Calculate", color = white)
                        }

                        OutlinedButton(
                            modifier = Modifier.weight(1f),
                            onClick = {
                                val value = lastValue ?: return@OutlinedButton
                                val unit = lastUnit ?: return@OutlinedButton
                                if (uid == null) return@OutlinedButton

                                val inputsMap = when (type) {
                                    CalculatorType.BMI -> mapOf(
                                        "height" to height, "heightUnit" to heightUnit, "height_cm" to toCm(height.toDoubleOrNull() ?: 0.0, heightUnit),
                                        "weight" to weight, "weightUnit" to weightUnit, "weight_kg" to toKg(weight.toDoubleOrNull() ?: 0.0, weightUnit)
                                    )
                                    CalculatorType.BMR -> mapOf(
                                        "height" to height, "heightUnit" to heightUnit, "height_cm" to toCm(height.toDoubleOrNull() ?: 0.0, heightUnit),
                                        "weight" to weight, "weightUnit" to weightUnit, "weight_kg" to toKg(weight.toDoubleOrNull() ?: 0.0, weightUnit),
                                        "age_years" to (age.toIntOrNull() ?: 0), "gender" to gender
                                    )
                                    CalculatorType.WATER -> mapOf(
                                        "weight" to weight, "weightUnit" to weightUnit, "weight_kg" to toKg(weight.toDoubleOrNull() ?: 0.0, weightUnit)
                                    )
                                    CalculatorType.BODY_FAT -> mapOf(
                                        "gender" to gender,
                                        "height" to height, "heightUnit" to heightUnit, "height_cm" to toCm(height.toDoubleOrNull() ?: 0.0, heightUnit),
                                        "neck" to neck, "neckUnit" to neckUnit, "neck_cm" to toCm(neck.toDoubleOrNull() ?: 0.0, neckUnit),
                                        "waist" to waist, "waistUnit" to waistUnit, "waist_cm" to toCm(waist.toDoubleOrNull() ?: 0.0, waistUnit),
                                        "hip" to if (gender=="Female") hip else null,
                                        "hipUnit" to if (gender=="Female") hipUnit else null,
                                        "hip_cm" to if (gender=="Female") toCm(hip.toDoubleOrNull() ?: 0.0, hipUnit) else null
                                    )
                                }

                                isSaving = true
                                FirebaseFirestore.getInstance()
                                    .collection("users").document(uid)
                                    .collection("calculators").document(type.name)
                                    .set(
                                        hashMapOf(
                                            "type" to type.name,
                                            "result" to value,
                                            "unit" to unit,
                                            "version" to 1,
                                            "updatedAt" to FieldValue.serverTimestamp()
                                        )
                                    )
                                    .addOnSuccessListener {
                                        isSaving = false
                                        Toast.makeText(ctx, "Saved successfully ✅", Toast.LENGTH_SHORT).show()
                                    }
                                    .addOnFailureListener { e ->
                                        isSaving = false
                                        Toast.makeText(ctx, "Save failed: ${e.message}", Toast.LENGTH_SHORT).show()
                                    }
                            },
                            enabled = lastValue != null && !isSaving,
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = colorResource(id = R.color.white)),
                            border = BorderStroke(1.dp, primary)
                        ) {
                            Text("Save", color = primary)
                        }
                    }

                    if (resultText != null) {
                        Text(
                            text = resultText!!,
                            style = MaterialTheme.typography.titleLarge,
                            textAlign = TextAlign.Center
                        )
                    }
                    if (!adviceText.isNullOrBlank()) {
                        Text(
                            text = adviceText!!,
                            style = MaterialTheme.typography.bodyMedium,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// ---- Chips ----
@Composable
private fun SelectChip(text: String, selected: Boolean, primary: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(text) },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = primary.copy(alpha = 0.18f),
            selectedLabelColor = primary
        )
    )
}

// ---- Primary-colored numeric field ----
@Composable
private fun LabeledNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    primary: androidx.compose.ui.graphics.Color
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        label = { Text(label) },
        singleLine = true,
        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
        modifier = Modifier.fillMaxWidth(),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = primary,
            unfocusedBorderColor = primary.copy(alpha = 0.7f),
            cursorColor = primary,
            focusedLabelColor = primary
        )
    )
}

// ---- Numeric field with unit dropdown (full names in menu, abbrev in field) ----
@Composable
private fun UnitNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unitAbbr: String,
    onUnitChange: (String) -> Unit,
    unitOptions: List<Pair<String, String>>, // ("Centimeters" to "cm")
    primary: androidx.compose.ui.graphics.Color
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            label = { Text(label) },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            trailingIcon = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(unitAbbr)
                    IconButton(onClick = { expanded = true }) {
                        Icon(Icons.Default.ArrowDropDown, contentDescription = "Choose unit")
                    }
                }
            },
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = primary,
                unfocusedBorderColor = primary.copy(alpha = 0.7f),
                cursorColor = primary,
                focusedLabelColor = primary
            )
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            unitOptions.forEach { (full, abbr) ->
                DropdownMenuItem(
                    text = { Text(full) },
                    onClick = {
                        onUnitChange(abbr)
                        expanded = false
                    }
                )
            }
        }
    }
}

// ---- Color helpers ----
@Composable
private fun pastelFor(type: CalculatorType) = when (type) {
    CalculatorType.BMI -> colorResource(id = R.color.pastel_green)
    CalculatorType.BMR -> colorResource(id = R.color.pastel_pink)
    CalculatorType.WATER -> colorResource(id = R.color.pastel_blue)
    CalculatorType.BODY_FAT -> colorResource(id = R.color.pastel_yellow)
}

// ---- Strings ----
private fun titleFor(type: CalculatorType): String = when (type) {
    CalculatorType.BMI -> "BMI"
    CalculatorType.BMR -> "BMR"
    CalculatorType.WATER -> "Water Intake"
    CalculatorType.BODY_FAT -> "Body Fat %"
}

private fun fullFormFor(type: CalculatorType): String = when (type) {
    CalculatorType.BMI -> "Body Mass Index"
    CalculatorType.BMR -> "Basal Metabolic Rate"
    CalculatorType.WATER -> "Daily Water Intake"
    CalculatorType.BODY_FAT -> "Body Fat Percentage"
}

private fun descriptionFor(type: CalculatorType): String = when (type) {
    CalculatorType.BMI ->
        "BMI estimates your body fat using your height and weight. It’s a quick screening tool to understand whether your weight is in a healthy range for your height."
    CalculatorType.BMR ->
        "BMR is the number of calories your body needs at rest to keep vital functions running. It helps you plan daily calorie targets for weight loss, gain, or maintenance."
    CalculatorType.WATER ->
        "Your recommended daily water intake is estimated from your body weight. Increase fluids during hot weather, illness, or workouts."
    CalculatorType.BODY_FAT ->
        "This uses the U.S. Navy method with body measurements to estimate your body fat percentage, a better indicator of composition than weight alone."
}

private fun centeredResultText(value: Double, unit: String, type: CalculatorType): String {
    return when (type) {
        CalculatorType.BMR -> "Result: ${"%.0f".format(value)} $unit"
        CalculatorType.WATER -> "Result: ${"%.1f".format(value)} $unit"
        CalculatorType.BMI -> "Result: ${"%.1f".format(value)} $unit"
        CalculatorType.BODY_FAT -> "Body Fat: ${"%.1f".format(value)} $unit"
    }
}

private fun adviceFor(type: CalculatorType, value: Double, gender: String): String {
    return when (type) {
        CalculatorType.BMI -> when {
            value < 18.5 -> "You are underweight. Consider a balanced diet to gain weight."
            value < 25 -> "Your weight is normal. Maintain your healthy lifestyle."
            value < 30 -> "You are overweight. Regular exercise and a balanced diet are recommended."
            else -> "Obese. Seek guidance for weight management."
        }
        CalculatorType.BMR -> "This is your daily calorie requirement at rest."
        CalculatorType.WATER -> "Drink adequate water daily for optimal health."
        CalculatorType.BODY_FAT -> when (gender) {
            "Male" -> when {
                value < 6 -> "Too low body fat. May affect health."
                value < 24 -> "Healthy range."
                value < 31 -> "Overfat. Consider exercise and diet."
                else -> "Obese. Seek professional guidance."
            }
            "Female" -> when {
                value < 16 -> "Too low body fat. May affect health."
                value < 31 -> "Healthy range."
                value < 36 -> "Overfat. Consider exercise and diet."
                else -> "Obese. Seek professional guidance."
            }
            else -> ""
        }
    }
}

// ---- Conversions ----
private fun toKg(value: Double, unit: String) = when (unit) {
    "kg" -> value
    "g" -> value / 1000
    "lb" -> value / 2.205
    else -> value
}

// ✅ Fixed feet.inches parsing
private fun toCm(value: Double, unit: String) = when (unit) {
    "cm" -> value
    "m" -> value * 100
    "ft" -> {
        val feet = value.toInt()
        val inches = ((value - feet) * 100).coerceIn(0.0, 11.99) // 0.10 → 10 inches
        feet * 30.48 + inches * 2.54
    }
    else -> value
}

// ---- Helpers ----
private fun digitsOnly(input: String) = input.filter { it.isDigit() }
private fun numbersOnly(input: String) = input.filter { it.isDigit() || it == '.' }

// ---- Calculations ----
private fun computeBmi(heightCm: Double, weightKg: Double) = weightKg / ((heightCm / 100).let { it * it })

private fun computeBmr(heightCm: Double, weightKg: Double, age: Int, gender: String): Double {
    return if (gender == "Male")
        66.47 + 13.75 * weightKg + 5.003 * heightCm - 6.755 * age
    else
        655.1 + 9.563 * weightKg + 1.850 * heightCm - 4.676 * age
}

private fun computeWaterLiters(weightKg: Double) = weightKg * 0.033

private fun computeBodyFatPercent(heightCm: Double, neckCm: Double, waistCm: Double, hipCm: Double?, gender: String): Double {
    return if (gender == "Male") {
        495 / (1.0324 - 0.19077 * log10(waistCm - neckCm) + 0.15456 * log10(heightCm)) - 450
    } else {
        hipCm ?: 0.0
        495 / (1.29579 - 0.35004 * log10(waistCm + (hipCm ?: 0.0) - neckCm) + 0.22100 * log10(heightCm)) - 450
    }
}
