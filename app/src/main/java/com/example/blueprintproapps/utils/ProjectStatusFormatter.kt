package com.example.blueprintproapps.utils

object ProjectStatusFormatter {
    fun progressFor(status: String?): Int {
        val value = status?.lowercase().orEmpty()
        return when {
            "finish" in value || "done" in value || "complete" in value -> 100
            "final" in value -> 90
            "compliance" in value || "permit" in value -> 66
            "review" in value || "development" in value -> 33
            else -> 0
        }
    }

    fun phaseFor(status: String?): String {
        val value = status?.lowercase().orEmpty()
        return when {
            "finish" in value || "done" in value || "complete" in value -> "Completed"
            "final" in value -> "Finalization"
            "compliance" in value || "permit" in value -> "Compliance Documents"
            "review" in value || "development" in value -> "Review"
            "ongoing" in value || "start" in value || "planning" in value || "concept" in value -> "Planning"
            else -> status ?: "Not started"
        }
    }

    fun colorFor(status: String?): String {
        val value = status?.lowercase().orEmpty()
        return when {
            "finish" in value || "done" in value || "complete" in value -> "#10B981"
            "final" in value -> "#06B6D4"
            "compliance" in value || "permit" in value -> "#8B5CF6"
            "review" in value || "development" in value -> "#F59E0B"
            "ongoing" in value -> "#3B82F6"
            "delete" in value -> "#EF4444"
            else -> "#64748B"
        }
    }
}
