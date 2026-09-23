package com.cardioconnect.presentation.onboarding

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BluetoothSearching
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.ui.components.MedicalSafetyNotice
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.ElectricCyan
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary

data class OnboardingPageData(
    val title: String,
    val description: String,
    val icon: ImageVector,
    val accentColor: Color
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit
) {
    val currentPage = remember { mutableIntStateOf(0) }

    val pages = listOf(
        OnboardingPageData(
            title = "Connect Your ECG Device",
            description = "Connect compatible wireless medical sensors and ECG hardware securely via Bluetooth Low Energy.",
            icon = Icons.Default.BluetoothSearching,
            accentColor = ElectricCyan
        ),
        OnboardingPageData(
            title = "Monitor in Real Time",
            description = "View hospital-grade scrolling ECG waveforms with standard millimeter grid calibration, instantaneous heart rate, SpO₂, and rhythm data.",
            icon = Icons.Default.Favorite,
            accentColor = PhosphorGreen
        ),
        OnboardingPageData(
            title = "Save Your Sessions",
            description = "Record cardiac telemetry sessions, inspect rhythm history with pan/zoom playback, and export clinical PDF & CSV reports.",
            icon = Icons.Default.PictureAsPdf,
            accentColor = PhosphorGreen
        )
    )

    val currentData = pages[currentPage.intValue]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .padding(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Icon Bubble
            Box(
                modifier = Modifier
                    .size(110.dp)
                    .background(currentData.accentColor.copy(alpha = 0.12f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = currentData.icon,
                    contentDescription = null,
                    tint = currentData.accentColor,
                    modifier = Modifier.size(54.dp)
                )
            }

            Spacer(modifier = Modifier.height(36.dp))

            Text(
                text = currentData.title,
                color = TextPrimary,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = currentData.description,
                color = TextSecondary,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Page Indicator Dots
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                pages.indices.forEach { index ->
                    val isActive = index == currentPage.intValue
                    Box(
                        modifier = Modifier
                            .size(if (isActive) 24.dp else 8.dp, 8.dp)
                            .background(
                                color = if (isActive) PhosphorGreen else TextTertiary.copy(alpha = 0.4f),
                                shape = RoundedCornerShape(4.dp)
                            )
                    )
                }
            }

            Spacer(modifier = Modifier.height(36.dp))

            MedicalSafetyNotice(modifier = Modifier.padding(horizontal = 8.dp))
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (currentPage.intValue > 0) {
                TextButton(onClick = { currentPage.intValue-- }) {
                    Text(text = "Back", color = TextSecondary, fontSize = 15.sp)
                }
            } else {
                TextButton(onClick = onFinished) {
                    Text(text = "Skip", color = TextTertiary, fontSize = 15.sp)
                }
            }

            Button(
                onClick = {
                    if (currentPage.intValue < pages.size - 1) {
                        currentPage.intValue++
                    } else {
                        onFinished()
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = PhosphorGreen),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(
                    text = if (currentPage.intValue == pages.size - 1) "Get Started" else "Next",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 2.dp)
                )
            }
        }
    }
}
