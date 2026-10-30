package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import com.example.ui.components.AppHeader
import com.example.ui.components.HistorySection
import com.example.ui.components.LoadingDialog
import com.example.ui.components.NotesInputSection
import com.example.ui.components.ResultView
import com.example.ui.theme.MeetingSummarizerTheme

@Composable
fun MeetingSummarizerApp(
    viewModel: MeetingSummarizerViewModel
) {
    val isDarkMode by viewModel.isDarkMode.collectAsState()
    val currentScreen by viewModel.currentScreen.collectAsState()
    val uiState by viewModel.uiState.collectAsState()
    val notesText by viewModel.notesText.collectAsState()
    val selectedMeetingType by viewModel.selectedMeetingType.collectAsState()
    val selectedEmailTone by viewModel.selectedEmailTone.collectAsState()
    val activeSummary by viewModel.activeSummary.collectAsState()
    val isSaved by viewModel.isSaved.collectAsState()
    val toastMessage by viewModel.toastMessage.collectAsState()
    val historyList by viewModel.historyList.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(toastMessage) {
        toastMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearToast()
        }
    }

    MeetingSummarizerTheme(darkTheme = isDarkMode) {
        // Enforce full RTL layout for Arabic
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
            Scaffold(
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("app_scaffold"),
                snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
                topBar = {
                    AppHeader(
                        isDarkMode = isDarkMode,
                        onToggleDarkMode = { viewModel.toggleDarkMode() },
                        showBackButton = currentScreen is Screen.Results,
                        onBackClick = { viewModel.navigateToHome() }
                    )
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .background(MaterialTheme.colorScheme.background),
                    contentAlignment = Alignment.TopCenter
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .widthIn(max = 760.dp)
                            .padding(horizontal = 16.dp, vertical = 14.dp)
                    ) {
                        when (currentScreen) {
                            is Screen.Home -> {
                                HomeScreen(
                                    notesText = notesText,
                                    onNotesChange = viewModel::onNotesChanged,
                                    selectedMeetingType = selectedMeetingType,
                                    onMeetingTypeChange = viewModel::setMeetingType,
                                    selectedEmailTone = selectedEmailTone,
                                    onEmailToneChange = viewModel::setEmailTone,
                                    onSummarizeClick = viewModel::summarize,
                                    onClearClick = viewModel::clearNotes,
                                    onSampleClick = viewModel::loadSampleNotes,
                                    isLoading = uiState is SummarizerUiState.Loading,
                                    errorMessage = (uiState as? SummarizerUiState.Error)?.message,
                                    historyList = historyList,
                                    onOpenSummary = viewModel::openSummary,
                                    onDeleteSummary = viewModel::deleteSummary
                                )
                            }

                            is Screen.Results -> {
                                BackHandler {
                                    viewModel.navigateToHome()
                                }

                                activeSummary?.let { summary ->
                                    ResultView(
                                        summary = summary,
                                        isSaved = isSaved,
                                        onToggleTask = viewModel::toggleTaskCompletion,
                                        onUpdateTitle = viewModel::updateSummaryTitle,
                                        onSaveSummary = viewModel::saveCurrentSummary,
                                        onNewSummary = viewModel::startNewSummary,
                                        onShowToast = viewModel::showToast
                                    )
                                }
                            }
                        }
                    }

                    // Loading overlay dialog
                    if (uiState is SummarizerUiState.Loading) {
                        LoadingDialog(message = "جاري تحليل الملاحظات...")
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeScreen(
    notesText: String,
    onNotesChange: (String) -> Unit,
    selectedMeetingType: com.example.data.model.MeetingType,
    onMeetingTypeChange: (com.example.data.model.MeetingType) -> Unit,
    selectedEmailTone: com.example.data.model.EmailTone,
    onEmailToneChange: (com.example.data.model.EmailTone) -> Unit,
    onSummarizeClick: () -> Unit,
    onClearClick: () -> Unit,
    onSampleClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String?,
    historyList: List<com.example.data.model.MeetingSummary>,
    onOpenSummary: (com.example.data.model.MeetingSummary) -> Unit,
    onDeleteSummary: (Long) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp)
    ) {
        NotesInputSection(
            notesText = notesText,
            onNotesChange = onNotesChange,
            selectedMeetingType = selectedMeetingType,
            onMeetingTypeChange = onMeetingTypeChange,
            selectedEmailTone = selectedEmailTone,
            onEmailToneChange = onEmailToneChange,
            onSummarizeClick = onSummarizeClick,
            onClearClick = onClearClick,
            onSampleClick = onSampleClick,
            isLoading = isLoading,
            errorMessage = errorMessage
        )

        Spacer(modifier = Modifier.height(24.dp))

        HistorySection(
            historyList = historyList,
            onOpenSummary = onOpenSummary,
            onDeleteSummary = onDeleteSummary
        )
    }
}
