package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.SlateBorder
import com.example.ui.theme.TealAccent

data class StepInfo(val number: Int, val title: String)

val SCRAPER_STEPS = listOf(
    StepInfo(1, "الملف والأعمدة"),
    StepInfo(2, "الموقع والحقول"),
    StepInfo(3, "تحديد النتائج"),
    StepInfo(4, "التشغيل التلقائي"),
    StepInfo(5, "التصدير")
)

@Composable
fun StepProgressBar(
    currentStep: Int,
    onStepClicked: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 2.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            SCRAPER_STEPS.forEachIndexed { index, step ->
                val isCompleted = index < currentStep
                val isCurrent = index == currentStep

                val circleBg by animateColorAsState(
                    targetValue = when {
                        isCompleted -> TealAccent
                        isCurrent -> BluePrimary
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    },
                    animationSpec = tween(300)
                )

                val textColor = when {
                    isCompleted || isCurrent -> MaterialTheme.colorScheme.primary
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }

                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onStepClicked(index) }
                        .padding(vertical = 4.dp)
                ) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(circleBg)
                    ) {
                        if (isCompleted) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "${step.number}",
                                color = if (isCurrent) Color.White else MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = step.title,
                        fontSize = 11.sp,
                        fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                        color = textColor,
                        maxLines = 1
                    )
                }

                if (index < SCRAPER_STEPS.size - 1) {
                    Divider(
                        modifier = Modifier
                            .width(16.dp)
                            .padding(bottom = 16.dp),
                        color = if (index < currentStep) TealAccent else SlateBorder,
                        thickness = 2.dp
                    )
                }
            }
        }
    }
}
