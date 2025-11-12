// File: DoctorCalculatorScreen.kt
package com.example.caresync.DoctorHome

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.caresync.R
import com.example.caresync.PatientHome.CalculatorType
import kotlin.math.log10
import kotlin.math.max

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DoctorCalculatorScreen(
    navController: NavController,
    type: CalculatorType
) {
    val primary = colorResource(id = R.color.primary_button_color)
    val white = colorResource(id = R.color.white)
    val ctx = LocalContext.current

    var gender by remember { mutableStateOf("Male") }
    var height by remember { mutableStateOf("") }
    var weight by remember { mutableStateOf("") }
    var age by remember { mutableStateOf("") }
    var neck by remember { mutableStateOf("") }
    var waist by remember { mutableStateOf("") }
    var hip by remember { mutableStateOf("") }

    var heightUnit by remember { mutableStateOf("cm") }
    var weightUnit by remember { mutableStateOf("kg") }
    var neckUnit by remember { mutableStateOf("cm") }
    var waistUnit by remember { mutableStateOf("cm") }
    var hipUnit by remember { mutableStateOf("cm") }

    var resultText by remember { mutableStateOf<String?>(null) }
    var adviceText by remember { mutableStateOf<String?>(null) }

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

                    // 🧮 Calculate only (no save)
                    Button(
                        modifier = Modifier.fillMaxWidth(),
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

                            resultText = centeredResultText(v, unit, type)
                            adviceText = adviceFor(type, v, gender)
                        },
                        enabled = canCalculate,
                        colors = ButtonDefaults.buttonColors(containerColor = primary)
                    ) {
                        Text("Calculate", color = white)
                    }

                    // 🧾 Results
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

// --- Reusable UI parts ---
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

@Composable
private fun LabeledNumberField(label: String, value: String, onValueChange: (String) -> Unit, primary: androidx.compose.ui.graphics.Color) {
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

@Composable
private fun UnitNumberField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    unitAbbr: String,
    onUnitChange: (String) -> Unit,
    unitOptions: List<Pair<String, String>>,
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
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
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

// --- Utility Functions (same as patient) ---
@Composable
private fun pastelFor(type: CalculatorType) = when (type) {
    CalculatorType.BMI -> colorResource(id = R.color.pastel_green)
    CalculatorType.BMR -> colorResource(id = R.color.pastel_pink)
    CalculatorType.WATER -> colorResource(id = R.color.pastel_blue)
    CalculatorType.BODY_FAT -> colorResource(id = R.color.pastel_yellow)
}

private fun titleFor(type: CalculatorType) = when (type) {
    CalculatorType.BMI -> "BMI"
    CalculatorType.BMR -> "BMR"
    CalculatorType.WATER -> "Water Intake"
    CalculatorType.BODY_FAT -> "Body Fat %"
}

private fun fullFormFor(type: CalculatorType) = when (type) {
    CalculatorType.BMI -> "Body Mass Index"
    CalculatorType.BMR -> "Basal Metabolic Rate"
    CalculatorType.WATER -> "Daily Water Intake"
    CalculatorType.BODY_FAT -> "Body Fat Percentage"
}

private fun descriptionFor(type: CalculatorType) = when (type) {
    CalculatorType.BMI -> "BMI estimates your body fat using your height and weight."
    CalculatorType.BMR -> "BMR shows calories your body needs at rest."
    CalculatorType.WATER -> "Recommended daily water intake based on weight."
    CalculatorType.BODY_FAT -> "Estimates body fat percentage using body measurements."
}

private fun centeredResultText(value: Double, unit: String, type: CalculatorType) =
    "Result: ${"%.1f".format(value)} $unit"

private fun numbersOnly(s: String): String {
    var out = s.filter { it.isDigit() || it == '.' }
    val firstDot = out.indexOf('.')
    if (firstDot != -1) out = out.substring(0, firstDot + 1) + out.substring(firstDot + 1).replace(".", "")
    return out
}

private fun digitsOnly(s: String): String = s.filter { it.isDigit() }

private fun toCm(value: Double, unit: String): Double = when (unit.lowercase()) {
    "cm" -> value
    "m" -> value * 100.0
    "ft" -> value * 30.48
    "in" -> value * 2.54
    else -> value
}

private fun toKg(value: Double, unit: String): Double = when (unit.lowercase()) {
    "kg" -> value
    "g" -> value / 1000.0
    "lb" -> value * 0.45359237
    else -> value
}

private fun computeBmi(heightCm: Double, weightKg: Double): Double {
    val hM = (heightCm / 100.0).coerceAtLeast(0.0001)
    return weightKg / (hM * hM)
}

private fun computeBmr(heightCm: Double, weightKg: Double, ageYears: Int, gender: String): Double {
    val base = 10 * weightKg + 6.25 * heightCm - 5 * ageYears
    return if (gender.equals("Male", true)) base + 5 else base - 161
}

private fun computeWaterLiters(weightKg: Double): Double = weightKg * 0.033

private fun computeBodyFatPercent(
    heightCm: Double,
    neckCm: Double,
    waistCm: Double,
    hipCm: Double?,
    gender: String
): Double {
    val h = max(heightCm, 0.0001)
    return if (gender.equals("Male", true)) {
        495.0 / (1.0324 - 0.19077 * log10(max(waistCm - neckCm, 0.0001)) + 0.15456 * log10(h)) - 450.0
    } else {
        val hip = hipCm ?: 0.0
        495.0 / (1.29579 - 0.35004 * log10(max(waistCm + hip - neckCm, 0.0001)) + 0.22100 * log10(h)) - 450.0
    }
}

private fun adviceFor(type: CalculatorType, value: Double, gender: String): String {
    return when (type) {
        CalculatorType.BMI -> when {
            value < 18.5 -> "Underweight: increase calories."
            value < 25.0 -> "Healthy range: maintain balance."
            value < 30.0 -> "Overweight: more activity advised."
            else -> "Obese: gradual lifestyle changes recommended."
        }
        CalculatorType.BMR -> "This is your base calorie need."
        CalculatorType.WATER -> "Drink water throughout the day."
        CalculatorType.BODY_FAT -> if (gender.equals("Male", true)) {
            when {
                value < 6 -> "Very lean."
                value < 14 -> "Athletic."
                value < 18 -> "Fit."
                value < 25 -> "Average."
                else -> "High — gradual fat loss advised."
            }
        } else {
            when {
                value < 14 -> "Very lean."
                value < 21 -> "Athletic."
                value < 25 -> "Fit."
                value < 32 -> "Average."
                else -> "High — gradual fat loss advised."
            }
        }
    }
}
