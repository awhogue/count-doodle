package com.awhogue.countdoodle.ui.components

import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.produceState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import com.awhogue.countdoodle.util.formatCountdown
import kotlinx.coroutines.delay

@Composable
fun LiveCountdownText(
    targetEpochMillis: Long,
    hasTime: Boolean,
    modifier: Modifier = Modifier,
    color: Color = Color.Unspecified,
    style: TextStyle = LocalTextStyle.current,
) {
    val tickMs = if (hasTime) 1_000L else 60_000L
    val nowMs by produceState(initialValue = System.currentTimeMillis(), targetEpochMillis, hasTime) {
        while (true) {
            value = System.currentTimeMillis()
            delay(tickMs)
        }
    }
    Text(
        text = formatCountdown(nowMs, targetEpochMillis, hasTime),
        modifier = modifier,
        color = color,
        style = style,
    )
}
