package com.example.ui

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.CampusDatabase
import com.example.data.local.CollegeNoticeEntity
import com.example.data.local.CourseEntity
import com.example.data.local.FacultyEntity
import com.example.data.local.FaqEntity
import com.example.data.repository.CampusRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Locale

enum class AppScreen {
    LANDING,
    CHAT,
    STUDENT_DASHBOARD,
    ADMIN_PANEL,
    PROJECT_DOCS
}

enum class AvatarState(val stateName: String) {
    IDLE("idle"),
    LISTENING("listening"),
    THINKING("thinking"),
    SPEAKING("speaking")
}

class CampusViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: CampusRepository
    private var tts: TextToSpeech? = null
    private var speechRecognizer: SpeechRecognizer? = null

    // Navigation State
    private val _currentScreen = MutableStateFlow(AppScreen.LANDING)
    val currentScreen: StateFlow<AppScreen> = _currentScreen.asStateFlow()

    // 3D Avatar State
    private val _avatarState = MutableStateFlow(AvatarState.IDLE)
    val avatarState: StateFlow<AvatarState> = _avatarState.asStateFlow()

    // Chat State
    private val _chatInput = MutableStateFlow("")
    val chatInput: StateFlow<String> = _chatInput.asStateFlow()

    private val _isProcessing = MutableStateFlow(false)
    val isProcessing: StateFlow<Boolean> = _isProcessing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _lastAiResponse = MutableStateFlow<String?>(null)
    val lastAiResponse: StateFlow<String?> = _lastAiResponse.asStateFlow()

    // Voice states
    private val _isVoiceListening = MutableStateFlow(false)
    val isVoiceListening: StateFlow<Boolean> = _isVoiceListening.asStateFlow()

    private val _isSpeakingAudio = MutableStateFlow(false)
    val isSpeakingAudio: StateFlow<Boolean> = _isSpeakingAudio.asStateFlow()

    private val _speechRmsLevel = MutableStateFlow(0f)
    val speechRmsLevel: StateFlow<Float> = _speechRmsLevel.asStateFlow()

    private val _voiceStatusMessage = MutableStateFlow<String?>(null)
    val voiceStatusMessage: StateFlow<String?> = _voiceStatusMessage.asStateFlow()

    // Admin state
    private val _isAdminLoggedIn = MutableStateFlow(false)
    val isAdminLoggedIn: StateFlow<Boolean> = _isAdminLoggedIn.asStateFlow()

    private val _adminTab = MutableStateFlow(0) // 0: Stats, 1: Notices, 2: Courses, 3: Faculty, 4: FAQs
    val adminTab: StateFlow<Int> = _adminTab.asStateFlow()

    // Database reactive streams
    val notices = MutableStateFlow<List<CollegeNoticeEntity>>(emptyList())
    val courses = MutableStateFlow<List<CourseEntity>>(emptyList())
    val faculty = MutableStateFlow<List<FacultyEntity>>(emptyList())
    val faqs = MutableStateFlow<List<FaqEntity>>(emptyList())

    val chatMessages = repositoryFlowWrapper()

    val studentProfile = CampusDatabase.getDatabase(application).campusDao().getStudentProfile()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val examSchedule = CampusDatabase.getDatabase(application).campusDao().getExamSchedule()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val placementStats = CampusDatabase.getDatabase(application).campusDao().getPlacementStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        val db = CampusDatabase.getDatabase(application)
        repository = CampusRepository(db.campusDao())

        viewModelScope.launch {
            repository.initializeData()
            repository.allNotices.collect { notices.value = it }
        }
        viewModelScope.launch {
            repository.allCourses.collect { courses.value = it }
        }
        viewModelScope.launch {
            repository.allFaculty.collect { faculty.value = it }
        }
        viewModelScope.launch {
            repository.allFaqs.collect { faqs.value = it }
        }

        initTTS(application)
    }

    private fun repositoryFlowWrapper() =
        CampusDatabase.getDatabase(getApplication()).campusDao().getAllChatMessages()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private fun initTTS(context: Context) {
        tts = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
            }
        }
    }

    fun navigateTo(screen: AppScreen) {
        _currentScreen.value = screen
        if (screen != AppScreen.CHAT) {
            stopAudioSpeech()
        }
    }

    fun onChatInputChange(newText: String) {
        _chatInput.value = newText
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun sendChatMessage(overridePrompt: String? = null, isVoice: Boolean = false) {
        val prompt = overridePrompt ?: _chatInput.value.trim()
        if (prompt.isEmpty() || _isProcessing.value) return

        _chatInput.value = ""
        _isProcessing.value = true
        _avatarState.value = AvatarState.THINKING

        viewModelScope.launch {
            try {
                val response = repository.askAssistant(prompt, isVoice = isVoice)
                _lastAiResponse.value = response
                _avatarState.value = AvatarState.IDLE

                // If query originated from voice or student preference, speak it
                if (isVoice) {
                    speakText(response)
                }
            } catch (e: Exception) {
                Log.e("CampusViewModel", "Error processing message", e)
                _avatarState.value = AvatarState.IDLE
            } finally {
                _isProcessing.value = false
            }
        }
    }

    fun regenerateLastResponse() {
        val lastAnswer = _lastAiResponse.value
        if (!lastAnswer.isNullOrBlank()) {
            sendChatMessage("Tell me more in detail about that last answer", isVoice = false)
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            repository.clearChat()
            stopAudioSpeech()
            _avatarState.value = AvatarState.IDLE
        }
    }

    fun speakText(text: String) {
        stopAudioSpeech()
        // Strip markdown asterisks and bullet points for natural speech
        val cleanedText = text.replace("**", "").replace("*", "").replace("•", "")
        _avatarState.value = AvatarState.SPEAKING
        _isSpeakingAudio.value = true

        tts?.speak(cleanedText, TextToSpeech.QUEUE_FLUSH, null, "CAMPUS_AI_TTS")

        // Auto reset speaking state after proportional duration
        val estimatedDurationMs = (cleanedText.split(" ").size * 320L).coerceIn(2000L, 20000L)
        viewModelScope.launch {
            kotlinx.coroutines.delay(estimatedDurationMs)
            if (_isSpeakingAudio.value) {
                _isSpeakingAudio.value = false
                _avatarState.value = AvatarState.IDLE
            }
        }
    }

    fun stopAudioSpeech() {
        if (_isSpeakingAudio.value) {
            tts?.stop()
            _isSpeakingAudio.value = false
            _avatarState.value = AvatarState.IDLE
        }
    }

    fun toggleVoiceRecognition(context: Context) {
        if (_isVoiceListening.value) {
            stopVoiceRecognition()
        } else {
            startVoiceRecognition(context)
        }
    }

    fun startVoiceRecognition(context: Context) {
        stopAudioSpeech()
        if (SpeechRecognizer.isRecognitionAvailable(context)) {
            _isVoiceListening.value = true
            _avatarState.value = AvatarState.LISTENING
            _voiceStatusMessage.value = "Listening... Speak your question now"

            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(object : RecognitionListener {
                        override fun onReadyForSpeech(params: Bundle?) {
                            _voiceStatusMessage.value = "Microphone ready, listening..."
                        }
                        override fun onBeginningOfSpeech() {
                            _voiceStatusMessage.value = "Capturing speech..."
                        }
                        override fun onRmsChanged(rmsdB: Float) {
                            _speechRmsLevel.value = (rmsdB + 2f).coerceAtLeast(0f)
                        }
                        override fun onBufferReceived(buffer: ByteArray?) {}
                        override fun onEndOfSpeech() {
                            _isVoiceListening.value = false
                            _voiceStatusMessage.value = "Processing your query..."
                        }
                        override fun onError(error: Int) {
                            _isVoiceListening.value = false
                            _avatarState.value = AvatarState.IDLE
                            val errorText = when (error) {
                                SpeechRecognizer.ERROR_NO_MATCH -> "No speech recognized. Tap mic to retry."
                                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> "No speech detected. Tap mic to retry."
                                SpeechRecognizer.ERROR_AUDIO -> "Audio recording error. Check microphone."
                                SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS -> "Microphone permission required."
                                SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT -> "Network issue while recognizing speech."
                                SpeechRecognizer.ERROR_RECOGNIZER_BUSY -> "Voice recognizer is busy. Please wait a moment."
                                else -> "Speech recognition ended. Tap mic to speak again."
                            }
                            _voiceStatusMessage.value = errorText
                        }
                        override fun onResults(results: Bundle?) {
                            _isVoiceListening.value = false
                            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val text = matches?.firstOrNull()?.trim()
                            if (!text.isNullOrBlank()) {
                                _voiceStatusMessage.value = "Recognized: \"$text\""
                                sendChatMessage(text, isVoice = true)
                            } else {
                                _voiceStatusMessage.value = "No speech detected. Tap mic to retry."
                                _avatarState.value = AvatarState.IDLE
                            }
                        }
                        override fun onPartialResults(partialResults: Bundle?) {
                            val matches = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                            val partialText = matches?.firstOrNull()
                            if (!partialText.isNullOrBlank()) {
                                _voiceStatusMessage.value = "Hearing: \"$partialText\"..."
                            }
                        }
                        override fun onEvent(eventType: Int, params: Bundle?) {}
                    })
                }

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                    putExtra(RecognizerIntent.EXTRA_PROMPT, "CampusAI is listening to your question...")
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                }
                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isVoiceListening.value = false
                _avatarState.value = AvatarState.IDLE
                _voiceStatusMessage.value = "Could not initialize voice recognizer: ${e.message}"
            }
        } else {
            _isVoiceListening.value = false
            _avatarState.value = AvatarState.IDLE
            _voiceStatusMessage.value = "Speech recognition service is not available on this device. Use text input or speech dialog."
        }
    }

    fun onVoiceSpeechResult(recognizedText: String) {
        val clean = recognizedText.trim()
        if (clean.isNotBlank()) {
            _voiceStatusMessage.value = "Recognized: \"$clean\""
            sendChatMessage(clean, isVoice = true)
        }
    }

    fun dismissVoiceStatus() {
        _voiceStatusMessage.value = null
    }

    fun stopVoiceRecognition() {
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.destroy()
        } catch (_: Exception) {}
        speechRecognizer = null
        _isVoiceListening.value = false
        _avatarState.value = AvatarState.IDLE
    }

    // Admin features
    fun loginAdmin(password: String): Boolean {
        if (password == "admin123" || password == "campus2026") {
            _isAdminLoggedIn.value = true
            return true
        }
        return false
    }

    fun logoutAdmin() {
        _isAdminLoggedIn.value = false
    }

    fun setAdminTab(index: Int) {
        _adminTab.value = index
    }

    fun createNotice(title: String, category: String, department: String, content: String) {
        viewModelScope.launch {
            val notice = CollegeNoticeEntity(
                title = title,
                category = category,
                department = department,
                date = "2026-03-18",
                content = content,
                isPinned = category == "Urgent"
            )
            repository.addNotice(notice)
        }
    }

    fun removeNotice(id: Int) {
        viewModelScope.launch {
            repository.deleteNotice(id)
        }
    }

    fun createCourse(code: String, name: String, dept: String, degree: String, duration: String, fees: String, intake: Int, eligibility: String, desc: String) {
        viewModelScope.launch {
            val course = CourseEntity(
                code = code,
                name = name,
                department = dept,
                degree = degree,
                duration = duration,
                feesPerYear = fees,
                intake = intake,
                eligibility = eligibility,
                description = desc
            )
            repository.addCourse(course)
        }
    }

    fun removeCourse(id: Int) {
        viewModelScope.launch {
            repository.deleteCourse(id)
        }
    }

    fun createFaculty(name: String, dept: String, designation: String, qual: String, email: String, cabin: String, officeHours: String) {
        viewModelScope.launch {
            val f = FacultyEntity(
                name = name,
                department = dept,
                designation = designation,
                qualification = qual,
                email = email,
                cabin = cabin,
                officeHours = officeHours
            )
            repository.addFaculty(f)
        }
    }

    fun removeFaculty(id: Int) {
        viewModelScope.launch {
            repository.deleteFaculty(id)
        }
    }

    fun createFaq(question: String, answer: String, category: String, keywords: String) {
        viewModelScope.launch {
            val faq = FaqEntity(
                question = question,
                answer = answer,
                category = category,
                keywords = keywords.lowercase(Locale.ROOT)
            )
            repository.addFaq(faq)
        }
    }

    fun removeFaq(id: Int) {
        viewModelScope.launch {
            repository.deleteFaq(id)
        }
    }

    override fun onCleared() {
        super.onCleared()
        tts?.stop()
        tts?.shutdown()
        speechRecognizer?.destroy()
    }
}
