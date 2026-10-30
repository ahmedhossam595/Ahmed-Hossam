package com.example.ui.components

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatListBulleted
import androidx.compose.material.icons.automirrored.filled.ShortText
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.model.MeetingSummary
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.PrimaryBlue

@Composable
fun ResultView(
    summary: MeetingSummary,
    isSaved: Boolean,
    onToggleTask: (String) -> Unit,
    onUpdateTitle: (String) -> Unit,
    onSaveSummary: () -> Unit,
    onNewSummary: () -> Unit,
    onShowToast: (String) -> Unit
) {
    val context = LocalContext.current
    var isEditingTitle by remember { mutableStateOf(false) }
    var editedTitle by remember(summary.title) { mutableStateOf(summary.title) }

    fun copyToClipboard(label: String, text: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText(label, text)
        clipboard.setPrimaryClip(clip)
        onShowToast("تم النسخ ✓")
    }

    fun shareText(text: String, title: String) {
        val sendIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, text)
            putExtra(Intent.EXTRA_SUBJECT, title)
            type = "text/plain"
        }
        val shareIntent = Intent.createChooser(sendIntent, "مشاركة ملخص الاجتماع")
        context.startActivity(shareIntent)
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Proposed Title Card (Editable)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_title_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "عنوان الاجتماع المقترح",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = summary.title,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = {
                        editedTitle = summary.title
                        isEditingTitle = true
                    },
                    modifier = Modifier.testTag("edit_title_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "تعديل العنوان",
                        tint = AccentTeal
                    )
                }
            }
        }

        // 2. Quick Summary Card (2-3 sentences)
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_summary_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "ملخص سريع",
                    icon = Icons.AutoMirrored.Filled.ShortText,
                    onCopy = { copyToClipboard("ملخص الاجتماع", summary.summary) }
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary.summary,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        // 3. Key Points Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_key_points_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "النقاط الرئيسية",
                    icon = Icons.AutoMirrored.Filled.FormatListBulleted,
                    onCopy = {
                        val text = summary.keyPoints.joinToString("\n") { "• $it" }
                        copyToClipboard("النقاط الرئيسية", text)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    summary.keyPoints.forEach { point ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 8.dp)
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(AccentTeal)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = point,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 4. Decisions Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_decisions_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "القرارات المتخذة",
                    icon = Icons.Default.CheckCircle,
                    onCopy = {
                        val text = summary.decisions.joinToString("\n") { "✓ $it" }
                        copyToClipboard("القرارات المتخذة", text)
                    }
                )
                Spacer(modifier = Modifier.height(10.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    summary.decisions.forEach { decision ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.Top
                        ) {
                            Icon(
                                imageVector = Icons.Default.Done,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = decision,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // 5. Tasks Table Card
        TaskTable(
            tasks = summary.tasks,
            onToggleTask = onToggleTask,
            onCopyTasks = {
                val formattedTasks = summary.tasks.joinToString("\n") {
                    val status = if (it.isCompleted) "[تم الإنجاز]" else "[قيد التنفيذ]"
                    "$status ${it.task} (المسؤول: ${it.owner} | الموعد: ${it.deadline} | الأولوية: ${it.priority})"
                }
                copyToClipboard("مهام الاجتماع", formattedTasks)
            }
        )

        // 6. Follow-up Email Card
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .testTag("result_email_card"),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                SectionHeader(
                    title = "إيميل المتابعة (${summary.emailTone})",
                    icon = Icons.Default.Email,
                    onCopy = {
                        val fullEmail = "الموضوع: ${summary.followUpEmail.subject}\n\n${summary.followUpEmail.body}"
                        copyToClipboard("إيميل المتابعة", fullEmail)
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Subject Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            text = "الموضوع:",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = summary.followUpEmail.subject,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Body Box
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                        .padding(12.dp)
                ) {
                    Text(
                        text = summary.followUpEmail.body,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Action Buttons Row: نسخ الكل، تحميل/مشاركة، حفظ، تلخيص جديد
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            val fullSummaryText = buildString {
                appendLine("ملخص: ${summary.title}")
                appendLine("نوع الاجتماع: ${summary.meetingType}")
                appendLine("====================")
                appendLine("\n[الملخص السريع]")
                appendLine(summary.summary)
                appendLine("\n[النقاط الرئيسية]")
                summary.keyPoints.forEach { appendLine("• $it") }
                appendLine("\n[القرارات المتخذة]")
                summary.decisions.forEach { appendLine("✓ $it") }
                appendLine("\n[المهام المطلوبة]")
                summary.tasks.forEach {
                    val status = if (it.isCompleted) "[منجز]" else "[مطلوب]"
                    appendLine("$status ${it.task} (المسؤول: ${it.owner} | الموعد: ${it.deadline} | الأولوية: ${it.priority})")
                }
                appendLine("\n[إيميل المتابعة]")
                appendLine("الموضوع: ${summary.followUpEmail.subject}")
                appendLine(summary.followUpEmail.body)
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // نسخ الكل
                Button(
                    onClick = { copyToClipboard("الملخص الكامل", fullSummaryText) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("copy_all_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryBlue)
                ) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "نسخ الكل", style = MaterialTheme.typography.labelMedium)
                }

                // مشاركة / تحميل كملف نصي
                OutlinedButton(
                    onClick = { shareText(fullSummaryText, summary.title) },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("share_summary_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = AccentTeal
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "مشاركة / تصدير", style = MaterialTheme.typography.labelMedium, color = AccentTeal)
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // حفظ الملخص
                OutlinedButton(
                    onClick = onSaveSummary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("save_summary_button"),
                    shape = RoundedCornerShape(12.dp),
                    enabled = !isSaved
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Bookmark else Icons.Default.BookmarkBorder,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = if (isSaved) AccentTeal else MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (isSaved) "تم الحفظ ✓" else "حفظ الملخص",
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSaved) AccentTeal else MaterialTheme.colorScheme.onSurface
                    )
                }

                // تلخيص جديد
                Button(
                    onClick = onNewSummary,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("new_summary_button"),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentTeal)
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "تلخيص جديد", style = MaterialTheme.typography.labelMedium)
                }
            }
        }
    }

    // Title Edit Dialog
    if (isEditingTitle) {
        AlertDialog(
            onDismissRequest = { isEditingTitle = false },
            title = {
                Text(
                    text = "تعديل عنوان الاجتماع",
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                OutlinedTextField(
                    value = editedTitle,
                    onValueChange = { editedTitle = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("العنوان") },
                    singleLine = true
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (editedTitle.isNotBlank()) {
                            onUpdateTitle(editedTitle)
                            onShowToast("تم تحديث العنوان ✓")
                        }
                        isEditingTitle = false
                    }
                ) {
                    Text("حفظ")
                }
            },
            dismissButton = {
                TextButton(onClick = { isEditingTitle = false }) {
                    Text("إلغاء")
                }
            }
        )
    }
}

@Composable
private fun SectionHeader(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onCopy: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(PrimaryBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = PrimaryBlue,
                    modifier = Modifier.size(16.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(
            onClick = onCopy,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ContentCopy,
                contentDescription = "نسخ",
                tint = AccentTeal,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}
