package org.secondthought.countdoodle.data

import org.secondthought.countdoodle.util.searchEmojis
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
        pickEmojiByName(name)?.let { return it }
        val idx = (name.hashCode().absoluteValue) % emojiPalette.size
        return emojiPalette[idx]
    }

    /**
     * Try to find an emoji whose name or keywords match a word in [name].
     * Returns null if nothing matches; the caller falls back to the hash pick.
     * Tokens shorter than 3 chars are skipped to avoid matching short stop words
     * (which prefix-match too much of the index).
     */
    private fun pickEmojiByName(name: String): String? {
        val tokens = name.lowercase().split(Regex("[^a-z0-9]+")).filter { it.length >= 3 }
        for (t in tokens) {
            val results = searchEmojis(t)
            if (results.isNotEmpty()) return results.first().emoji
        }
        return null
    }

    fun colorArgbFor(name: String): Int {
        val idx = (name.hashCode().absoluteValue) % colorPalette.size
        return colorPalette[idx]
    }
}
