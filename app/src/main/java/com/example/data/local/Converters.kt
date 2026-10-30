package com.example.data.local

import androidx.room.TypeConverter
import com.example.data.model.FollowUpEmail
import com.example.data.model.TaskItem
import org.json.JSONArray
import org.json.JSONObject

class Converters {

    @TypeConverter
    fun fromStringList(value: List<String>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { array.put(it) }
        return array.toString()
    }

    @TypeConverter
    fun toStringList(value: String?): List<String> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<String>()
            for (i in 0 until array.length()) {
                list.add(array.getString(i))
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromTaskList(value: List<TaskItem>?): String {
        if (value == null) return "[]"
        val array = JSONArray()
        value.forEach { task ->
            val obj = JSONObject()
            obj.put("id", task.id)
            obj.put("task", task.task)
            obj.put("owner", task.owner)
            obj.put("deadline", task.deadline)
            obj.put("priority", task.priority)
            obj.put("isCompleted", task.isCompleted)
            array.put(obj)
        }
        return array.toString()
    }

    @TypeConverter
    fun toTaskList(value: String?): List<TaskItem> {
        if (value.isNullOrBlank()) return emptyList()
        return try {
            val array = JSONArray(value)
            val list = mutableListOf<TaskItem>()
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                list.add(
                    TaskItem(
                        id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                        task = obj.optString("task", ""),
                        owner = obj.optString("owner", "غير محدد"),
                        deadline = obj.optString("deadline", "غير محدد"),
                        priority = obj.optString("priority", "متوسطة"),
                        isCompleted = obj.optBoolean("isCompleted", false)
                    )
                )
            }
            list
        } catch (_: Exception) {
            emptyList()
        }
    }

    @TypeConverter
    fun fromFollowUpEmail(value: FollowUpEmail?): String {
        if (value == null) return "{}"
        val obj = JSONObject()
        obj.put("subject", value.subject)
        obj.put("body", value.body)
        return obj.toString()
    }

    @TypeConverter
    fun toFollowUpEmail(value: String?): FollowUpEmail {
        if (value.isNullOrBlank()) return FollowUpEmail("", "")
        return try {
            val obj = JSONObject(value)
            FollowUpEmail(
                subject = obj.optString("subject", ""),
                body = obj.optString("body", "")
            )
        } catch (_: Exception) {
            FollowUpEmail("", "")
        }
    }
}
