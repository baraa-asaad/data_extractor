package com.example.ui.screens

import android.webkit.WebView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefineExtractionRulesScreen(
    targetUrl: String,
    testIdNumber: String,
    testBirthYear: String,
    idSelector: String,
    birthYearSelector: String,
    submitSelector: String,
    pickerMode: VisualPickerMode,
    extractionRules: List<ExtractionRule>,
    onTestDataChanged: (idNum: String, birthYear: String) -> Unit,
    onExecuteTestQuery: () -> Unit,
    onPickerModeChanged: (VisualPickerMode) -> Unit,
    onSelectorPicked: (mode: VisualPickerMode, selector: String, textSnippet: String) -> Unit,
    onAddExtractionRule: (label: String, selector: String) -> Unit,
    onRemoveExtractionRule: (id: String) -> Unit,
    onUpdateRuleLabel: (id: String, newLabel: String) -> Unit,
    onWebViewCreated: (WebView) -> Unit,
    onNextClicked: () -> Unit,
    onPreviousClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showAddDialog by remember { mutableStateOf(false) }
    var newLabelInput by remember { mutableStateOf("") }
    var newSelectorInput by remember { mutableStateOf("") }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // Step Header & Test Query Bar
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
                    text = "الخطوة 3: تجربة الاستعلام وتحديد البيانات المراد استخراجها",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = testIdNumber,
                        onValueChange = { onTestDataChanged(it, testBirthYear) },
                        label = { Text("هوية تجريبية") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    OutlinedTextField(
                        value = testBirthYear,
                        onValueChange = { onTestDataChanged(testIdNumber, it) },
                        label = { Text("سنة تجريبية") },
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )

                    Button(
                        onClick = onExecuteTestQuery,
                        colors = ButtonDefaults.buttonColors(containerColor = TealAccent)
                    ) {
                        Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("استعلام تجريبي")
                    }
                }
            }
        }

        // Selected Extraction Rules List
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                Icon(imageVector = Icons.Default.Checklist, contentDescription = null, tint = EmeraldSuccess)
                Text(
                    text = "الحقول المحددة للاستخراج (${extractionRules.size}):",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            Button(
                onClick = {
                    onPickerModeChanged(
                        if (pickerMode == VisualPickerMode.PICK_EXTRACTION_FIELD) VisualPickerMode.NONE else VisualPickerMode.PICK_EXTRACTION_FIELD
                    )
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (pickerMode == VisualPickerMode.PICK_EXTRACTION_FIELD) EmeraldSuccess else BluePrimary
                ),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp)
            ) {
                Icon(imageVector = Icons.Default.TouchApp, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (pickerMode == VisualPickerMode.PICK_EXTRACTION_FIELD) "انقر على العنصر في الشاشة" else "+ انقر لتحديد عنصر من الشاشة",
                    fontSize = 12.sp
                )
            }
        }

        if (extractionRules.isNotEmpty()) {
            LazyRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(extractionRules) { rule ->
                    ElevatedCard(
                        colors = CardDefaults.elevatedCardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(rule.label, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            Text("(${rule.cssSelector})", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            IconButton(
                                onClick = { onRemoveExtractionRule(rule.id) },
                                modifier = Modifier.size(20.dp)
                            ) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "حذف", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }

        // Live WebView
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
                onSelectorPicked = { mode, selector, snippet ->
                    if (mode == VisualPickerMode.PICK_EXTRACTION_FIELD) {
                        newSelectorInput = selector
                        newLabelInput = if (snippet.isNotBlank() && snippet.length <= 25) snippet else "حقل_${extractionRules.size + 1}"
                        showAddDialog = true
                    }
                    onSelectorPicked(mode, selector, snippet)
                },
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
                modifier = Modifier
                    .weight(2f)
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Text("المتابعة للبدء في الاستخراج التلقائي")
                Spacer(modifier = Modifier.width(6.dp))
                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null)
            }
        }
    }

    // Dialog for Naming the Selected Extraction Field
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("تسمية حقل الاستخراج") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("أدخل اسم هذا الحقل كما يظهر في نتائج أكسل (مثلاً: اسم الطالب، النتيجة، المعدل):")
                    OutlinedTextField(
                        value = newLabelInput,
                        onValueChange = { newLabelInput = it },
                        label = { Text("اسم العمود / الحقل") },
                        singleLine = true
                    )
                    Text("السيلكتور المحدد: $newSelectorInput", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (newLabelInput.isNotBlank()) {
                            onAddExtractionRule(newLabelInput, newSelectorInput)
                        }
                        showAddDialog = false
                    }
                ) {
                    Text("إضافة")
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
