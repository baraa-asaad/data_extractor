package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ExcelRow
import com.example.ui.theme.BluePrimary
import com.example.ui.theme.EmeraldSuccess
import com.example.ui.theme.TealAccent

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImportFileScreen(
    fileName: String?,
    headers: List<String>,
    parsedRows: List<ExcelRow>,
    selectedIdColumn: String?,
    selectedBirthYearColumn: String?,
    onFileSelected: (Uri, String) -> Unit,
    onLoadSampleData: () -> Unit,
    onColumnsSelected: (idCol: String?, yearCol: String?) -> Unit,
    onNextClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            onFileSelected(uri, "ملف_الطلاب.csv")
        }
    }

    var showIdDropdown by remember { mutableStateOf(false) }
    var showYearDropdown by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Step Header Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Description,
                    contentDescription = null,
                    tint = BluePrimary,
                    modifier = Modifier.size(32.dp)
                )
                Column {
                    Text(
                        text = "الخطوة 1: استيراد ملف أكسل تحديد الأعمدة",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "اختر ملف Excel أو CSV يحتوي على أرقام الهوية وسنوات الميلاد للطلاب",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Upload Buttons Row
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = { filePickerLauncher.launch("*/*") },
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
            ) {
                Icon(imageVector = Icons.Default.FileUpload, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("رفع ملف Excel / CSV")
            }

            OutlinedButton(
                onClick = onLoadSampleData,
                modifier = Modifier
                    .weight(1f)
                    .height(50.dp)
            ) {
                Icon(imageVector = Icons.Default.AutoFixHigh, contentDescription = null, tint = TealAccent)
                Spacer(modifier = Modifier.width(6.dp))
                Text("تجربة بيانات نموذجية")
            }
        }

        // File Status Indicator
        if (fileName != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = EmeraldSuccess.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, EmeraldSuccess)
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(imageVector = Icons.Default.CheckCircle, contentDescription = null, tint = EmeraldSuccess)
                    Column {
                        Text(
                            text = "تم تحميل الملف: $fileName",
                            fontWeight = FontWeight.Bold,
                            fontSize = 14.sp,
                            color = EmeraldSuccess
                        )
                        Text(
                            text = "إجمالي عدد الطلاب: ${parsedRows.size} طالب",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }

        // Column Mapping Section
        if (headers.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ربط أعمدة الملف مع بيانات الاستعلام:",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )

                    // ID Column Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عمود رقم الهوية:")
                        Box {
                            OutlinedButton(
                                onClick = { showIdDropdown = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (selectedIdColumn != null) BluePrimary else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(selectedIdColumn ?: "اختر العمود")
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = showIdDropdown,
                                onDismissRequest = { showIdDropdown = false }
                            ) {
                                headers.forEach { header ->
                                    DropdownMenuItem(
                                        text = { Text(header) },
                                        onClick = {
                                            onColumnsSelected(header, selectedBirthYearColumn)
                                            showIdDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Divider()

                    // Birth Year Column Selector
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("عمود سنة الميلاد:")
                        Box {
                            OutlinedButton(
                                onClick = { showYearDropdown = true },
                                colors = ButtonDefaults.outlinedButtonColors(
                                    contentColor = if (selectedBirthYearColumn != null) TealAccent else MaterialTheme.colorScheme.onSurface
                                )
                            ) {
                                Text(selectedBirthYearColumn ?: "اختر العمود")
                                Icon(imageVector = Icons.Default.ArrowDropDown, contentDescription = null)
                            }

                            DropdownMenu(
                                expanded = showYearDropdown,
                                onDismissRequest = { showYearDropdown = false }
                            ) {
                                headers.forEach { header ->
                                    DropdownMenuItem(
                                        text = { Text(header) },
                                        onClick = {
                                            onColumnsSelected(selectedIdColumn, header)
                                            showYearDropdown = false
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Parsed Rows Table Preview
        if (parsedRows.isNotEmpty()) {
            Text(
                text = "معاينة الصفوف المستوردة:",
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
                    itemsIndexed(parsedRows) { index, row ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("#${row.rowIndex}", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                Column(horizontalAlignment = Alignment.End) {
                                    Text("الهوية: ${row.idNumber}", fontWeight = FontWeight.Medium)
                                    Text("سنة الميلاد: ${row.birthYear}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                            }
                        }
                    }
                }
            }
        } else {
            Spacer(modifier = Modifier.weight(1f))
        }

        // Bottom Action Button
        Button(
            onClick = onNextClicked,
            enabled = parsedRows.isNotEmpty() && selectedIdColumn != null && selectedBirthYearColumn != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = BluePrimary)
        ) {
            Text("المتابعة لضبط رابط وموقع الاستعلام", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = null) // RTL back arrow goes left/next
        }
    }
}
