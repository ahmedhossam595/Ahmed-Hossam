package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.EmailTone
import com.example.data.model.MeetingType
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryBlue

@Composable
fun NotesInputSection(
    notesText: String,
    onNotesChange: (String) -> Unit,
    selectedMeetingType: MeetingType,
    onMeetingTypeChange: (MeetingType) -> Unit,
    selectedEmailTone: EmailTone,
    onEmailToneChange: (EmailTone) -> Unit,
    onSummarizeClick: () -> Unit,
    onClearClick: () -> Unit,
    onSampleClick: () -> Unit,
    isLoading: Boolean,
    errorMessage: String? = null
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("notes_input_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // Header for meeting notes section
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "ملاحظات الاجتماع",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                // Try Sample Button
                OutlinedButton(
                    onClick = onSampleClick,
                    modifier = Modifier.testTag("sample_button"),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AccentTeal
                    ),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(
                        horizontal = 10.dp,
                        vertical = 6.dp
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Lightbulb,
                        contentDescription = "جرّب مثال",
                        modifier = Modifier.size(16.dp),
                        tint = AccentTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "جرّب مثال",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Text Area
            OutlinedTextField(
                value = notesText,
                onValueChange = onNotesChange,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .testTag("notes_text_field"),
                placeholder = {
                    Text(
                        text = "الصق هنا ملاحظات الاجتماع المبعثرة، النقاط، المحادثات، أو ما تم تدوينه...",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                },
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outlineVariant,
                    focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f),
                    unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                ),
                textStyle = MaterialTheme.typography.bodyMedium
            )

            // Live Character and Word counter
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 6.dp, start = 4.dp, end = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val wordsCount = if (notesText.isBlank()) 0 else notesText.trim().split("\\s+".toRegex()).size
                Text(
                    text = "$wordsCount كلمة",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${notesText.length} حرف",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            // Error display
            AnimatedVisibility(visible = errorMessage != null) {
                if (errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 10.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFFEE2E2))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = errorMessage,
                            color = Color(0xFFDC2626),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 1) Meeting Type Selection
            Text(
                text = "نوع الاجتماع:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 4 Meeting Types
            MeetingTypeSelector(
                selected = selectedMeetingType,
                onSelect = onMeetingTypeChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 2) Email Tone Selection
            Text(
                text = "نبرة إيميل المتابعة:",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))

            // 3 Email Tones
            EmailToneSelector(
                selected = selectedEmailTone,
                onSelect = onEmailToneChange
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Primary: لخّص الآن
                Button(
                    onClick = onSummarizeClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp)
                        .testTag("summarize_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PrimaryBlue,
                        contentColor = Color.White
                    ),
                    enabled = !isLoading
                ) {
                    if (isLoading) {
                        CircularProgressIndicator(
                            color = Color.White,
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "جاري التحليل...",
                            style = MaterialTheme.typography.labelLarge
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "لخّص الآن",
                            style = MaterialTheme.typography.labelLarge,
                            fontSize = 15.sp
                        )
                    }
                }

                // Secondary: مسح
                OutlinedButton(
                    onClick = onClearClick,
                    modifier = Modifier
                        .height(50.dp)
                        .testTag("clear_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    ),
                    border = ButtonDefaults.outlinedButtonBorder(enabled = !isLoading && notesText.isNotBlank()),
                    enabled = !isLoading && notesText.isNotBlank()
                ) {
                    Icon(
                        imageVector = Icons.Default.Clear,
                        contentDescription = "مسح",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "مسح",
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    }
}

@Composable
private fun MeetingTypeSelector(
    selected: MeetingType,
    onSelect: (MeetingType) -> Unit
) {
    val items = listOf(
        MeetingType.TEAM to Icons.Default.Groups,
        MeetingType.CLIENT to Icons.Default.RecordVoiceOver,
        MeetingType.QUICK_CALL to Icons.Default.Schedule,
        MeetingType.EXECUTIVE to Icons.Default.Security
    )

    Column(
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeetingTypeChip(
                item = items[0].first,
                icon = items[0].second,
                isSelected = selected == items[0].first,
                onSelect = { onSelect(items[0].first) },
                modifier = Modifier.weight(1f)
            )
            MeetingTypeChip(
                item = items[1].first,
                icon = items[1].second,
                isSelected = selected == items[1].first,
                onSelect = { onSelect(items[1].first) },
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MeetingTypeChip(
                item = items[2].first,
                icon = items[2].second,
                isSelected = selected == items[2].first,
                onSelect = { onSelect(items[2].first) },
                modifier = Modifier.weight(1f)
            )
            MeetingTypeChip(
                item = items[3].first,
                icon = items[3].second,
                isSelected = selected == items[3].first,
                onSelect = { onSelect(items[3].first) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun MeetingTypeChip(
    item: MeetingType,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    isSelected: Boolean,
    onSelect: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isSelected) AccentTeal else MaterialTheme.colorScheme.outlineVariant
    val bgColor = if (isSelected) AccentTeal.copy(alpha = 0.12f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    val contentColor = if (isSelected) AccentTeal else MaterialTheme.colorScheme.onSurface

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
            .background(bgColor)
            .clickable { onSelect() }
            .padding(horizontal = 10.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(16.dp),
                tint = contentColor
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = item.titleAr,
                style = MaterialTheme.typography.labelMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun EmailToneSelector(
    selected: EmailTone,
    onSelect: (EmailTone) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        EmailTone.entries.forEach { tone ->
            val isSelected = selected == tone
            val borderColor = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.outlineVariant
            val bgColor = if (isSelected) PrimaryBlue.copy(alpha = 0.1f) else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
            val contentColor = if (isSelected) PrimaryBlue else MaterialTheme.colorScheme.onSurface

            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .border(1.5.dp, borderColor, RoundedCornerShape(12.dp))
                    .background(bgColor)
                    .clickable { onSelect(tone) }
                    .padding(vertical = 10.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = contentColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(
                        text = tone.titleAr,
                        style = MaterialTheme.typography.labelMedium,
                        color = contentColor
                    )
                }
            }
        }
    }
}
