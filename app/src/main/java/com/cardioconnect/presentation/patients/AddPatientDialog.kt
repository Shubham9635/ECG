package com.cardioconnect.presentation.patients

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.cardioconnect.domain.model.PatientProfile
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.EmergencyRed
import com.cardioconnect.ui.theme.MedicalAmber
import com.cardioconnect.ui.theme.HospitalBlue
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary
import java.util.Locale

@Composable
fun AddPatientDialog(
    suggestedId: String,
    existingPatientIds: List<String>,
    onSavePatient: (PatientProfile) -> Boolean,
    onDismiss: () -> Unit
) {
    var patientId by remember { mutableStateOf(suggestedId) }
    var fullName by remember { mutableStateOf("") }
    var ageStr by remember { mutableStateOf("") }
    var dob by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Male") }
    var heightStr by remember { mutableStateOf("") }
    var weightStr by remember { mutableStateOf("") }
    var bloodGroup by remember { mutableStateOf("B+") }
    var phone by remember { mutableStateOf("") }
    var emergencyContact by remember { mutableStateOf("") }
    var medicalNotes by remember { mutableStateOf("") }
    var medications by remember { mutableStateOf("") }
    var allergies by remember { mutableStateOf("") }
    var clinicianName by remember { mutableStateOf("Dr. Amit Verma") }

    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Live BMI calculation
    val heightVal = heightStr.toFloatOrNull() ?: 0f
    val weightVal = weightStr.toFloatOrNull() ?: 0f
    val bmi = if (heightVal > 0 && weightVal > 0) {
        val hMeters = heightVal / 100f
        weightVal / (hMeters * hMeters)
    } else 0f

    val genders = listOf("Male", "Female", "Other")
    val bloodGroups = listOf("A+", "A-", "B+", "B-", "AB+", "AB-", "O+", "O-")

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = CardDark),
            border = androidx.compose.foundation.BorderStroke(1.dp, CardBorderDark),
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = RoundedCornerShape(10.dp),
                            color = PhosphorGreen.copy(alpha = 0.15f),
                            modifier = Modifier.padding(end = 10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PersonAdd,
                                contentDescription = null,
                                tint = PhosphorGreen,
                                modifier = Modifier.padding(8.dp)
                            )
                        }
                        Column {
                            Text(
                                text = "Add New Patient",
                                color = TextPrimary,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Register patient for ECG monitoring",
                                color = TextTertiary,
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextTertiary)
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable Form Body
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 440.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (errorMessage != null) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = EmergencyRed.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.4f)),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = errorMessage ?: "",
                                color = EmergencyRed,
                                fontSize = 12.sp,
                                modifier = Modifier.padding(10.dp)
                            )
                        }
                    }

                    // Patient ID (with auto-suggest indicator)
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = patientId,
                            onValueChange = {
                                patientId = it
                                errorMessage = null
                            },
                            label = { Text("Patient ID *") },
                            placeholder = { Text("e.g. P-10248") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhosphorGreen,
                                unfocusedBorderColor = CardBorderDark,
                                focusedLabelColor = PhosphorGreen,
                                unfocusedLabelColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = fullName,
                            onValueChange = {
                                fullName = it
                                errorMessage = null
                            },
                            label = { Text("Full Name *") },
                            placeholder = { Text("e.g. John Doe") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhosphorGreen,
                                unfocusedBorderColor = CardBorderDark,
                                focusedLabelColor = PhosphorGreen,
                                unfocusedLabelColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Age & Gender
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = ageStr,
                            onValueChange = { ageStr = it.filter { ch -> ch.isDigit() } },
                            label = { Text("Age (yrs) *") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhosphorGreen,
                                unfocusedBorderColor = CardBorderDark,
                                focusedLabelColor = PhosphorGreen,
                                unfocusedLabelColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = dob,
                            onValueChange = { dob = it },
                            label = { Text("Date of Birth") },
                            placeholder = { Text("DD Mon YYYY") },
                            modifier = Modifier.weight(1.5f),
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhosphorGreen,
                                unfocusedBorderColor = CardBorderDark,
                                focusedLabelColor = PhosphorGreen,
                                unfocusedLabelColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )
                    }

                    // Gender Selector
                    Text("Gender *", color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        genders.forEach { g ->
                            val selected = gender.equals(g, ignoreCase = true)
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) PhosphorGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                                border = androidx.compose.foundation.BorderStroke(
                                    1.dp,
                                    if (selected) PhosphorGreen else CardBorderDark
                                ),
                                onClick = { gender = g },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = g,
                                        color = if (selected) PhosphorGreen else TextSecondary,
                                        fontSize = 12.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Physical Info: Height, Weight & Calculated BMI
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = heightStr,
                            onValueChange = { heightStr = it },
                            label = { Text("Height (cm)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhosphorGreen,
                                unfocusedBorderColor = CardBorderDark,
                                focusedLabelColor = PhosphorGreen,
                                unfocusedLabelColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        OutlinedTextField(
                            value = weightStr,
                            onValueChange = { weightStr = it },
                            label = { Text("Weight (kg)") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PhosphorGreen,
                                unfocusedBorderColor = CardBorderDark,
                                focusedLabelColor = PhosphorGreen,
                                unfocusedLabelColor = TextTertiary,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary
                            )
                        )

                        // BMI Preview Badge
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .height(56.dp)
                                .background(Color.White.copy(alpha = 0.04f), RoundedCornerShape(8.dp))
                                .padding(horizontal = 8.dp, vertical = 6.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Calculated BMI", color = TextTertiary, fontSize = 9.sp)
                            Text(
                                text = if (bmi > 0) String.format(Locale.getDefault(), "%.1f", bmi) else "—",
                                color = if (bmi in 18.5..24.9) PhosphorGreen else if (bmi >= 25.0) MedicalAmber else TextSecondary,
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }
                    }

                    // Blood Group
                    Text("Blood Group", color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bloodGroups.take(4).forEach { bg ->
                            val selected = bloodGroup == bg
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selected) HospitalBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) HospitalBlue else CardBorderDark),
                                onClick = { bloodGroup = bg },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = bg,
                                        color = if (selected) HospitalBlue else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        bloodGroups.drop(4).forEach { bg ->
                            val selected = bloodGroup == bg
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (selected) HospitalBlue.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                                border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) HospitalBlue else CardBorderDark),
                                onClick = { bloodGroup = bg },
                                modifier = Modifier.weight(1f)
                            ) {
                                Box(modifier = Modifier.padding(vertical = 6.dp), contentAlignment = Alignment.Center) {
                                    Text(
                                        text = bg,
                                        color = if (selected) HospitalBlue else TextSecondary,
                                        fontSize = 11.sp,
                                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal
                                    )
                                }
                            }
                        }
                    }

                    // Contact Info
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Phone Number") },
                        placeholder = { Text("+91 98765 00000") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhosphorGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = PhosphorGreen,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = emergencyContact,
                        onValueChange = { emergencyContact = it },
                        label = { Text("Emergency Contact") },
                        placeholder = { Text("Name and contact number") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhosphorGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = PhosphorGreen,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    // Clinical Fields
                    OutlinedTextField(
                        value = medicalNotes,
                        onValueChange = { medicalNotes = it },
                        label = { Text("Existing Medical Notes / Diagnoses") },
                        placeholder = { Text("Hypertension, prior CAD, pacemaker, etc.") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 3,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhosphorGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = PhosphorGreen,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = medications,
                        onValueChange = { medications = it },
                        label = { Text("Current Medications") },
                        placeholder = { Text("Beta-blockers, ACE inhibitors, Statins...") },
                        modifier = Modifier.fillMaxWidth(),
                        maxLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhosphorGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = PhosphorGreen,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = allergies,
                        onValueChange = { allergies = it },
                        label = { Text("Allergies") },
                        placeholder = { Text("Penicillin, Sulfa, Latex...") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhosphorGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = PhosphorGreen,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )

                    OutlinedTextField(
                        value = clinicianName,
                        onValueChange = { clinicianName = it },
                        label = { Text("Attending Clinician / Doctor") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = PhosphorGreen,
                            unfocusedBorderColor = CardBorderDark,
                            focusedLabelColor = PhosphorGreen,
                            unfocusedLabelColor = TextTertiary,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Cancel", color = TextSecondary)
                    }

                    Button(
                        onClick = {
                            val trimmedId = patientId.trim()
                            val trimmedName = fullName.trim()
                            val parsedAge = ageStr.toIntOrNull()

                            if (trimmedId.isEmpty()) {
                                errorMessage = "Patient ID cannot be blank."
                                return@Button
                            }
                            if (trimmedName.isEmpty()) {
                                errorMessage = "Patient full name is required."
                                return@Button
                            }
                            if (parsedAge == null || parsedAge <= 0) {
                                errorMessage = "Please enter a valid age."
                                return@Button
                            }
                            if (existingPatientIds.any { it.equals(trimmedId, ignoreCase = true) }) {
                                errorMessage = "Patient ID '$trimmedId' already exists. Please choose a unique ID."
                                return@Button
                            }

                            val newPatient = PatientProfile(
                                patientId = trimmedId,
                                fullName = trimmedName,
                                age = parsedAge,
                                dateOfBirth = dob.ifBlank { "Unspecified" },
                                gender = gender,
                                heightCm = heightStr.toFloatOrNull() ?: 0f,
                                weightKg = weightStr.toFloatOrNull() ?: 0f,
                                bloodGroup = bloodGroup,
                                phoneNumber = phone.ifBlank { "Unspecified" },
                                emergencyContact = emergencyContact.ifBlank { "Unspecified" },
                                existingMedicalNotes = medicalNotes.ifBlank { "None recorded" },
                                currentMedications = medications.ifBlank { "None recorded" },
                                allergies = allergies.ifBlank { "None reported" },
                                clinicianName = clinicianName.ifBlank { "Unassigned" },
                                totalSessions = 0,
                                lastEcgTimestampMs = 0L
                            )

                            val success = onSavePatient(newPatient)
                            if (!success) {
                                errorMessage = "Failed to save patient. Duplicate ID detected."
                            } else {
                                onDismiss()
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                        modifier = Modifier.weight(1.5f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, tint = Color.Black)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save & Select", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
