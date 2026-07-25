package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExtractionStatus
import com.example.data.RowExtractionResult
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExportResultsScreen(
    results: List<RowExtractionResult>,
    extractionFields: List<String>,
    onExportToExcel: (Uri) -> Unit,
    onSaveProject: (title: String) -> Unit,
    onRestartProcess: () -> Unit,
    modifier: Modifier = Modifier
) {
    var projectTitleInput by remember { mutableStateOf("") }
    var showSaveDialog by remember { mutableStateOf(false) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri: Uri? ->
        if (uri != null) {
            onExportToExcel(uri)
        }
    }

    val successCount = results.count { it.status == ExtractionStatus.SUCCESS }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.FileDownload, contentDescription = null, tint = EmeraldSuccess, modifier = Modifier.size(32.dp))
                    Column {
                        Text(
                            text = "الخطوة 5: تصدير نتائج الطلاب في ملف Excel / CSV",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "الملف المصدَّر يدعم اللغة العربية بالكامل وبدقة عالية على Microsoft Excel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        // Summary Stats Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ElevatedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(containerColor = BluePrimary.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("إجمالي المعالجة", fontSize = 11.sp, color = BluePrimary)
                    Text("${results.size}", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = BluePrimary)
                }
            }

            ElevatedCard(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.elevatedCardColors(containerColor = EmeraldSuccess.copy(alpha = 0.1f))
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("الاستخراج الناجح", fontSize = 11.sp, color = EmeraldSuccess)
                    Text("$successCount", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = EmeraldSuccess)
                }
            }
        }

        // Export & Save Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { exportLauncher.launch("نتائج_استخراج_الطلاب.csv") },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = EmeraldSuccess)
            ) {
                Icon(imageVector = Icons.Default.Download, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("تصدير إلى أكسل", fontWeight = FontWeight.Bold)
            }

            OutlinedButton(
                onClick = { showSaveDialog = true },
                modifier = Modifier
                    .weight(1f)
                    .height(52.dp)
            ) {
                Icon(imageVector = Icons.Default.Bookmark, contentDescription = null)
                Spacer(modifier = Modifier.width(6.dp))
                Text("حفظ هذا المشروع")
            }
        }

        // Final Preview Data Table
        Text(
            text = "جدول المعاينة النهائية قبل التصدير:",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )

        Surface(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
        ) {
            LazyColumn(
                modifier = Modifier.padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                itemsIndexed(results) { index, item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                    ) {
                        Column(
                            modifier = Modifier.padding(12.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("#${item.rowIndex} - الهوية: ${item.idNumber}", fontWeight = FontWeight.Bold)
                                Text("السنة: ${item.birthYear}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                            }

                            Divider(modifier = Modifier.padding(vertical = 4.dp))

                            item.extractedValues.forEach { (key, value) ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(key, fontSize = 12.sp, color = MaterialTheme.colorScheme.primary)
                                    Text(value, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Restart Process Button
        OutlinedButton(
            onClick = onRestartProcess,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(imageVector = Icons.Default.Refresh, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("بدء استخراج جديد لملف آخر")
        }
    }

    // Save Project Template Dialog
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("حفظ إعدادات المشروع") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("احفظ إعدادات السيلكتور والموقع لإعادة استخدامها مستقبلاً بنقرة واحدة:")
                    OutlinedTextField(
                        value = projectTitleInput,
                        onValueChange = { projectTitleInput = it },
                        label = { Text("اسم المشروع (مثال: نتائج طلاب المدرسة)") },
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveProject(projectTitleInput)
                        showSaveDialog = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}
