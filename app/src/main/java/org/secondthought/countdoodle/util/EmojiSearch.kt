package org.secondthought.countdoodle.util

data class EmojiEntry(
    val emoji: String,
    val name: String,
    val keywords: List<String>,
)

/**
 * Search [index] for entries matching [query].
 *
 * Tokenizes the query on whitespace; an entry matches when every query token is a
 * prefix of some token in the entry's name or keywords. Results are ordered with
 * name-prefix matches before keyword-only matches, then by shorter names first.
 */
fun searchEmojis(query: String, index: List<EmojiEntry> = EMOJI_INDEX): List<EmojiEntry> {
    val tokens = query.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
    if (tokens.isEmpty()) return emptyList()

    data class Scored(val entry: EmojiEntry, val rank: Int)

    return index.mapNotNull { entry ->
        val nameTokens = entry.name.lowercase().split(Regex("\\s+")).filter { it.isNotBlank() }
        val haystack = nameTokens + entry.keywords.map { it.lowercase() }

        val allMatch = tokens.all { t -> haystack.any { h -> h.startsWith(t) } }
        if (!allMatch) return@mapNotNull null

        val firstHitsName = nameTokens.any { it.startsWith(tokens.first()) }
        val rank = if (firstHitsName) 0 else 1
        Scored(entry, rank)
    }
        .sortedWith(compareBy({ it.rank }, { it.entry.name.length }, { it.entry.name }))
        .map { it.entry }
}
