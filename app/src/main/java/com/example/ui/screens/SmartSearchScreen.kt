package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiRepository
import com.example.ui.components.NeonGlowingCard
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
fun SmartSearchScreen(
    aiRepository: AiRepository,
    speechManager: SpeechManager,
    language: String,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var searchQuery by remember { mutableStateOf("") }
    var searchResult by remember { mutableStateOf("") }
    var isSearching by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val isSpeaking by speechManager.isSpeaking.collectAsState()

    val topicChips = if (language == "hi") {
        listOf(
            "क्वांटम कंप्यूटिंग भविष्य",
            "एआई एजेंट स्वायत्तता",
            "अन्तरिक्ष अन्वेषण 2026",
            "स्वस्थ दीर्घायु और पोषण",
            "ब्लॉकचेन और वेब3 सुरक्षा"
        )
    } else {
        listOf(
            "Quantum Computing 2026",
            "Autonomous AI Agent Systems",
            "Nuclear Fusion Energy Status",
            "Longevity & Cellular Health",
            "Edge AI vs Cloud LLMs"
        )
    }

    fun runSearch(query: String) {
        if (query.isBlank()) return
        isSearching = true
        errorMessage = null
        searchQuery = query
        speechManager.stop()

        scope.launch {
            try {
                val result = aiRepository.executeSmartSearch(query, language)
                searchResult = result
            } catch (e: Exception) {
                errorMessage = e.localizedMessage ?: "Search failed."
            } finally {
                isSearching = false
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("smart_search_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Search Header Card
        NeonGlowingCard(modifier = Modifier.fillMaxWidth(), glowBorder = true) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(NeonPinkContainer)
                        .border(1.dp, NeonPinkPrimary, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = CyberCyanAccent,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (language == "hi") "स्मार्ट खोज इंजन (Smart Search)" else "Neural Smart Search",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == "hi") "गहन संश्लेषण और संरचित विश्लेषण" else "Synthesizes multi-source knowledge & deep insights",
                        color = NeonPinkSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        // Search Input
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (language == "hi") "खोज विषय या शोध प्रश्न दर्ज करें..." else "Enter topic, theory, or investigation...",
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
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("search_query_input"),
                singleLine = true
            )

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        if (searchQuery.isNotBlank() && !isSearching)
                            Brush.radialGradient(listOf(NeonPinkPrimary, NeonPinkDark))
                        else
                            Brush.radialGradient(listOf(CyberCardElevated, CyberCardSurface))
                    )
                    .border(1.dp, if (searchQuery.isNotBlank()) NeonPinkPrimary else CyberBorder, RoundedCornerShape(14.dp))
                    .clickable(enabled = searchQuery.isNotBlank() && !isSearching) {
                        runSearch(searchQuery)
                    }
                    .testTag("execute_search_button"),
                contentAlignment = Alignment.Center
            ) {
                if (isSearching) {
                    CircularProgressIndicator(
                        color = Color.White,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Search",
                        tint = if (searchQuery.isNotBlank()) Color.White else TextMuted,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }

        // Quick Topic Chips
        Column {
            Text(
                text = if (language == "hi") "अनुशंसित विषय:" else "Recommended Research Topics:",
                color = TextSecondary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(6.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(end = 8.dp)
            ) {
                items(topicChips) { topic ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberBorder, RoundedCornerShape(10.dp))
                            .clickable { runSearch(topic) }
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = topic,
                            color = NeonPinkLight,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Error Banner
        if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(ErrorRed.copy(alpha = 0.2f))
                    .border(1.dp, ErrorRed, RoundedCornerShape(12.dp))
                    .padding(12.dp)
            ) {
                Text(
                    text = errorMessage!!,
                    color = Color(0xFFFFB4AB),
                    fontSize = 12.sp
                )
            }
        }

        // Search Result Card
        if (searchResult.isNotEmpty()) {
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
                            text = if (language == "hi") "विश्लेषण परिणाम" else "Synthesized Report",
                            color = NeonPinkPrimary,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )

                        // Audio Readout Toggle
                        IconButton(
                            onClick = {
                                if (isSpeaking) {
                                    speechManager.stop()
                                } else {
                                    speechManager.speak(searchResult, language)
                                }
                            },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = if (isSpeaking) Icons.Default.Stop else Icons.Default.VolumeUp,
                                contentDescription = "Read Aloud",
                                tint = if (isSpeaking) NeonPinkPrimary else TextSecondary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    SelectionContainer {
                        Text(
                            text = searchResult,
                            color = TextPrimary,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
