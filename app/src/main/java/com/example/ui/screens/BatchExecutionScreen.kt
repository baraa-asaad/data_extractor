package com.example.ui.screens

import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtractionRule
import com.example.data.ExtractionStatus
import com.example.data.RowExtractionResult
import com.example.ui.components.InteractiveWebScraperView
import com.example.ui.components.VisualPickerMode
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.RoseError
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BatchExecutionScreen(
    targetUrl: String,
    idSelector: String,
    birthYearSelector: String,
    submitSelector: String,
    delayMs: Long,
    extractionRules: List<ExtractionRule>,
    batchResults: List<RowExtractionResult>,
    isBatchRunning: Boolean,
    isBatchPaused: Boolean,
    currentBatchIndex: Int,
    onStartBatch: () -> Unit,
    onPauseBatch: () -> Unit,
    onStopBatch: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalCount = batchResults.size
    val completedCount = batchResults.count { it.status == ExtractionStatus.SUCCESS || it.status == ExtractionStatus.FAILED }
    val successCount = batchResults.count { it.status == ExtractionStatus.SUCCESS }
    val progress = if (totalCount > 0) completedCount.toFloat() / totalCount else 0f

    var showLiveWebView by remember { mutableStateOf(true) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Step Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "الخطوة 4: بدء الاستخراج التلقائي لكافة الطلاب",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                // Progress Bar
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "التقدم: $completedCount من $totalCount طالب",
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                        Text(
                            text = "${(progress * 100).toInt()}%",
                            fontWeight = FontWeight.Bold,
                            color = BluePrimary,
                            fontSize = 13.sp
                        )
                    }

                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(10.dp)
                            .clip(RoundedCornerShape(5.dp)),
                        color = TealAccent,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }

                // Batch Control Buttons
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (!isBatchRunning) {
                        Button(
                            onClick = onStartBatch,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
                        ) {
                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("بدء الاستخراج التلقائي")
                        }
                    } else {
                        Button(
                            onClick = onPauseBatch,
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isBatchPaused) EmeraldSuccess else Color(0xFFD97706)
                            )
                        ) {
                            Icon(
                                imageVector = if (isBatchPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
                                contentDescription = null
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(if (isBatchPaused) "استئناف" else "إيقاف مؤقت")
                        }

                        OutlinedButton(
                            onClick = onStopBatch,
                            modifier = Modifier.height(44.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = RoseError)
                        ) {
                            Icon(imageVector = Icons.Default.Stop, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("إيقاف")
                        }
                    }

                    // Toggle Live WebView preview button
                    IconButton(
                        onClick = { showLiveWebView = !showLiveWebView },
                        modifier = Modifier.height(44.dp)
                    ) {
                        Icon(
                            imageVector = if (showLiveWebView) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                            contentDescription = "عرض/إخفاء الشاشة الحية",
                            tint = BluePrimary
                        )
                    }
                }
            }
        }

        // Live WebView (Small preview window while batching)
        AnimatedVisibility(visible = showLiveWebView) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp),
                shape = RoundedCornerShape(12.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
            ) {
                InteractiveWebScraperView(
                    url = targetUrl,
                    pickerMode = VisualPickerMode.NONE,
                    selectedIdSelector = idSelector,
                    selectedBirthYearSelector = birthYearSelector,
                    selectedSubmitSelector = submitSelector,
                    extractionRules = extractionRules,
                    onSelectorPicked = { _, _, _ -> },
                    onPageLoaded = {},
                    onWebViewCreated = onWebViewCreated
                )
            }
        }

        // Real-Time Results Table Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "نتائج الاستخراج المباشرة:",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold
            )

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                AssistChip(
                    onClick = { },
                    label = { Text("نجاح: $successCount", fontSize = 11.sp, color = EmeraldSuccess) },
                    leadingIcon = { Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(14.dp)) }
                )
            }
        }

        // Real-time Batch Results List
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            if (batchResults.isEmpty()) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text("انقر على زر 'بدء الاستخراج التلقائي' للبدء", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    itemsIndexed(batchResults) { index, item ->
                        val isCurrent = index == currentBatchIndex && isBatchRunning

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = when {
                                    isCurrent -> BluePrimary.copy(alpha = 0.1f)
                                    item.status == ExtractionStatus.SUCCESS -> EmeraldSuccess.copy(alpha = 0.05f)
                                    item.status == ExtractionStatus.FAILED -> RoseError.copy(alpha = 0.05f)
                                    else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                }
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                                        Text("#${item.rowIndex}", fontWeight = FontWeight.Bold)
                                        Text("الهوية: ${item.idNumber}", fontWeight = FontWeight.Medium)
                                        Text("(${item.birthYear})", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                    }

                                    // Status Badge
                                    Surface(
                                        shape = RoundedCornerShape(12.dp),
                                        color = when (item.status) {
                                            ExtractionStatus.SUCCESS -> EmeraldSuccess
                                            ExtractionStatus.FAILED -> RoseError
                                            ExtractionStatus.IN_PROGRESS -> BluePrimary
                                            ExtractionStatus.PENDING -> MaterialTheme.colorScheme.outline
                                            ExtractionStatus.SKIPPED -> Color.Gray
                                        },
                                        contentColor = Color.White
                                    ) {
                                        Text(
                                            text = when (item.status) {
                                                ExtractionStatus.SUCCESS -> "تم النجاح"
                                                ExtractionStatus.FAILED -> "فشل"
                                                ExtractionStatus.IN_PROGRESS -> "جاري الاستخراج..."
                                                ExtractionStatus.PENDING -> "انتظار"
                                                ExtractionStatus.SKIPPED -> "تجاوز"
                                            },
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Extracted Values Preview
                                if (item.extractedValues.isNotEmpty()) {
                                    Divider(modifier = Modifier.padding(vertical = 4.dp))
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                                    ) {
                                        item.extractedValues.forEach { (key, value) ->
                                            Column {
                                                Text(key, fontSize = 10.sp, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                                                Text(value.ifBlank { "بلا قيمة" }, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Navigation Buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onPreviousClicked,
                modifier = Modifier
                    .weight(1f)
                    .height(48.dp)
            ) {
                Text("السابق")
            }

            Button(
                onClick = onNextClicked,
                enabled = completedCount > 0,
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("المتابعة لمعاينة وتصدير أكسل")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    }
}
