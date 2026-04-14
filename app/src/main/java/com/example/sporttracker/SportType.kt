package com.example.sporttracker

import androidx.compose.ui.graphics.Color
import com.example.sporttracker.db.SportDefinitionEntity
import kotlin.math.abs

private val builtinSportColors: Map<String, Color> = mapOf(
    "GYM" to Color(0xFF7C4DFF),
    "FOOTBALL" to Color(0xFF2E7D32),
    "RUNNING" to Color(0xFFFF6D00),
    "TABLE_TENNIS" to Color(0xFF00838F),
    "TENNIS" to Color(0xFF43A047),
    "SWIMMING" to Color(0xFF1565C0),
    "CYCLING" to Color(0xFF6D4C41)
)

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

fun sportEmojiFor(definition: SportDefinitionEntity?): String =
    definition?.emoji?.ifBlank { "🏅" } ?: "🏅"

fun sportLabelFor(definition: SportDefinitionEntity?, sportId: String): String =
    definition?.nameUa?.ifBlank { sportId } ?: sportId
