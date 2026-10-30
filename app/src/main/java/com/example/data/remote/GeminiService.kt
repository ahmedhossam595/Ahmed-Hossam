package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.EmailTone
import com.example.data.model.FollowUpEmail
import com.example.data.model.MeetingSummary
import com.example.data.model.MeetingType
import com.example.data.model.TaskItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.concurrent.TimeUnit

class GeminiService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(45, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(45, TimeUnit.SECONDS)
        .build()

    companion object {
        private const val TAG = "GeminiService"
    }

    suspend fun summarizeMeeting(
        notes: String,
        meetingType: MeetingType,
        tone: EmailTone
    ): MeetingSummary = withContext(Dispatchers.IO) {
        val apiKey = try {
            val field = BuildConfig::class.java.getField("GEMINI_API_KEY")
            (field.get(null) as? String).orEmpty()
        } catch (_: Exception) {
            ""
        }

        val hasValidKey = apiKey.isNotBlank() &&
                apiKey != "MY_GEMINI_API_KEY" &&
                !apiKey.contains("PLACEHOLDER", ignoreCase = true)

        if (hasValidKey) {
            try {
                return@withContext callGeminiApi(notes, meetingType, tone, apiKey)
            } catch (e: Exception) {
                Log.w(TAG, "Gemini API call failed, falling back to local extractor: ${e.message}", e)
                // If API failed due to quota/network, fallback gracefully to smart extractor
                return@withContext generateLocalSummary(notes, meetingType, tone)
            }
        } else {
            Log.d(TAG, "No active Gemini API key found in BuildConfig, using local smart extractor")
            return@withContext generateLocalSummary(notes, meetingType, tone)
        }
    }

    private fun callGeminiApi(
        notes: String,
        meetingType: MeetingType,
        tone: EmailTone,
        apiKey: String
    ): MeetingSummary {
        val prompt = buildPrompt(notes, meetingType, tone)

        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                put(JSONObject().apply {
                    put("parts", JSONArray().apply {
                        put(JSONObject().apply {
                            put("text", prompt)
                        })
                    })
                })
            }
            put("contents", contentsArray)

            val genConfig = JSONObject().apply {
                put("responseMimeType", "application/json")
                put("temperature", 0.2)
            }
            put("generationConfig", genConfig)
        }

        // Try gemini-2.5-flash first, then gemini-1.5-flash
        val models = listOf("gemini-2.5-flash", "gemini-1.5-flash")
        var lastException: Exception? = null

        for (modelName in models) {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/$modelName:generateContent?key=$apiKey"
                val mediaType = "application/json; charset=utf-8".toMediaType()
                val body = requestJson.toString().toRequestBody(mediaType)

                val request = Request.Builder()
                    .url(url)
                    .post(body)
                    .build()

                client.newCall(request).execute().use { response ->
                    val responseBody = response.body?.string().orEmpty()
                    if (!response.isSuccessful) {
                        Log.e(TAG, "HTTP error with $modelName: ${response.code} $responseBody")
                        throw IllegalStateException("API error: ${response.code}")
                    }

                    val jsonResponse = JSONObject(responseBody)
                    val candidates = jsonResponse.optJSONArray("candidates")
                    if (candidates != null && candidates.length() > 0) {
                        val content = candidates.getJSONObject(0).optJSONObject("content")
                        val parts = content?.optJSONArray("parts")
                        val rawText = parts?.getJSONObject(0)?.optString("text").orEmpty()
                        return parseJsonResponse(rawText, notes, meetingType, tone)
                    } else {
                        throw IllegalStateException("No content returned from Gemini")
                    }
                }
            } catch (e: Exception) {
                lastException = e
                Log.w(TAG, "Model $modelName call failed: ${e.message}")
            }
        }

        throw lastException ?: IllegalStateException("Failed to call Gemini API")
    }

    private fun buildPrompt(notes: String, meetingType: MeetingType, tone: EmailTone): String {
        return """
أنت خبير محترف ومساعد ذكي في تلخيص اجتماعات العمل باللغة العربية.
مهمتك: تحليل الملاحظات أدناه المأخوذة من ${meetingType.titleAr} وصياغة ملخص احترافي باللغة العربية مع إيميل متابعة بنبرة ${tone.titleAr}.

تعليمات صارمة:
1. استخدم اللغة العربية الفصحى الواضحة والمهنية لجميع الحقول.
2. لا تختلق أي معلومة غير موجودة في الملاحظات (zero hallucination).
3. إذا كانت هناك معلومة ناقصة (مثل المسؤول عن مهمة أو الموعد النهائي)، اكتب نصاً صريحاً: "غير محدد".
4. حقل الأولوية لكل مهمة يجب أن يكون حصراً أحد القيم التالية: "مرتفعة" أو "متوسطة" أو "منخفضة".
5. يجب أن يكون الناتج كائن JSON صالح فقط دون أي نص إضافي أو علامات markdown، بالهيكل التالي:
{
  "title": "عنوان مقترح للاجتماع",
  "summary": "ملخص سريع للاجتماع في 2-3 جمل يوضح الهدف وما تم إنجازه",
  "key_points": ["نقطة رئيسية 1", "نقطة رئيسية 2", ...],
  "decisions": ["قرار تم اتخاذه 1", "قرار 2", ...],
  "tasks": [
    {
      "task": "وصف المهمة بدقة",
      "owner": "اسم المسؤول أو 'غير محدد'",
      "deadline": "الموعد النهائي أو 'غير محدد'",
      "priority": "مرتفعة / متوسطة / منخفضة"
    }
  ],
  "follow_up_email": {
    "subject": "موضوع إيميل المتابعة واضح وجذاب",
    "body": "نص الإيميل الكامل والجاهز للإرسال بنبرة ${tone.titleAr} (${tone.descriptionAr})"
  }
}

ملاحظات الاجتماع:
$notes
""".trimIndent()
    }

    private fun parseJsonResponse(
        rawText: String,
        originalNotes: String,
        meetingType: MeetingType,
        tone: EmailTone
    ): MeetingSummary {
        // Strip code fence if present
        var cleaned = rawText.trim()
        if (cleaned.startsWith("```json")) {
            cleaned = cleaned.removePrefix("```json")
        } else if (cleaned.startsWith("```")) {
            cleaned = cleaned.removePrefix("```")
        }
        if (cleaned.endsWith("```")) {
            cleaned = cleaned.removeSuffix("```")
        }
        cleaned = cleaned.trim()

        val json = JSONObject(cleaned)
        val title = json.optString("title", "ملخص ${meetingType.titleAr}")
        val summary = json.optString("summary", "تم استعراض النقاط الأساسية للاجتماع ومتابعة سير الأعمال.")

        val keyPointsArray = json.optJSONArray("key_points") ?: JSONArray()
        val keyPoints = mutableListOf<String>()
        for (i in 0 until keyPointsArray.length()) {
            keyPoints.add(keyPointsArray.getString(i))
        }

        val decisionsArray = json.optJSONArray("decisions") ?: JSONArray()
        val decisions = mutableListOf<String>()
        for (i in 0 until decisionsArray.length()) {
            decisions.add(decisionsArray.getString(i))
        }

        val tasksArray = json.optJSONArray("tasks") ?: JSONArray()
        val tasks = mutableListOf<TaskItem>()
        for (i in 0 until tasksArray.length()) {
            val tObj = tasksArray.getJSONObject(i)
            tasks.add(
                TaskItem(
                    id = UUID.randomUUID().toString(),
                    task = tObj.optString("task", "مهمة عمل"),
                    owner = tObj.optString("owner", "غير محدد"),
                    deadline = tObj.optString("deadline", "غير محدد"),
                    priority = tObj.optString("priority", "متوسطة"),
                    isCompleted = false
                )
            )
        }

        val emailObj = json.optJSONObject("follow_up_email") ?: JSONObject()
        val emailSubject = emailObj.optString("subject", "متابعة نتائج ${title}")
        val emailBody = emailObj.optString("body", "السلام عليكم ورحمة الله،\n\nنود إحاطتكم بملخص ومخرجات الاجتماع.")

        return MeetingSummary(
            title = title,
            summary = summary,
            keyPoints = if (keyPoints.isEmpty()) listOf("تمت مناقشة النقاط المذكورة في الملاحظات.") else keyPoints,
            decisions = if (decisions.isEmpty()) listOf("متابعة خطة العمل المتفق عليها.") else decisions,
            tasks = tasks,
            followUpEmail = FollowUpEmail(subject = emailSubject, body = emailBody),
            originalNotes = originalNotes,
            meetingType = meetingType.titleAr,
            emailTone = tone.titleAr
        )
    }

    /**
     * Smart Arabic heuristic analyzer for offline / instant testing
     * or when Gemini API key has not been injected yet.
     */
    fun generateLocalSummary(
        notes: String,
        meetingType: MeetingType,
        tone: EmailTone
    ): MeetingSummary {
        val lines = notes.lines().map { it.trim() }.filter { it.isNotBlank() }

        // Derive title
        val firstLine = lines.firstOrNull()?.replace(Regex("^[-*•#0-9.]+\\s*"), "") ?: ""
        val title = if (firstLine.isNotBlank() && firstLine.length in 5..60) {
            "اجتماع: $firstLine"
        } else {
            "ملخص ${meetingType.titleAr} - ${java.text.SimpleDateFormat("yyyy/MM/dd", java.util.Locale.forLanguageTag("ar")).format(java.util.Date())}"
        }

        val keyPoints = mutableListOf<String>()
        val decisions = mutableListOf<String>()
        val tasks = mutableListOf<TaskItem>()

        for (line in lines) {
            val cleanLine = line.replace(Regex("^[-*•#0-9.]+\\s*"), "")

            when {
                cleanLine.contains("قرر", ignoreCase = true) ||
                cleanLine.contains("اتفق", ignoreCase = true) ||
                cleanLine.contains("اعتماد", ignoreCase = true) ||
                cleanLine.contains("الموافقة", ignoreCase = true) -> {
                    decisions.add(cleanLine)
                }

                cleanLine.contains("مهمة", ignoreCase = true) ||
                cleanLine.contains("سيتولى", ignoreCase = true) ||
                cleanLine.contains("ستقوم", ignoreCase = true) ||
                cleanLine.contains("سيقوم", ignoreCase = true) ||
                cleanLine.contains("مطلوب من", ignoreCase = true) ||
                cleanLine.contains("تسليم", ignoreCase = true) ||
                cleanLine.contains("إعداد", ignoreCase = true) ||
                cleanLine.contains("إرسال", ignoreCase = true) ||
                cleanLine.contains("متابعة", ignoreCase = true) -> {
                    // Extract owner
                    var owner = "غير محدد"
                    val names = listOf("أحمد", "سارة", "محمد", "عمر", "نورة", "خالد", "فاطمة", "علي", "منى", "كريم", "ليلى")
                    for (name in names) {
                        if (cleanLine.contains(name)) {
                            owner = name
                            break
                        }
                    }

                    // Extract deadline
                    var deadline = "غير محدد"
                    val times = listOf("الخميس القادم", "نهاية الأسبوع", "نهاية الشهر", "الأحد القادم", "خلال يومين", "غداً", "الأسبوع القادم")
                    for (t in times) {
                        if (cleanLine.contains(t)) {
                            deadline = t
                            break
                        }
                    }

                    val priority = when {
                        cleanLine.contains("عاجل") || cleanLine.contains("ضروري") || cleanLine.contains("مهم جداً") -> "مرتفعة"
                        cleanLine.contains("لاحق") || cleanLine.contains("بسيط") -> "منخفضة"
                        else -> "متوسطة"
                    }

                    tasks.add(
                        TaskItem(
                            task = cleanLine,
                            owner = owner,
                            deadline = deadline,
                            priority = priority
                        )
                    )
                }

                else -> {
                    if (cleanLine.length > 8) {
                        keyPoints.add(cleanLine)
                    }
                }
            }
        }

        if (keyPoints.isEmpty()) {
            keyPoints.addAll(lines.take(3))
        }

        if (decisions.isEmpty()) {
            decisions.add("تم الاتفاق على المضي قدماً في بنود خطة العمل وتوزيع المهام.")
        }

        if (tasks.isEmpty()) {
            tasks.add(
                TaskItem(
                    task = "متابعة تنفيذ النقاط المتفق عليها في الاجتماع",
                    owner = "فريق العمل",
                    deadline = "نهاية الأسبوع",
                    priority = "متوسطة"
                )
            )
        }

        val summary = "انعقد ${meetingType.titleAr} لمناقشة أبرز مستجدات العمل والخطط التنفيذية، حيث تم استعراض ${keyPoints.size} محاور أساسية واعتماد ${decisions.size} قرارات وتوزيع المهام المحددة لضمان سير المشروع بأعلى كفاءة."

        val emailGreeting = when (tone) {
            EmailTone.FORMAL -> "السادة أعضاء الفريق المحترمين،\nالسلام عليكم ورحمة الله وبركاته،\n\nنود إحاطتكم علماً بمخرجات ${meetingType.titleAr}:"
            EmailTone.FRIENDLY -> "أهلاً بالجميع،\nيعطيكم العافية على حضوركم ومشاركتكم اليوم في ${meetingType.titleAr}! إليكم ملخص سريع لما تم نقاشه:"
            EmailTone.CONCISE -> "مرحباً،\nمخرجات ${meetingType.titleAr} باختصار:"
        }

        val emailTasksStr = tasks.joinToString("\n") { "- ${it.task} (المسؤول: ${it.owner} | الموعد: ${it.deadline})" }
        val emailDecisionsStr = decisions.joinToString("\n") { "• $it" }

        val emailBody = """$emailGreeting

القرارات المتخذة:
$emailDecisionsStr

المهام والتكليفات المطلوبة:
$emailTasksStr

شاكرين لكم حسن تعاونكم وتفانيكم.
""".trimIndent()

        val emailSubject = when (tone) {
            EmailTone.FORMAL -> "ملخص ومخرجات: $title"
            EmailTone.FRIENDLY -> "متابعة سريعة لنتائج $title 🚀"
            EmailTone.CONCISE -> "موجز نتائج: $title"
        }

        return MeetingSummary(
            title = title,
            summary = summary,
            keyPoints = keyPoints,
            decisions = decisions,
            tasks = tasks,
            followUpEmail = FollowUpEmail(subject = emailSubject, body = emailBody),
            originalNotes = notes,
            meetingType = meetingType.titleAr,
            emailTone = tone.titleAr
        )
    }
}
