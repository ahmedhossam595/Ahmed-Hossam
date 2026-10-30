package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.data.model.FollowUpEmail
import com.example.data.model.MeetingSummary
import com.example.data.model.TaskItem

@Entity(tableName = "summaries")
data class SummaryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val summary: String,
    val keyPoints: List<String>,
    val decisions: List<String>,
    val tasks: List<TaskItem>,
    val followUpEmail: FollowUpEmail,
    val originalNotes: String,
    val meetingType: String,
    val emailTone: String,
    val createdAt: Long = System.currentTimeMillis()
)

fun SummaryEntity.toDomain(): MeetingSummary = MeetingSummary(
    id = id,
    title = title,
    summary = summary,
    keyPoints = keyPoints,
    decisions = decisions,
    tasks = tasks,
    followUpEmail = followUpEmail,
    originalNotes = originalNotes,
    meetingType = meetingType,
    emailTone = emailTone,
    createdAt = createdAt
)

fun MeetingSummary.toEntity(): SummaryEntity = SummaryEntity(
    id = id,
    title = title,
    summary = summary,
    keyPoints = keyPoints,
    decisions = decisions,
    tasks = tasks,
    followUpEmail = followUpEmail,
    originalNotes = originalNotes,
    meetingType = meetingType,
    emailTone = emailTone,
    createdAt = createdAt
)
