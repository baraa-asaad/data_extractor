package com.example.ui

import android.app.Application
import android.net.Uri
import android.webkit.WebView
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.*
import com.example.data.db.ScraperDatabase
import com.example.data.db.ScraperRepository
import com.example.ui.components.VisualPickerMode
import com.example.util.ExcelCsvParser
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*

class ScraperViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: ScraperRepository

    init {
        val db = ScraperDatabase.getDatabase(application)
        repository = ScraperRepository(db.scraperDao())
    }

    val savedProjects: StateFlow<List<ScraperProject>> = repository.allProjects
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // Current Active Step Index (0 to 4)
    private val _currentStep = MutableStateFlow(0)
    val currentStep: StateFlow<Int> = _currentStep.asStateFlow()

    // File Import State
    private val _fileName = MutableStateFlow<String?>(null)
    val fileName: StateFlow<String?> = _fileName.asStateFlow()

    private val _headers = MutableStateFlow<List<String>>(emptyList())
    val headers: StateFlow<List<String>> = _headers.asStateFlow()

    private val _parsedRows = MutableStateFlow<List<ExcelRow>>(emptyList())
    val parsedRows: StateFlow<List<ExcelRow>> = _parsedRows.asStateFlow()

    private val _selectedIdColumn = MutableStateFlow<String?>(null)
    val selectedIdColumn: StateFlow<String?> = _selectedIdColumn.asStateFlow()

    private val _selectedBirthYearColumn = MutableStateFlow<String?>(null)
    val selectedBirthYearColumn: StateFlow<String?> = _selectedBirthYearColumn.asStateFlow()

    // Web Selector Config State
    private val _targetUrl = MutableStateFlow("https://")
    val targetUrl: StateFlow<String> = _targetUrl.asStateFlow()

    private val _idSelector = MutableStateFlow("")
    val idSelector: StateFlow<String> = _idSelector.asStateFlow()

    private val _birthYearSelector = MutableStateFlow("")
    val birthYearSelector: StateFlow<String> = _birthYearSelector.asStateFlow()

    private val _submitSelector = MutableStateFlow("")
    val submitSelector: StateFlow<String> = _submitSelector.asStateFlow()

    private val _delayMs = MutableStateFlow(2500L)
    val delayMs: StateFlow<Long> = _delayMs.asStateFlow()

    private val _pickerMode = MutableStateFlow(VisualPickerMode.NONE)
    val pickerMode: StateFlow<VisualPickerMode> = _pickerMode.asStateFlow()

    // Test Run & Extraction Fields Rules
    private val _testIdNumber = MutableStateFlow("1002003004")
    val testIdNumber: StateFlow<String> = _testIdNumber.asStateFlow()

    private val _testBirthYear = MutableStateFlow("2005")
    val testBirthYear: StateFlow<String> = _testBirthYear.asStateFlow()

    private val _extractionRules = MutableStateFlow<List<ExtractionRule>>(emptyList())
    val extractionRules: StateFlow<List<ExtractionRule>> = _extractionRules.asStateFlow()

    // Batch Scraper Execution State
    private val _batchResults = MutableStateFlow<List<RowExtractionResult>>(emptyList())
    val batchResults: StateFlow<List<RowExtractionResult>> = _batchResults.asStateFlow()

    private val _isBatchRunning = MutableStateFlow(false)
    val isBatchRunning: StateFlow<Boolean> = _isBatchRunning.asStateFlow()

    private val _isBatchPaused = MutableStateFlow(false)
    val isBatchPaused: StateFlow<Boolean> = _isBatchPaused.asStateFlow()

    private val _currentBatchIndex = MutableStateFlow(0)
    val currentBatchIndex: StateFlow<Int> = _currentBatchIndex.asStateFlow()

    private val _statusMessage = MutableStateFlow<String?>(null)
    val statusMessage: StateFlow<String?> = _statusMessage.asStateFlow()

    // Reference to active WebView
    var activeWebView: WebView? = null

    fun setStep(step: Int) {
        _currentStep.value = step.coerceIn(0, 4)
    }

    fun nextStep() {
        _currentStep.value = (_currentStep.value + 1).coerceAtMost(4)
    }

    fun previousStep() {
        _currentStep.value = (_currentStep.value - 1).coerceAtLeast(0)
    }

    // Step 1: File & Columns
    fun importExcelFile(uri: Uri, name: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val (parsedHeaders, rawRows) = ExcelCsvParser.parseFile(getApplication(), uri)
                _fileName.value = name
                _headers.value = parsedHeaders

                // Auto-detect ID and BirthYear columns if matching keywords exist
                val detectedId = parsedHeaders.firstOrNull {
                    it.contains("هوية") || it.contains("ID") || it.contains("رقم") || it.contains("سجل")
                } ?: parsedHeaders.firstOrNull()

                val detectedYear = parsedHeaders.firstOrNull {
                    it.contains("سنة") || it.contains("تاريخ") || it.contains("ميلاد") || it.contains("Year") || it.contains("Birth")
                } ?: parsedHeaders.getOrNull(1)

                _selectedIdColumn.value = detectedId
                _selectedBirthYearColumn.value = detectedYear

                rebuildExcelRows(rawRows, detectedId, detectedYear)
                _statusMessage.value = "تم استيراد الملف بنجاح (${rawRows.size} طالب)"
            } catch (e: Exception) {
                _statusMessage.value = "خطأ أثناء قراءة الملف: ${e.localizedMessage}"
            }
        }
    }

    fun loadSampleData() {
        val (sampleHeaders, sampleRows) = ExcelCsvParser.getSampleStudentData()
        _fileName.value = "نموذج_طلاب_افتراضي.csv"
        _headers.value = sampleHeaders
        _selectedIdColumn.value = "رقم الهوية"
        _selectedBirthYearColumn.value = "سنة الميلاد"
        rebuildExcelRows(sampleRows, "رقم الهوية", "سنة الميلاد")
        _statusMessage.value = "تم تحميل بيانات تجريبية (4 طلاب)"
    }

    fun selectColumns(idCol: String?, birthYearCol: String?) {
        _selectedIdColumn.value = idCol
        _selectedBirthYearColumn.value = birthYearCol

        // Rebuild excel rows with current selection
        val currentRowsRaw = _parsedRows.value.map { it.allColumns }
        rebuildExcelRows(currentRowsRaw, idCol, birthYearCol)
    }

    private fun rebuildExcelRows(rawRows: List<Map<String, String>>, idCol: String?, birthYearCol: String?) {
        val mapped = rawRows.mapIndexed { index, map ->
            val idVal = if (idCol != null) map[idCol] ?: "" else ""
            val yearVal = if (birthYearCol != null) map[birthYearCol] ?: "" else ""
            ExcelRow(
                rowIndex = index + 1,
                idNumber = idVal,
                birthYear = yearVal,
                allColumns = map
            )
        }
        _parsedRows.value = mapped

        if (mapped.isNotEmpty()) {
            _testIdNumber.value = mapped.first().idNumber
            _testBirthYear.value = mapped.first().birthYear
        }
    }

    // Step 2: URL & Selectors
    fun setTargetUrl(url: String) {
        _targetUrl.value = url
    }

    fun setPickerMode(mode: VisualPickerMode) {
        _pickerMode.value = mode
    }

    fun updateSelector(mode: VisualPickerMode, selector: String, textSnippet: String) {
        when (mode) {
            VisualPickerMode.PICK_ID_INPUT -> {
                _idSelector.value = selector
                _statusMessage.value = "تم تحديد حقل الهوية: $selector"
            }
            VisualPickerMode.PICK_BIRTH_YEAR_INPUT -> {
                _birthYearSelector.value = selector
                _statusMessage.value = "تم تحديد حقل سنة الميلاد: $selector"
            }
            VisualPickerMode.PICK_SUBMIT_BUTTON -> {
                _submitSelector.value = selector
                _statusMessage.value = "تم تحديد زر الاستعلام: $selector"
            }
            VisualPickerMode.PICK_EXTRACTION_FIELD -> {
                // Add new extraction field rule
                addExtractionRule("حقل_${_extractionRules.value.size + 1}", selector)
            }
            VisualPickerMode.NONE -> {}
        }
        _pickerMode.value = VisualPickerMode.NONE
    }

    fun manualUpdateSelectors(idSel: String, yearSel: String, submitSel: String) {
        _idSelector.value = idSel
        _birthYearSelector.value = yearSel
        _submitSelector.value = submitSel
    }

    fun autoDetectSelectors() {
        val wv = activeWebView ?: return
        val js = """
            (function() {
                var inputs = Array.from(document.querySelectorAll('input'));
                var idInput = inputs.find(i => i.name.toLowerCase().includes('id') || i.id.toLowerCase().includes('id') || i.placeholder.includes('هوية') || i.placeholder.includes('القومي'));
                var yearInput = inputs.find(i => i.name.toLowerCase().includes('year') || i.name.toLowerCase().includes('birth') || i.placeholder.includes('سنة') || i.placeholder.includes('تاريخ'));
                var btn = document.querySelector('button[type="submit"], input[type="submit"], button') || Array.from(document.querySelectorAll('*')).find(e => e.innerText && e.innerText.includes('استعلام'));
                
                function getSel(el) {
                    if (!el) return '';
                    if (el.id) return '#' + el.id;
                    if (el.name) return el.tagName.toLowerCase() + '[name="' + el.name + '"]';
                    return el.tagName.toLowerCase();
                }
                
                return JSON.stringify({
                    idSel: getSel(idInput) || (inputs[0] ? getSel(inputs[0]) : ''),
                    yearSel: getSel(yearInput) || (inputs[1] ? getSel(inputs[1]) : ''),
                    submitSel: getSel(btn)
                });
            })();
        """.trimIndent()

        wv.evaluateJavascript(js) { res ->
            try {
                if (res != null && res != "null") {
                    val clean = res.trim('"').replace("\\\"", "\"")
                    val orgJson = org.json.JSONObject(clean)
                    val foundId = orgJson.optString("idSel")
                    val foundYear = orgJson.optString("yearSel")
                    val foundSubmit = orgJson.optString("submitSel")

                    if (foundId.isNotBlank()) _idSelector.value = foundId
                    if (foundYear.isNotBlank()) _birthYearSelector.value = foundYear
                    if (foundSubmit.isNotBlank()) _submitSelector.value = foundSubmit

                    _statusMessage.value = "تم اكتشاف حقول النموذج تلقائياً"
                }
            } catch (e: Exception) {
                _statusMessage.value = "لم يتم التعرف التلقائي على الحقول، يرجى تحديدها يدوياً"
            }
        }
    }

    // Step 3: Test Run & Define Extraction Rules
    fun setTestData(idNum: String, birthYear: String) {
        _testIdNumber.value = idNum
        _testBirthYear.value = birthYear
    }

    fun executeTestQuery() {
        val wv = activeWebView ?: run {
            _statusMessage.value = "لم يتم تحميل صفحة المستعرض بعد"
            return
        }

        val idSel = _idSelector.value
        val yearSel = _birthYearSelector.value
        val submitSel = _submitSelector.value
        val idVal = _testIdNumber.value
        val yearVal = _testBirthYear.value

        val js = """
            (function() {
                try {
                    var idEl = document.querySelector('$idSel');
                    var yearEl = document.querySelector('$yearSel');
                    var btnEl = document.querySelector('$submitSel');

                    if (idEl) {
                        idEl.value = '$idVal';
                        idEl.dispatchEvent(new Event('input', { bubbles: true }));
                        idEl.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    if (yearEl) {
                        yearEl.value = '$yearVal';
                        yearEl.dispatchEvent(new Event('input', { bubbles: true }));
                        yearEl.dispatchEvent(new Event('change', { bubbles: true }));
                    }

                    setTimeout(function() {
                        if (btnEl) {
                            btnEl.click();
                        } else if (idEl && idEl.form) {
                            idEl.form.submit();
                        }
                    }, 400);
                    return 'OK';
                } catch(e) {
                    return 'ERROR: ' + e.message;
                }
            })();
        """.trimIndent()

        wv.evaluateJavascript(js) {
            _statusMessage.value = "جاري تنفيذ استعلام الاختبار..."
        }
    }

    fun addExtractionRule(label: String, selector: String) {
        val rules = _extractionRules.value.toMutableList()
        val rule = ExtractionRule(
            id = System.currentTimeMillis().toString(),
            label = label,
            cssSelector = selector
        )
        rules.add(rule)
        _extractionRules.value = rules
        _statusMessage.value = "تم إضافة حقل الاستخراج: $label"
    }

    fun removeExtractionRule(id: String) {
        _extractionRules.value = _extractionRules.value.filter { it.id != id }
    }

    fun updateRuleLabel(id: String, newLabel: String) {
        _extractionRules.value = _extractionRules.value.map {
            if (it.id == id) it.copy(label = newLabel) else it
        }
    }

    // Step 4: Batch Extraction Engine
    fun startBatchExtraction() {
        val rows = _parsedRows.value
        if (rows.isEmpty()) {
            _statusMessage.value = "لا توجد بيانات طلاب للاستخراج"
            return
        }

        val wv = activeWebView ?: run {
            _statusMessage.value = "المستعرض غير جاهز"
            return
        }

        _isBatchRunning.value = true
        _isBatchPaused.value = false
        _currentBatchIndex.value = 0

        val initialResults = rows.map {
            RowExtractionResult(
                rowIndex = it.rowIndex,
                idNumber = it.idNumber,
                birthYear = it.birthYear,
                status = ExtractionStatus.PENDING
            )
        }
        _batchResults.value = initialResults

        viewModelScope.launch(Dispatchers.Main) {
            processNextRowInBatch()
        }
    }

    fun pauseBatchExtraction() {
        _isBatchPaused.value = !_isBatchPaused.value
        _statusMessage.value = if (_isBatchPaused.value) "تم إيقاف الاستخراج مؤقتاً" else "جاري استئناف الاستخراج"
    }

    fun stopBatchExtraction() {
        _isBatchRunning.value = false
        _isBatchPaused.value = false
        _statusMessage.value = "تم إيقاف عملية الاستخراج"
    }

    private suspend fun processNextRowInBatch() {
        if (!_isBatchRunning.value) return

        while (_isBatchPaused.value) {
            delay(500)
            if (!_isBatchRunning.value) return
        }

        val index = _currentBatchIndex.value
        val rows = _parsedRows.value

        if (index >= rows.size) {
            _isBatchRunning.value = false
            _statusMessage.value = "اكتملت عملية استخراج كافة البيانات بنجاح!"
            return
        }

        val currentRow = rows[index]

        // Update status to IN_PROGRESS
        updateRowStatus(index, ExtractionStatus.IN_PROGRESS)

        val wv = activeWebView
        if (wv == null) {
            updateRowStatus(index, ExtractionStatus.FAILED, errorMessage = "المستعرض غير متاح")
            _currentBatchIndex.value = index + 1
            processNextRowInBatch()
            return
        }

        // Fill inputs and click submit
        val idSel = _idSelector.value
        val yearSel = _birthYearSelector.value
        val submitSel = _submitSelector.value
        val idVal = currentRow.idNumber
        val yearVal = currentRow.birthYear

        val submitJs = """
            (function() {
                try {
                    var idEl = document.querySelector('$idSel');
                    var yearEl = document.querySelector('$yearSel');
                    var btnEl = document.querySelector('$submitSel');

                    if (idEl) {
                        idEl.value = '$idVal';
                        idEl.dispatchEvent(new Event('input', { bubbles: true }));
                        idEl.dispatchEvent(new Event('change', { bubbles: true }));
                    }
                    if (yearEl) {
                        yearEl.value = '$yearVal';
                        yearEl.dispatchEvent(new Event('input', { bubbles: true }));
                        yearEl.dispatchEvent(new Event('change', { bubbles: true }));
                    }

                    setTimeout(function() {
                        if (btnEl) {
                            btnEl.click();
                        } else if (idEl && idEl.form) {
                            idEl.form.submit();
                        }
                    }, 300);
                } catch(e) {}
            })();
        """.trimIndent()

        wv.evaluateJavascript(submitJs, null)

        // Wait for configured delay (page render / server response)
        delay(_delayMs.value)

        // Extract result fields
        val rules = _extractionRules.value
        val rulesJsonArray = org.json.JSONArray()
        rules.forEach { r ->
            val obj = org.json.JSONObject()
            obj.put("label", r.label)
            obj.put("selector", r.cssSelector)
            rulesJsonArray.put(obj)
        }

        val extractJs = """
            (function() {
                var extracted = {};
                var rules = ${rulesJsonArray.toString()};
                
                for (var i = 0; i < rules.length; i++) {
                    var r = rules[i];
                    var el = document.querySelector(r.selector);
                    extracted[r.label] = el ? (el.innerText || el.value || '').trim() : '';
                }
                
                // If no rules provided, fallback to standard body text or result table
                if (rules.length === 0) {
                    var bodyText = document.body ? document.body.innerText.trim() : '';
                    extracted['نتيجة الصفحة'] = bodyText.substring(0, 300);
                }
                
                return JSON.stringify(extracted);
            })();
        """.trimIndent()

        wv.evaluateJavascript(extractJs) { resJson ->
            val extractedMap = mutableMapOf<String, String>()
            try {
                if (resJson != null && resJson != "null") {
                    val clean = resJson.trim('"').replace("\\\"", "\"")
                    val jsonObj = org.json.JSONObject(clean)
                    val keys = jsonObj.keys()
                    while (keys.hasNext()) {
                        val key = keys.next()
                        extractedMap[key] = jsonObj.optString(key)
                    }
                }
                updateRowStatus(index, ExtractionStatus.SUCCESS, extractedValues = extractedMap)
            } catch (e: Exception) {
                updateRowStatus(index, ExtractionStatus.FAILED, errorMessage = e.localizedMessage)
            }

            _currentBatchIndex.value = index + 1
            viewModelScope.launch(Dispatchers.Main) {
                processNextRowInBatch()
            }
        }
    }

    private fun updateRowStatus(
        index: Int,
        status: ExtractionStatus,
        extractedValues: Map<String, String> = emptyMap(),
        errorMessage: String? = null
    ) {
        val currentList = _batchResults.value.toMutableList()
        if (index in currentList.indices) {
            val item = currentList[index]
            currentList[index] = item.copy(
                status = status,
                extractedValues = if (extractedValues.isNotEmpty()) extractedValues else item.extractedValues,
                errorMessage = errorMessage
            )
            _batchResults.value = currentList
        }
    }

    // Step 5: Export to Excel / CSV
    fun exportToExcel(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val rulesLabels = _extractionRules.value.map { it.label }
                val extraFields = if (rulesLabels.isNotEmpty()) rulesLabels else listOf("نتيجة الصفحة")

                ExcelCsvParser.exportToExcelCsv(
                    context = getApplication(),
                    uri = uri,
                    originalHeaders = _headers.value,
                    extractedFields = extraFields,
                    results = _batchResults.value,
                    rows = _parsedRows.value
                )
                _statusMessage.value = "تم تصدير النتائج بنجاح إلى ملف أكسل يدعم العربية"
            } catch (e: Exception) {
                _statusMessage.value = "خطأ أثناء التصدير: ${e.localizedMessage}"
            }
        }
    }

    // Room Project Persistence
    fun saveCurrentProject(title: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val rulesArray = org.json.JSONArray()
            _extractionRules.value.forEach { r ->
                val obj = org.json.JSONObject()
                obj.put("id", r.id)
                obj.put("label", r.label)
                obj.put("cssSelector", r.cssSelector)
                rulesArray.put(obj)
            }

            val project = ScraperProject(
                title = title.ifBlank { "مشروع استخراج ${_targetUrl.value}" },
                targetUrl = _targetUrl.value,
                idSelector = _idSelector.value,
                birthYearSelector = _birthYearSelector.value,
                submitSelector = _submitSelector.value,
                extractionRulesJson = rulesArray.toString()
            )

            repository.saveProject(project)
            _statusMessage.value = "تم حفظ إعدادات المشروع في القاعدة بنجاح"
        }
    }

    fun loadProject(project: ScraperProject) {
        _targetUrl.value = project.targetUrl
        _idSelector.value = project.idSelector
        _birthYearSelector.value = project.birthYearSelector
        _submitSelector.value = project.submitSelector

        val rulesList = mutableListOf<ExtractionRule>()
        try {
            val jsonArray = org.json.JSONArray(project.extractionRulesJson)
            for (i in 0 until jsonArray.length()) {
                val obj = jsonArray.getJSONObject(i)
                rulesList.add(
                    ExtractionRule(
                        id = obj.optString("id", System.currentTimeMillis().toString()),
                        label = obj.optString("label"),
                        cssSelector = obj.optString("cssSelector")
                    )
                )
            }
        } catch (e: Exception) {}

        _extractionRules.value = rulesList
        _currentStep.value = 1 // Go to URL setup
        _statusMessage.value = "تم تحميل إعدادات المشروع: ${project.title}"
    }

    fun deleteProject(id: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            repository.deleteProject(id)
            _statusMessage.value = "تم حذف المشروع"
        }
    }

    fun clearStatusMessage() {
        _statusMessage.value = null
    }
}
