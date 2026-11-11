package com.example

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubble
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.ui.components.FuturisticHeader
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.ProfileScreen
import com.example.ui.screens.ToolsScreen
import com.example.ui.screens.VoiceScreen
import com.example.ui.theme.CyberBlackBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SpeechManager
import com.example.util.configureAppCheck

data class NavItem(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector
)

class MainActivity : ComponentActivity() {

    private lateinit var speechManager: SpeechManager
    private val viewModel: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // STEP 1: Mandatory App Check contract wiring with launch intent before UI/Firebase
        configureAppCheck(this, intent)

        speechManager = SpeechManager(this)

        enableEdgeToEdge()

        setContent {
            MyApplicationTheme {
                MainAppContent(
                    viewModel = viewModel,
                    speechManager = speechManager
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        speechManager.shutdown()
    }
}

@Composable
fun MainAppContent(
    viewModel: AppViewModel,
    speechManager: SpeechManager
) {
    val context = LocalContext.current
    val currentScreen by viewModel.currentScreen.collectAsState()
    val language by viewModel.language.collectAsState()
    val selectedToolsTab by viewModel.selectedToolsTab.collectAsState()

    val sessions by viewModel.sessions.collectAsState()
    val currentSessionId by viewModel.currentSessionId.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val isGenerating by viewModel.isGenerating.collectAsState()
    val streamingText by viewModel.streamingText.collectAsState()
    val chatErrorMessage by viewModel.chatErrorMessage.collectAsState()

    val notes by viewModel.notes.collectAsState()
    val reminders by viewModel.reminders.collectAsState()
    val voiceHistory by viewModel.voiceHistory.collectAsState()

    // Request permissions for microphone on first launch if not granted
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { _ -> }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
        }
    }

    // BackHandler: return to home screen when not on home
    BackHandler(enabled = currentScreen != "home") {
        viewModel.navigateTo("home")
    }

    val navItems = listOf(
        NavItem("home", "Home", "होम", Icons.Default.Home),
        NavItem("chats", "Chats", "चैट", Icons.Default.ChatBubble),
        NavItem("voice", "Voice", "ध्वनि", Icons.Default.Mic),
        NavItem("tools", "Tools", "टूल्स", Icons.Default.Widgets),
        NavItem("profile", "Profile", "प्रोफ़ाइल", Icons.Default.Person)
    )

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = CyberBlackBackground,
        topBar = {
            FuturisticHeader(
                currentLanguage = language,
                onLanguageToggle = { viewModel.toggleLanguage() }
            )
        },
        bottomBar = {
            // Futuristic Neon Bottom Navigation Bar
            Surface(
                color = CyberDarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(0.5.dp, CyberBorder, RoundedCornerShape(0.dp))
                    .testTag("bottom_navigation_bar")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceAround,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    navItems.forEach { item ->
                        val isSelected = currentScreen == item.id
                        val isCenterVoice = item.id == "voice"

                        if (isCenterVoice) {
                            // Elevated Glowing Center Voice Action
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isSelected)
                                            Brush.radialGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                                        else
                                            Brush.radialGradient(listOf(CyberCardElevated, CyberCardSurface))
                                    )
                                    .border(
                                        if (isSelected) 2.dp else 1.dp,
                                        if (isSelected) NeonPinkLight else CyberBorder,
                                        CircleShape
                                    )
                                    .clickable { viewModel.navigateTo("voice") }
                                    .testTag("bottom_nav_voice"),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Mic,
                                    contentDescription = "Voice",
                                    tint = if (isSelected) Color.White else NeonPinkPrimary,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        } else {
                            Column(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable { viewModel.navigateTo(item.id) }
                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                    .testTag("bottom_nav_${item.id}"),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.titleEn,
                                    tint = if (isSelected) NeonPinkPrimary else TextMuted,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.height(3.dp))
                                Text(
                                    text = if (language == "hi") item.titleHi else item.titleEn,
                                    color = if (isSelected) TextPrimary else TextMuted,
                                    fontSize = 10.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AnimatedContent(
                targetState = currentScreen,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "screen_transition"
            ) { screen ->
                when (screen) {
                    "home" -> HomeScreen(
                        language = language,
                        recentSessions = sessions,
                        onNavigateToChatWithPrompt = { prompt ->
                            viewModel.startNewChat()
                            viewModel.sendMessage(prompt)
                        },
                        onNavigateToChat = { viewModel.navigateTo("chats") },
                        onNavigateToVoice = { viewModel.navigateTo("voice") },
                        onNavigateToTools = { tab -> viewModel.navigateToTools(tab) }
                    )
                    "chats" -> ChatScreen(
                        currentSessionId = currentSessionId,
                        sessions = sessions,
                        messages = messages,
                        isGenerating = isGenerating,
                        streamingText = streamingText,
                        errorMessage = chatErrorMessage,
                        language = language,
                        speechManager = speechManager,
                        voiceHistory = voiceHistory,
                        onRecordVoiceCommand = { text -> viewModel.recordVoiceCommand(text) },
                        onDeleteVoiceCommand = { id -> viewModel.deleteVoiceCommand(id) },
                        onClearVoiceHistory = { viewModel.clearVoiceHistory() },
                        onSendMessage = { text -> viewModel.sendMessage(text) },
                        onSelectSession = { id -> viewModel.selectSession(id) },
                        onNewChat = { viewModel.startNewChat() },
                        onDeleteSession = { id -> viewModel.deleteSession(id) },
                        onClearError = { viewModel.clearChatError() }
                    )
                    "voice" -> VoiceScreen(
                        language = language,
                        onLanguageToggle = { viewModel.toggleLanguage() },
                        speechManager = speechManager,
                        aiRepository = viewModel.aiRepository,
                        onSaveToChatHistory = { q, a -> viewModel.saveVoiceExchange(q, a) }
                    )
                    "tools" -> ToolsScreen(
                        selectedTab = selectedToolsTab,
                        onTabSelected = { tab -> viewModel.selectToolsTab(tab) },
                        aiRepository = viewModel.aiRepository,
                        speechManager = speechManager,
                        notes = notes,
                        reminders = reminders,
                        language = language,
                        onSaveNote = { title, content, tag -> viewModel.saveNote(title, content, tag) },
                        onDeleteNote = { note -> viewModel.deleteNote(note) },
                        onAddReminder = { title, desc, prio -> viewModel.addReminder(title, desc, prio) },
                        onToggleReminderCompleted = { id, comp -> viewModel.toggleReminderCompleted(id, comp) },
                        onDeleteReminder = { rem -> viewModel.deleteReminder(rem) }
                    )
                    "profile" -> ProfileScreen(
                        language = language,
                        onLanguageChange = { lang -> viewModel.setLanguage(lang) },
                        speechManager = speechManager,
                        onClearAllHistory = { viewModel.clearAllHistory() }
                    )
                }
            }
        }
    }
}
