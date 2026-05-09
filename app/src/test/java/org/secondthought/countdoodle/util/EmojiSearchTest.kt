package org.secondthought.countdoodle.util

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class EmojiSearchTest {

    private val sample = listOf(
        EmojiEntry("🎂", "birthday cake", listOf("birthday", "cake", "party", "celebration")),
        EmojiEntry("🎉", "party popper", listOf("party", "celebration", "tada", "confetti")),
        EmojiEntry("🐱", "cat face", listOf("cat", "kitten", "animal", "pet")),
        EmojiEntry("🐶", "dog face", listOf("dog", "puppy", "animal", "pet")),
        EmojiEntry("✈️", "airplane", listOf("plane", "flight", "travel", "trip")),
        EmojiEntry("🏠", "house", listOf("home", "building", "house")),
    )

    @Test
    fun `blank query returns empty`() {
        assertThat(searchEmojis("", sample)).isEmpty()
        assertThat(searchEmojis("   ", sample)).isEmpty()
    }

    @Test
    fun `exact name match found`() {
        val result = searchEmojis("airplane", sample)
        assertThat(result.map { it.emoji }).containsExactly("✈️")
    }

    @Test
    fun `keyword match found`() {
        val result = searchEmojis("celebration", sample).map { it.emoji }
        assertThat(result).containsExactly("🎂", "🎉")
    }

    @Test
    fun `prefix match works`() {
        val result = searchEmojis("birth", sample).map { it.emoji }
        assertThat(result).containsExactly("🎂")
    }

    @Test
    fun `is case insensitive`() {
        val a = searchEmojis("CAT", sample).map { it.emoji }
        val b = searchEmojis("cat", sample).map { it.emoji }
        assertThat(a).isEqualTo(b)
        assertThat(a).containsExactly("🐱")
    }

    @Test
    fun `multi-token query requires all tokens to match`() {
        val result = searchEmojis("animal pet", sample).map { it.emoji }
        assertThat(result).containsExactly("🐱", "🐶")

        val none = searchEmojis("animal flight", sample)
        assertThat(none).isEmpty()
    }

    @Test
    fun `name-prefix matches rank above keyword-only matches`() {
        val data = listOf(
            EmojiEntry("🎉", "party popper", listOf("party", "celebration")),
            EmojiEntry("🥳", "partying face", listOf("party", "birthday")),
            EmojiEntry("🎂", "birthday cake", listOf("birthday", "party")),
        )
        val result = searchEmojis("party", data).map { it.emoji }
        // Both name-prefixed entries (🎉, 🥳) come before keyword-only (🎂)
        assertThat(result.indexOf("🎂")).isGreaterThan(result.indexOf("🎉"))
        assertThat(result.indexOf("🎂")).isGreaterThan(result.indexOf("🥳"))
    }

    @Test
    fun `bundled index has unique emojis`() {
        val dupes = EMOJI_INDEX.groupBy { it.emoji }.filter { it.value.size > 1 }.keys
        assertThat(dupes).isEmpty()
    }

    @Test
    fun `bundled index has reasonable coverage`() {
        // Sanity-check the bundled index — common queries should return something
        listOf("cake", "heart", "star", "smile", "cat", "dog", "plane", "car")
            .forEach { q ->
                assertThat(searchEmojis(q, EMOJI_INDEX)).isNotEmpty()
            }
    }
}
