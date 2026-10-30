package com.example.data.model

import java.util.UUID

enum class MeetingType(val titleAr: String, val descriptionAr: String) {
    TEAM("اجتماع فريق", "متابعة دورية ومهام الفريق"),
    CLIENT("اجتماع عميل", "متطلبات العميل والاتفاقيات"),
    QUICK_CALL("مكالمة سريعة", "متابعة موجزة ونقاط سريعة"),
    EXECUTIVE("اجتماع إداري", "قرارات استراتيجية وتوجيهات");

    companion object {
        fun fromTitle(title: String): MeetingType {
            return entries.firstOrNull { it.titleAr == title } ?: TEAM
        }
    }
}

enum class EmailTone(val titleAr: String, val descriptionAr: String) {
    FORMAL("رسمي", "صيغة مهنية ورسمية موجهة للإدارة والعملاء"),
    FRIENDLY("ودي", "لهجة ودية وتعاونية لزملاء العمل"),
    CONCISE("مختصر", "نقاط سريعة وموجزة جداً");

    companion object {
        fun fromTitle(title: String): EmailTone {
            return entries.firstOrNull { it.titleAr == title } ?: FORMAL
        }
    }
}

data class TaskItem(
    val id: String = UUID.randomUUID().toString(),
    val task: String,
    val owner: String = "غير محدد",
    val deadline: String = "غير محدد",
    val priority: String = "متوسطة", // مرتفعة، متوسطة، منخفضة
    val isCompleted: Boolean = false
)

data class FollowUpEmail(
    val subject: String,
    val body: String
)

data class MeetingSummary(
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
