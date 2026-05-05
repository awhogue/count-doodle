package org.secondthought.countdoodle.data

import kotlin.math.absoluteValue

object Defaults {

    private val emojiPalette = listOf(
        "🎉", "🎂", "✈️", "💍", "🎓", "🌴", "🎄", "🎁",
        "🚀", "❤️", "🏖️", "🏔️", "🍰", "🎵", "📅", "🏆",
    )

    private val colorPalette = listOf(
        0xFF1F6FEB.toInt(), 0xFFE85D75.toInt(), 0xFF34A853.toInt(), 0xFFFFB300.toInt(),
        0xFF8E44AD.toInt(), 0xFF16A085.toInt(), 0xFFD35400.toInt(), 0xFF2C3E50.toInt(),
        0xFFC0392B.toInt(), 0xFF2980B9.toInt(), 0xFF27AE60.toInt(), 0xFFF39C12.toInt(),
    )

    fun emojiFor(name: String): String {
        val idx = (name.hashCode().absoluteValue) % emojiPalette.size
        return emojiPalette[idx]
    }

    fun colorArgbFor(name: String): Int {
        val idx = (name.hashCode().absoluteValue) % colorPalette.size
        return colorPalette[idx]
    }
}
