package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a row parsed from the imported Excel or CSV file.
 */
data class ExcelRow(
    val rowIndex: Int,
    val idNumber: String,
    val birthYear: String,
    val allColumns: Map<String, String> = emptyMap()
)

/**
 * CSS Selector or XPath configuration for form fields.
 */
data class FormSelectorConfig(
    val targetUrl: String = "",
    val idInputSelector: String = "",
    val birthYearSelector: String = "",
    val submitButtonSelector: String = "",
    val delayAfterSubmitMs: Long = 2500L
)

/**
 * Rule for extracting a specific data field from the web page after query submission.
 */
data class ExtractionRule(
    val id: String,
    val label: String,          // e.g. "اسم الطالب", "النتيجة", "المعدل"
    val cssSelector: String,    // e.g. "#student-name", ".result-card .grade"
    val fallbackXpath: String = ""
)

/**
 * Result of executing query for a single student row.
 */
data class RowExtractionResult(
    val rowIndex: Int,
    val idNumber: String,
    val birthYear: String,
    val status: ExtractionStatus,
    val extractedValues: Map<String, String> = emptyMap(), // Key: Field Label, Value: Text
    val errorMessage: String? = null,
    val timestamp: Long = System.currentTimeMillis()
)

enum class ExtractionStatus {
    PENDING,
    IN_PROGRESS,
    SUCCESS,
    FAILED,
    SKIPPED
}

/**
 * Saved Automation Template / Project in Room
 */
@Entity(tableName = "scraper_projects")
data class ScraperProject(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val targetUrl: String,
    val idSelector: String,
    val birthYearSelector: String,
    val submitSelector: String,
    val extractionRulesJson: String, // Serialized list of ExtractionRule
    val createdDate: Long = System.currentTimeMillis()
)
