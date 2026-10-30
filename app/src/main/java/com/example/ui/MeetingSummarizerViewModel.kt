package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.toDomain
import com.example.data.local.toEntity
import com.example.data.model.EmailTone
import com.example.data.model.MeetingSummary
import com.example.data.model.MeetingType
import com.example.data.remote.GeminiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface Screen {
    data object Home : Screen
    data object Results : Screen
}

sealed interface SummarizerUiState {
    data object Idle : SummarizerUiState
    data object Loading : SummarizerUiState
    data class Success(val summary: MeetingSummary) : SummarizerUiState
    data class Error(val message: String) : SummarizerUiState
}

class MeetingSummarizerViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val summaryDao = database.summaryDao()
    private val geminiService = GeminiService()

    private val _notesText = MutableStateFlow("")
    val notesText: StateFlow<String> = _notesText.asStateFlow()

    private val _selectedMeetingType = MutableStateFlow(MeetingType.TEAM)
    val selectedMeetingType: StateFlow<MeetingType> = _selectedMeetingType.asStateFlow()

    private val _selectedEmailTone = MutableStateFlow(EmailTone.FORMAL)
    val selectedEmailTone: StateFlow<EmailTone> = _selectedEmailTone.asStateFlow()

    private val _isDarkMode = MutableStateFlow(false)
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    private val _currentScreen = MutableStateFlow<Screen>(Screen.Home)
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    private val _uiState = MutableStateFlow<SummarizerUiState>(SummarizerUiState.Idle)
    val uiState: StateFlow<SummarizerUiState> = _uiState.asStateFlow()

    private val _activeSummary = MutableStateFlow<MeetingSummary?>(null)
    val activeSummary: StateFlow<MeetingSummary?> = _activeSummary.asStateFlow()

    private val _isSaved = MutableStateFlow(false)
    val isSaved: StateFlow<Boolean> = _isSaved.asStateFlow()

    private val _toastMessage = MutableStateFlow<String?>(null)
    val toastMessage: StateFlow<String?> = _toastMessage.asStateFlow()

    val historyList: StateFlow<List<MeetingSummary>> = summaryDao.getAllSummaries()
        .map { entities -> entities.map { it.toDomain() } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun onNotesChanged(newText: String) {
        _notesText.value = newText
    }

    fun clearNotes() {
        _notesText.value = ""
        _toastMessage.value = "تم مسح الملاحظات"
    }

    fun setMeetingType(type: MeetingType) {
        _selectedMeetingType.value = type
    }

    fun setEmailTone(tone: EmailTone) {
        _selectedEmailTone.value = tone
    }

    fun toggleDarkMode() {
        _isDarkMode.value = !_isDarkMode.value
    }

    fun loadSampleNotes() {
        _notesText.value = SAMPLE_MEETING_NOTES
        _selectedMeetingType.value = MeetingType.TEAM
        _selectedEmailTone.value = EmailTone.FORMAL
        _toastMessage.value = "تم إدراج ملاحظات مثال تجريبي"
    }

    fun summarize() {
        val notes = _notesText.value.trim()
        if (notes.isBlank()) {
            _uiState.value = SummarizerUiState.Error("يرجى إدخال أو لصق ملاحظات الاجتماع أولاً.")
            return
        }
        if (notes.length < 15) {
            _uiState.value = SummarizerUiState.Error("الملاحظات قصيرة جداً. يرجى إدخال ملاحظات أكثر تفصيلاً للحصول على ملخص دقيق.")
            return
        }

        viewModelScope.launch {
            _uiState.value = SummarizerUiState.Loading
            try {
                val summary = geminiService.summarizeMeeting(
                    notes = notes,
                    meetingType = _selectedMeetingType.value,
                    tone = _selectedEmailTone.value
                )
                _activeSummary.value = summary
                _isSaved.value = false
                _uiState.value = SummarizerUiState.Success(summary)
                _currentScreen.value = Screen.Results
            } catch (e: Exception) {
                _uiState.value = SummarizerUiState.Error(
                    e.localizedMessage ?: "حدث خطأ أثناء تحليل الملاحظات. يرجى المحاولة مرة أخرى."
                )
            }
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        val current = _activeSummary.value ?: return
        val updatedTasks = current.tasks.map { task ->
            if (task.id == taskId) {
                task.copy(isCompleted = !task.isCompleted)
            } else {
                task
            }
        }
        val updatedSummary = current.copy(tasks = updatedTasks)
        _activeSummary.value = updatedSummary

        // If already saved in DB, update DB entry too
        if (updatedSummary.id > 0) {
            viewModelScope.launch {
                summaryDao.updateSummary(updatedSummary.toEntity())
            }
        }
    }

    fun updateSummaryTitle(newTitle: String) {
        val current = _activeSummary.value ?: return
        val updated = current.copy(title = newTitle)
        _activeSummary.value = updated

        if (updated.id > 0) {
            viewModelScope.launch {
                summaryDao.updateSummary(updated.toEntity())
            }
        }
    }

    fun saveCurrentSummary() {
        val current = _activeSummary.value ?: return
        viewModelScope.launch {
            val insertedId = summaryDao.insertSummary(current.toEntity())
            _activeSummary.value = current.copy(id = insertedId)
            _isSaved.value = true
            _toastMessage.value = "تم حفظ الملخص في الأرشيف ✓"
        }
    }

    fun openSummary(summary: MeetingSummary) {
        _activeSummary.value = summary
        _isSaved.value = true
        _uiState.value = SummarizerUiState.Success(summary)
        _currentScreen.value = Screen.Results
    }

    fun deleteSummary(summaryId: Long) {
        viewModelScope.launch {
            summaryDao.deleteById(summaryId)
            _toastMessage.value = "تم حذف الملخص"
            if (_activeSummary.value?.id == summaryId) {
                _currentScreen.value = Screen.Home
                _activeSummary.value = null
            }
        }
    }

    fun startNewSummary() {
        _currentScreen.value = Screen.Home
        _uiState.value = SummarizerUiState.Idle
    }

    fun navigateToHome() {
        _currentScreen.value = Screen.Home
    }

    fun showToast(message: String) {
        _toastMessage.value = message
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    companion object {
        val SAMPLE_MEETING_NOTES = """
اجتماع إطلاق التحديث الجديد لتطبيق الجوال (الإصدار 2.0)
التاريخ: اليوم 10:30 صباحاً
الحضور: أحمد (مدير المنتج)، سارة (قائدة التصميم)، عمر (المطور الرئيسي)، نورة (مسؤولة التسويق).

النقاط التي نوقشت:
- مراجعة التصاميم الجديدة لشاشات تسجيل الدخول وتجربة المستخدم السلسة.
- نتائج اختبارات الأداء الأخيرة أظهرت سرعة استجابة ممتازة بنسبة 35% أسرع لكن هناك تأخير بسيط في جلب الإشعارات على بعض الأجهزة القديمة.
- خطة الحملة الإعلانية الرقمية ومحتوى منصات التواصل الاجتماعي لزيادة التفاعل.
- تقرير النسخة التجريبية (Beta) أظهر رضا المستخدمين بنسبة 94%.

القرارات المتخذة:
- اعتماد التصميم النهائي للشاشات وتدفق المستخدم كما قدمته سارة.
- تأجيل موعد الإطلاق الرسمي 3 أيام لضمان حل مشكلة الإشعارات بالكامل وتفادي أي أخطاء برمجية.
- الموافقة على تخصيص ميزانية 4500 دولار للحملة التسويقية على تويتر ولينكد إن.

المهام والتكليفات المطلوبة:
- حل مشكلة تأخير جلب الإشعارات وإجراء اختبار شامل للأنظمة (المسؤول: عمر | الموعد: الخميس القادم | أولوية عاجلة جداً).
- تجهيز البانرات الإعلانية ومحتوى الفيديو القصير للحملة (المسؤول: سارة | الموعد: الأحد القادم | أولوية مرتفعة).
- صياغة جدول النشر التسويقي وتحديد الفئات المستهدفة (المسؤول: نورة | الموعد: نهاية الأسبوع | أولوية متوسطة).
- تحديث وثيقة ميزات الإصدار الجديد ومشاركتها مع فريق الدعم الفني وخدمة العملاء (المسؤول: أحمد | الموعد: خلال يومين | أولوية منخفضة).
""".trimIndent()
    }
}
