package com.cardioconnect.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.domain.model.PatientProfile
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary

@Composable
fun PatientEditDialog(
    initialPatient: PatientProfile,
    onSave: (PatientProfile) -> Unit,
    onDismiss: () -> Unit
) {
    var fullName by remember { mutableStateOf(initialPatient.fullName) }
    var patientId by remember { mutableStateOf(initialPatient.patientId) }
    var age by remember { mutableStateOf(initialPatient.age.toString()) }
    var gender by remember { mutableStateOf(initialPatient.gender) }
    var bloodGroup by remember { mutableStateOf(initialPatient.bloodGroup) }
    var clinicianName by remember { mutableStateOf(initialPatient.clinicianName) }
    var allergies by remember { mutableStateOf(initialPatient.allergies) }
    var medications by remember { mutableStateOf(initialPatient.currentMedications) }
    var notes by remember { mutableStateOf(initialPatient.existingMedicalNotes) }

    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "Edit Patient Profile", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(scrollState),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedTextField(
                    value = fullName,
                    onValueChange = { fullName = it },
                    label = { Text("Full Name", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    OutlinedTextField(
                        value = patientId,
                        onValueChange = { patientId = it },
                        label = { Text("Patient ID", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = age,
                        onValueChange = { age = it },
                        label = { Text("Age", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.6f)
                    )
                    OutlinedTextField(
                        value = gender,
                        onValueChange = { gender = it },
                        label = { Text("Gender", fontSize = 11.sp) },
                        singleLine = true,
                        modifier = Modifier.weight(0.8f)
                    )
                }

                OutlinedTextField(
                    value = bloodGroup,
                    onValueChange = { bloodGroup = it },
                    label = { Text("Blood Group", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = clinicianName,
                    onValueChange = { clinicianName = it },
                    label = { Text("Attending Clinician", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = allergies,
                    onValueChange = { allergies = it },
                    label = { Text("Allergies", fontSize = 11.sp) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = medications,
                    onValueChange = { medications = it },
                    label = { Text("Current Medications", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Medical Notes", fontSize = 11.sp) },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val updated = initialPatient.copy(
                        fullName = fullName,
                        patientId = patientId,
                        age = age.toIntOrNull() ?: initialPatient.age,
                        gender = gender,
                        bloodGroup = bloodGroup,
                        clinicianName = clinicianName,
                        allergies = allergies,
                        currentMedications = medications,
                        existingMedicalNotes = notes
                    )
                    onSave(updated)
                },
                colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen)
            ) {
                Text("Save Changes", color = Color.Black, fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = TextSecondary)
            }
        },
        containerColor = CardDark
    )
}
