package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Translate
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderNeon
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.CyberVioletAccent
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.NeonPinkSecondary
import com.example.ui.theme.SuccessGreen
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@Composable
fun FuturisticHeader(
    currentLanguage: String,
    onLanguageToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberDarkSurface.copy(alpha = 0.95f),
        modifier = modifier
            .fillMaxWidth()
            .testTag("futuristic_header")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    colors = listOf(NeonPinkPrimary, NeonPinkDark, Color.Black)
                                )
                            )
                            .border(1.5.dp, NeonPinkPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = "MG AI Logo",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "MG AI",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Black,
                                letterSpacing = 1.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(NeonPinkContainer)
                                    .border(0.5.dp, NeonPinkPrimary, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "3.8 FLASH",
                                    color = NeonPinkLight,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 0.5.sp
                                )
                            }
                        }
                        Text(
                            text = "Intelligence That Understands",
                            color = NeonPinkSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                // Language Toggle Chip
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(CyberCardSurface)
                        .border(1.dp, CyberBorderNeon, RoundedCornerShape(20.dp))
                        .clickable { onLanguageToggle() }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                        .testTag("language_toggle_button"),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Translate,
                            contentDescription = "Language",
                            tint = NeonPinkPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (currentLanguage == "hi") "हिन्दी" else "EN",
                            color = TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NeonPulsingOrb(
    isActive: Boolean = true,
    size: Dp = 120.dp,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )
    val ringRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(6000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ringRotation"
    )

    Box(
        modifier = modifier
            .size(size)
            .testTag("neon_pulsing_orb"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(this.size.width / 2f, this.size.height / 2f)
            val baseRadius = this.size.minDimension / 2.6f

            // Outer glow ring
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        NeonPinkPrimary.copy(alpha = if (isActive) 0.45f else 0.15f),
                        CyberVioletAccent.copy(alpha = if (isActive) 0.2f else 0.05f),
                        Color.Transparent
                    ),
                    center = center,
                    radius = baseRadius * (if (isActive) pulseScale * 1.3f else 1.1f)
                ),
                radius = baseRadius * (if (isActive) pulseScale * 1.3f else 1.1f),
                center = center
            )

            // Dynamic orbit arcs
            val sweep = 60f
            val arcCount = 3
            for (i in 0 until arcCount) {
                val startAngle = ringRotation + (i * 120f)
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(NeonPinkPrimary, CyberCyanAccent, NeonPinkPrimary),
                        center = center
                    ),
                    startAngle = startAngle,
                    sweepAngle = sweep,
                    useCenter = false,
                    topLeft = Offset(center.x - baseRadius * 1.15f, center.y - baseRadius * 1.15f),
                    size = androidx.compose.ui.geometry.Size(baseRadius * 2.3f, baseRadius * 2.3f),
                    style = Stroke(width = 2.5.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            // Inner core
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        Color.White,
                        NeonPinkLight,
                        NeonPinkPrimary,
                        NeonPinkDark,
                        CyberDarkSurface
                    ),
                    center = center,
                    radius = baseRadius * (if (isActive) pulseScale else 0.9f)
                ),
                radius = baseRadius * 0.75f,
                center = center
            )
        }
    }
}

@Composable
fun AudioWaveformVisualizer(
    isListening: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "waveform")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 2f * PI.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(54.dp)
            .testTag("audio_waveform"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxWidth().height(54.dp)) {
            val barCount = 28
            val spacing = size.width / (barCount + 1)
            val centerY = size.height / 2f

            for (i in 0 until barCount) {
                val x = spacing * (i + 1)
                val normalizedIdx = i.toFloat() / barCount
                val heightMultiplier = if (isListening) {
                    (sin(wavePhase + normalizedIdx * 4 * PI.toFloat()) + 1.2f) * 0.45f
                } else {
                    0.15f
                }

                val barHeight = (size.height * 0.75f * heightMultiplier).coerceIn(4.dp.toPx(), size.height)

                drawLine(
                    brush = Brush.verticalGradient(
                        colors = listOf(NeonPinkPrimary, CyberVioletAccent, CyberCyanAccent)
                    ),
                    start = Offset(x, centerY - barHeight / 2),
                    end = Offset(x, centerY + barHeight / 2),
                    strokeWidth = 3.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }
        }
    }
}

@Composable
fun NeonGlowingCard(
    modifier: Modifier = Modifier,
    glowBorder: Boolean = true,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val cardModifier = modifier
        .clip(RoundedCornerShape(16.dp))
        .background(CyberCardSurface)
        .border(
            width = if (glowBorder) 1.dp else 0.5.dp,
            brush = if (glowBorder) {
                Brush.linearGradient(
                    colors = listOf(NeonPinkPrimary, CyberBorder, CyberVioletAccent)
                )
            } else {
                Brush.linearGradient(colors = listOf(CyberBorder, CyberBorder))
            },
            shape = RoundedCornerShape(16.dp)
        )
        .then(if (onClick != null) Modifier.clickable { onClick() } else Modifier)
        .padding(16.dp)

    Box(modifier = cardModifier) {
        content()
    }
}

@Composable
fun StatusBadge(
    text: String,
    isActive: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(CyberCardElevated)
            .border(0.5.dp, if (isActive) SuccessGreen.copy(alpha = 0.5f) else CyberBorder, RoundedCornerShape(12.dp))
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(6.dp)
                .clip(CircleShape)
                .background(if (isActive) SuccessGreen else TextMuted)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            color = if (isActive) TextPrimary else TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
