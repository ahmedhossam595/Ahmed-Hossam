package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.model.EmailTone
import com.example.data.model.MeetingType
import com.example.data.remote.GeminiService
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("مُلخّص الاجتماعات", appName)
    }

    @Test
    fun `test meeting summarizer local extraction`() {
        val service = GeminiService()
        val notes = """
            اجتماع أسبوعي للفريق
            - اعتماد الميزانية الجديدة
            - إرسال التقرير النهائي (المسؤول: أحمد | الموعد: الخميس القادم)
        """.trimIndent()

        val summary = service.generateLocalSummary(
            notes = notes,
            meetingType = MeetingType.TEAM,
            tone = EmailTone.FORMAL
        )

        assertNotNull(summary)
        assertTrue(summary.title.isNotBlank())
        assertTrue(summary.tasks.isNotEmpty())
        assertEquals("أحمد", summary.tasks.first().owner)
    }
}
