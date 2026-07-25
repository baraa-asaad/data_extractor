package com.example.util

import android.content.Context
import android.net.Uri
import com.example.data.ExcelRow
import com.example.data.RowExtractionResult
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.io.BufferedWriter

object ExcelCsvParser {

    /**
     * Parses an Excel (XLSX/XLS) or CSV file from a Content Uri.
     * For maximum compatibility without huge heavy binary libraries,
     * this handles CSV natively (with UTF-8 / UTF-8 BOM support)
     * and fallback delimiter parsing (comma, semicolon, tab).
     */
    fun parseFile(context: Context, uri: Uri): Pair<List<String>, List<Map<String, String>>> {
        val inputStream = context.contentResolver.openInputStream(uri)
            ?: throw IllegalArgumentException("Could not open file URI")

        val reader = BufferedReader(InputStreamReader(inputStream, Charsets.UTF_8))
        val lines = reader.readLines().filter { it.isNotBlank() }
        reader.close()

        if (lines.isEmpty()) {
            return Pair(emptyList(), emptyList())
        }

        // Detect delimiter (comma, semicolon, tab)
        val firstLine = lines.first().removePrefix("\uFEFF") // Remove UTF-8 BOM if present
        val delimiter = when {
            firstLine.contains("\t") -> "\t"
            firstLine.contains(";") -> ";"
            else -> ","
        }

        val headers = parseCsvLine(firstLine, delimiter)
            .mapIndexed { index, header ->
                val cleaned = header.trim().trim('"')
                if (cleaned.isNotBlank()) cleaned else "Column_${index + 1}"
            }

        val dataRows = mutableListOf<Map<String, String>>()

        for (i in 1 until lines.size) {
            val line = lines[i]
            val values = parseCsvLine(line, delimiter)
            val rowMap = mutableMapOf<String, String>()
            for (j in headers.indices) {
                val value = values.getOrNull(j)?.trim()?.trim('"') ?: ""
                rowMap[headers[j]] = value
            }
            if (rowMap.values.any { it.isNotBlank() }) {
                dataRows.add(rowMap)
            }
        }

        return Pair(headers, dataRows)
    }

    private fun parseCsvLine(line: String, delimiter: String): List<String> {
        val result = mutableListOf<String>()
        val sb = StringBuilder()
        var inQuotes = false

        for (ch in line) {
            when {
                ch == '"' -> inQuotes = !inQuotes
                ch.toString() == delimiter && !inQuotes -> {
                    result.add(sb.toString())
                    sb.clear()
                }
                else -> sb.append(ch)
            }
        }
        result.add(sb.toString())
        return result
    }

    /**
     * Exports extraction results to an Excel-compatible CSV file formatted in UTF-8 with BOM
     * so Microsoft Excel opens Arabic text flawlessly without encoding issues.
     */
    fun exportToExcelCsv(
        context: Context,
        uri: Uri,
        originalHeaders: List<String>,
        extractedFields: List<String>,
        results: List<RowExtractionResult>,
        rows: List<ExcelRow>
    ) {
        val outputStream = context.contentResolver.openOutputStream(uri)
            ?: throw IllegalArgumentException("Could not write to destination URI")

        val writer = BufferedWriter(OutputStreamWriter(outputStream, Charsets.UTF_8))

        // Write UTF-8 BOM for Microsoft Excel Arabic support
        outputStream.write(0xEF)
        outputStream.write(0xBB)
        outputStream.write(0xBF)

        val headerRow = mutableListOf<String>()
        headerRow.add("رقم السطر")
        headerRow.add("حالة الاستخراج")
        headerRow.add("رقم الهوية")
        headerRow.add("سنة الميلاد")

        // Add extra extracted field headers
        headerRow.addAll(extractedFields)

        // Add original columns
        headerRow.addAll(originalHeaders)

        writer.write(headerRow.joinToString(",") { escapeCsvCell(it) })
        writer.newLine()

        val rowMap = rows.associateBy { it.rowIndex }

        for (result in results.sortedBy { it.rowIndex }) {
            val cellValues = mutableListOf<String>()
            cellValues.add(result.rowIndex.toString())
            cellValues.add(when(result.status) {
                com.example.data.ExtractionStatus.SUCCESS -> "تم بنجاح"
                com.example.data.ExtractionStatus.FAILED -> "فشل: ${result.errorMessage ?: ""}"
                com.example.data.ExtractionStatus.IN_PROGRESS -> "قيد المعالجة"
                com.example.data.ExtractionStatus.PENDING -> "قيد الانتظار"
                com.example.data.ExtractionStatus.SKIPPED -> "تم التجاوز"
            })
            cellValues.add(result.idNumber)
            cellValues.add(result.birthYear)

            // Extracted values
            for (field in extractedFields) {
                cellValues.add(result.extractedValues[field] ?: "")
            }

            // Original columns
            val orig = rowMap[result.rowIndex]
            if (orig != null) {
                for (h in originalHeaders) {
                    cellValues.add(orig.allColumns[h] ?: "")
                }
            }

            writer.write(cellValues.joinToString(",") { escapeCsvCell(it) })
            writer.newLine()
        }

        writer.flush()
        writer.close()
    }

    private fun escapeCsvCell(value: String): String {
        var clean = value.replace("\n", " ").replace("\r", " ")
        if (clean.contains(",") || clean.contains("\"")) {
            clean = clean.replace("\"", "\"\"")
            return "\"$clean\""
        }
        return clean
    }

    /**
     * Sample student dataset for quick testing and demonstration
     */
    fun getSampleStudentData(): Pair<List<String>, List<Map<String, String>>> {
        val headers = listOf("اسم الطالب الافتراضي", "رقم الهوية", "سنة الميلاد", "المحافظة")
        val data = listOf(
            mapOf("اسم الطالب الافتراضي" to "أحمد محمد علي", "رقم الهوية" to "900123456", "سنة الميلاد" to "2005", "المحافظة" to "الرياض"),
            mapOf("اسم الطالب الافتراضي" to "فاطمة إبراهيم خليل", "رقم الهوية" to "900654321", "سنة الميلاد" to "2006", "المحافظة" to "جدة"),
            mapOf("اسم الطالب الافتراضي" to "محمود حسن السيد", "رقم الهوية" to "900987654", "سنة الميلاد" to "2005", "المحافظة" to "الدمام"),
            mapOf("اسم الطالب الافتراضي" to "سارة عبد الله يوسف", "رقم الهوية" to "900456789", "سنة الميلاد" to "2004", "المحافظة" to "مكة المكرمة")
        )
        return Pair(headers, data)
    }
}
