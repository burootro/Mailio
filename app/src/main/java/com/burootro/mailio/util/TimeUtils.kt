package com.burootro.mailio.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object TimeUtils {

    private val arabicLocale = Locale("ar")

    /**
     * وقت نسبي مختصر للقوائم: "دلوقتي"، "٥ د"، "٣ س"، "أمس"، "١٢ مارس"
     */
    fun relativeShort(timestamp: Long, now: Long = System.currentTimeMillis()): String {
        val diff = now - timestamp

        return when {
            diff < 60_000 -> "دلوقتي"
            diff < 3_600_000 -> "${diff / 60_000} د"
            diff < 86_400_000 && isSameDay(timestamp, now) -> "${diff / 3_600_000} س"
            isYesterday(timestamp, now) -> "أمس"
            diff < 604_800_000 -> "${diff / 86_400_000} ي"
            isSameYear(timestamp, now) -> format(timestamp, "d MMM")
            else -> format(timestamp, "d MMM yyyy")
        }
    }

    /**
     * وقت كامل لشاشة الرسالة: "١٢ مارس ٢٠٢٥، ٣:٤٥ م"
     */
    fun fullDateTime(timestamp: Long): String =
        format(timestamp, "d MMMM yyyy، h:mm a")

    /**
     * الوقت المتبقي: "٩ د ٣٢ ث"، "٥ س ١٢ د"، "٣ أيام"
     */
    fun countdown(remainingMillis: Long): String {
        if (remainingMillis <= 0) return "انتهت"

        val seconds = remainingMillis / 1000
        val minutes = seconds / 60
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days ${plural(days, "يوم", "يومين", "أيام")}"
            hours > 0 -> "$hours س ${minutes % 60} د"
            minutes > 0 -> "$minutes د ${seconds % 60} ث"
            else -> "$seconds ث"
        }
    }

    private fun plural(count: Long, one: String, two: String, many: String): String =
        when (count) {
            1L -> one
            2L -> two
            else -> many
        }

    private fun format(timestamp: Long, pattern: String): String =
        SimpleDateFormat(pattern, arabicLocale).format(Date(timestamp))

    private fun isSameDay(a: Long, b: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = a }
        val calB = Calendar.getInstance().apply { timeInMillis = b }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR) &&
            calA.get(Calendar.DAY_OF_YEAR) == calB.get(Calendar.DAY_OF_YEAR)
    }

    private fun isYesterday(timestamp: Long, now: Long): Boolean {
        val yesterday = Calendar.getInstance().apply {
            timeInMillis = now
            add(Calendar.DAY_OF_YEAR, -1)
        }
        val target = Calendar.getInstance().apply { timeInMillis = timestamp }
        return yesterday.get(Calendar.YEAR) == target.get(Calendar.YEAR) &&
            yesterday.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)
    }

    private fun isSameYear(a: Long, b: Long): Boolean {
        val calA = Calendar.getInstance().apply { timeInMillis = a }
        val calB = Calendar.getInstance().apply { timeInMillis = b }
        return calA.get(Calendar.YEAR) == calB.get(Calendar.YEAR)
    }
}

/**
 * ألوان الأفاتار — نفس المرسل ياخد نفس اللون دايماً
 */
object AvatarColors {

    private val palette = listOf(
        0xFF22D3EE, 0xFF8B5CF6, 0xFF10B981, 0xFFF59E0B,
        0xFFF43F5E, 0xFF3B82F6, 0xFFEC4899, 0xFF14B8A6
    )

    fun forSeed(seed: String): Long {
        val index = (seed.hashCode().let { if (it < 0) -it else it }) % palette.size
        return palette[index]
    }
}
