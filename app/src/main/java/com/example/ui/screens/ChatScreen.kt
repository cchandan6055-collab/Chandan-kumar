package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Input
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.data.local.ChatMessageEntity
import com.example.data.local.ChatSessionEntity
import com.example.data.local.VoiceHistoryEntity
import com.example.ui.theme.CyberBlackBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderNeon
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberVioletAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.NeonPinkSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SpeechManager
import com.example.util.VoiceRecognizerHelper
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    currentSessionId: String,
    sessions: List<ChatSessionEntity>,
    messages: List<ChatMessageEntity>,
    isGenerating: Boolean,
    streamingText: String,
    errorMessage: String?,
    language: String,
    speechManager: SpeechManager,
    voiceHistory: List<VoiceHistoryEntity> = emptyList(),
    onRecordVoiceCommand: (String) -> Unit = {},
    onDeleteVoiceCommand: (Long) -> Unit = {},
    onClearVoiceHistory: () -> Unit = {},
    onSendMessage: (String) -> Unit,
    onSelectSession: (String) -> Unit,
    onNewChat: () -> Unit,
    onDeleteSession: (String) -> Unit,
    onClearError: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val listState = rememberLazyListState()
    var inputText by remember { mutableStateOf("") }
    var showHistorySheet by remember { mutableStateOf(false) }
    var showVoiceHistorySheet by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState()
    val voiceSheetState = rememberModalBottomSheetState()
    val isSpeaking by speechManager.isSpeaking.collectAsState()

    // Voice recognizer helper using Android SpeechRecognizer API
    val voiceHelper = remember { VoiceRecognizerHelper(context) }
    val isListening by voiceHelper.isListening.collectAsState()
    val partialVoiceText by voiceHelper.partialText.collectAsState()
    val rmsLevel by voiceHelper.rmsLevel.collectAsState()
    val voiceError by voiceHelper.errorMessage.collectAsState()

    var showVoiceRecordingInterface by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var voiceLanguage by remember { mutableStateOf(language) }

    LaunchedEffect(language) {
        voiceLanguage = language
    }

    DisposableEffect(voiceHelper) {
        onDispose {
            voiceHelper.destroy()
        }
    }

    fun triggerHaptic(strong: Boolean = false) {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createOneShot(
                        if (strong) 80 else 40,
                        VibrationEffect.DEFAULT_AMPLITUDE
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    // Recording duration timer
    LaunchedEffect(isListening) {
        if (isListening) {
            recordingSeconds = 0
            while (isListening) {
                delay(1000L)
                recordingSeconds += 1
            }
        }
    }

    fun startRecordingFlow() {
        speechManager.stop()
        triggerHaptic()
        showVoiceRecordingInterface = true

        voiceHelper.startListening(
            language = voiceLanguage,
            onFinalResult = { recognizedText ->
                if (recognizedText.isNotBlank()) {
                    inputText = recognizedText
                    onRecordVoiceCommand(recognizedText)
                }
            }
        )
    }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startRecordingFlow()
        } else {
            Toast.makeText(
                context,
                if (language == "hi") "आवाज़ से लिखने के लिए माइक्रोफ़ोन अनुमति आवश्यक है" else "Microphone permission required for voice-to-text",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    fun handleMicTap() {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
            if (isListening) {
                voiceHelper.stopListening()
                triggerHaptic()
            } else {
                startRecordingFlow()
            }
        } else {
            micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // Auto-scroll on new messages
    LaunchedEffect(messages.size, streamingText) {
        val totalItems = messages.size + (if (streamingText.isNotEmpty() || isGenerating) 1 else 0)
        if (totalItems > 0) {
            listState.animateScrollToItem(totalItems - 1)
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .imePadding()
            .testTag("chat_screen")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Chat Top Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberDarkSurface)
                    .border(0.5.dp, CyberBorder, RoundedCornerShape(0.dp))
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonPinkContainer)
                            .border(1.dp, NeonPinkPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            tint = NeonPinkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = if (language == "hi") "MG AI चैट" else "MG AI Chat",
                            color = TextPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "hi") "जेमिनी 3.8 फ़्लैश • वॉइस समर्थित" else "Gemini 3.8 Flash • Voice Enabled",
                            color = NeonPinkSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Voice History Button
                    IconButton(
                        onClick = { showVoiceHistorySheet = true },
                        modifier = Modifier.testTag("voice_history_top_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = "Voice History",
                            tint = if (voiceHistory.isNotEmpty()) NeonPinkPrimary else TextSecondary
                        )
                    }

                    // Chat History Button
                    IconButton(
                        onClick = { showHistorySheet = true },
                        modifier = Modifier.testTag("chat_history_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.History,
                            contentDescription = "Chat History",
                            tint = TextSecondary
                        )
                    }

                    // New Chat Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(NeonPinkPrimary)
                            .clickable { onNewChat() }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("new_chat_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "New Chat",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (language == "hi") "नया" else "New",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Error Message Banner (if any)
            AnimatedVisibility(visible = errorMessage != null) {
                errorMessage?.let { error ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(ErrorRed.copy(alpha = 0.2f))
                            .border(0.5.dp, ErrorRed, RoundedCornerShape(0.dp))
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = error,
                            color = Color(0xFFFFB4AB),
                            fontSize = 12.sp,
                            modifier = Modifier.weight(1f)
                        )
                        IconButton(onClick = { onClearError() }) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Dismiss",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Message List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(modifier = Modifier.height(8.dp)) }

                if (messages.isEmpty() && streamingText.isEmpty() && !isGenerating) {
                    item {
                        EmptyChatState(
                            language = language,
                            onSuggestionClick = { suggestion ->
                                onSendMessage(suggestion)
                            }
                        )
                    }
                }

                items(messages) { message ->
                    ChatMessageBubble(
                        message = message,
                        language = language,
                        isSpeaking = isSpeaking,
                        onSpeak = { text ->
                            speechManager.speak(text, language)
                        },
                        onStopSpeak = {
                            speechManager.stop()
                        },
                        onCopy = { text ->
                            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                            val clip = ClipData.newPlainText("MG AI Message", text)
                            clipboard.setPrimaryClip(clip)
                            Toast.makeText(context, if (language == "hi") "कॉपी किया गया" else "Copied to clipboard", Toast.LENGTH_SHORT).show()
                        }
                    )
                }

                // Streaming Live Message Bubble
                if (streamingText.isNotEmpty() || (isGenerating && messages.lastOrNull()?.role == "user")) {
                    item {
                        StreamingMessageBubble(
                            text = streamingText,
                            isLoading = isGenerating && streamingText.isEmpty()
                        )
                    }
                }

                item { Spacer(modifier = Modifier.height(12.dp)) }
            }

            // Voice-to-Text Embedded Recording Panel
            AnimatedVisibility(
                visible = showVoiceRecordingInterface,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                ChatVoiceRecordingPanel(
                    isListening = isListening,
                    rmsLevel = rmsLevel,
                    partialText = partialVoiceText,
                    recordingSeconds = recordingSeconds,
                    voiceLanguage = voiceLanguage,
                    voiceError = voiceError,
                    recentVoiceHistory = voiceHistory.take(4),
                    onOpenFullHistory = { showVoiceHistorySheet = true },
                    onSelectRecentVoice = { selectedPrompt ->
                        inputText = selectedPrompt
                        triggerHaptic()
                    },
                    onToggleVoiceLanguage = {
                        voiceLanguage = if (voiceLanguage == "en") "hi" else "en"
                        if (isListening) {
                            voiceHelper.stopListening()
                            voiceHelper.startListening(voiceLanguage) { recognized ->
                                if (recognized.isNotBlank()) {
                                    inputText = recognized
                                    onRecordVoiceCommand(recognized)
                                }
                            }
                        }
                    },
                    onStopRecording = {
                        voiceHelper.stopListening()
                        triggerHaptic()
                    },
                    onResumeRecording = {
                        startRecordingFlow()
                    },
                    onCancelRecording = {
                        voiceHelper.cancel()
                        showVoiceRecordingInterface = false
                        triggerHaptic()
                    },
                    onUseText = { text ->
                        if (text.isNotBlank()) {
                            inputText = text
                            onRecordVoiceCommand(text)
                        }
                        voiceHelper.cancel()
                        showVoiceRecordingInterface = false
                        triggerHaptic()
                    },
                    onSendNow = { text ->
                        voiceHelper.cancel()
                        showVoiceRecordingInterface = false
                        triggerHaptic(strong = true)
                        if (text.isNotBlank()) {
                            onRecordVoiceCommand(text)
                            onSendMessage(text)
                        } else if (inputText.isNotBlank()) {
                            val msg = inputText
                            inputText = ""
                            onSendMessage(msg)
                        }
                    }
                )
            }

            // Chat Input Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(CyberDarkSurface)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .navigationBarsPadding(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Microphone Recording Trigger Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (isListening)
                                Brush.radialGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                            else
                                Brush.radialGradient(listOf(CyberCardElevated, CyberCardSurface))
                        )
                        .border(
                            1.5.dp,
                            if (isListening) NeonPinkLight else CyberBorderNeon,
                            CircleShape
                        )
                        .clickable { handleMicTap() }
                        .testTag("chat_voice_input_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isListening) Icons.Default.MicOff else Icons.Default.Mic,
                        contentDescription = "Voice-to-Text Input",
                        tint = if (isListening) Color.White else NeonPinkPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = if (language == "hi") "MG AI से पूछें या माइक दबाएँ..." else "Ask MG AI or tap mic...",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPinkPrimary,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonPinkPrimary,
                        focusedContainerColor = CyberCardSurface,
                        unfocusedContainerColor = CyberCardSurface
                    ),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_input_field"),
                    maxLines = 4
                )

                Spacer(modifier = Modifier.width(8.dp))

                // Send Button
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(
                            if (inputText.isNotBlank() && !isGenerating)
                                Brush.radialGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                            else
                                Brush.radialGradient(listOf(CyberCardElevated, CyberCardSurface))
                        )
                        .border(
                            1.dp,
                            if (inputText.isNotBlank()) NeonPinkPrimary else CyberBorder,
                            CircleShape
                        )
                        .clickable(enabled = inputText.isNotBlank() && !isGenerating) {
                            val textToSend = inputText.trim()
                            if (textToSend.isNotEmpty()) {
                                inputText = ""
                                onSendMessage(textToSend)
                            }
                        }
                        .testTag("chat_send_button"),
                    contentAlignment = Alignment.Center
                ) {
                    if (isGenerating) {
                        CircularProgressIndicator(
                            color = NeonPinkPrimary,
                            strokeWidth = 2.dp,
                            modifier = Modifier.size(18.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Send",
                            tint = if (inputText.isNotBlank()) Color.White else TextMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }

        // Voice Commands History Bottom Sheet
        if (showVoiceHistorySheet) {
            VoiceHistoryBottomSheet(
                voiceHistory = voiceHistory,
                language = language,
                onDismiss = { showVoiceHistorySheet = false },
                sheetState = voiceSheetState,
                onInsertIntoInput = { text ->
                    inputText = if (inputText.isBlank()) text else "$inputText $text"
                    showVoiceHistorySheet = false
                    triggerHaptic()
                    Toast.makeText(context, if (language == "hi") "इनपुट में डाला गया" else "Inserted into chat", Toast.LENGTH_SHORT).show()
                },
                onSendDirectly = { text ->
                    showVoiceHistorySheet = false
                    triggerHaptic(strong = true)
                    onSendMessage(text)
                },
                onDeleteVoiceCommand = { id ->
                    onDeleteVoiceCommand(id)
                },
                onClearAll = {
                    onClearVoiceHistory()
                }
            )
        }

        // Chat Sessions History Bottom Sheet
        if (showHistorySheet) {
            ModalBottomSheet(
                onDismissRequest = { showHistorySheet = false },
                sheetState = sheetState,
                containerColor = CyberDarkSurface
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (language == "hi") "बातचीत का इतिहास" else "Chat History",
                            color = TextPrimary,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(NeonPinkPrimary)
                                .clickable {
                                    showHistorySheet = false
                                    onNewChat()
                                }
                                .padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Text(
                                text = if (language == "hi") "+ नई चैट" else "+ New Chat",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    if (sessions.isEmpty()) {
                        Text(
                            text = if (language == "hi") "कोई पुरानी बातचीत नहीं है" else "No saved chat sessions yet",
                            color = TextMuted,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(vertical = 24.dp)
                        )
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            items(sessions) { session ->
                                val isSelected = session.id == currentSessionId
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(if (isSelected) CyberCardElevated else CyberCardSurface)
                                        .border(
                                            1.dp,
                                            if (isSelected) NeonPinkPrimary else CyberBorder,
                                            RoundedCornerShape(12.dp)
                                        )
                                        .clickable {
                                            onSelectSession(session.id)
                                            showHistorySheet = false
                                        }
                                        .padding(12.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = session.title,
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = session.lastMessage.ifBlank { "No preview" },
                                            color = TextMuted,
                                            fontSize = 12.sp,
                                            maxLines = 1
                                        )
                                    }
                                    IconButton(
                                        onClick = { onDeleteSession(session.id) },
                                        modifier = Modifier.size(32.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete Session",
                                            tint = TextMuted,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

/**
 * Bottom Sheet displaying full history of transcribed voice commands
 * with search, relative timestamp, delete, and 1-tap re-insertion or sending.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun VoiceHistoryBottomSheet(
    voiceHistory: List<VoiceHistoryEntity>,
    language: String,
    onDismiss: () -> Unit,
    sheetState: androidx.compose.material3.SheetState,
    onInsertIntoInput: (String) -> Unit,
    onSendDirectly: (String) -> Unit,
    onDeleteVoiceCommand: (Long) -> Unit,
    onClearAll: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var showClearConfirmation by remember { mutableStateOf(false) }

    val filteredHistory = remember(voiceHistory, searchQuery) {
        if (searchQuery.isBlank()) {
            voiceHistory
        } else {
            voiceHistory.filter { it.text.contains(searchQuery.trim(), ignoreCase = true) }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = CyberDarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .testTag("voice_history_sheet")
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(NeonPinkContainer)
                            .border(1.dp, NeonPinkPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = NeonPinkPrimary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = if (language == "hi") "ध्वनि आदेश इतिहास" else "Voice Command History",
                            color = TextPrimary,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (language == "hi") "${voiceHistory.size} आदेश सहेजे गए" else "${voiceHistory.size} Transcriptions Saved",
                            color = NeonPinkSecondary,
                            fontSize = 11.sp
                        )
                    }
                }

                if (voiceHistory.isNotEmpty()) {
                    Text(
                        text = if (language == "hi") "साफ़ करें" else "Clear All",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clickable { showClearConfirmation = true }
                            .padding(4.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Search Bar for Voice History
            if (voiceHistory.size > 2) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = {
                        Text(
                            text = if (language == "hi") "ध्वनि इतिहास खोजें..." else "Search past voice queries...",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonPinkPrimary,
                        unfocusedBorderColor = CyberBorder,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary,
                        cursorColor = NeonPinkPrimary,
                        focusedContainerColor = CyberCardSurface,
                        unfocusedContainerColor = CyberCardSurface
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 10.dp),
                    singleLine = true
                )
            }

            if (filteredHistory.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Mic,
                            contentDescription = null,
                            tint = TextMuted,
                            modifier = Modifier.size(40.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = if (voiceHistory.isEmpty()) {
                                if (language == "hi") "कोई ध्वनि आदेश इतिहास नहीं है" else "No transcribed voice commands yet"
                            } else {
                                if (language == "hi") "कोई मेल नहीं मिला" else "No matching voice commands found"
                            },
                            color = TextSecondary,
                            fontSize = 13.sp
                        )
                        Text(
                            text = if (language == "hi") "माइक दबाकर बोलें और आदेश यहाँ सहेजे जाएँगे" else "Speak into the microphone to log voice queries here",
                            color = TextMuted,
                            fontSize = 11.sp
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(380.dp)
                ) {
                    items(filteredHistory, key = { it.id }) { item ->
                        val relativeTime = formatRelativeTime(item.timestamp)
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(CyberCardSurface)
                                .border(1.dp, CyberBorderNeon, RoundedCornerShape(12.dp))
                                .padding(12.dp)
                                .testTag("voice_history_item_${item.id}")
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(4.dp))
                                                .background(CyberCardElevated)
                                                .border(0.5.dp, CyberBorder, RoundedCornerShape(4.dp))
                                                .padding(horizontal = 6.dp, vertical = 2.dp)
                                        ) {
                                            Text(
                                                text = if (item.language == "hi") "HI" else "EN",
                                                color = NeonPinkLight,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(6.dp))
                                        Text(
                                            text = relativeTime,
                                            color = TextMuted,
                                            fontSize = 11.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = { onDeleteVoiceCommand(item.id) },
                                        modifier = Modifier.size(26.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = "Delete",
                                            tint = TextMuted,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(6.dp))

                                SelectionContainer {
                                    Text(
                                        text = item.text,
                                        color = TextPrimary,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.Medium,
                                        lineHeight = 18.sp
                                    )
                                }

                                Spacer(modifier = Modifier.height(10.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.End,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Insert into Chat Input Field
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(CyberCardElevated)
                                            .border(1.dp, CyberBorder, RoundedCornerShape(8.dp))
                                            .clickable { onInsertIntoInput(item.text) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("insert_voice_text_${item.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Input,
                                                contentDescription = null,
                                                tint = CyberCyanAccent,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (language == "hi") "इनपुट में जोड़ें" else "Insert to Chat",
                                                color = TextPrimary,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.SemiBold
                                            )
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(8.dp))

                                    // Send Immediately to Gemini
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(
                                                Brush.horizontalGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                                            )
                                            .clickable { onSendDirectly(item.text) }
                                            .padding(horizontal = 10.dp, vertical = 6.dp)
                                            .testTag("send_voice_direct_${item.id}"),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Icon(
                                                imageVector = Icons.Default.Send,
                                                contentDescription = null,
                                                tint = Color.White,
                                                modifier = Modifier.size(13.dp)
                                            )
                                            Spacer(modifier = Modifier.width(5.dp))
                                            Text(
                                                text = if (language == "hi") "भेजें" else "Send to AI",
                                                color = Color.White,
                                                fontSize = 11.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    if (showClearConfirmation) {
        AlertDialog(
            onDismissRequest = { showClearConfirmation = false },
            containerColor = CyberDarkSurface,
            title = {
                Text(
                    text = if (language == "hi") "ध्वनि इतिहास साफ़ करें?" else "Clear Voice History?",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "hi")
                        "क्या आप सभी ट्रांसक्राइब किए गए ध्वनि आदेशों को हटाना चाहते हैं?"
                    else
                        "Do you want to permanently clear all recorded voice commands from history?",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showClearConfirmation = false
                    }
                ) {
                    Text("Clear All", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmation = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }
}

private fun formatRelativeTime(timestamp: Long): String {
    val diff = System.currentTimeMillis() - timestamp
    val mins = diff / (1000 * 60)
    val hours = mins / 60
    val days = hours / 24
    return when {
        mins < 1 -> "Just now"
        mins < 60 -> "${mins}m ago"
        hours < 24 -> "${hours}h ago"
        else -> "${days}d ago"
    }
}

/**
 * Embedded Native Voice-to-Text Recording Panel with dynamic audio waveform,
 * real-time transcription, language selection, quick recent voice history chips, and action controls.
 */
@Composable
private fun ChatVoiceRecordingPanel(
    isListening: Boolean,
    rmsLevel: Float,
    partialText: String,
    recordingSeconds: Int,
    voiceLanguage: String,
    voiceError: String?,
    recentVoiceHistory: List<VoiceHistoryEntity> = emptyList(),
    onOpenFullHistory: () -> Unit,
    onSelectRecentVoice: (String) -> Unit,
    onToggleVoiceLanguage: () -> Unit,
    onStopRecording: () -> Unit,
    onResumeRecording: () -> Unit,
    onCancelRecording: () -> Unit,
    onUseText: (String) -> Unit,
    onSendNow: (String) -> Unit
) {
    val mins = recordingSeconds / 60
    val secs = recordingSeconds % 60
    val durationFormatted = String.format("%02d:%02d", mins, secs)

    // Pulse animation for recording badge
    val infiniteTransition = rememberInfiniteTransition(label = "rec_dot_pulse")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dotAlpha"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 6.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(CyberCardSurface)
            .border(1.5.dp, if (isListening) NeonPinkPrimary else CyberBorderNeon, RoundedCornerShape(18.dp))
            .padding(14.dp)
            .testTag("voice_recording_panel")
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            // Top Status Bar: Recording Indicator, Timer, Voice History Shortcut & Language Pill
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(
                                if (isListening) ErrorRed.copy(alpha = dotAlpha) else TextMuted
                            )
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isListening) "RECORDING" else "PAUSED",
                        color = if (isListening) NeonPinkLight else TextMuted,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = durationFormatted,
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Quick Voice History Sheet Trigger
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                            .clickable { onOpenFullHistory() }
                            .padding(horizontal = 7.dp, vertical = 4.dp)
                            .testTag("voice_panel_history_shortcut"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.RecordVoiceOver,
                                contentDescription = null,
                                tint = NeonPinkPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "History",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    // Speech Language Switch Pill
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorderNeon, RoundedCornerShape(14.dp))
                            .clickable { onToggleVoiceLanguage() }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                            .testTag("voice_panel_lang_toggle"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Translate,
                                contentDescription = null,
                                tint = NeonPinkPrimary,
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (voiceLanguage == "hi") "हिन्दी" else "EN",
                                color = TextPrimary,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            // Real-Time Audio Sound Wave Reactive Visualizer
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(34.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxWidth().height(34.dp)) {
                    val barCount = 32
                    val step = size.width / (barCount + 1)
                    val centerY = size.height / 2f

                    for (i in 0 until barCount) {
                        val x = step * (i + 1)
                        val centerDist = kotlin.math.abs((i - barCount / 2f) / (barCount / 2f))
                        val bellCurve = (1f - centerDist * 0.6f).coerceIn(0.2f, 1f)

                        val dynamicHeight = if (isListening) {
                            (size.height * rmsLevel * bellCurve * (0.8f + (i % 3) * 0.2f))
                                .coerceIn(4.dp.toPx(), size.height)
                        } else {
                            4.dp.toPx()
                        }

                        drawLine(
                            brush = Brush.verticalGradient(
                                colors = listOf(NeonPinkPrimary, CyberVioletAccent, CyberCyanAccent)
                            ),
                            start = Offset(x, centerY - dynamicHeight / 2),
                            end = Offset(x, centerY + dynamicHeight / 2),
                            strokeWidth = 2.5.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    }
                }
            }

            // Live Transcription Display Area
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(CyberCardElevated)
                    .padding(10.dp)
            ) {
                if (partialText.isNotBlank()) {
                    SelectionContainer {
                        Text(
                            text = partialText,
                            color = TextPrimary,
                            fontSize = 13.sp,
                            lineHeight = 18.sp
                        )
                    }
                } else if (voiceError != null) {
                    Text(
                        text = voiceError,
                        color = ErrorRed,
                        fontSize = 12.sp
                    )
                } else {
                    Text(
                        text = if (voiceLanguage == "hi")
                            "बोलें, आपकी आवाज़ यहाँ टेक्स्ट में दिखाई देगी..."
                        else
                            "Listening... speak naturally and your words will appear here.",
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            // Quick Recent Voice History Chips row (if any)
            if (recentVoiceHistory.isNotEmpty()) {
                Column {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Recent Voice Commands:",
                            color = TextMuted,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "All History",
                            color = NeonPinkPrimary,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.clickable { onOpenFullHistory() }
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        contentPadding = PaddingValues(end = 6.dp)
                    ) {
                        items(recentVoiceHistory) { hist ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(CyberCardElevated)
                                    .border(0.5.dp, CyberBorder, RoundedCornerShape(8.dp))
                                    .clickable { onSelectRecentVoice(hist.text) }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = if (hist.text.length > 24) hist.text.substring(0, 22) + "..." else hist.text,
                                    color = NeonPinkLight,
                                    fontSize = 11.sp,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }

            // Action Controls: Cancel, Toggle Listen, Insert to Input, Send Direct
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Cancel / Trash
                IconButton(
                    onClick = onCancelRecording,
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(CyberCardElevated)
                        .testTag("voice_cancel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cancel Recording",
                        tint = ErrorRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Pause / Resume Toggle
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardElevated)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                if (isListening) onStopRecording() else onResumeRecording()
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("voice_toggle_listen_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = null,
                                tint = NeonPinkPrimary,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (isListening) "Pause" else "Resume",
                                color = TextPrimary,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Use Text in TextField
                    if (partialText.isNotBlank()) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(CyberCardElevated)
                                .border(1.dp, CyberBorderNeon, RoundedCornerShape(10.dp))
                                .clickable { onUseText(partialText) }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("voice_insert_text_button"),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = null,
                                    tint = CyberCyanAccent,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Edit Text",
                                    color = TextPrimary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    // Send Directly to Gemini
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(
                                Brush.horizontalGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                            )
                            .clickable(enabled = partialText.isNotBlank()) {
                                onSendNow(partialText)
                            }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .testTag("voice_send_direct_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Send,
                                contentDescription = "Send",
                                tint = Color.White,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Send",
                                color = Color.White,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyChatState(
    language: String,
    onSuggestionClick: (String) -> Unit
) {
    val suggestions = if (language == "hi") {
        listOf(
            "MG AI क्या कर सकता है?",
            "साइबरपंक क्या है? सरल व्याख्या दें",
            "हिंदी में प्रेरणादायक उद्धरण लिखें",
            "जेटपैक कंपोज़ में स्टेट कैसे प्रबंधित करें?"
        )
    } else {
        listOf(
            "What can MG AI do?",
            "Explain Cyberpunk aesthetics simply",
            "Write a motivational thought for the day",
            "How does StateFlow work in Kotlin?"
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 40.dp, horizontal = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(CircleShape)
                .background(NeonPinkContainer)
                .border(1.5.dp, NeonPinkPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = NeonPinkPrimary,
                modifier = Modifier.size(32.dp)
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (language == "hi") "MG AI से संवाद शुरू करें" else "Commence Neural Dialogue",
            color = TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold
        )

        Text(
            text = if (language == "hi") "माइक दबाकर बोलें या प्रश्न लिखकर पूछें" else "Tap mic to speak or enter a prompt",
            color = TextSecondary,
            fontSize = 13.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            suggestions.forEach { suggestion ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, CyberBorder, RoundedCornerShape(12.dp))
                        .clickable { onSuggestionClick(suggestion) }
                        .padding(horizontal = 14.dp, vertical = 10.dp)
                ) {
                    Text(
                        text = "•  $suggestion",
                        color = NeonPinkLight,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    language: String,
    isSpeaking: Boolean,
    onSpeak: (String) -> Unit,
    onStopSpeak: () -> Unit,
    onCopy: (String) -> Unit
) {
    val isUser = message.role == "user"
    var speakingThisMessage by remember { mutableStateOf(false) }

    LaunchedEffect(isSpeaking) {
        if (!isSpeaking) {
            speakingThisMessage = false
        }
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start,
            verticalAlignment = Alignment.Top
        ) {
            if (!isUser) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(NeonPinkContainer)
                        .border(1.dp, NeonPinkPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = null,
                        tint = NeonPinkPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.85f)
                    .clip(
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .background(
                        if (isUser)
                            Brush.linearGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                        else
                            Brush.linearGradient(listOf(CyberCardSurface, CyberCardElevated))
                    )
                    .border(
                        1.dp,
                        if (isUser) NeonPinkLight.copy(alpha = 0.5f) else CyberBorderNeon,
                        RoundedCornerShape(
                            topStart = 16.dp,
                            topEnd = 16.dp,
                            bottomStart = if (isUser) 16.dp else 4.dp,
                            bottomEnd = if (isUser) 4.dp else 16.dp
                        )
                    )
                    .padding(14.dp)
            ) {
                Column {
                    if (isUser && message.isVoice) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(bottom = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice transcribed",
                                tint = Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(12.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "Voice Input",
                                color = Color.White.copy(alpha = 0.8f),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    SelectionContainer {
                        Text(
                            text = message.text,
                            color = if (isUser) Color.White else TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 20.sp
                        )
                    }

                    if (!isUser) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // TTS Speak / Stop Button
                            IconButton(
                                onClick = {
                                    if (speakingThisMessage) {
                                        onStopSpeak()
                                        speakingThisMessage = false
                                    } else {
                                        onSpeak(message.text)
                                        speakingThisMessage = true
                                    }
                                },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = if (speakingThisMessage) Icons.Default.Stop else Icons.Default.VolumeUp,
                                    contentDescription = "Speak Aloud",
                                    tint = if (speakingThisMessage) NeonPinkPrimary else TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }

                            // Copy Button
                            IconButton(
                                onClick = { onCopy(message.text) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy Message",
                                    tint = TextMuted,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StreamingMessageBubble(
    text: String,
    isLoading: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(NeonPinkContainer)
                .border(1.dp, NeonPinkPrimary, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = NeonPinkPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
        Spacer(modifier = Modifier.width(8.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .background(CyberCardSurface)
                .border(1.dp, CyberBorderNeon, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp, bottomStart = 4.dp, bottomEnd = 16.dp))
                .padding(14.dp)
        ) {
            if (isLoading && text.isEmpty()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        color = NeonPinkPrimary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "MG AI is thinking...",
                        color = NeonPinkLight,
                        fontSize = 13.sp
                    )
                }
            } else {
                Text(
                    text = text,
                    color = TextPrimary,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
