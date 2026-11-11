package com.example.ui.screens

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.NeonGlowingCard
import com.example.ui.theme.CyberBlackBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberBorderNeon
import com.example.ui.theme.CyberCardElevated
import com.example.ui.theme.CyberCardSurface
import com.example.ui.theme.CyberCyanAccent
import com.example.ui.theme.CyberVioletAccent
import com.example.ui.theme.NeonPinkContainer
import com.example.ui.theme.NeonPinkDark
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.NeonPinkSecondary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import kotlinx.coroutines.delay

@Composable
fun TimerScreen(
    language: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current

    var totalDurationSeconds by remember { mutableIntStateOf(25 * 60) }
    var remainingSeconds by remember { mutableIntStateOf(25 * 60) }
    var isRunning by remember { mutableStateOf(false) }

    fun triggerVibration() {
        try {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createWaveform(longArrayOf(0, 300, 150, 400), -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(500)
            }
        } catch (_: Exception) {}
    }

    LaunchedEffect(isRunning, remainingSeconds) {
        if (isRunning && remainingSeconds > 0) {
            delay(1000L)
            remainingSeconds -= 1
            if (remainingSeconds == 0) {
                isRunning = false
                triggerVibration()
            }
        }
    }

    val progress = if (totalDurationSeconds > 0) {
        remainingSeconds.toFloat() / totalDurationSeconds.toFloat()
    } else 0f

    val animatedProgress by animateFloatAsState(targetValue = progress, label = "timer_progress")

    val minutes = remainingSeconds / 60
    val seconds = remainingSeconds % 60
    val timeFormatted = String.format("%02d:%02d", minutes, seconds)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
            .testTag("timer_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        // Header
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
                        imageVector = Icons.Default.Timer,
                        contentDescription = null,
                        tint = NeonPinkPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = if (language == "hi") "एआई फोकस टाइमर" else "Cyber Focus Timer",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = if (language == "hi") "उत्पादकता और पोमोडोरो चक्र" else "Pomodoro & Deep Focus Neural Cadence",
                        color = NeonPinkSecondary,
                        fontSize = 12.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Circular Timer Visualizer
        Box(
            modifier = Modifier
                .size(240.dp)
                .testTag("timer_display_circle"),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(240.dp)) {
                val center = Offset(size.width / 2, size.height / 2)
                val radius = (size.minDimension / 2) - 16.dp.toPx()

                // Background track
                drawCircle(
                    color = CyberCardElevated,
                    radius = radius,
                    center = center,
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )

                // Neon progress arc
                drawArc(
                    brush = Brush.sweepGradient(
                        colors = listOf(NeonPinkPrimary, CyberVioletAccent, CyberCyanAccent, NeonPinkPrimary),
                        center = center
                    ),
                    startAngle = -90f,
                    sweepAngle = 360f * animatedProgress,
                    useCenter = false,
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                    style = Stroke(width = 12.dp.toPx(), cap = StrokeCap.Round)
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = timeFormatted,
                    color = TextPrimary,
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Black,
                    fontFamily = FontFamily.Monospace,
                    letterSpacing = 2.sp
                )
                Text(
                    text = if (isRunning)
                        (if (language == "hi") "सक्रिय ध्यान चक्र" else "FOCUSING")
                    else
                        (if (remainingSeconds == 0) (if (language == "hi") "समाप्त!" else "COMPLETE") else (if (language == "hi") "विराम" else "PAUSED")),
                    color = if (isRunning) NeonPinkLight else TextMuted,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Adjust minutes buttons
        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberCardSurface)
                    .border(1.dp, CyberBorder, CircleShape)
                    .clickable(enabled = !isRunning) {
                        if (totalDurationSeconds > 60) {
                            totalDurationSeconds -= 60
                            remainingSeconds = totalDurationSeconds
                        }
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "-1 min",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = "${totalDurationSeconds / 60} ${if (language == "hi") "मिनट" else "Min"}",
                color = TextSecondary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold
            )

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(CyberCardSurface)
                    .border(1.dp, CyberBorder, CircleShape)
                    .clickable(enabled = !isRunning) {
                        totalDurationSeconds += 60
                        remainingSeconds = totalDurationSeconds
                    }
                    .padding(8.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "+1 min",
                    tint = TextPrimary,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Control Actions (Play/Pause, Reset)
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Reset Button
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(CyberCardSurface)
                    .border(1.dp, CyberBorder, CircleShape)
                    .clickable {
                        isRunning = false
                        remainingSeconds = totalDurationSeconds
                    }
                    .testTag("reset_timer_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Reset Timer",
                    tint = TextSecondary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(24.dp))

            // Play / Pause FAB
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(NeonPinkPrimary, NeonPinkDark)
                        )
                    )
                    .border(2.dp, NeonPinkLight, CircleShape)
                    .clickable {
                        isRunning = !isRunning
                    }
                    .testTag("toggle_timer_button"),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isRunning) "Pause" else "Start",
                    tint = Color.White,
                    modifier = Modifier.size(36.dp)
                )
            }
        }

        // Preset Quick Chips
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = if (language == "hi") "प्रीसेट अवधियाँ:" else "Quick Presets:",
                color = TextSecondary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))

            val presets = listOf(
                1 to (if (language == "hi") "1 मिनट" else "1 Min"),
                5 to (if (language == "hi") "5 मिनट" else "5 Min"),
                15 to (if (language == "hi") "15 मिनट" else "15 Min"),
                25 to (if (language == "hi") "25 मि (पोमोडोरो)" else "25m Pomodoro"),
                45 to (if (language == "hi") "45 मि (गहन ध्यान)" else "45m Deep Work")
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                presets.forEach { (mins, label) ->
                    val isCurrent = totalDurationSeconds == mins * 60
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(if (isCurrent) NeonPinkContainer else CyberCardSurface)
                            .border(1.dp, if (isCurrent) NeonPinkPrimary else CyberBorder, RoundedCornerShape(10.dp))
                            .clickable {
                                isRunning = false
                                totalDurationSeconds = mins * 60
                                remainingSeconds = mins * 60
                            }
                            .padding(vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "${mins}m",
                            color = if (isCurrent) NeonPinkLight else TextPrimary,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
