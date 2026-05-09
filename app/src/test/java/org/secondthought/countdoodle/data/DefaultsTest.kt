package org.secondthought.countdoodle.data

import com.google.common.truth.Truth.assertThat
import org.junit.Test

class DefaultsTest {

    @Test fun `same name yields same emoji`() {
        assertThat(Defaults.emojiFor("Birthday")).isEqualTo(Defaults.emojiFor("Birthday"))
    }

    @Test fun `same name yields same color`() {
        assertThat(Defaults.colorArgbFor("Birthday")).isEqualTo(Defaults.colorArgbFor("Birthday"))
    }

    @Test fun `different names usually yield different emoji`() {
        // Probabilistic but with a fixed palette, vary across enough names.
        val names = listOf("Birthday", "Vacation", "Wedding", "Concert", "Trip", "Exam", "Move", "Baby")
        val emojis = names.map(Defaults::emojiFor).toSet()
        assertThat(emojis.size).isAtLeast(2)
    }

    @Test fun `different names usually yield different colors`() {
        val names = listOf("Birthday", "Vacation", "Wedding", "Concert", "Trip", "Exam", "Move", "Baby")
        val colors = names.map(Defaults::colorArgbFor).toSet()
        assertThat(colors.size).isAtLeast(2)
    }

    @Test fun `emoji is non-empty`() {
        assertThat(Defaults.emojiFor("Anything")).isNotEmpty()
    }

    @Test fun `color is fully opaque`() {
        val argb = Defaults.colorArgbFor("Anything")
        // Top byte (alpha) should be 0xFF.
        assertThat(argb ushr 24).isEqualTo(0xFF)
    }

    @Test fun `empty name still works`() {
        Defaults.emojiFor("")
        Defaults.colorArgbFor("")
    }

    @Test fun `name matches index picks the matching emoji`() {
        assertThat(Defaults.emojiFor("Birthday")).isEqualTo("🎂")
        assertThat(Defaults.emojiFor("Wedding")).isEqualTo("💒")
        assertThat(Defaults.emojiFor("Halloween")).isEqualTo("🎃")
        assertThat(Defaults.emojiFor("Christmas")).isEqualTo("🎄")
    }

    @Test fun `match works on a word inside the name`() {
        // "Mom's birthday" should pick the birthday emoji.
        assertThat(Defaults.emojiFor("Mom's birthday")).isEqualTo("🎂")
        // "Trip to Paris" should match "trip" keyword (✈️ airplane).
        assertThat(Defaults.emojiFor("Trip to Paris")).isEqualTo("✈️")
    }

    @Test fun `unmatched name falls back to deterministic hash pick`() {
        // "Zzqzx" matches nothing in the index; should still return a non-empty
        // emoji and be stable across calls.
        val a = Defaults.emojiFor("Zzqzx")
        val b = Defaults.emojiFor("Zzqzx")
        assertThat(a).isNotEmpty()
        assertThat(a).isEqualTo(b)
    }
}
