package com.cardioconnect.presentation.patients

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorHeart
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
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
import com.cardioconnect.domain.model.PatientProfile
import com.cardioconnect.presentation.MainViewModel
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun PatientsScreen(
    viewModel: MainViewModel,
    onNavigateToProfile: (String) -> Unit,
    onNavigateToMonitor: () -> Unit
) {
    val allPatients by viewModel.allPatients.collectAsState()
    val activePatient by viewModel.activePatient.collectAsState()
    val isRecording by viewModel.isRecording.collectAsState()

    var searchQuery by remember { mutableStateOf("") }
    var showAddDialog by remember { mutableStateOf(false) }
    var pendingPatientToSwitch by remember { mutableStateOf<PatientProfile?>(null) }

    val filteredPatients = remember(allPatients, searchQuery) {
        if (searchQuery.isBlank()) allPatients
        else {
            val q = searchQuery.trim().lowercase()
            allPatients.filter {
                it.fullName.lowercase().contains(q) ||
                it.patientId.lowercase().contains(q) ||
                it.phoneNumber.lowercase().contains(q) ||
                it.clinicianName.lowercase().contains(q)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(horizontal = 16.dp, vertical = 12.dp)
    ) {
        // Top Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Patients",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${allPatients.size} Registered Patients • Active: ${activePatient.fullName}",
                    color = TextSecondary,
                    fontSize = 11.5.sp
                )
            }

            Button(
                onClick = { showAddDialog = true },
                colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                shape = RoundedCornerShape(10.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.Black, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Patient", color = Color.Black, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Search by name, ID, phone or doctor...", color = TextTertiary, fontSize = 13.sp) },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search", tint = TextTertiary, modifier = Modifier.size(18.dp))
            },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = PhosphorGreen,
                unfocusedBorderColor = CardBorderDark,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary,
                focusedContainerColor = CardDark,
                unfocusedContainerColor = CardDark
            )
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Patient Cards List
        if (filteredPatients.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Person,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (searchQuery.isBlank()) "No patients found" else "No matching patients for \"$searchQuery\"",
                        color = TextSecondary,
                        fontSize = 14.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(bottom = 24.dp)
            ) {
                items(filteredPatients, key = { it.patientId }) { patient ->
                    val isActive = patient.patientId == activePatient.patientId
                    PatientDirectoryCard(
                        patient = patient,
                        isActive = isActive,
                        onMonitorClick = {
                            if (isActive) {
                                onNavigateToMonitor()
                            } else {
                                if (isRecording) {
                                    pendingPatientToSwitch = patient
                                } else {
                                    viewModel.setActivePatient(patient.patientId)
                                    onNavigateToMonitor()
                                }
                            }
                        },
                        onViewProfileClick = {
                            onNavigateToProfile(patient.patientId)
                        }
                    )
                }
            }
        }
    }

    // Add Patient Dialog
    if (showAddDialog) {
        val suggestedId = viewModel.getNextSuggestedPatientId()
        val existingIds = allPatients.map { it.patientId }
        AddPatientDialog(
            suggestedId = suggestedId,
            existingPatientIds = existingIds,
            onSavePatient = { newPatient ->
                viewModel.addPatient(newPatient)
            },
            onDismiss = { showAddDialog = false }
        )
    }

    // Active Recording Safety Guard Dialog
    pendingPatientToSwitch?.let { targetPatient ->
        PatientSafetyGuardDialog(
            currentPatient = activePatient,
            targetPatient = targetPatient,
            onConfirmStopAndSwitch = {
                viewModel.stopRecordingAndSwitchPatient(targetPatient.patientId) {
                    pendingPatientToSwitch = null
                    onNavigateToMonitor()
                }
            },
            onDismiss = { pendingPatientToSwitch = null }
        )
    }
}

@Composable
private fun PatientDirectoryCard(
    patient: PatientProfile,
    isActive: Boolean,
    onMonitorClick: () -> Unit,
    onViewProfileClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardDark),
        border = BorderStroke(1.dp, if (isActive) PhosphorGreen.copy(alpha = 0.5f) else CardBorderDark),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // Top Row: Avatar + Name/ID + Status Badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .background(
                                if (isActive) PhosphorGreen.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = getInitials(patient.fullName),
                            color = if (isActive) PhosphorGreen else TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        )
                    }

                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = patient.fullName,
                                color = TextPrimary,
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold
                            )
                            if (isActive) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = "Active",
                                    tint = PhosphorGreen,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                        Text(
                            text = "ID: ${patient.patientId} • ${patient.age} yrs • ${patient.gender}",
                            color = TextTertiary,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.5.sp
                        )
                    }
                }

                // Active / Inactive Badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = if (isActive) PhosphorGreen.copy(alpha = 0.15f) else Color.White.copy(alpha = 0.04f),
                    border = BorderStroke(1.dp, if (isActive) PhosphorGreen.copy(alpha = 0.4f) else CardBorderDark)
                ) {
                    Text(
                        text = if (isActive) "ACTIVE" else "IDLE",
                        color = if (isActive) PhosphorGreen else TextTertiary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sub-info Row: Last ECG & Total Sessions & Blood Group
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White.copy(alpha = 0.02f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = TextTertiary,
                        modifier = Modifier.size(13.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Last ECG: ${formatLastEcg(patient.lastEcgTimestampMs)}",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )
                }

                Text(
                    text = "Blood: ${patient.bloodGroup} • ${patient.totalSessions} Sessions",
                    color = TextTertiary,
                    fontSize = 11.sp
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Monitor & View Profile
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onMonitorClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) PhosphorGreen else Color.White.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.MonitorHeart,
                        contentDescription = null,
                        tint = if (isActive) Color.Black else PhosphorGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isActive) "Live Monitor" else "Monitor Patient",
                        color = if (isActive) Color.Black else TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onViewProfileClick,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.weight(1f),
                    contentPadding = PaddingValues(vertical = 6.dp)
                ) {
                    Text(
                        text = "View Profile",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
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

private fun formatLastEcg(timestamp: Long?): String {
    if (timestamp == null || timestamp <= 0L) return "Never"
    val diffMs = System.currentTimeMillis() - timestamp
    val hours = diffMs / 3600000L
    return when {
        hours < 1 -> "Just now"
        hours < 24 -> "Today (${hours}h ago)"
        hours < 48 -> "Yesterday"
        else -> SimpleDateFormat("dd MMM, hh:mm a", Locale.getDefault()).format(Date(timestamp))
    }
}
