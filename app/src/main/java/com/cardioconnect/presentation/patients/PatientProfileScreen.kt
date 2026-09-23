package com.cardioconnect.presentation.patients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.components.PatientEditDialog
import com.cardioconnect.ui.theme.BackgroundDark
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
fun PatientProfileScreen(
    patientId: String,
    viewModel: MainViewModel,
    onBackClick: () -> Unit,
    onNavigateToMonitor: () -> Unit
) {
    val allPatients by viewModel.allPatients.collectAsState()
    val activePatient by viewModel.activePatient.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    val patient = allPatients.firstOrNull { it.patientId == patientId } ?: activePatient

    val isActive = patient.patientId == activePatient.patientId
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmDialog by remember { mutableStateOf(false) }
    var showRecordingGuardDialog by remember { mutableStateOf(false) }

    val heightMeters = patient.heightCm / 100f
    val bmi = if (heightMeters > 0 && patient.weightKg > 0) {
        patient.weightKg / (heightMeters * heightMeters)
    } else 0f

    val bmiCategory = when {
        bmi == 0f -> "N/A"
        bmi < 18.5f -> "Underweight"
        bmi < 25f -> "Normal weight"
        bmi < 30f -> "Overweight"
        else -> "Obese"
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
    ) {
        // App Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Text(
                text = "Patient Profile",
                color = TextPrimary,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.weight(1f))
            IconButton(onClick = { showEditDialog = true }) {
                Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = PhosphorGreen)
            }
        }

        // Scrollable Body
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Patient Header Card
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, if (isActive) PhosphorGreen.copy(alpha = 0.5f) else CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(56.dp)
                                    .background(
                                        if (isActive) PhosphorGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.08f),
                                        CircleShape
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = getInitials(patient.fullName),
                                    color = if (isActive) PhosphorGreen else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                )
                            }

                            Column {
                                Text(
                                    text = patient.fullName,
                                    color = TextPrimary,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "ID: ${patient.patientId}",
                                    color = TextTertiary,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = if (isActive) PhosphorGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                            border = BorderStroke(1.dp, if (isActive) PhosphorGreen.copy(alpha = 0.4f) else CardBorderDark)
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = null,
                                        tint = PhosphorGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                }
                                Text(
                                    text = if (isActive) "ACTIVE PATIENT" else "IDLE",
                                    color = if (isActive) PhosphorGreen else TextTertiary,
                                    fontSize = 10.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Quick Action: Monitor
                    Button(
                        onClick = {
                            if (isActive) {
                                onNavigateToMonitor()
                            } else {
                                if (isRecording) {
                                    showRecordingGuardDialog = true
                                } else {
                                    viewModel.setActivePatient(patient.patientId)
                                    onNavigateToMonitor()
                                }
                            }
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isActive) PhosphorGreen else Color.White.copy(alpha = 0.08f)
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(vertical = 10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MonitorHeart,
                            contentDescription = null,
                            tint = if (isActive) Color.Black else PhosphorGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (isActive) "Open Live ECG Monitor" else "Set as Active & Monitor",
                            color = if (isActive) Color.Black else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                }
            }

            // Physical & Body Metrics Grid
            Text(
                text = "PHYSICAL & VITALS METRICS",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(title = "Age", value = "${patient.age} yrs", sub = patient.dateOfBirth, modifier = Modifier.weight(1f))
                MetricMiniCard(title = "Gender", value = patient.gender, sub = "Biological", modifier = Modifier.weight(1f))
                MetricMiniCard(title = "Blood Group", value = patient.bloodGroup, sub = "ABO typing", modifier = Modifier.weight(1f))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                MetricMiniCard(
                    title = "Height",
                    value = if (patient.heightCm > 0) "${patient.heightCm.toInt()} cm" else "—",
                    sub = "Standing",
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Weight",
                    value = if (patient.weightKg > 0) "${patient.weightKg.toInt()} kg" else "—",
                    sub = "Scale",
                    modifier = Modifier.weight(1f)
                )
                MetricMiniCard(
                    title = "Body Mass Index",
                    value = if (bmi > 0) String.format(Locale.getDefault(), "%.1f", bmi) else "—",
                    sub = bmiCategory,
                    valueColor = if (bmi in 18.5..24.9) PhosphorGreen else if (bmi >= 25.0) MedicalAmber else TextPrimary,
                    modifier = Modifier.weight(1f)
                )
            }

            // Emergency & Contact Info
            Text(
                text = "COMMUNICATION & EMERGENCY",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.Phone, contentDescription = null, tint = HospitalBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Phone Number:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = patient.phoneNumber, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.WarningAmber, contentDescription = null, tint = MedicalAmber, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(text = "Emergency Contact:", color = TextSecondary, fontSize = 12.sp)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(text = patient.emergencyContact, color = TextPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Clinical Record
            Text(
                text = "CLINICAL PROFILE",
                color = TextTertiary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold
            )

            Card(
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ClinicalItemRow(
                        icon = Icons.Default.LocalHospital,
                        title = "Attending Clinician / Doctor",
                        detail = patient.clinicianName,
                        tint = PhosphorGreen
                    )

                    ClinicalItemRow(
                        icon = Icons.Default.Notes,
                        title = "Existing Medical Notes / Diagnoses",
                        detail = patient.existingMedicalNotes,
                        tint = HospitalBlue
                    )

                    ClinicalItemRow(
                        icon = Icons.Default.Medication,
                        title = "Current Medications",
                        detail = patient.currentMedications,
                        tint = MedicalAmber
                    )

                    ClinicalItemRow(
                        icon = Icons.Default.WarningAmber,
                        title = "Allergies",
                        detail = patient.allergies,
                        tint = EmergencyRed
                    )
                }
            }

            // Danger Zone: Delete Patient (Only if more than 1 patient exists)
            if (allPatients.size > 1) {
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(
                    onClick = { showDeleteConfirmDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    border = BorderStroke(1.dp, EmergencyRed.copy(alpha = 0.5f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = EmergencyRed, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Delete Patient Profile", color = EmergencyRed, fontSize = 12.5.sp)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Edit Patient Dialog
    if (showEditDialog) {
        PatientEditDialog(
            initialPatient = patient,
            onSave = { updated ->
                viewModel.updatePatient(updated)
                showEditDialog = false
            },
            onDismiss = { showEditDialog = false }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmDialog) {
        androidx.compose.material3.AlertDialog(
            onDismissRequest = { showDeleteConfirmDialog = false },
            title = { Text("Delete Patient Profile", color = TextPrimary) },
            text = {
                Text(
                    "Are you sure you want to delete ${patient.fullName} (${patient.patientId})? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deletePatient(patient.patientId)
                        showDeleteConfirmDialog = false
                        onBackClick()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = EmergencyRed)
                ) {
                    Text("Delete", color = Color.White)
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirmDialog = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = CardDark
        )
    }

    // Recording Guard Dialog
    if (showRecordingGuardDialog) {
        PatientSafetyGuardDialog(
            currentPatient = activePatient,
            targetPatient = patient,
            onConfirmStopAndSwitch = {
                viewModel.stopRecordingAndSwitchPatient(patient.patientId) {
                    showRecordingGuardDialog = false
                    onNavigateToMonitor()
                }
            },
            onDismiss = { showRecordingGuardDialog = false }
        )
    }
}

@Composable
private fun MetricMiniCard(
    title: String,
    value: String,
    sub: String,
    valueColor: Color = TextPrimary,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = CardDark,
        border = BorderStroke(1.dp, CardBorderDark),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(10.dp)
        ) {
            Text(text = title, color = TextTertiary, fontSize = 10.5.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = valueColor, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = sub, color = TextTertiary, fontSize = 10.sp)
        }
    }
}

@Composable
private fun ClinicalItemRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    detail: String,
    tint: Color
) {
    Column {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(text = title, color = TextSecondary, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = detail.ifBlank { "Not recorded" },
            color = TextPrimary,
            fontSize = 13.sp,
            lineHeight = 18.sp,
            modifier = Modifier.padding(start = 20.dp)
        )
    }
}

private fun getInitials(name: String): String {
    val parts = name.trim().split(" ")
    return if (parts.size >= 2) {
        val first = parts[0].firstOrNull() ?: 'P'
        val second = parts[1].firstOrNull() ?: ' '
        "$first$second".trim().uppercase()
    } else {
        (parts.firstOrNull()?.take(2) ?: "P").uppercase()
    }
}
