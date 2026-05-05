package org.secondthought.countdoodle.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "events")
data class Event(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val dateEpochMillis: Long,
    val hasTime: Boolean = false,
    val emoji: String? = null,
    val backgroundColorArgb: Int? = null,
    val photoUri: String? = null,
) {
    val displayEmoji: String get() = emoji ?: Defaults.emojiFor(name)
    val displayColorArgb: Int get() = backgroundColorArgb ?: Defaults.colorArgbFor(name)
}
