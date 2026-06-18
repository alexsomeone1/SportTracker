package com.example.sporttracker

import androidx.compose.ui.graphics.Color
import com.example.sporttracker.db.SportDefinitionEntity
import kotlin.math.abs

data class SportCardStyle(
    val accent: Color,
    val lightGradientStart: Color,
    val lightGradientEnd: Color,
    val darkGradientStart: Color,
    val darkGradientEnd: Color,
    val iconBoxLight: Color,
    val iconBoxDark: Color
)

private val builtinSportCardStyles: Map<String, SportCardStyle> = mapOf(
    "GYM" to SportCardStyle(
        accent = Color(0xFF9B59B6),
        lightGradientStart = Color(0xFFE8D5FF),
        lightGradientEnd = Color(0xFFF8F2FF),
        darkGradientStart = Color(0xFF3D2858),
        darkGradientEnd = Color(0xFF14101C),
        iconBoxLight = Color(0xFFD4BBFF),
        iconBoxDark = Color(0xFF2A1D3D)
    ),
    "FOOTBALL" to SportCardStyle(
        accent = Color(0xFF43A047),
        lightGradientStart = Color(0xFFD5FFD9),
        lightGradientEnd = Color(0xFFF0FFF2),
        darkGradientStart = Color(0xFF1E3D22),
        darkGradientEnd = Color(0xFF0E140F),
        iconBoxLight = Color(0xFFB8E6BC),
        iconBoxDark = Color(0xFF1A2E1C)
    ),
    "RUNNING" to SportCardStyle(
        accent = Color(0xFFFF6D00),
        lightGradientStart = Color(0xFFFFE0CC),
        lightGradientEnd = Color(0xFFFFF5EE),
        darkGradientStart = Color(0xFF4A2800),
        darkGradientEnd = Color(0xFF1A1008),
        iconBoxLight = Color(0xFFFFCC99),
        iconBoxDark = Color(0xFF3D2200)
    ),
    "TABLE_TENNIS" to SportCardStyle(
        accent = Color(0xFF00838F),
        lightGradientStart = Color(0xFFB2EBF2),
        lightGradientEnd = Color(0xFFE8FAFC),
        darkGradientStart = Color(0xFF003D44),
        darkGradientEnd = Color(0xFF0A1214),
        iconBoxLight = Color(0xFF80DEEA),
        iconBoxDark = Color(0xFF002A30)
    ),
    "TENNIS" to SportCardStyle(
        accent = Color(0xFF7CB342),
        lightGradientStart = Color(0xFFDCEDC8),
        lightGradientEnd = Color(0xFFF5FAEF),
        darkGradientStart = Color(0xFF2E4018),
        darkGradientEnd = Color(0xFF101408),
        iconBoxLight = Color(0xFFC5E1A5),
        iconBoxDark = Color(0xFF243012)
    ),
    "SWIMMING" to SportCardStyle(
        accent = Color(0xFF1565C0),
        lightGradientStart = Color(0xFFBBDEFB),
        lightGradientEnd = Color(0xFFEEF6FF),
        darkGradientStart = Color(0xFF0D2744),
        darkGradientEnd = Color(0xFF0A1018),
        iconBoxLight = Color(0xFF90CAF9),
        iconBoxDark = Color(0xFF0A1E36)
    ),
    "CYCLING" to SportCardStyle(
        accent = Color(0xFFFF8A50),
        lightGradientStart = Color(0xFFFFE5D5),
        lightGradientEnd = Color(0xFFFFF8F4),
        darkGradientStart = Color(0xFF4A2E18),
        darkGradientEnd = Color(0xFF181008),
        iconBoxLight = Color(0xFFFFCCAA),
        iconBoxDark = Color(0xFF3D2510)
    )
)

private val builtinSportColors: Map<String, Color> = builtinSportCardStyles.mapValues { it.value.accent }

private val fallbackPalette = listOf(
    Color(0xFF5C6BC0),
    Color(0xFF00897B),
    Color(0xFFE65100),
    Color(0xFF6A1B9A),
    Color(0xFF00695C),
    Color(0xFFC62828)
)

fun sportColorForId(sportId: String?): Color {
    if (sportId == null) return Color(0xFF546E7A)
    return builtinSportColors[sportId] ?: fallbackPalette[abs(sportId.hashCode()) % fallbackPalette.size]
}

fun sportCardStyleForId(sportId: String): SportCardStyle {
    return builtinSportCardStyles[sportId] ?: run {
        val accent = sportColorForId(sportId)
        SportCardStyle(
            accent = accent,
            lightGradientStart = accent.copy(alpha = 0.35f),
            lightGradientEnd = accent.copy(alpha = 0.08f),
            darkGradientStart = accent.copy(alpha = 0.45f),
            darkGradientEnd = Color(0xFF121212),
            iconBoxLight = accent.copy(alpha = 0.35f),
            iconBoxDark = accent.copy(alpha = 0.25f)
        )
    }
}

fun sportEmojiFor(definition: SportDefinitionEntity?): String =
    definition?.emoji?.ifBlank { "🏅" } ?: "🏅"

fun sportLabelFor(definition: SportDefinitionEntity?, sportId: String): String =
    definition?.nameUa?.ifBlank { sportId } ?: sportId
