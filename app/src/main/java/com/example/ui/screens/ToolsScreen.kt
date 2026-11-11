package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.NoteAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.ai.AiRepository
import com.example.data.local.NoteEntity
import com.example.data.local.ReminderEntity
import com.example.ui.theme.CyberBlackBackground
import com.example.ui.theme.CyberBorder
import com.example.ui.theme.CyberDarkSurface
import com.example.ui.theme.NeonPinkLight
import com.example.ui.theme.NeonPinkPrimary
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.util.SpeechManager

data class ToolTabItem(
    val id: String,
    val titleEn: String,
    val titleHi: String,
    val icon: ImageVector
)

@Composable
fun ToolsScreen(
    initialTab: String = "images",
    selectedTab: String,
    onTabSelected: (String) -> Unit,
    aiRepository: AiRepository,
    speechManager: SpeechManager,
    notes: List<NoteEntity>,
    reminders: List<ReminderEntity>,
    language: String,
    onSaveNote: (String, String, String) -> Unit,
    onDeleteNote: (NoteEntity) -> Unit,
    onAddReminder: (String, String, String) -> Unit,
    onToggleReminderCompleted: (Long, Boolean) -> Unit,
    onDeleteReminder: (ReminderEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    val tabs = listOf(
        ToolTabItem("images", "Image Studio", "छवि निर्माण", Icons.Default.Image),
        ToolTabItem("search", "Smart Search", "स्मार्ट खोज", Icons.Default.Search),
        ToolTabItem("timer", "Timer", "टाइमर", Icons.Default.Timer),
        ToolTabItem("notes", "AI Notes", "नोट्स", Icons.Default.NoteAdd),
        ToolTabItem("reminders", "Reminders", "रिमाइंडर", Icons.Default.Alarm)
    )

    val currentTabIndex = tabs.indexOfFirst { it.id == selectedTab }.coerceAtLeast(0)

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(CyberBlackBackground)
            .testTag("tools_screen")
    ) {
        // Tab Bar
        ScrollableTabRow(
            selectedTabIndex = currentTabIndex,
            containerColor = CyberDarkSurface,
            contentColor = NeonPinkPrimary,
            edgePadding = 12.dp,
            indicator = { tabPositions ->
                if (currentTabIndex < tabPositions.size) {
                    TabRowDefaults.SecondaryIndicator(
                        Modifier.tabIndicatorOffset(tabPositions[currentTabIndex]),
                        color = NeonPinkPrimary,
                        height = 3.dp
                    )
                }
            },
            divider = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberBorder)
                )
            }
        ) {
            tabs.forEachIndexed { index, tab ->
                val isSelected = currentTabIndex == index
                Tab(
                    selected = isSelected,
                    onClick = { onTabSelected(tab.id) },
                    modifier = Modifier.testTag("tool_tab_${tab.id}"),
                    text = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = tab.icon,
                                contentDescription = null,
                                tint = if (isSelected) NeonPinkPrimary else TextMuted,
                                modifier = Modifier.size(16.dp)
                            )
                            Box(modifier = Modifier.padding(start = 6.dp)) {
                                Text(
                                    text = if (language == "hi") tab.titleHi else tab.titleEn,
                                    color = if (isSelected) TextPrimary else TextSecondary,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                                )
                            }
                        }
                    }
                )
            }
        }

        // Active Tab Screen Content
        Box(modifier = Modifier.weight(1f)) {
            when (selectedTab) {
                "images" -> ImageGenScreen(
                    aiRepository = aiRepository,
                    language = language
                )
                "search" -> SmartSearchScreen(
                    aiRepository = aiRepository,
                    speechManager = speechManager,
                    language = language
                )
                "timer" -> TimerScreen(
                    language = language
                )
                "notes" -> NotesScreen(
                    notes = notes,
                    aiRepository = aiRepository,
                    language = language,
                    onSaveNote = onSaveNote,
                    onDeleteNote = onDeleteNote
                )
                "reminders" -> RemindersScreen(
                    reminders = reminders,
                    language = language,
                    onAddReminder = onAddReminder,
                    onToggleCompleted = onToggleReminderCompleted,
                    onDeleteReminder = onDeleteReminder
                )
                else -> ImageGenScreen(
                    aiRepository = aiRepository,
                    language = language
                )
            }
        }
    }
}
