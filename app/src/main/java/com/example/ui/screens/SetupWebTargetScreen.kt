package com.example.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtractionRule
import com.example.ui.components.InteractiveWebScraperView
import com.example.ui.components.VisualPickerMode
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SetupWebTargetScreen(
    targetUrl: String,
    idSelector: String,
    birthYearSelector: String,
    submitSelector: String,
    pickerMode: VisualPickerMode,
    extractionRules: List<ExtractionRule>,
    onUrlChanged: (String) -> Unit,
    onPickerModeChanged: (VisualPickerMode) -> Unit,
    onSelectorPicked: (mode: VisualPickerMode, selector: String, textSnippet: String) -> Unit,
    onAutoDetect: () -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var urlInput by remember { mutableStateOf(targetUrl) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Step Header & URL Input Card
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
                    text = "الخطوة 2: ادخال رابط الموقع وتحديد صناديق الادخال والزر",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = urlInput,
                        onValueChange = {
                            urlInput = it
                            onUrlChanged(it)
                        },
                        label = { Text("رابط الموقع المستهدف (URL)") },
                        leadingIcon = { Icon(imageVector = Icons.Default.Language, contentDescription = null) },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = { onUrlChanged(urlInput) },
                        colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
                    ) {
                        Text("انتقال")
                    }
                }
            }
        }

        // Picker Action Badges Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Pick ID Input Button
            FilterChip(
                selected = pickerMode == VisualPickerMode.PICK_ID_INPUT,
                onClick = {
                    onPickerModeChanged(if (pickerMode == VisualPickerMode.PICK_ID_INPUT) VisualPickerMode.NONE else VisualPickerMode.PICK_ID_INPUT)
                },
                label = {
                    Text(if (idSelector.isNotBlank()) "الهوية: $idSelector" else "تحديد صندوق الهوية")
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (idSelector.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.Badge,
                        contentDescription = null,
                        tint = if (idSelector.isNotBlank()) BluePrimary else MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // Pick Birth Year Input Button
            FilterChip(
                selected = pickerMode == VisualPickerMode.PICK_BIRTH_YEAR_INPUT,
                onClick = {
                    onPickerModeChanged(if (pickerMode == VisualPickerMode.PICK_BIRTH_YEAR_INPUT) VisualPickerMode.NONE else VisualPickerMode.PICK_BIRTH_YEAR_INPUT)
                },
                label = {
                    Text(if (birthYearSelector.isNotBlank()) "السنة: $birthYearSelector" else "تحديد سنة الميلاد")
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (birthYearSelector.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.CalendarToday,
                        contentDescription = null,
                        tint = if (birthYearSelector.isNotBlank()) TealAccent else MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier.weight(1f)
            )

            // Pick Submit Button
            FilterChip(
                selected = pickerMode == VisualPickerMode.PICK_SUBMIT_BUTTON,
                onClick = {
                    onPickerModeChanged(if (pickerMode == VisualPickerMode.PICK_SUBMIT_BUTTON) VisualPickerMode.NONE else VisualPickerMode.PICK_SUBMIT_BUTTON)
                },
                label = {
                    Text(if (submitSelector.isNotBlank()) "الزر: $submitSelector" else "تحديد زر الاستعلام")
                },
                leadingIcon = {
                    Icon(
                        imageVector = if (submitSelector.isNotBlank()) Icons.Default.CheckCircle else Icons.Default.SmartButton,
                        contentDescription = null,
                        tint = if (submitSelector.isNotBlank()) Color(0xFFD97706) else MaterialTheme.colorScheme.onSurface
                    )
                },
                modifier = Modifier.weight(1f)
            )
        }

        // Auto Detect Button Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedButton(
                onClick = onAutoDetect,
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("اكتشاف العناصر تلقائياً", fontSize = 12.sp)
            }

            Text(
                text = "انقر فوق المربع أعلاه ثم انقر على العنصر في الموقع مباشرة",
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Interactive WebView Box
        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            InteractiveWebScraperView(
                url = targetUrl,
                pickerMode = pickerMode,
                selectedIdSelector = idSelector,
                selectedBirthYearSelector = birthYearSelector,
                selectedSubmitSelector = submitSelector,
                extractionRules = extractionRules,
                onSelectorPicked = onSelectorPicked,
                onPageLoaded = {},
                onWebViewCreated = onWebViewCreated
            )
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
                enabled = idSelector.isNotBlank() && birthYearSelector.isNotBlank() && submitSelector.isNotBlank(),
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("المتابعة لتحديد البيانات المراد استخراجها")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    }
}
