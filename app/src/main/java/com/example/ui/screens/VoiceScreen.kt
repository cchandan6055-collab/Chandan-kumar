package com.example.ui.screens

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiRepository
import com.example.ui.components.AudioWaveformVisualizer
import com.example.ui.components.NeonGlowingCard
import com.example.ui.components.NeonPulsingOrb
import com.example.ui.components.StatusBadge
import com.example.ui.theme.CyberBlackBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderNeon
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberVioletAccent
import com.example.ui.theme.ErrorRed
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.NeonPinkSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SpeechManager
import kotlinx.coroutines.launch

@Composable
fun VoiceScreen(
    language: String,
    onLanguageToggle: () -> Unit,
    speechManager: SpeechManager,
    aiRepository: AiRepository,
    onSaveToChatHistory: (String, String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val isSpeaking by speechManager.isSpeaking.collectAsState()

    var lastQuery by remember { mutableStateOf("") }
    var lastAnswer by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    fun triggerHaptic() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    val speechLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spoken = result.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)?.firstOrNull()
            if (!spoken.isNullOrBlank()) {
                lastQuery = spoken
                isProcessing = true
                errorMessage = null
                triggerHaptic()

                scope.launch {
                    try {
                        val answer = aiRepository.generateAnswer(spoken, language)
                        lastAnswer = answer
                        isProcessing = false
                        // Automatically speak out the response
                        speechManager.speak(answer, language)
                        onSaveToChatHistory(spoken, answer)
                    } catch (e: Exception) {
                        isProcessing = false
                        errorMessage = e.localizedMessage ?: "Voice processing failed."
                    }
                }
            }
        }
    }

    fun startListening() {
        speechManager.stop()
        triggerHaptic()
        try {
            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE,
                    if (language == "hi") "hi-IN" else "en-US"
                )
                putExtra(
                    RecognizerIntent.EXTRA_PROMPT,
                    if (language == "hi") "MG AI को बोलें..." else "Speak to MG AI..."
                )
            }
            speechLauncher.launch(intent)
        } catch (e: Exception) {
            Toast.makeText(
                context,
                if (language == "hi") "ध्वनि पहचान उपलब्ध नहीं है" else "Voice recognizer not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("voice_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Status bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusBadge(
                    text = if (isSpeaking)
                        (if (language == "hi") "बोल रहा हूँ..." else "Speaking Answer")
                    else if (isProcessing)
                        (if (language == "hi") "सोच रहा हूँ..." else "Neural Processing")
                    else
                        (if (language == "hi") "ध्वनि मोड तैयार" else "Voice Mode Active"),
                    isActive = isSpeaking || isProcessing
                )

                // Voice Language switch
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(16.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, CyberBorderNeon, RoundedCornerShape(16.dp))
                        .clickable { onLanguageToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = if (language == "hi") "🇮🇳 हिंदी" else "🌐 English",
                        color = TextPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Large Animated Glowing Orb
            Box(
                modifier = Modifier
                    .size(200.dp)
                    .clickable { startListening() }
                    .testTag("voice_orb_touch_target"),
                contentAlignment = Alignment.Center
            ) {
                NeonPulsingOrb(
                    isActive = isSpeaking || isProcessing,
                    size = 190.dp
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Waveform visualizer
            AudioWaveformVisualizer(
                isListening = isSpeaking || isProcessing,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = if (isProcessing) {
                    if (language == "hi") "उत्तर तैयार किया जा रहा है..." else "Synthesizing answer with Gemini..."
                } else if (isSpeaking) {
                    if (language == "hi") "MG AI बोल रहा है" else "MG AI is responding aloud"
                } else {
                    if (language == "hi") "बोलने के लिए माइक्रोफ़ोन या ओर्ब छुएँ" else "Tap Orb or Mic to Speak"
                },
                color = if (isProcessing || isSpeaking) NeonPinkLight else TextPrimary,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Text(
                text = if (language == "hi")
                    "उच्चारित प्रश्नों का तत्काल विश्लेषण और वाक् उत्तर"
                else
                    "Real-time voice query analysis with spoken audio response",
                color = TextSecondary,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 4.dp)
            )

            // Spoken Query & Answer Card
            AnimatedVisibility(visible = lastQuery.isNotEmpty() || lastAnswer.isNotEmpty()) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (lastQuery.isNotEmpty()) {
                        NeonGlowingCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowBorder = false
                        ) {
                            Column {
                                Text(
                                    text = if (language == "hi") "आपका प्रश्न:" else "You asked:",
                                    color = NeonPinkSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = lastQuery,
                                    color = TextPrimary,
                                    fontSize = 14.sp
                                )
                            }
                        }
                    }

                    if (lastAnswer.isNotEmpty()) {
                        NeonGlowingCard(
                            modifier = Modifier.fillMaxWidth(),
                            glowBorder = true
                        ) {
                            Column {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "MG AI:",
                                        color = NeonPinkPrimary,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                    if (isSpeaking) {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(NeonPinkDark)
                                                .clickable { speechManager.stop() }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.Stop,
                                                    contentDescription = "Stop",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (language == "hi") "रोकें" else "Stop",
                                                    color = Color.White,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    } else {
                                        Box(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(12.dp))
                                                .background(CyberCardElevated)
                                                .clickable { speechManager.speak(lastAnswer, language) }
                                                .padding(horizontal = 8.dp, vertical = 4.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Icon(
                                                    imageVector = Icons.Default.VolumeUp,
                                                    contentDescription = "Replay",
                                                    tint = NeonPinkPrimary,
                                                    modifier = Modifier.size(12.dp)
                                                )
                                                Spacer(modifier = Modifier.width(4.dp))
                                                Text(
                                                    text = if (language == "hi") "सुने" else "Replay",
                                                    color = TextPrimary,
                                                    fontSize = 10.sp,
                                                    fontWeight = FontWeight.Bold
                                                )
                                            }
                                        }
                                    }
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = lastAnswer,
                                    color = TextPrimary,
                                    fontSize = 14.sp,
                                    lineHeight = 20.sp
                                )
                            }
                        }
                    }
                }
            }

            // Error banner
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = errorMessage!!,
                    color = ErrorRed,
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Bottom Controls
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonPinkPrimary, NeonPinkDark, Color.Black)
                        )
                    )
                    .border(2.dp, NeonPinkLight, CircleShape)
                    .clickable(enabled = !isProcessing) {
                        startListening()
                    }
                    .testTag("voice_mic_fab_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isProcessing) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(28.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Tap to speak",
                        tint = Color.White,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (language == "hi") "टैप करें और बोलें" else "Tap Mic to Start Speaking",
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
    }
}
