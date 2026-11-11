package com.example.ui.screens

import android.widget.Toast
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.example.ui.components.NeonGlowingCard
import com.example.ui.components.StatusBadge
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

@Composable
fun ProfileScreen(
    language: String,
    onLanguageChange: (String) -> Unit,
    speechManager: SpeechManager,
    onClearAllHistory: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showClearConfirm by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }
    var hapticsEnabled by remember { mutableStateOf(true) }
    var speechSpeed by remember { mutableFloatStateOf(1.0f) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("profile_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Profile Identity Header
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = true) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                listOf(NeonPinkPrimary, NeonPinkDark, Color.Black)
                            )
                        )
                        .border(1.5.dp, NeonPinkPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.AutoAwesome,
                        contentDescription = "MG AI",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = "MG AI Assistant",
                        color = TextPrimary,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 0.5.sp
                    )
                    Text(
                        text = "Intelligence That Understands",
                        color = NeonPinkSecondary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Native Android v1.0 • Gemini 3.8 Flash",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
            }
        }

        // Security & App Check Status Card
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = false) {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = "Security",
                            tint = SuccessGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Firebase App Check",
                            color = TextPrimary,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    StatusBadge(text = "ZERO-TRUST ACTIVE", isActive = true)
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "App Check cryptographically attests that API requests originate from your genuine MG AI app instance. No API keys are bundled or exposed.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 17.sp
                )
            }
        }

        // Language & Localization Preferences
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = false) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Translate,
                        contentDescription = null,
                        tint = NeonPinkPrimary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "hi") "भाषा प्राथमिकता (Language)" else "Language Preference",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    listOf("en" to "English (US)", "hi" to "हिन्दी (India)").forEach { (code, label) ->
                        val isSelected = language == code
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) NeonPinkContainer else CyberCardElevated)
                                .border(
                                    1.dp,
                                    if (isSelected) NeonPinkPrimary else CyberBorder,
                                    RoundedCornerShape(12.dp)
                                )
                                .clickable { onLanguageChange(code) }
                                .padding(vertical = 10.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) NeonPinkLight else TextSecondary,
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

        // Voice & Audio Output Tuning
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = false) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = CyberVioletAccent,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (language == "hi") "वाक् गति (Speech Rate)" else "Voice Output Speed",
                        color = TextPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = String.format("%.1fx", speechSpeed),
                        color = NeonPinkLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (speechSpeed < 1.0f) "Slow" else if (speechSpeed > 1.0f) "Fast" else "Normal",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }

                Slider(
                    value = speechSpeed,
                    onValueChange = { speechSpeed = it },
                    valueRange = 0.7f..1.5f,
                    steps = 7,
                    colors = SliderDefaults.colors(
                        thumbColor = NeonPinkPrimary,
                        activeTrackColor = NeonPinkPrimary,
                        inactiveTrackColor = CyberBorder
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                // Test Audio sample button
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .clickable {
                            val sample = if (language == "hi")
                                "नमस्ते, मैं MG AI हूँ। Intelligence That Understands."
                            else
                                "Hello, I am MG AI. Intelligence That Understands."
                            speechManager.speak(sample, language, speechRate = speechSpeed)
                        }
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = if (language == "hi") "वाक् नमूना सुनें" else "Test Voice Speech",
                        color = TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }

        // Privacy and Data Controls
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = false) {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = if (language == "hi") "गोपनीयता और डेटा नियंत्रण" else "Privacy & Local Data Controls",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                // View Privacy Policy
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .clickable { showPrivacyDialog = true }
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Info,
                            contentDescription = null,
                            tint = CyberCyanAccent,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == "hi") "गोपनीयता नीति और अनुमति विवरण" else "Privacy & Permissions Transparency",
                            color = TextPrimary,
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "View",
                        color = NeonPinkPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Clear Chat History Action
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberCardElevated)
                        .clickable { showClearConfirm = true }
                        .padding(12.dp)
                        .testTag("clear_history_row"),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = ErrorRed,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = if (language == "hi") "सभी चैट इतिहास साफ़ करें" else "Clear Local Chat History",
                            color = Color(0xFFFFB4AB),
                            fontSize = 13.sp
                        )
                    }
                    Text(
                        text = "Clear",
                        color = ErrorRed,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }

    // Clear Confirmation Dialog
    if (showClearConfirm) {
        AlertDialog(
            onDismissRequest = { showClearConfirm = false },
            containerColor = CyberDarkSurface,
            title = {
                Text(
                    text = if (language == "hi") "इतिहास साफ़ करें?" else "Clear Chat History?",
                    color = TextPrimary,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Text(
                    text = if (language == "hi")
                        "क्या आप वास्तव में अपने डिवाइस से सभी सहेजे गए चैट सत्र और संदेश हटाना चाहते हैं?"
                    else
                        "This will permanently delete all local chat sessions and messages stored on this device.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAllHistory()
                        showClearConfirm = false
                        Toast.makeText(context, if (language == "hi") "इतिहास साफ़ कर दिया गया" else "History cleared", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.testTag("confirm_clear_history_button")
                ) {
                    Text("Delete All", color = ErrorRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirm = false }) {
                    Text("Cancel", color = TextSecondary)
                }
            }
        )
    }

    // Privacy Transparency Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            containerColor = CyberDarkSurface,
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Security,
                        contentDescription = null,
                        tint = NeonPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Privacy & Security in MG AI",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "1. Zero-Trust API Architecture",
                        color = NeonPinkLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "MG AI does not bundle or expose static Gemini API keys in the app. Model inference is routed through Firebase AI Logic with Firebase App Check hardware/reCAPTCHA token attestation.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "2. Microphone Access",
                        color = NeonPinkLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Microphone permission is strictly activated on-demand when you explicitly tap the mic button. No ambient or continuous recording occurs.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )

                    Text(
                        text = "3. Device-Local Storage",
                        color = NeonPinkLight,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Chat history, notes, and reminders are stored purely within an encrypted on-device SQLite Room database and are never sold or transmitted to third parties.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("Done", color = NeonPinkPrimary, fontWeight = FontWeight.Bold)
                }
            }
        )
    }
}
