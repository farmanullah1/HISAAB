package com.example.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Palette as explicitly chosen:
// Primary Gradient: Linear gradient from #FF6B6B (Coral) to #FFD93D (Yellow)
val CoralPrimary = Color(0xFFFF6B6B)
val YellowAccent = Color(0xFFFFD93D)
val HeroGradientBrush = Brush.linearGradient(
    colors = listOf(CoralPrimary, YellowAccent)
)

// Background: #FFF9F5 (Soft warm cream)
val WarmCreamBackground = Color(0xFFFFF9F5)
// Card Background: White (#FFFFFF)
val PureWhiteCard = Color(0xFFFFFFFF)

// Text Primary: #2D2A26 (Deep charcoal)
val TextDeepCharcoal = Color(0xFF2D2A26)
// Text Secondary: #6B6B6B (Medium gray)
val TextMediumGray = Color(0xFF6B6B6B)

// Borders: Soft orange (#FFF3E0) or gray (#EDE8E3)
val BorderSoftOrange = Color(0xFFFFF3E0)
val BorderSubtle = Color(0xFFEFE8E1)

// Semantic colors
val IncomeGreen = Color(0xFF10B981)
val ExpenseCoral = Color(0xFFFF6B6B)
val TransferBlue = Color(0xFF3B82F6)

// Category Colors:
// Food: Rose/Pink (#FF6B6B)
val CategoryFood = Color(0xFFFF6B6B)
// Travel: Blue (#3B82F6)
val CategoryTravel = Color(0xFF3B82F6)
// Shopping: Emerald (#10B981)
val CategoryShopping = Color(0xFF10B981)
val CategoryBills = Color(0xFFF59E0B)
val CategoryHealth = Color(0xFFEC4899)
val CategoryEducation = Color(0xFF8B5CF6)
val CategorySalary = Color(0xFF10B981)
val CategoryFreelance = Color(0xFF06B6D4)
val CategoryOther = Color(0xFF6B7280)
