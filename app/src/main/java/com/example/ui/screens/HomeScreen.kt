package com.example.ui.screens

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.ChatBubbleOutline
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.local.ChatSessionEntity
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
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.NeonPinkSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    language: String,
    recentSessions: List<ChatSessionEntity>,
    onNavigateToChatWithPrompt: (String) -> Unit,
    onNavigateToChat: () -> Unit,
    onNavigateToVoice: () -> Unit,
    onNavigateToTools: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val quickSuggestions = if (language == "hi") {
        listOf(
            "क्वांटम कंप्यूटिंग को सरल शब्दों में समझाएं",
            "साइंस-फिक्शन साइबरपंक कहानी के विचार दें",
            "एक प्रेरक सुबह का विचार साझा करें",
            "एंड्रॉयड जेटपैक कंपोज़ के सर्वोत्तम तरीके",
            "तनाव कम करने के लिए 5 मिनट की माइंडफुलनेस तकनीक"
        )
    } else {
        listOf(
            "Explain Quantum Computing in simple terms",
            "Brainstorm sci-fi cyberpunk novel concepts",
            "Give me a high-yield 30-minute daily routine",
            "Write a clean Kotlin Jetpack Compose sample",
            "5-minute mindfulness breathing exercise"
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .padding(horizontal = 16.dp)
            .testTag("home_screen_content"),
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(6.dp))
            // Hero AI Orb Nexus Card
            NeonGlowingCard(
                modifier = Modifier.fillMaxWidth(),
                glowBorder = true
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        StatusBadge(
                            text = if (language == "hi") "एआई ऑनलाइन • जेमिनी 3.8" else "ONLINE • GEMINI 3.8",
                            isActive = true
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Security,
                                contentDescription = "App Check Active",
                                tint = NeonPinkPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "APP CHECK",
                                color = NeonPinkLight,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Animated Glowing AI Sphere
                    NeonPulsingOrb(
                        isActive = true,
                        size = 130.dp,
                        modifier = Modifier.clickable { onNavigateToVoice() }
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = if (language == "hi") "नमस्ते! मैं MG AI हूँ" else "Hello, I am MG AI",
                        color = TextPrimary,
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.5.sp
                    )

                    Text(
                        text = if (language == "hi")
                            "आपकी सोच को समझने वाली अगली पीढ़ी की बुद्धिमत्ता"
                        else
                            "Intelligence That Understands • Ready to assist",
                        color = NeonPinkSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Normal
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Fast Voice Launch Button
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                Brush.horizontalGradient(
                                    listOf(NeonPinkPrimary, CyberVioletAccent)
                                )
                            )
                            .clickable { onNavigateToVoice() }
                            .padding(horizontal = 24.dp, vertical = 10.dp)
                            .testTag("launch_voice_assistant_button"),
                        contentAlignment = Alignment.Center
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Mic,
                                contentDescription = "Voice assistant",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = if (language == "hi") "बोलकर पूछें (Voice Input)" else "Speak to MG AI",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }

        // Quick Feature Grid
        item {
            Text(
                text = if (language == "hi") "त्वरित सेवाएँ (Quick Access)" else "Quick Neural Commands",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = if (language == "hi") "चैट सहायक" else "AI Chat",
                        subtitle = if (language == "hi") "स्ट्रीमिंग उत्तर" else "Instant Chat",
                        icon = Icons.Default.ChatBubbleOutline,
                        iconTint = NeonPinkPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToChat
                    )
                    QuickActionTile(
                        title = if (language == "hi") "चित्र निर्माण" else "Image Studio",
                        subtitle = if (language == "hi") "नैनो बनाना एआई" else "Nano Banana 2.1",
                        icon = Icons.Default.Image,
                        iconTint = CyberVioletAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTools("images") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = if (language == "hi") "स्मार्ट खोज" else "Smart Search",
                        subtitle = if (language == "hi") "गहन विश्लेषण" else "Deep Synthesis",
                        icon = Icons.Default.Search,
                        iconTint = CyberCyanAccent,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTools("search") }
                    )
                    QuickActionTile(
                        title = if (language == "hi") "स्मार्ट नोट्स" else "AI Notes",
                        subtitle = if (language == "hi") "संक्षिप्त और विस्तृत" else "Local & Synced",
                        icon = Icons.Default.NoteAdd,
                        iconTint = NeonPinkLight,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTools("notes") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickActionTile(
                        title = if (language == "hi") "टाइमर" else "Focus Timer",
                        subtitle = if (language == "hi") "काउंटडाउन रिंग" else "Cyber Countdown",
                        icon = Icons.Default.Timer,
                        iconTint = NeonPinkSecondary,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateToTools("timer") }
                    )
                    QuickActionTile(
                        title = if (language == "hi") "ध्वनि सहायक" else "Voice Nexus",
                        subtitle = if (language == "hi") "हिंदी और अंग्रेजी" else "Hindi & English",
                        icon = Icons.Default.Mic,
                        iconTint = NeonPinkPrimary,
                        modifier = Modifier.weight(1f),
                        onClick = onNavigateToVoice
                    )
                }
            }
        }

        // Prompt Suggestions Carousel
        item {
            Text(
                text = if (language == "hi") "सुझाए गए विचार" else "Inspiration & Prompts",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(10.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(end = 16.dp)
            ) {
                items(quickSuggestions) { prompt ->
                    Box(
                        modifier = Modifier
                            .width(260.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(CyberCardSurface)
                            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
                            .clickable { onNavigateToChatWithPrompt(prompt) }
                            .padding(14.dp)
                            .testTag("prompt_chip_${prompt.hashCode()}")
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    tint = NeonPinkPrimary,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (language == "hi") "त्वरित प्रश्न" else "Quick Prompt",
                                    color = NeonPinkSecondary,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = prompt,
                                color = TextPrimary,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Normal,
                                maxLines = 2
                            )
                        }
                    }
                }
            }
        }

        // Recent Chats Section
        if (recentSessions.isNotEmpty()) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (language == "hi") "हाल की बातचीत" else "Recent Conversations",
                        color = TextSecondary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = if (language == "hi") "सभी देखें" else "View All",
                        color = NeonPinkPrimary,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { onNavigateToChat() }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            items(recentSessions.take(3)) { session ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToChat() }
                        .testTag("recent_session_${session.id}"),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = CyberCardSurface),
                    border = CardDefaults.outlinedCardBorder().copy(brush = Brush.linearGradient(listOf(CyberBorder, CyberBorder)))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberCardElevated)
                                .border(1.dp, NeonPinkPrimary.copy(alpha = 0.5f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChatBubbleOutline,
                                contentDescription = null,
                                tint = NeonPinkPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = session.title,
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                maxLines = 1
                            )
                            Text(
                                text = session.lastMessage.ifBlank { "No messages" },
                                color = TextMuted,
                                fontSize = 12.sp,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun QuickActionTile(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(14.dp))
            .background(CyberCardSurface)
            .border(1.dp, CyberBorder, RoundedCornerShape(14.dp))
            .clickable { onClick() }
            .padding(12.dp)
            .testTag("quick_action_${title.replace(" ", "_").lowercase()}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(iconTint.copy(alpha = 0.12f))
                    .border(1.dp, iconTint.copy(alpha = 0.4f), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = iconTint,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = subtitle,
                    color = TextMuted,
                    fontSize = 11.sp,
                    maxLines = 1
                )
            }
        }
    }
}
