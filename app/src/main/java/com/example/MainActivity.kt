package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.ScraperViewModel
import com.example.ui.components.StepProgressBar
import com.example.ui.screens.*
import com.example.ui.theme.DataExtractorTheme

class MainActivity : ComponentActivity() {

    private val viewModel: ScraperViewModel by viewModels()

    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DataExtractorTheme {
                // Ensure complete Arabic Right-To-Left (RTL) Layout Direction
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {

                    val currentStep by viewModel.currentStep.collectAsStateWithLifecycle()
                    val fileName by viewModel.fileName.collectAsStateWithLifecycle()
                    val headers by viewModel.headers.collectAsStateWithLifecycle()
                    val parsedRows by viewModel.parsedRows.collectAsStateWithLifecycle()
                    val selectedIdColumn by viewModel.selectedIdColumn.collectAsStateWithLifecycle()
                    val selectedBirthYearColumn by viewModel.selectedBirthYearColumn.collectAsStateWithLifecycle()

                    val targetUrl by viewModel.targetUrl.collectAsStateWithLifecycle()
                    val idSelector by viewModel.idSelector.collectAsStateWithLifecycle()
                    val birthYearSelector by viewModel.birthYearSelector.collectAsStateWithLifecycle()
                    val submitSelector by viewModel.submitSelector.collectAsStateWithLifecycle()
                    val pickerMode by viewModel.pickerMode.collectAsStateWithLifecycle()

                    val testIdNumber by viewModel.testIdNumber.collectAsStateWithLifecycle()
                    val testBirthYear by viewModel.testBirthYear.collectAsStateWithLifecycle()
                    val extractionRules by viewModel.extractionRules.collectAsStateWithLifecycle()

                    val batchResults by viewModel.batchResults.collectAsStateWithLifecycle()
                    val isBatchRunning by viewModel.isBatchRunning.collectAsStateWithLifecycle()
                    val isBatchPaused by viewModel.isBatchPaused.collectAsStateWithLifecycle()
                    val currentBatchIndex by viewModel.currentBatchIndex.collectAsStateWithLifecycle()

                    val savedProjects by viewModel.savedProjects.collectAsStateWithLifecycle()
                    val statusMessage by viewModel.statusMessage.collectAsStateWithLifecycle()

                    val snackbarHostState = remember { SnackbarHostState() }
                    var showSavedProjectsSheet by remember { mutableStateOf(false) }

                    // Display Snackbar on status message
                    LaunchedEffect(statusMessage) {
                        statusMessage?.let { msg ->
                            snackbarHostState.showSnackbar(msg)
                            viewModel.clearStatusMessage()
                        }
                    }

                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        topBar = {
                            CenterAlignedTopAppBar(
                                title = {
                                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                        Icon(imageVector = Icons.Default.TravelExplore, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                                        Text(
                                            text = "مستخرج بيانات الطلاب",
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                },
                                actions = {
                                    IconButton(onClick = { showSavedProjectsSheet = true }) {
                                        Badge(
                                            containerColor = MaterialTheme.colorScheme.primary
                                        ) {
                                            Text("${savedProjects.size}")
                                        }
                                        Icon(
                                            imageVector = Icons.Default.FolderSpecial,
                                            contentDescription = "المشاريع المحفوظة"
                                        )
                                    }
                                }
                            )
                        },
                        snackbarHost = { SnackbarHost(snackbarHostState) }
                    ) { innerPadding ->
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(innerPadding)
                        ) {
                            // Step Progress Bar
                            StepProgressBar(
                                currentStep = currentStep,
                                onStepClicked = { step -> viewModel.setStep(step) }
                            )

                            // Step Content Views
                            Box(modifier = Modifier.weight(1f)) {
                                when (currentStep) {
                                    0 -> ImportFileScreen(
                                        fileName = fileName,
                                        headers = headers,
                                        parsedRows = parsedRows,
                                        selectedIdColumn = selectedIdColumn,
                                        selectedBirthYearColumn = selectedBirthYearColumn,
                                        onFileSelected = { uri, name -> viewModel.importExcelFile(uri, name) },
                                        onLoadSampleData = { viewModel.loadSampleData() },
                                        onColumnsSelected = { idCol, yearCol -> viewModel.selectColumns(idCol, yearCol) },
                                        onNextClicked = { viewModel.nextStep() }
                                    )

                                    1 -> SetupWebTargetScreen(
                                        targetUrl = targetUrl,
                                        idSelector = idSelector,
                                        birthYearSelector = birthYearSelector,
                                        submitSelector = submitSelector,
                                        pickerMode = pickerMode,
                                        extractionRules = extractionRules,
                                        onUrlChanged = { url -> viewModel.setTargetUrl(url) },
                                        onPickerModeChanged = { mode -> viewModel.setPickerMode(mode) },
                                        onSelectorPicked = { mode, sel, txt -> viewModel.updateSelector(mode, sel, txt) },
                                        onAutoDetect = { viewModel.autoDetectSelectors() },
                                        onWebViewCreated = { wv -> viewModel.activeWebView = wv },
                                        onNextClicked = { viewModel.nextStep() },
                                        onPreviousClicked = { viewModel.previousStep() }
                                    )

                                    2 -> DefineExtractionRulesScreen(
                                        targetUrl = targetUrl,
                                        testIdNumber = testIdNumber,
                                        testBirthYear = testBirthYear,
                                        idSelector = idSelector,
                                        birthYearSelector = birthYearSelector,
                                        submitSelector = submitSelector,
                                        pickerMode = pickerMode,
                                        extractionRules = extractionRules,
                                        onTestDataChanged = { idNum, year -> viewModel.setTestData(idNum, year) },
                                        onExecuteTestQuery = { viewModel.executeTestQuery() },
                                        onPickerModeChanged = { mode -> viewModel.setPickerMode(mode) },
                                        onSelectorPicked = { mode, sel, txt -> viewModel.updateSelector(mode, sel, txt) },
                                        onAddExtractionRule = { label, sel -> viewModel.addExtractionRule(label, sel) },
                                        onRemoveExtractionRule = { id -> viewModel.removeExtractionRule(id) },
                                        onUpdateRuleLabel = { id, label -> viewModel.updateRuleLabel(id, label) },
                                        onWebViewCreated = { wv -> viewModel.activeWebView = wv },
                                        onNextClicked = { viewModel.nextStep() },
                                        onPreviousClicked = { viewModel.previousStep() }
                                    )

                                    3 -> BatchExecutionScreen(
                                        targetUrl = targetUrl,
                                        idSelector = idSelector,
                                        birthYearSelector = birthYearSelector,
                                        submitSelector = submitSelector,
                                        delayMs = viewModel.delayMs.value,
                                        extractionRules = extractionRules,
                                        batchResults = batchResults,
                                        isBatchRunning = isBatchRunning,
                                        isBatchPaused = isBatchPaused,
                                        currentBatchIndex = currentBatchIndex,
                                        onStartBatch = { viewModel.startBatchExtraction() },
                                        onPauseBatch = { viewModel.pauseBatchExtraction() },
                                        onStopBatch = { viewModel.stopBatchExtraction() },
                                        onWebViewCreated = { wv -> viewModel.activeWebView = wv },
                                        onNextClicked = { viewModel.nextStep() },
                                        onPreviousClicked = { viewModel.previousStep() }
                                    )

                                    4 -> ExportResultsScreen(
                                        results = batchResults,
                                        extractionFields = extractionRules.map { it.label },
                                        onExportToExcel = { uri -> viewModel.exportToExcel(uri) },
                                        onSaveProject = { title -> viewModel.saveCurrentProject(title) },
                                        onRestartProcess = { viewModel.setStep(0) }
                                    )
                                }
                            }
                        }

                        // Saved Projects Sheet Dialog
                        if (showSavedProjectsSheet) {
                            ModalBottomSheet(
                                onDismissRequest = { showSavedProjectsSheet = false }
                            ) {
                                SavedProjectsScreen(
                                    projects = savedProjects,
                                    onSelectProject = { project ->
                                        viewModel.loadProject(project)
                                        showSavedProjectsSheet = false
                                    },
                                    onDeleteProject = { id -> viewModel.deleteProject(id) },
                                    onClose = { showSavedProjectsSheet = false }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
