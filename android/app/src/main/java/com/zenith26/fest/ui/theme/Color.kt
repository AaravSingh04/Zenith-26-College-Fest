package com.zenith26.fest.ui.theme

import androidx.compose.ui.graphics.Color

// Brand palette matching Tailwind CSS
val Indigo50 = Color(0xFFEEF2FF)
val Indigo100 = Color(0xFFE0E7FF)
val Indigo200 = Color(0xFFC7D2FE)
val Indigo300 = Color(0xFFA5B4FC)
val Indigo500 = Color(0xFF6366F1)
val Indigo600 = Color(0xFF4F46E5)
val Indigo700 = Color(0xFF4338CA)
val Indigo900 = Color(0xFF312E81)
val Indigo950 = Color(0xFF1E1B4B)

val Violet600 = Color(0xFF7C3AED)
val Violet700 = Color(0xFF6D28D9)
val Pink300 = Color(0xFFF9A8D4)
val Pink400 = Color(0xFFF472B6)
val Pink500 = Color(0xFFEC4899)
val Pink600 = Color(0xFFDB2777)
val Cyan300 = Color(0xFF67E8F9)

// Slate neutrals
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)
val Slate950 = Color(0xFF020617)

// Status colors
val Emerald50 = Color(0xFFECFDF5)
val Emerald200 = Color(0xFFA7F3D0)
val Emerald600 = Color(0xFF059669)
val Emerald700 = Color(0xFF047857)

val Rose50 = Color(0xFFFFF1F2)
val Rose200 = Color(0xFFFECDD3)
val Rose600 = Color(0xFFE11D48)
val Rose700 = Color(0xFFBE123C)

val Amber50 = Color(0xFFFFFBEB)
val Amber200 = Color(0xFFFDE68A)
val Amber500 = Color(0xFFF59E0B)
val Amber600 = Color(0xFFD97706)
val Amber800 = Color(0xFF92400E)

val Purple50 = Color(0xFFFAF5FF)
val Purple200 = Color(0xFFE9D5FF)
val Purple600 = Color(0xFF9333EA)
val Purple700 = Color(0xFF7E22CE)

val Blue50 = Color(0xFFEFF6FF)
val Blue200 = Color(0xFFBFDBFE)
val Blue700 = Color(0xFF1D4ED8)

// Category Colors Helper
data class CategoryStyle(
    val bg: Color,
    val text: Color,
    val border: Color
)

fun getCategoryStyle(category: String): CategoryStyle {
    return when (category.trim().lowercase()) {
        "technical" -> CategoryStyle(Blue50, Blue700, Blue200)
        "cultural" -> CategoryStyle(Color(0xFFFDF2F8), Color(0xFFBE185D), Color(0xFFFBCFE8))
        "sports" -> CategoryStyle(Emerald50, Emerald700, Emerald200)
        "gaming" -> CategoryStyle(Purple50, Purple700, Purple200)
        "workshop" -> CategoryStyle(Amber50, Amber800, Amber200)
        else -> CategoryStyle(Slate100, Slate700, Slate200)
    }
}
