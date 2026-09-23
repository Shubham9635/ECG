package com.cardioconnect.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.cardioconnect.ui.theme.BackgroundDark
import com.cardioconnect.ui.theme.CardBorderDark
import com.cardioconnect.ui.theme.CardDark
import com.cardioconnect.ui.theme.PhosphorGreen
import com.cardioconnect.ui.theme.TextPrimary
import com.cardioconnect.ui.theme.TextSecondary
import com.cardioconnect.ui.theme.TextTertiary

data class EducationalCondition(
    val title: String,
    val description: String,
    val criteria: String
)

@Composable
fun EcgConditionsGuideScreen(
    onBackClick: () -> Unit
) {
    val scrollState = rememberScrollState()

    val conditions = listOf(
        EducationalCondition(
            title = "Sinus Bradycardia",
            description = "Heart rate is below the algorithm's configured threshold (typically <60 BPM) while the rhythm remains consistent with sinus node pacing.",
            criteria = "Regular P-waves preceding each QRS complex at <60 BPM."
        ),
        EducationalCondition(
            title = "Sinus Tachycardia",
            description = "Heart rate is above the configured threshold (>100 BPM) with a normal sinus rhythm pattern. Commonly triggered by exertion, stress, fever, or dehydration.",
            criteria = "Normal P-wave morphology and PR intervals at >100 BPM."
        ),
        EducationalCondition(
            title = "Atrial Fibrillation",
            description = "An irregular rhythm pattern with disorganized atrial activity and variable ventricular response detected by a validated ECG algorithm.",
            criteria = "Absence of discrete P-waves and irregularly irregular RR intervals."
        ),
        EducationalCondition(
            title = "Premature Beats (PAC / PVC)",
            description = "Early beats arising from ectopic foci in the atria or ventricles, disrupting the normal rhythmic cardiac cycle.",
            criteria = "Premature QRS complex followed by a compensatory pause."
        ),
        EducationalCondition(
            title = "Conduction Abnormalities",
            description = "Delays or blocks in the heart's specialized conduction pathway (e.g. First-degree AV block or Bundle Branch Block).",
            criteria = "PR interval >200 ms or QRS duration >120 ms."
        ),
        EducationalCondition(
            title = "ST-Segment Abnormality",
            description = "Elevation or depression of the ST-segment relative to the PR baseline, warranting urgent medical assessment.",
            criteria = "ST displacement >0.1 mV (1 mm) from isoelectric line."
        ),
        EducationalCondition(
            title = "QT/QTc Abnormality",
            description = "Prolongation or shortening of the heart's electrical repolarization phase, calculated using the Bazett rate-correction formula.",
            criteria = "QTc >450 ms in males or >460 ms in females."
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDark)
            .verticalScroll(scrollState)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = TextPrimary)
            }
            Column {
                Text(
                    text = "ECG Clinical Reference Guide",
                    color = TextPrimary,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Educational Reference on Common Cardiac Patterns",
                    color = TextTertiary,
                    fontSize = 11.5.sp
                )
            }
        }

        conditions.forEach { condition ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = CardDark),
                border = BorderStroke(1.dp, CardBorderDark)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = condition.title,
                        color = PhosphorGreen,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = condition.description,
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "ECG Criteria: ${condition.criteria}",
                        color = TextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        MedicalSafetyNotice()

        Spacer(modifier = Modifier.height(16.dp))
    }
}
