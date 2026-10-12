package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = CoralPrimary,
    onPrimary = Color.White,
    primaryContainer = BorderSoftOrange,
    onPrimaryContainer = TextDeepCharcoal,
    secondary = YellowAccent,
    onSecondary = TextDeepCharcoal,
    secondaryContainer = Color(0xFFFFF3CD),
    onSecondaryContainer = TextDeepCharcoal,
    tertiary = IncomeGreen,
    onTertiary = Color.White,
    background = WarmCreamBackground,
    onBackground = TextDeepCharcoal,
    surface = PureWhiteCard,
    onSurface = TextDeepCharcoal,
    surfaceVariant = Color(0xFFF9F4EE),
    onSurfaceVariant = TextMediumGray,
    outline = BorderSubtle,
    outlineVariant = BorderSoftOrange
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = LightColorScheme,
        typography = Typography,
        content = content
    )
}
