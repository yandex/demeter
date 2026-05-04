package com.yandex.demeter.profiler.coroutine.tracer.ui.internal.theme

import androidx.compose.ui.graphics.Color
import com.yandex.demeter.annotations.InternalDemeterApi
import com.yandex.demeter.internal.WarningLevel

/**
 * Color definitions matching the Demeter color scheme from profiler-ui resources.
 *
 * Maps [WarningLevel] thresholds to Compose [Color] values:
 * - Zero  (< 16ms)  -> transparent background, dark text
 * - First (>= 16ms) -> yellow background
 * - Second(>= 50ms) -> orange background
 * - Third (>= 150ms)-> red background, white text
 */
@InternalDemeterApi
internal object CoroutineTracerColors {

    // Backgrounds — softened versions of d2m_bg_warning_* for readability
    val backgroundZero = Color.Transparent
    val backgroundFirst = Color(0x40FFE699)     // d2m_bg_warning_1, 25% alpha
    val backgroundSecond = Color(0x50FFCD83)    // d2m_bg_warning_2, 31% alpha
    val backgroundThird = Color(0x60FF916E)     // d2m_bg_warning_3, 37% alpha

    // Text colors matching d2m_font_* from profiler-ui/res/values/colors.xml
    val textDefaultTitle = Color(0xFF333333)     // d2m_font_default_title
    val textDefaultDescription = Color(0xFF777777) // d2m_font_default_description
    val textWarning12 = Color(0xFF222222)        // d2m_font_warning_1_and_2
    val textWarning3 = Color(0xFF993322)         // dark red-brown for softened warning 3 bg

    // Accent colors
    val threadChip = Color(0xFF00BFA5)           // teal, matching inject UI
    val cancelledBadge = Color(0xFFE53935)       // red
    val errorBadge = Color(0xFFE53935)           // red

    fun backgroundForLevel(level: WarningLevel): Color = when (level) {
        WarningLevel.Zero -> backgroundZero
        WarningLevel.First -> backgroundFirst
        WarningLevel.Second -> backgroundSecond
        WarningLevel.Third -> backgroundThird
    }

    fun textForLevel(level: WarningLevel): Color = when (level) {
        WarningLevel.Zero -> textDefaultTitle
        WarningLevel.First, WarningLevel.Second -> textWarning12
        WarningLevel.Third -> textWarning3
    }

    fun descriptionTextForLevel(level: WarningLevel): Color = when (level) {
        WarningLevel.Zero -> textDefaultDescription
        WarningLevel.First, WarningLevel.Second -> textWarning12
        WarningLevel.Third -> textWarning3
    }
}
