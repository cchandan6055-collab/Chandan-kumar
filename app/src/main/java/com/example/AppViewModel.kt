package com.example

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.ai.AiRepository
import com.example.data.local.AppDatabase
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import com.example.data.local.VoiceHistoryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val db = AppDatabase.getInstance(application)
    private val chatDao = db.chatDao()
    private val noteDao = db.noteDao()
    private val reminderDao = db.reminderDao()
    private val voiceHistoryDao = db.voiceHistoryDao()

    val aiRepository = AiRepository()

    // Navigation and Language State
    private val _currentScreen = MutableStateFlow("home")
    val currentScreen: StateFlow<String> = _currentScreen.asStateFlow()

    private val _selectedToolsTab = MutableStateFlow("images")
    val selectedToolsTab: StateFlow<String> = _selectedToolsTab.asStateFlow()

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language.asStateFlow()

    // Chat Sessions
    val sessions: StateFlow<List<ChatSessionEntity>> = chatDao.getAllSessions()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _currentSessionId = MutableStateFlow(UUID.randomUUID().toString())
    val currentSessionId: StateFlow<String> = _currentSessionId.asStateFlow()

    private val _messages = MutableStateFlow<List<ChatMessageEntity>>(emptyList())
    val messages: StateFlow<List<ChatMessageEntity>> = _messages.asStateFlow()

    // Generation State
    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingText = MutableStateFlow("")
    val streamingText: StateFlow<String> = _streamingText.asStateFlow()

    private val _chatErrorMessage = MutableStateFlow<String?>(null)
    val chatErrorMessage: StateFlow<String?> = _chatErrorMessage.asStateFlow()

    // Notes & Reminders
    val notes: StateFlow<List<NoteEntity>> = noteDao.getAllNotes()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val reminders: StateFlow<List<ReminderEntity>> = reminderDao.getAllReminders()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val voiceHistory: StateFlow<List<VoiceHistoryEntity>> = voiceHistoryDao.getAllVoiceHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        // Observe messages for current session
        viewModelScope.launch {
            _currentSessionId.collect { sessionId ->
                chatDao.getMessagesForSession(sessionId).collect { msgs ->
                    _messages.value = msgs
                }
            }
        }
    }

    fun navigateTo(screen: String) {
        _currentScreen.value = screen
    }

    fun navigateToTools(tab: String) {
        _selectedToolsTab.value = tab
        _currentScreen.value = "tools"
    }

    fun selectToolsTab(tab: String) {
        _selectedToolsTab.value = tab
    }

    fun toggleLanguage() {
        _language.value = if (_language.value == "en") "hi" else "en"
    }

    fun setLanguage(lang: String) {
        _language.value = lang
    }

    fun startNewChat() {
        _currentSessionId.value = UUID.randomUUID().toString()
        _streamingText.value = ""
        _chatErrorMessage.value = null
        _currentScreen.value = "chats"
    }

    fun selectSession(sessionId: String) {
        _currentSessionId.value = sessionId
        _streamingText.value = ""
        _chatErrorMessage.value = null
        _currentScreen.value = "chats"
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            chatDao.deleteSession(sessionId)
            chatDao.deleteMessagesForSession(sessionId)
            if (_currentSessionId.value == sessionId) {
                _currentSessionId.value = UUID.randomUUID().toString()
            }
        }
    }

    fun clearAllHistory() {
        viewModelScope.launch {
            chatDao.clearAllSessions()
            chatDao.clearAllMessages()
            _currentSessionId.value = UUID.randomUUID().toString()
        }
    }

    fun clearChatError() {
        _chatErrorMessage.value = null
    }

    fun sendMessage(userText: String, isVoice: Boolean = false) {
        val sessionId = _currentSessionId.value
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            // Ensure session exists
            var session = chatDao.getSessionById(sessionId)
            if (session == null) {
                val title = if (userText.length > 36) userText.substring(0, 33) + "..." else userText
                session = ChatSessionEntity(
                    id = sessionId,
                    title = title,
                    lastMessage = userText,
                    updatedAt = now,
                    language = _language.value
                )
                chatDao.insertSession(session)
            } else {
                chatDao.updateSession(session.copy(lastMessage = userText, updatedAt = now))
            }

            // Save user message
            val userMsg = ChatMessageEntity(
                sessionId = sessionId,
                role = "user",
                text = userText,
                timestamp = now,
                isVoice = isVoice,
                language = _language.value
            )
            chatDao.insertMessage(userMsg)
            if (isVoice) {
                recordVoiceCommand(userText, _language.value)
            }

            _isGenerating.value = true
            _streamingText.value = ""
            _chatErrorMessage.value = null

            val history = _messages.value.map { it.role to it.text }
            val accumulated = StringBuilder()

            try {
                aiRepository.streamChat(
                    prompt = userText,
                    history = history,
                    language = _language.value
                ).collect { chunk ->
                    accumulated.append(chunk)
                    _streamingText.value = accumulated.toString()
                }

                val replyText = accumulated.toString().trim()
                if (replyText.isNotEmpty()) {
                    val modelMsg = ChatMessageEntity(
                        sessionId = sessionId,
                        role = "model",
                        text = replyText,
                        timestamp = System.currentTimeMillis(),
                        isVoice = false,
                        language = _language.value
                    )
                    chatDao.insertMessage(modelMsg)
                    chatDao.updateSession(session.copy(lastMessage = replyText, updatedAt = System.currentTimeMillis()))
                } else {
                    _chatErrorMessage.value = "Model returned empty response."
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e("AppViewModel", "Chat streaming error", e)
                _chatErrorMessage.value = e.localizedMessage ?: "Failed to generate response."
            } finally {
                _isGenerating.value = false
                _streamingText.value = ""
            }
        }
    }

    fun saveVoiceExchange(query: String, answer: String) {
        val sessionId = _currentSessionId.value
        val now = System.currentTimeMillis()

        viewModelScope.launch {
            var session = chatDao.getSessionById(sessionId)
            if (session == null) {
                session = ChatSessionEntity(
                    id = sessionId,
                    title = if (query.length > 36) query.substring(0, 33) + "..." else query,
                    lastMessage = answer,
                    updatedAt = now,
                    language = _language.value
                )
                chatDao.insertSession(session)
            } else {
                chatDao.updateSession(session.copy(lastMessage = answer, updatedAt = now))
            }

            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    role = "user",
                    text = query,
                    timestamp = now,
                    isVoice = true,
                    language = _language.value
                )
            )
            recordVoiceCommand(query, _language.value)

            chatDao.insertMessage(
                ChatMessageEntity(
                    sessionId = sessionId,
                    role = "model",
                    text = answer,
                    timestamp = now + 1,
                    isVoice = true,
                    language = _language.value
                )
            )
        }
    }

    // Notes Operations
    fun saveNote(title: String, content: String, tag: String) {
        viewModelScope.launch {
            noteDao.insertNote(
                NoteEntity(
                    title = title,
                    content = content,
                    timestamp = System.currentTimeMillis(),
                    tag = tag
                )
            )
        }
    }

    fun deleteNote(note: NoteEntity) {
        viewModelScope.launch {
            noteDao.deleteNote(note)
        }
    }

    // Reminders Operations
    fun addReminder(title: String, description: String, priority: String) {
        viewModelScope.launch {
            reminderDao.insertReminder(
                ReminderEntity(
                    title = title,
                    description = description,
                    dueTimeMillis = System.currentTimeMillis() + (3600 * 1000),
                    isCompleted = false,
                    priority = priority
                )
            )
        }
    }

    fun toggleReminderCompleted(id: Long, completed: Boolean) {
        viewModelScope.launch {
            reminderDao.setCompleted(id, completed)
        }
    }

    fun deleteReminder(reminder: ReminderEntity) {
        viewModelScope.launch {
            reminderDao.deleteReminder(reminder)
        }
    }

    // Voice History Operations
    fun recordVoiceCommand(text: String, language: String = _language.value) {
        val trimmed = text.trim()
        if (trimmed.isNotBlank()) {
            viewModelScope.launch {
                voiceHistoryDao.insertVoiceCommand(
                    VoiceHistoryEntity(
                        text = trimmed,
                        timestamp = System.currentTimeMillis(),
                        language = language
                    )
                )
            }
        }
    }

    fun deleteVoiceCommand(id: Long) {
        viewModelScope.launch {
            voiceHistoryDao.deleteById(id)
        }
    }

    fun clearVoiceHistory() {
        viewModelScope.launch {
            voiceHistoryDao.clearAll()
        }
    }
}
