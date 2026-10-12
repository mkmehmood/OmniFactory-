package com.example.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Semantic status colours shared by every screen, so no screen carries its own hex literal.
 * Tones are tuned to read on both the Soft Cloud and Soft Slate surfaces.
 */
object SoftColors {
    val positive = Color(0xFF10B981)   // cash in, profit, success
    val negative = Color(0xFFEF4444)   // cash out, loss, destructive
    val warning = Color(0xFFF59E0B)    // pending, caution
    val info = Color(0xFF3B82F6)       // transfers, neutral information
    val sky = Color(0xFF38BDF8)
    val violet = Color(0xFF8B5CF6)
    val orange = Color(0xFFF97316)
    val gold = Color(0xFFFFD700)
    val silver = Color(0xFF94A3B8)
    val bronze = Color(0xFFB45309)
}
